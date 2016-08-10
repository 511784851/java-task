package com.blemobi.gamification.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blemobi.gamification.core.GamificationManager;
import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.LocalHttpClient;
import com.blemobi.library.util.CommonUtil;
import com.blemobi.sep.probuf.GamificationProtos.PTaskKey;
import com.blemobi.sep.probuf.ResultProtos.PMessage;

public class TaskProcessTest {

	@Before
	public void setup() throws Exception {
		String[] arg = new String[] { "-env", "local" };
		GamificationManager.main(arg);
	}

	//@Test
	public void testAccept() throws Exception {
		String uuid = "0efe519d-cddf-412c-a5e0-2e8f14f80edb";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("taskKey", PTaskKey.PUBLISH.toString()));

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		String basePath = "/gamification/task/accept";

		BaseHttpClient clientUtil = new LocalHttpClient(basePath, params, cookies);
		PMessage message = clientUtil.postMethod();

		System.out.println(message);
	}

	@Test
	public void testSerNotify() throws Exception {
		String uuid = "0efe519d-cddf-412c-a5e0-2e8f14f80edb";

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("uuid", uuid));
		params.add(new BasicNameValuePair("taskKey", PTaskKey.PUBLISH.toString()));

		String basePath = "/gamification/task/serNotify";

		BaseHttpClient clientUtil = new LocalHttpClient(basePath, params, null);
		PMessage message = clientUtil.postMethod();

		System.out.println(message);
	}
	
	@After
	public void tearDown() {
		System.exit(0);
	}
}
