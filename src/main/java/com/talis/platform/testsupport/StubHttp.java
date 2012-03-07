package com.talis.platform.testsupport;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.rules.ExternalResource;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

public class StubHttp extends ExternalResource {
	
	private int port;
    private Server server;
    private VerifyableHandler globalHandler;

	Queue<StubCallDefn> defns = new LinkedList<StubCallDefn>();
	private boolean ordered = true;
	private boolean strict = true;
    
    public StubHttp() {
		this.port = findFreePort();    	
    }
    
    public StubHttp(int port) {
		this.port = port;    	
    }
    
	@Override
	public void before() throws Throwable {
		if (server != null) {
			after();
		}
        server = new Server(port);
        server.start();
        waitUntilUp();
	}

	@Override
	public void after() {
        try {
			stop();
	        server.join();
		} catch (Exception e) {
			throw new RuntimeException("Runtime exception while tearing down StubHttp", e);
		}
        server = null;
		verify();
	}

	public void stop() throws Exception {
		server.stop();
		waitUntilDown();
	}

	public int getPort() {
		return port;
	}

	public String getBaseUrl() {
		return String.format("http://localhost:%d/", getPort());
	}
	
	public void reset() throws Exception {
		server.removeHandler(globalHandler);
		globalHandler = null;
		stop();
		start();
	}

	public void waitUntilUp() throws InterruptedException {		
		// Wait until jetty says it's ready
		while (! server.isStarted()) {
			Thread.sleep(100);
		}		
		// Wait until we start receiving http responses
		boolean responding = false;
		HttpClient client = new DefaultHttpClient();
		while(!responding) {
			HttpGet getRequest = new HttpGet(getBaseUrl());
			try {
				final HttpResponse resp = client.execute(getRequest);
				responding = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(100);
		}		
	}
	
	public void waitUntilDown() throws InterruptedException {		
		// Wait until jetty says it's stopped
		while (! server.isStopped()) {
			Thread.sleep(100);
		}	
	}

	public void start() throws Exception {
        server.start();
        waitUntilUp();		
	}
		
	
	public static int findFreePort() {
		ServerSocket server;
		try {
			server = new ServerSocket(0);
			int port = server.getLocalPort();
			server.close();
			return port;
		} catch (IOException e) {
			throw new RuntimeException("IOException while trying to find a free port", e);
		}
	}
	
	public StubCallDefn expect(String method, String path) {
		StubCallDefn callDefn = new StubCallDefn(method, path);
		defns.add(callDefn);
		return callDefn;
	}

	public void replay() {
		server.removeHandler(globalHandler);
		if (ordered) {
			globalHandler = new QueuedHandler(defns);
		} else {
			globalHandler = new UnorderedHandler(defns);
		}
		server.setHandler(globalHandler);
	}

	public void verify() {
		if (globalHandler != null) {
			globalHandler.verify(strict);
		}
	}

	public void verifyAndTeardown() throws Throwable {
		try {
			this.verify();
		} catch (Throwable t) {
			throw t;
		} finally {
			this.reset();
			this.after();
		}
	}

	public void allowUnordered() {
		this.ordered = false;
	}

	public void allowCallsToNotHappen() {
		this.strict  = false;
	}
}
