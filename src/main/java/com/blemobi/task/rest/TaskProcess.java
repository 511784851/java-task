package com.blemobi.task.rest;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.library.exception.BaseException;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.util.TaskUtil;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/user")
public class TaskProcess {
	/**
	 * 任务列表
	 * 
	 * @param taskId
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@GET
	@Path("list")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage list(@CookieParam("uuid") String uuid) throws BaseException {
		TaskUtil taskUtil = new TaskUtil(uuid);
		taskUtil.init();
		return taskUtil.list();
	}

	/**
	 * 任务接取
	 * 
	 * @param taskId
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("receive")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage receive(@CookieParam("uuid") String uuid, @CookieParam("token") String token,
			@FormParam("taskId") int taskId) throws BaseException {
		TaskUtil taskUtil = new TaskUtil(uuid, taskId);
		return taskUtil.receive();
	}

	/**
	 * 任务领奖励
	 * 
	 * @param taskId
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("reward")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage reward(@CookieParam("uuid") String uuid, @CookieParam("token") String token,
			@FormParam("taskId") int taskId) throws BaseException {
		TaskUtil taskUtil = new TaskUtil(uuid, taskId);
		return taskUtil.reward();
	}

}