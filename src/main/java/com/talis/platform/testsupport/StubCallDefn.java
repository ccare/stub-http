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
			
	public StubCallDefn(String method, String path) {
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

	public StubCallDefn andReturn(int status) {
		this.returnStatus = status;
		this.returnEntity = null;
		return this;		
	}

	public StubCallDefn andReturn(int status, byte[] entity) {
		this.returnStatus = status;
		this.returnEntity = entity;
		return this;
	}

	public StubCallDefn andReturn(int status, String entity) {
		return andReturn(status, entity.getBytes());
	}

	public StubCallDefn andReturn(int status, File entity) throws IOException {
		return andReturn(status, FileUtils.readFileToByteArray(entity));
	}

	public StubCallDefn andReturn(int status, InputStream entity) throws IOException {
		return andReturn(status, IOUtils.toByteArray(entity));
	}

	public StubCallDefn andReturn(int status, byte[] entity, String type) {
		this.returnStatus = status;
		this.returnEntity = entity;
		this.returnType = type;
		return this;
	}

	public StubCallDefn andReturn(int status, String entity, String type) {
		return andReturn(status, entity.getBytes(), type);
	}

	public StubCallDefn andReturn(int status, File entity, String type) throws IOException {
		return andReturn(status, FileUtils.readFileToByteArray(entity), type);
	}
	
	public StubCallDefn withHeader(String header, String value) {
		headers.put(header, value);
		return this;
	}

	public StubCallDefn withEntity(String entity) {
		this.entity = entity;
		return this;
	}

	public StubCallDefn withEntity(File entity) throws IOException {
		this.entity = FileUtils.readFileToString(entity);
		return this;
	}
	
}