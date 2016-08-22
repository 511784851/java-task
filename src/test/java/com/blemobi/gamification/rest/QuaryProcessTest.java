package com.blemobi.gamification.rest;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.http.NameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blemobi.gamification.core.GamificationManager;
import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.LocalHttpClient;
import com.blemobi.library.util.CommonUtil;
import com.blemobi.sep.probuf.GamificationProtos.PGamification;
import com.blemobi.sep.probuf.ResultProtos.PMessage;

public class QuaryProcessTest {
	private int port;

	@Before
	public void setup() throws Exception {
		port = 9018;
		String[] arg = new String[] { "-env", "local" };
		GamificationManager.main(arg);
	}

	@Test
	public void testTask() throws Exception {
		String uuid = "0efe519d-cddf-412c-a5e0-2e8f14f80edb";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		String basePath = "/gamification/user/task";

		BaseHttpClient clientUtil = new LocalHttpClient("127.0.0.1", port, basePath, null, cookies);
		PMessage message = clientUtil.getMethod();

		PGamification gamification = PGamification.parseFrom(message.getData());
		System.out.println(gamification);
	}

	@Test
	public void testAchievement() throws Exception {
		String uuid = "0efe519d-cddf-412c-a5e0-2e8f14f80edb";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		String basePath = "/gamification/user/achievement";

		BaseHttpClient clientUtil = new LocalHttpClient("127.0.0.1", port, basePath, null, cookies);
		PMessage message = clientUtil.getMethod();

		System.out.println(message);
	}

	@After
	public void tearDown() {
		System.exit(0);
	}
}
