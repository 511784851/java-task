package com.blemobi.gamification.rest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.LocalHttpClient;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.TaskProtos.PCallback;
import com.blemobi.sep.probuf.TaskProtos.PCallbackArray;

public class CallbackProcessTest {
	private int port;

	@Before
	public void setup() throws Exception {
		port = 9018;
		String[] arg = new String[] { "-env", "local" };
		// GamificationManager.main(arg);
	}

	@Test
	public void testSerNotify() throws Exception {
		String basePath = "/task/callback/msgid";

		PCallback callback = PCallback.newBuilder().setUuid("123456789").setMsgid(1001)
				.setTime(System.currentTimeMillis()).build();

		PCallbackArray callbackArray = PCallbackArray.newBuilder().addCallback(callback).build();

		BaseHttpClient clientUtil = new LocalHttpClient("127.0.0.1", port, basePath, null, null);
		PMessage message = clientUtil.postBodyMethod(callbackArray.toByteArray(), "application/x-protobuf");
	}

	@After
	public void tearDown() {
		System.exit(0);
	}
}
