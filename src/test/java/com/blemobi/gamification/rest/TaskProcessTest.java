package com.blemobi.gamification.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blemobi.library.client.LocalHttpClient;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.core.TaskManager;

public class TaskProcessTest {
	private int port;
	private String cookie;

	@Before
	public void setup() throws Exception {
		port = 9018;
		cookie = "uuid=1472020016134289985;token=EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==;";
		String[] arg = new String[] { "-env", "local" };
		TaskManager.main(arg);
	}

	// @Test
	public void testList() throws Exception {
		StringBuilder basePath = new StringBuilder("/v1/task/gold/list");

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("language", "zh_cn"));

		LocalHttpClient clientUtil = new LocalHttpClient("192.168.7.212", port, null, cookie, null, null);
		PMessage message = clientUtil.getMethod(basePath.toString());
		assertEquals("PTask", message.getType());
	}

	// @Test
	public void testReceive() throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ID", "1011"));

		StringBuilder basePath = new StringBuilder("/v1/task/gold/receive");

		LocalHttpClient clientUtil = new LocalHttpClient("192.168.7.212", port, params, cookie, null, null);
		PMessage message = clientUtil.postMethod(basePath.toString());
		assertEquals("PResult", message.getType());
	}

	@Test
	public void testDetails() throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("type", "1"));

		StringBuilder basePath = new StringBuilder("/v1/task/gold/details?idx=1497510678810&size=2");

		LocalHttpClient clientUtil = new LocalHttpClient("192.168.7.212", port, params, cookie, null, null);
		PMessage message = clientUtil.getMethod(basePath.toString());
		assertEquals("PResult", message.getType());
	}

	@After
	public void tearDown() {
		System.exit(0);
	}
}
