package com.talis.platform.testsupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.handler.AbstractHandler;

public class QueuedHandler extends VerifyableHandler {
	private final Queue<StubCallDefn> callStack;	
	private final StringBuffer assertionLog = new StringBuffer();
	private final AtomicBoolean isOk = new AtomicBoolean(true); 
	
	public QueuedHandler(Queue<StubCallDefn> callStack) {
		this.callStack = callStack;
	}

	public void verify(boolean strictMode) {
		if (strictMode) {
			int outstandingCalls = callStack.size();
			while(!callStack.isEmpty()) {
				StubCallDefn d = callStack.poll();
				assertionLog.append("Expected, but didn't get: ");
				assertionLog.append(String.format("%s %s", d.getExpectedMethod(), d.getExpectedPath()));
				assertionLog.append("\n");
			}
			
			assertTrue("Stub server did not get correct set of calls: \n" + assertionLog.toString(), isOk.get());
			assertEquals(" The callstack was not zero at end of test, not all expected requests were made: \n" + assertionLog.toString(), 0, outstandingCalls);
		}
	}

	@Override
	protected StubCallDefn getExpectedCallDefn(String target,
			HttpServletRequest req) {
		return callStack.poll();
	}
}