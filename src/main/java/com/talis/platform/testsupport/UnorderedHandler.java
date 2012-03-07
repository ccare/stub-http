package com.talis.platform.testsupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import org.mortbay.jetty.handler.AbstractHandler;

public class UnorderedHandler extends VerifyableHandler {
	private final Map<MethodPath, StubCallDefn> callStack;	
	
	public UnorderedHandler(Queue<StubCallDefn> callStack) {
		this.callStack = new HashMap<MethodPath, StubCallDefn>();
		for (StubCallDefn defn : callStack) {
			String expectedMethod = (String) defn.getExpectedMethod();
			String expectedPath = (String) defn.getExpectedPath();
			this.callStack.put(new MethodPath(expectedMethod, expectedPath), defn);
		}
	}

	@Override
	protected StubCallDefn getExpectedCallDefn(String target,
			HttpServletRequest req) {
		MethodPath methodPathPair = new MethodPath(req.getMethod(), target);
		StubCallDefn defn = callStack.remove(methodPathPair);
		return defn;
	}

	public void verify(boolean strictMode) {
		if (strictMode) {
			int outstandingCalls = callStack.size();
			for (Map.Entry<MethodPath, StubCallDefn> item : callStack.entrySet()) {
				StubCallDefn d = item.getValue();
				assertionLog.append("Expected, but didn't get: ");
				assertionLog.append(String.format("%s %s", d.getExpectedMethod(), d.getExpectedPath()));
				assertionLog.append("\n");
			}
			
			assertTrue("Stub server did not get correct set of calls: \n" + assertionLog.toString(), isOk.get());
			assertEquals(" The callstack was not zero at end of test, not all expected requests were made: \n" + assertionLog.toString(), 0, outstandingCalls);
		}		
	}
}

class MethodPath {
	public final String method;
	public final String path;
	
	public MethodPath(String method, String path) {
		this.method = method;
		this.path = path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodPath other = (MethodPath) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
	
	
}