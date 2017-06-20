package com.blemobi.task.rest;

import java.io.IOException;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.service.TaskService;
import com.blemobi.task.service.UserTask;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/v1/task/gold")
public class TaskProcess {

	/**
	 * 金币和任务列表
	 * 
	 * @param uuid
	 * @param language
	 * @return
	 * @throws IOException
	 */
	@GET
	@Path("list")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage list(@CookieParam("uuid") String uuid, @QueryParam("language") String language) throws IOException {
		UserTask userTask = new UserTask(uuid);
		userTask.init();

		TaskService taskService = new TaskService(uuid);
		return taskService.list();
	}

	/**
	 * 领奖励
	 * 
	 * @param uuid
	 * @param ID
	 * @return
	 */
	@POST
	@Path("receive")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage receive(@CookieParam("uuid") String uuid, @FormParam("ID") short ID) {
		TaskService taskService = new TaskService(uuid);
		return taskService.receive(ID);
	}

	/**
	 * 金币明细
	 * 
	 * @param uuid
	 * @param language
	 * @param size
	 * @param start
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("details")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage details(@CookieParam("uuid") String uuid, @QueryParam("language") String language,
			@QueryParam("idx") long idx, @QueryParam("size") int size) {
		TaskService taskService = new TaskService(uuid);
		return taskService.details(idx, size, 0, 0, (byte) 0);
	}
}