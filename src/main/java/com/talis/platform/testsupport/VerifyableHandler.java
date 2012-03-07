/*
 *    Copyright 2012 Talis Systems Ltd
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.talis.platform.testsupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.handler.AbstractHandler;

public abstract class VerifyableHandler extends AbstractHandler {
	
	final StringBuffer assertionLog = new StringBuffer();
	final AtomicBoolean isOk = new AtomicBoolean(true); 

	public abstract void verify(boolean strictMode);

	@Override
	public void handle(String target, HttpServletRequest req, HttpServletResponse resp, int dispatch)
			throws IOException, ServletException {
				try {		
					StubCallDefn defn = getExpectedCallDefn(target, req);
					if (defn == null) {
						assertionLog.append("Unexpected call: ");
						assertionLog.append(String.format("%s %s", req.getMethod(), target));
						assertionLog.append("\n");
						fail("fail test, no more handlers");
					}
							
					assertEquals(defn.getExpectedPath(), target);		
					assertEquals(defn.getExpectedMethod(), req.getMethod());
					
					BufferedReader reader = req.getReader();
					
					String entity = IOUtils.toString(reader);
					assertEquals(defn.getExpectedEntity(), entity);
							
					for (String key: defn.getHeaders().keySet()) {
						String expected = defn.getHeaders().get(key);
						String value = req.getHeader(key);
						assertEquals("Headers don't match for header "+ key, expected, value);
					}
					
					resp.setStatus(defn.getExpectedReturnStatus());
					resp.setHeader("Content-Type", defn.getExpectedReturnType());
					
					byte[] expectedReturnEntity = defn.getExpectedReturnEntity();
					if (expectedReturnEntity != null) {
						resp.getOutputStream().write(expectedReturnEntity);
					}			
					resp.flushBuffer();
				} catch (RuntimeException t) {
					isOk.set(false);
					assertionLog.append(t.getStackTrace());
					assertionLog.append("\n");
					throw t;
				} catch (Error t) {
					isOk.set(false);
					assertionLog.append(t.getMessage());
					assertionLog.append("\n");
					throw t;
				}
			}

	protected abstract StubCallDefn getExpectedCallDefn(String target, HttpServletRequest req);
}
