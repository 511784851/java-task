package com.blemobi.gamification.rest;

import javax.servlet.http.Cookie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blemobi.gamification.core.GamificationManager;
import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.util.CommonUtil;
import com.blemobi.sep.probuf.ResultProtos.PMessage;

public class QuaryProcessTest {

	@Before
	public void setup() throws Exception {
		String[] arg = new String[] { "-env", "local" };
		GamificationManager.main(arg);
	}

	//@Test
	public void testTask() throws Exception {
		String uuid = "0efe519d-cddf-412c-a5e0-2e8f14f80edb";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		String basePath = "/gamification/user/task";

		BaseHttpClient clientUtil = new BaseHttpClient() {
			@Override
			public String createServerUrl(String basePath) {
				String[] accountInfo = new String[]{"localhost","9018"};
				return super.createUrl(accountInfo, basePath);
			}
		};
		PMessage message = clientUtil.getMethod(clientUtil.createServerUrl(basePath), null, cookies);

		System.out.println(message);
	}

	//@Test
	public void testAchievement() throws Exception {
		String uuid = "0efe519d-cddf-412c-a5e0-2e8f14f80edb";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		String basePath = "/gamification/user/achievement";

		BaseHttpClient clientUtil = new BaseHttpClient() {
			@Override
			public String createServerUrl(String basePath) {
				String[] accountInfo = new String[]{"localhost","9018"};
				return super.createUrl(accountInfo, basePath);
			}
		};
		PMessage message = clientUtil.getMethod(clientUtil.createServerUrl(basePath), null, cookies);

		System.out.println(message);
	}
	
	@After
	public void tearDown() {
		System.exit(0);
	}
}
