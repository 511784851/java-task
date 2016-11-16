package com.blemobi.task.rest;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.http.client.ClientProtocolException;

import com.blemobi.library.exception.BaseException;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.TaskProtos.PTaskUserBasic;
import com.blemobi.sep.probuf.TaskProtos.PTaskUserPk;
import com.blemobi.task.basic.LevelHelper;
import com.blemobi.task.basic.LevelInfo;
import com.blemobi.task.notify.UserRelation;
import com.blemobi.task.util.Constant;
import com.blemobi.task.util.RankingUtil;
import com.blemobi.task.util.TaskUtil;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

import redis.clients.jedis.Jedis;

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
		String userInfoKey = Constant.GAME_USER_INFO + uuid;
		Jedis jedis = RedisManager.getRedis();
		boolean bool = jedis.exists(userInfoKey);
		RedisManager.returnResource(jedis);
		if (!bool) {// 未初始化
			PMessage message = UserRelation.getUserInfo(uuid);
			if (!"PUser".equals(message.getType())) {
				ReslutUtil.createErrorMessage(1001006, "用户不存在");
			}

			PUser user = PUser.parseFrom(message.getData());
			int levelType = user.getLevelInfo().getLevelType();
			if (!UserRelation.levelList.contains(levelType)) {
				ReslutUtil.createErrorMessage(2201000, "没有权限使用任务系统");
			}

			TaskUtil taskUtil = new TaskUtil(uuid, user);
			taskUtil.init();
		}

		TaskUtil taskUtil = new TaskUtil(uuid, language);
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
		TaskUtil taskUtil = new TaskUtil(uuid, language);
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
	public PMessage receive(@CookieParam("uuid") String uuid, @FormParam("taskIds") String taskIds)
			throws BaseException {
		String[] taskidArray = taskIds.split(",");
		for (String taskid : taskidArray) {
			TaskUtil taskUtil = new TaskUtil(uuid, Integer.parseInt(taskid));
			taskUtil.receive();
		}
		return ReslutUtil.createSucceedMessage();
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

	/**
	 * 排行
	 * 
	 * @param taskId
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	@GET
	@Path("rank")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage rank(@CookieParam("uuid") String uuid, @QueryParam("scope") String scope,
			@QueryParam("language") String language) throws BaseException, ClientProtocolException, IOException {

		RankingUtil rankingUtil = new RankingUtil(uuid);
		if ("public".equals(scope)) {
			return rankingUtil.rankingAll();
		} else if ("friend".equals(scope)) {
			return rankingUtil.rankFirend();
		} else if ("follower".equals(scope)) {
			return rankingUtil.rankFollow();
		}

		return null;
	}

	/**
	 * 排行
	 * 
	 * @param taskId
	 *            任务ID
	 * @return PMessage 返回PMessage对象数据
	 * @throws BaseException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	@GET
	@Path("pk")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage pk(@CookieParam("uuid") String uuid, @QueryParam("pk_uuid") String pk_uuid,
			@QueryParam("language") String language) throws BaseException, ClientProtocolException, IOException {
		Jedis jedis = RedisManager.getRedis();

		String userInfoKey = Constant.GAME_USER_INFO + uuid;
		PTaskUserBasic userBasic = getUserBasic(uuid, jedis, userInfoKey, language);

		String pkUserInfoKey = Constant.GAME_USER_INFO + pk_uuid;
		PTaskUserBasic pkUserBasic = getUserBasic(pk_uuid, jedis, pkUserInfoKey, language);

		int userTaskTotol = 0; // 自己已完成任务总数
		int pkUserTaskTotol = 0; // 对方已完成任务总数

		Map<String, String> userInfo = jedis.hgetAll(userInfoKey);
		if (userInfo != null) {
			userTaskTotol = Integer.parseInt(userInfo.get("num"));
		}

		Map<String, String> pkuserInfo = jedis.hgetAll(pkUserInfoKey);
		if (pkuserInfo != null) {
			pkUserTaskTotol = Integer.parseInt(pkuserInfo.get("num"));
		}

		RedisManager.returnResource(jedis);

		PTaskUserPk taskUserPk = PTaskUserPk.newBuilder().setUserBasic(userBasic).setPkUserBasic(pkUserBasic)
				.setUserTaskTotol(userTaskTotol).setPkUserTaskTotol(pkUserTaskTotol).build();

		return ReslutUtil.createReslutMessage(taskUserPk);
	}

	/*
	 * 获取用户基础信息
	 */
	private PTaskUserBasic getUserBasic(String uuid, Jedis jedis, String userInfoKey, String language)
			throws IOException {
		Map<String, String> userInfo = jedis.hgetAll(userInfoKey);
		if (userInfo != null) {
			int level = Integer.parseInt(userInfo.get("level"));
			long exp = Long.parseLong(userInfo.get("exp"));

			PMessage message = UserRelation.getUserInfo(uuid);
			PUser user = PUser.parseFrom(message.getData());

			LevelInfo levelInfo = LevelHelper.getLevelInfoByLevel(level);
			LevelInfo nextLevelInfo = LevelHelper.getNextLevelInfoByLevel(level);

			return PTaskUserBasic.newBuilder().setLevel(level).setExp(exp).setLevelName(levelInfo.getTitle(language))
					.setNextLevel(nextLevelInfo.getLevel()).setNextLevelExp(nextLevelInfo.getExp_min())
					.setNextLevelName(nextLevelInfo.getTitle(language)).setNickname(user.getNickname())
					.setHeadimg(user.getHeadImgURL()).build();
		}
		return null;
	}
}