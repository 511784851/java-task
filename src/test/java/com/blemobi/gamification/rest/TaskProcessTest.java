package com.blemobi.gamification.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.LocalHttpClient;
import com.blemobi.library.util.CommonUtil;
import com.blemobi.sep.probuf.ResultProtos.PMessage;

public class TaskProcessTest {
	private int port;

	@Before
	public void setup() throws Exception {
		port = 9018;
		String[] arg = new String[] { "-env", "local" };
		// GamificationManager.main(arg);
	}

	 @Test
	public void testList() throws Exception {
		String uuid = "123456789";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		String basePath = "/task/user/list";

		BaseHttpClient clientUtil = new LocalHttpClient("127.0.0.1", port, basePath, null, cookies);
		PMessage message = clientUtil.getMethod();
	}

	 // @Test
	public void testReward() throws Exception {
		String uuid = "123456789";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("taskId", "2"));

		String basePath = "/task/reward";

		BaseHttpClient clientUtil = new LocalHttpClient("127.0.0.1", port, basePath, params, cookies);
		PMessage message = clientUtil.postMethod();
	}

	@After
	public void tearDown() {
		System.exit(0);
	}
}
