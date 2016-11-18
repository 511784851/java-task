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
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.TaskProtos.PTaskUserBasic;
import com.blemobi.sep.probuf.TaskProtos.PTaskUserPk;
import com.blemobi.task.basic.LevelHelper;
import com.blemobi.task.basic.LevelInfo;
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
			if (!taskUtil.receive()) {
				return ReslutUtil.createErrorMessage(2901001, "任务不可接取");
			}
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
	 * pk
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
		Map<String, String> userInfo = jedis.hgetAll(Constant.GAME_USER_INFO + uuid); // 自己
		Map<String, String> pkuserInfo = jedis.hgetAll(Constant.GAME_USER_INFO + pk_uuid); // 对方
		RedisManager.returnResource(jedis);

		PTaskUserBasic userBasic = getUserBasic(userInfo, language);
		PTaskUserBasic pkUserBasic = getUserBasic(pkuserInfo, language);

		PTaskUserPk taskUserPk = PTaskUserPk.newBuilder().setUserBasic(userBasic).setPkUserBasic(pkUserBasic)
				.setUserTaskTotol(Integer.parseInt(userInfo.get("num")))
				.setPkUserTaskTotol(Integer.parseInt(pkuserInfo.get("num"))).build();

		return ReslutUtil.createReslutMessage(taskUserPk);
	}

	/*
	 * 获取用户基础信息
	 */
	private PTaskUserBasic getUserBasic(Map<String, String> userInfo, String language) throws IOException {
		int level = Integer.parseInt(userInfo.get("level"));
		long exp = Long.parseLong(userInfo.get("exp"));

		LevelInfo levelInfo = LevelHelper.getLevelInfoByLevel(level);
		LevelInfo nextLevelInfo = LevelHelper.getNextLevelInfoByLevel(level);

		return PTaskUserBasic.newBuilder().setLevel(level).setExp(exp).setLevelName(levelInfo.getTitle(language))
				.setNextLevel(nextLevelInfo.getLevel()).setNextLevelExp(nextLevelInfo.getExp_min())
				.setNextLevelName(nextLevelInfo.getTitle(language)).setNickname(userInfo.get("nickname"))
				.setHeadimg(userInfo.get("headimg")).build();
	}
}