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
import com.blemobi.library.util.CommonUtil;
import com.blemobi.sep.probuf.ResultProtos.PMessage;

public class NotifyProcessTest {

	@Before
	public void setup() throws Exception {
		String[] arg = new String[] { "-env", "local" };
		GamificationManager.main(arg);
	}

	//@Test
	public void test() throws Exception {
		String uuid = "0efe519d-cddf-412c-a5e0-2e8f14f80edb";
		String token = "EiBmN2UzMzM5ZWFiOGZmZTJkZTg5MTE2NGQ2YjJiOGRiMBjYtte8BQ==";
		String taskKey = "PUBLISH";

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("taskKey", taskKey));

		Cookie[] cookies = CommonUtil.createLoginCookieParams(uuid, token);
		String basePath = "/gamification/task/notify";

		BaseHttpClient clientUtil = new BaseHttpClient() {
			@Override
			public String createServerUrl(String basePath) {
				String[] accountInfo = new String[]{"localhost","9018"};
				return super.createUrl(accountInfo, basePath);
			}
		};
		PMessage message = clientUtil.postMethod(clientUtil.createServerUrl(basePath), params, cookies);

		System.out.println(message);
	}

	@After
	public void tearDown() {
		System.exit(0);
	}
}
