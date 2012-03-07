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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

public class StubCallDefn {

	private final String method;
	private final String path;
	private int returnStatus = HttpStatus.SC_OK;
	private String entity = "";
	private byte[] returnEntity = "".getBytes();
	private String returnType = "text/plain";
	private final Map<String, String> headers = new HashMap<String, String>();
	private final Map<String, String> returnHeaders = new HashMap<String, String>();

	public StubCallDefn(final String method, final String path) {
		this.method = method;
		this.path = path;
	}

	public Map<String, String> getReturnHeaders() {
		return returnHeaders;
	}

	public String getExpectedMethod() {
		return method;
	}

	public Object getExpectedEntity() {
		return entity;
	}

	public String getExpectedReturnType() {
		return returnType;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public byte[] getExpectedReturnEntity() {
		return returnEntity;
	}

	public int getExpectedReturnStatus() {
		return returnStatus;
	}

	public String getExpectedPath() {
		return path;
	}

	public StubCallDefn andReturn(final int status) {
		this.returnStatus = status;
		this.returnEntity = null;
		return this;
	}

	public StubCallDefn andReturn(final int status, 
			final byte[] entity) {
		this.returnStatus = status;
		this.returnEntity = entity;
		return this;
	}

	public StubCallDefn andReturn(final int status, 
			final String entity) {
		return andReturn(status, entity.getBytes());
	}

	public StubCallDefn andReturn(final int status, 
			final File entity) throws IOException {
		return andReturn(status, FileUtils.readFileToByteArray(entity));
	}

	public StubCallDefn andReturn(final int status, final InputStream entity)
			throws IOException {
		return andReturn(status, IOUtils.toByteArray(entity));
	}

	public StubCallDefn andReturn(final int status, 
			final byte[] entity, 
			final String type) {
		this.returnStatus = status;
		this.returnEntity = entity;
		this.returnType = type;
		return this;
	}

	public StubCallDefn andReturn(final int status, 
			final String entity, 
			final String type) {
		return andReturn(status, entity.getBytes(), type);
	}

	public StubCallDefn andReturn(final int status, 
			final File entity, 
			final String type)
			throws IOException {
		return andReturn(status, FileUtils.readFileToByteArray(entity), type);
	}

	public StubCallDefn withHeader(final String header, 
			final String value) {
		headers.put(header, value);
		return this;
	}

	public StubCallDefn withEntity(final String entity) {
		this.entity = entity;
		return this;
	}

	public StubCallDefn withEntity(final File entity) throws IOException {
		this.entity = FileUtils.readFileToString(entity);
		return this;
	}

	public void returnHeader(String header, String value) {
		returnHeaders.put(header, value);
	}

}