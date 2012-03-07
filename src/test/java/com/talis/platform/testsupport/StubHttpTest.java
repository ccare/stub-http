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

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.junit.Before;
import org.junit.Test;

public class StubHttpTest {
	
	private HttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager());

	public StubHttp stubHttp;
	
	@Before
	public void setup() throws Throwable {
		 stubHttp = new StubHttp();
		 stubHttp.before();
	}
	
	@Test
	public void noCallsExpected() throws Exception {
		stubHttp.replay();		
		stubHttp.verify();
	}
		
	@Test
	public void noCallsExpectedButCallMade() throws Exception {
		stubHttp.replay();		
		
		HttpResponse execute = httpClient.execute(new HttpGet(stubHttp.getBaseUrl() + "/my/path"));

		assertTestFailureWithErrorPrefix("Stub server did not get correct set of calls:");
	}
		
	@Test
	public void expectedOneGetReturning404() throws Exception {
		String expectedEntity = "Not Found";
		int expectedStatus = 404;
		
		stubHttp.expect("GET", "/my/path").andReturn(expectedStatus, expectedEntity);
		stubHttp.replay();
		
		assertExpectedStatusAndEntity(expectedEntity, expectedStatus, 
				new HttpGet(stubHttp.getBaseUrl() + "/my/path"));
		
		stubHttp.verify();
	}
		
	@Test
	public void expectedOneGetReturning200() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("GET", "/my/path").andReturn(200, expectedEntity);
		stubHttp.replay();
		
		assertExpectedStatusAndEntity(expectedEntity, 200, 
				new HttpGet(stubHttp.getBaseUrl() + "/my/path"));
		
		stubHttp.verify();
	}
		
	@Test
	public void expectedOnePutReturning200() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("PUT", "/my/path").andReturn(200, expectedEntity);
		stubHttp.replay();
		
		assertExpectedStatusAndEntity(expectedEntity, 200, 
				new HttpPut(stubHttp.getBaseUrl() + "/my/path"));
		
		stubHttp.verify();
	}
		
	@Test
	public void expectedOnePutReturning200WithCorrectData() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("PUT", "/my/path").withEntity("mydata").andReturn(200, expectedEntity);
		stubHttp.replay();
		
		HttpPut put = new HttpPut(stubHttp.getBaseUrl() + "/my/path");
		put.setEntity(new StringEntity("mydata"));
		assertExpectedStatusAndEntity(expectedEntity, 200, put);
		
		stubHttp.verify();
	}
		
	@Test
	public void expectedOnePutReturning200WithBadData() throws Exception {
		stubHttp.expect("PUT", "/my/path").withEntity("mydata").andReturn(200, "My data...");
		stubHttp.replay();
		
		HttpPut put = new HttpPut(stubHttp.getBaseUrl() + "/my/path");
		put.setEntity(new StringEntity("not_my_data"));
		assertExpectedStatusAndEntity("", 500, put);

		assertTestFailureWithErrorPrefix("Stub server did not get correct set of calls");
	}
		
	@Test
	public void expectedOnePutReturning200ButGotGET() throws Exception {
		stubHttp.expect("PUT", "/my/path").andReturn(200, "My data...");
		stubHttp.replay();
		
		HttpGet get = new HttpGet(stubHttp.getBaseUrl() + "/my/path");
		assertExpectedStatusAndEntity("", 500, get);

		assertTestFailureWithErrorPrefix("Stub server did not get correct set of calls");
	}
		
	@Test
	public void expectedOnePutReturning200WithCorrectDataAndContentType() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("PUT", "/my/path")
			.withEntity("mydata")
			.withHeader("Content-Type", "text/turtle")
			.andReturn(200, expectedEntity);
		stubHttp.replay();
		
		HttpPut put = new HttpPut(stubHttp.getBaseUrl() + "/my/path");
		put.setEntity(new StringEntity("mydata"));
		put.setHeader(HttpHeaders.CONTENT_TYPE, "text/turtle");
		assertExpectedStatusAndEntity(expectedEntity, 200, put);
		
		stubHttp.verify();
	}
		
	@Test
	public void expectedOnePutReturning200WithCorrectDataAndBadContentType() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("PUT", "/my/path")
			.withEntity("mydata")
			.withHeader(HttpHeaders.CONTENT_TYPE, "text/turtle")
			.andReturn(200, expectedEntity);
		stubHttp.replay();
		
		HttpPut put = new HttpPut(stubHttp.getBaseUrl() + "/my/path");
		put.setEntity(new StringEntity("mydata"));
		put.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		assertExpectedStatusAndEntity("", 500, put);

		assertTestFailureWithErrorPrefix("Stub server did not get correct set of calls");
	}
		
	@Test
	public void expectedOneGetWithIncorrectHeaders() throws Exception {
		stubHttp.expect("GET", "/my/path")
			.withHeader("X-foo", "bar")
			.withHeader("X-bar", "baz")
			.andReturn(200, "");
		stubHttp.replay();
		
		HttpGet get = new HttpGet(stubHttp.getBaseUrl() + "/my/path");
		assertExpectedStatusAndEntity("", 500, get);

		assertTestFailureWithErrorPrefix("Stub server did not get correct set of calls");
	}
		
	@Test
	public void expectedOneGetWithCorrectHeaders() throws Exception {
		stubHttp.expect("GET", "/my/path")
			.withHeader("X-foo", "bar")
			.withHeader("X-bar", "baz")
			.andReturn(200, "");
		stubHttp.replay();
		
		HttpGet get = new HttpGet(stubHttp.getBaseUrl() + "/my/path");
		get.setHeader("X-foo", "bar");
		get.setHeader("X-bar", "baz");
		assertExpectedStatusAndEntity("", 200, get);

		stubHttp.verify();
	}
		
		
	@Test
	public void expectedANumberOfGets() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("GET", "/my/path").andReturn(200, expectedEntity);
		stubHttp.expect("GET", "/my/path").andReturn(200, expectedEntity);
		stubHttp.expect("GET", "/my/path").andReturn(404, expectedEntity);
		stubHttp.expect("GET", "/my/path").andReturn(200, expectedEntity);
		stubHttp.replay();
		
		String uri = stubHttp.getBaseUrl() + "/my/path";
		
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri));
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri));
		assertExpectedStatusAndEntity(expectedEntity, 404, new HttpGet(uri));
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri));

		stubHttp.verify();
	}
		
	@Test
	public void expectedANumberOfGetsButNotEnoughMade() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("GET", "/my/path").andReturn(200, expectedEntity);
		stubHttp.expect("GET", "/my/path").andReturn(200, expectedEntity);
		stubHttp.expect("GET", "/my/path").andReturn(404, expectedEntity);
		stubHttp.expect("GET", "/my/path").andReturn(200, expectedEntity);
		stubHttp.replay();
		
		String uri = stubHttp.getBaseUrl() + "/my/path";
		
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri));
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri));
		assertExpectedStatusAndEntity(expectedEntity, 404, new HttpGet(uri));

		assertTestFailureWithErrorPrefix("Not all expected requests were made");
	}
		
	@Test
	public void expectedANumberOfGetsToDifferentPaths() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("GET", "/my/path1").andReturn(200, expectedEntity);
		stubHttp.expect("GET", "/my/path2").andReturn(200, expectedEntity);
		stubHttp.expect("GET", "/my/path3").andReturn(404, expectedEntity);
		stubHttp.expect("GET", "/my/path4").andReturn(200, expectedEntity);
		stubHttp.replay();
		
		String uri = stubHttp.getBaseUrl() + "/my/path";
		
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri + "1"));
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri + "2"));
		assertExpectedStatusAndEntity(expectedEntity, 404, new HttpGet(uri + "3"));
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri + "4"));

		stubHttp.verify();
	}
		
	@Test
	public void expectedANumberOfGetsToDifferentPathsButWrongOrderMade() throws Exception {
		String expectedEntity = "My data...";
		stubHttp.expect("GET", "/my/path1").andReturn(200, expectedEntity);
		stubHttp.expect("GET", "/my/path2").andReturn(200, expectedEntity);
		stubHttp.expect("GET", "/my/path3").andReturn(404, expectedEntity);
		stubHttp.expect("GET", "/my/path4").andReturn(200, expectedEntity);
		stubHttp.replay();
		
		String uri = stubHttp.getBaseUrl() + "/my/path";
		
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri + "1"));
		assertExpectedStatusAndEntity(expectedEntity, 200, new HttpGet(uri + "2"));
		assertExpectedStatusAndEntity("", 500, new HttpGet(uri + "4"));
		assertExpectedStatusAndEntity("", 500, new HttpGet(uri + "3"));

		assertTestFailureWithErrorPrefix("Stub server did not get correct set of calls:");
	}
		
	@Test
	public void expectedOneGetButNoneMade() throws Exception {
		stubHttp.expect("GET", "/my/path").andReturn(404, "Not Found");
		stubHttp.replay();
		
		assertTestFailureWithErrorPrefix("Not all expected requests were made");
	}

	private void assertExpectedStatusAndEntity(String expectedEntity,
			int expectedStatus, HttpRequestBase uri) throws IOException,
			ClientProtocolException {
		HttpResponse execute = httpClient.execute(uri);
		assertEquals(expectedStatus, execute.getStatusLine().getStatusCode());
		String entity = IOUtils.toString(execute.getEntity().getContent());
		assertEquals(expectedEntity, entity);
	}

	private void assertTestFailureWithErrorPrefix(String errorPrefix) {
		try {
			stubHttp.verify();
			fail("should have failed");
		} catch (AssertionError e) {
			// Expected an assertion error
		}
	}

}
