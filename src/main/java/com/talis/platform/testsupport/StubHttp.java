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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.rules.ExternalResource;
import org.mortbay.jetty.Server;

public class StubHttp extends ExternalResource {

	private final int port;

	Queue<StubCallDefn> defns = new LinkedList<StubCallDefn>();
	private boolean ordered = true;
	private boolean strict = true;
	private VerifyableHandler globalHandler;
	private Server server;

	public StubHttp() {
		this(findFreePort());
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
			String msg = "Runtime exception while tearing down StubHttp";
			throw new RuntimeException(msg, e);
		}
		server = null;
		verify();
	}

	public void stop() {
		try {
			server.stop();
			waitUntilDown();
		} catch (Exception e) {
			String msg = "Exception while stopping jetty";
			throw new RuntimeException(msg, e);
		}
	}

	public int getPort() {
		return port;
	}

	public String getBaseUrl() {
		return String.format("http://localhost:%d/", getPort());
	}

	public void reset() {
		server.removeHandler(globalHandler);
		globalHandler = null;
		stop();
		start();
	}

	@SuppressWarnings("PMD.EmptyCatchBlock")
	public void waitUntilUp() throws InterruptedException {
		// Wait until jetty says it's ready
		while (!server.isStarted()) {
			Thread.sleep(100);
		}
		// Wait until we start receiving http responses
		boolean responding = false;
		HttpClient client = new DefaultHttpClient();
		while (!responding) {
			HttpGet getRequest = new HttpGet(getBaseUrl());
			try {
				client.execute(getRequest);
				responding = true;
			} catch (Exception e) {
				// Ok to get an exception here
			}
			Thread.sleep(100);
		}
	}

	public void waitUntilDown() throws InterruptedException {
		// Wait until jetty says it's stopped
		while (!server.isStopped()) {
			Thread.sleep(100);
		}
	}

	public void start() {
		try {
			server.start();
			waitUntilUp();
		} catch (Exception e) {
			String msg = "Exception while starting stub HTTP server";
			throw new RuntimeException(msg, e);
		}
	}

	public static int findFreePort() {
		ServerSocket server;
		try {
			server = new ServerSocket(0);
			int port = server.getLocalPort();
			server.close();
			return port;
		} catch (IOException e) {
			throw new RuntimeException(
					"IOException while trying to find a free port", e);
		}
	}

	public StubCallDefn expect(final String method, final String path) {
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

	public void verifyAndTeardown() {
		try {
			this.verify();
		} finally {
			resetAndTeardown();
		}
	}

	private void resetAndTeardown() {
		try {
			this.reset();
			this.after();
		} catch (Exception e) {
			String msg = "Exception while resetting and tearing down stubHttp";
			throw new RuntimeException(msg, e);
		}
	}

	public void allowUnordered() {
		this.ordered = false;
	}

	public void allowCallsToNotHappen() {
		this.strict = false;
	}
}
