package com.blemobi.gamification.rest;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.gamification.task.TaskManager;
import com.blemobi.library.exception.BaseException;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/task")
public class TaskProcess {
	/**
	 * 任务通知
	 * 
	 * @param taskKey
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("serNotify")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage serNotify(@FormParam("uuid") String uuid, @FormParam("taskKey") String taskKey)
			throws BaseException {

		TaskManager taskManager = new TaskManager(uuid, taskKey);
		return taskManager.onFinish();
	}

	/**
	 * APP任务通知
	 * 
	 * @param taskKey
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("notify")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage notify(@CookieParam("uuid") String uuid, @FormParam("taskKey") String taskKey)
			throws BaseException {

		return this.serNotify(uuid, taskKey);
	}

	/**
	 * 任务接取
	 * 
	 * @param taskKey
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("accept")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage accept(@CookieParam("uuid") String uuid, @CookieParam("token") String token,
			@FormParam("taskKey") String taskKey) throws BaseException {

		TaskManager taskManager = new TaskManager(uuid, taskKey);
		return taskManager.onAccept();
	}
}