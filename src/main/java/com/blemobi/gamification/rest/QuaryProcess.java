package com.blemobi.gamification.rest;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.gamification.task.QuaryManager;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/user")
public class QuaryProcess {

	/**
	 * 查询任务明细
	 * 
	 * @param uuid
	 *            用户uuid
	 * @return PMessage 返回PMessage对象数据
	 */
	@GET
	@Path("task")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage detail(@CookieParam("uuid") String uuid) {
		QuaryManager quaryManager = new QuaryManager(uuid);
		return quaryManager.taskDetail();
	}

	/**
	 * 查询成就明细
	 * 
	 * @param uuid
	 *            用户uuid
	 * @return PMessage 返回PMessage对象数据
	 */
	@GET
	@Path("achievement")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage achievement(@CookieParam("uuid") String uuid) {

		QuaryManager quaryManager = new QuaryManager(uuid);
		return quaryManager.achievement();
	}
}