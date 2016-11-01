package com.blemobi.task.rest;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.library.exception.BaseException;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.TaskProtos.PCallbackArray;
import com.blemobi.task.util.CallbackManager;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/callback")
public class CallbackProcess {
	/**
	 * 消息回调（for server）
	 * 
	 * @param callbackArray
	 *            消息内容
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("msgid")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage msgCallback(PCallbackArray callbackArray) {
		CallbackManager callbackManager = new CallbackManager(callbackArray);
		return callbackManager.callback();
	}

	/**
	 * 任务回调（for client）
	 * 
	 * @param taskId
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("client")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage callback(@CookieParam("uuid") String uuid, @FormParam("taskId") int taskId) {

		return null;
	}
}