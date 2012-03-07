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
import static org.junit.Assert.assertTrue;

import java.util.Queue;

import javax.servlet.http.HttpServletRequest;

public class QueuedHandler extends VerifyableHandler {
	private final Queue<StubCallDefn> callStack;

	public QueuedHandler(Queue<StubCallDefn> callStack) {
		this.callStack = callStack;
	}

	@Override
	public void verify(final boolean strictMode) {
		if (strictMode) {
			int outstandingCalls = callStack.size();
			while (!callStack.isEmpty()) {
				StubCallDefn d = callStack.poll();
				assertionLog.append(String.format(
						"Expected, but didn't get: %s %s\n",
						d.getExpectedMethod(), d.getExpectedPath()));
			}

			assertTrue("Stub server did not get correct set of calls: \n"
					+ assertionLog.toString(), isOk.get());
			assertEquals("The callstack was not zero at end of test, not all "
					+ "expected requests seen: \n" + assertionLog.toString(),
					0, outstandingCalls);
		}
	}

	@Override
	protected StubCallDefn getExpectedCallDefn(final String target,
			final HttpServletRequest req) {
		return callStack.poll();
	}
}