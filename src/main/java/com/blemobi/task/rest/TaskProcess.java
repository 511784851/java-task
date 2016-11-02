package com.blemobi.task.rest;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.blemobi.library.exception.BaseException;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.notify.UserRelation;
import com.blemobi.task.util.TaskUtil;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

@Path("/user")
public class TaskProcess {

	/**
	 * 任务列表
	 * 
	 * @param language
	 *            语言
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@GET
	@Path("list")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage list(@CookieParam("uuid") String uuid, @QueryParam("language") String language) throws Exception {
		PMessage message = UserRelation.getUserInfo(uuid);
		if (!"PUser".equals(message.getType())) {
			return ReslutUtil.createErrorMessage(1801001, "用户不存在");
		}

		PUser user = PUser.parseFrom(message.getData());
		int levelType = user.getLevelInfo().getLevelType();
		if (UserRelation.levelList.contains(levelType)) {
			return ReslutUtil.createErrorMessage(1901001, "没有权限使用任务系统");
		}

		TaskUtil taskUtil = new TaskUtil(uuid, language, user.getNickname(), user.getHeadImgURL());
		taskUtil.init();
		return taskUtil.list();
	}

	/**
	 * 等级列表
	 * 
	 * @param language
	 *            语言
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@GET
	@Path("level")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage level(@CookieParam("uuid") String uuid, @QueryParam("language") String language) throws Exception {
		PMessage message = UserRelation.getUserInfo(uuid);
		if (!"PUser".equals(message.getType())) {
			return ReslutUtil.createErrorMessage(1801001, "用户不存在");
		}

		PUser user = PUser.parseFrom(message.getData());
		int levelType = user.getLevelInfo().getLevelType();
		if (UserRelation.levelList.contains(levelType)) {
			return ReslutUtil.createErrorMessage(1901001, "没有权限使用任务系统");
		}

		TaskUtil taskUtil = new TaskUtil(uuid, language, user.getNickname(), user.getHeadImgURL());
		taskUtil.init();
		return taskUtil.level();
	}

	/**
	 * 接任务
	 * 
	 * @param taskId
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("receive")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage receive(@CookieParam("uuid") String uuid, @FormParam("taskId") int taskId) throws BaseException {
		TaskUtil taskUtil = new TaskUtil(uuid, taskId);
		return taskUtil.receive();
	}

	/**
	 * 领奖励
	 * 
	 * @param taskId
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 */
	@POST
	@Path("reward")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage reward(@CookieParam("uuid") String uuid, @FormParam("taskId") int taskId) throws BaseException {
		TaskUtil taskUtil = new TaskUtil(uuid, taskId);
		return taskUtil.reward();
	}
}