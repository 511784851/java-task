package com.blemobi.gamification.rest;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blemobi.library.client.LocalHttpClient;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.TaskApiProtos.PTaskMsg;
import com.blemobi.sep.probuf.TaskApiProtos.PTaskMsgs;
import com.blemobi.task.core.TaskManager;

public class CallbackProcessTest {
	private int port;

	@Before
	public void setup() throws Exception {
		port = 9018;
		String[] arg = new String[] { "-env", "local" };
		TaskManager.main(arg);
	}

	@Test
	public void testSerNotify() throws Exception {
		StringBuilder basePath = new StringBuilder("/v1/task/callback/msgid");

		PTaskMsg taskMsg = PTaskMsg.newBuilder().setUuid("1472020016134289985").setMsgID(202).setCount(1).build();

		PTaskMsgs callbackArray = PTaskMsgs.newBuilder().addTaskMsg(taskMsg).build();

		LocalHttpClient clientUtil = new LocalHttpClient("127.0.0.1", port, null, null, callbackArray.toByteArray(),
				"application/x-protobuf");
		PMessage message = clientUtil.postBodyMethod(basePath.toString());
		assertEquals("PInt64List", message.getType());
	}

	@After
	public void tearDown() {
		System.exit(0);
	}
}
