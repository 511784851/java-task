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

	// @Test
	public void testList() throws Exception {
		String uuid = "1472020016134289985";
		String token = "GNXq6sAFIOe8lsWIwcuDZSoBbTIgZTVkZGE1OTBjNWU0NWE4MWRmOGU2NzViNGYxZmM0NzM";
		String basePath = "/task/user/list";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("language", "zh_cn"));

		BaseHttpClient clientUtil = new LocalHttpClient("192.168.1.245", port, basePath, null, cookies);
		PMessage message = clientUtil.getMethod();
	}

	@Test
	public void testLevel() throws Exception {
		String uuid = "123456789";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";
		String basePath = "/task/user/level";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("language", "zh_cn"));

		BaseHttpClient clientUtil = new LocalHttpClient("192.168.1.245", port, basePath, null, cookies);
		PMessage message = clientUtil.getMethod();
	}

	// @Test
	public void testReceive() throws Exception {
		String uuid = "123456789";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("taskId", "1002"));

		String basePath = "/task/user/receive";

		BaseHttpClient clientUtil = new LocalHttpClient("127.0.0.1", port, basePath, params, cookies);
		PMessage message = clientUtil.postMethod();
	}

	// @Test
	public void testReward() throws Exception {
		String uuid = "123456789";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("taskId", "2"));

		String basePath = "/task/user/reward";

		BaseHttpClient clientUtil = new LocalHttpClient("127.0.0.1", port, basePath, params, cookies);
		PMessage message = clientUtil.postMethod();
	}

	// @Test
	public void testRanking() throws Exception {
		String uuid = "1472020016134289985";
		String token = "GNXq6sAFIOe8lsWIwcuDZSoBbTIgZTVkZGE1OTBjNWU0NWE4MWRmOGU2NzViNGYxZmM0NzM";
		String basePath = "/task/user/rank";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("language", "zh_cn"));
		params.add(new BasicNameValuePair("scope", "follower"));

		BaseHttpClient clientUtil = new LocalHttpClient("192.168.1.245", port, basePath, params, cookies);
		PMessage message = clientUtil.getMethod();
	}

	// @Test
	public void testPK() throws Exception {
		String uuid = "1472020016134289985";
		String token = "GNXq6sAFIOe8lsWIwcuDZSoBbTIgZTVkZGE1OTBjNWU0NWE4MWRmOGU2NzViNGYxZmM0NzM";
		String basePath = "/task/user/pk";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("language", "zh_cn"));
		params.add(new BasicNameValuePair("pk_uuid", "1472020016134289985"));

		BaseHttpClient clientUtil = new LocalHttpClient("192.168.1.245", port, basePath, params, cookies);
		PMessage message = clientUtil.getMethod();
	}

	@After
	public void tearDown() {
		System.exit(0);
	}
}
