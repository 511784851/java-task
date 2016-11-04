package com.blemobi.task.rest;

import java.util.List;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.library.exception.BaseException;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.TaskProtos.PCallbackArray;
import com.blemobi.sep.probuf.TaskProtos.PExpLevel;
import com.blemobi.sep.probuf.TaskProtos.PExpLevelList;
import com.blemobi.task.basic.LevelHelper;
import com.blemobi.task.basic.LevelInfo;
import com.blemobi.task.basic.TaskHelper;
import com.blemobi.task.basic.TaskTag;
import com.blemobi.task.util.CallbackManager;
import com.blemobi.task.util.Constant;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

import redis.clients.jedis.Jedis;

@Path("/callback")
public class CallbackProcess {
	/**
	 * 消息回调（for server）
	 * 
	 * @param callbackArray
	 *            消息内容
	 * @return PMessage 返回PMessage对象数据
	 * @throws InvalidProtocolBufferException
	 * @throws BaseException
	 */
	@POST
	@Path("msgid")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage msgCallback(PMessage message) throws InvalidProtocolBufferException {
		PCallbackArray callbackArray = PCallbackArray.parseFrom(message.getData());
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
	@Path("app")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage callback(@CookieParam("uuid") String uuid, @FormParam("type") int type) {
		if (type != 4070) {
			return ReslutUtil.createErrorMessage(1801001, "任务类型错误");
		}

		Jedis jedis = RedisManager.getRedis();
		long dailyTime = TaskHelper.getDailyDate();
		// 消息ID对应的任务
		List<Integer> taskIdList = TaskHelper.getTaskListByMsgid(type);
		for (int taskId : taskIdList) {
			TaskTag tag = TaskHelper.getTaskTag(taskId);// 任务类型
			String userTaskKey = "";// 用户任务KEY
			if (TaskTag.MAIN == tag) {// 主线任务
				userTaskKey = Constant.GAME_TASK_MAIN + uuid;
			} else if (TaskTag.DAILY == tag) {// 日常任务
				userTaskKey = Constant.GAME_TASK_DAILY + uuid + ":" + dailyTime;
			}
			String targetStr = jedis.hget(userTaskKey, taskId + "");
			if (!Strings.isNullOrEmpty(targetStr)) {// 任务已初始化
				int target = Integer.parseInt(targetStr);
				if (target >= 0) {// 任务未完成
					// 累加一次任务进度
					jedis.hincrBy(userTaskKey, taskId + "", 1);
				}
			}
		}

		RedisManager.returnResource(jedis);
		return ReslutUtil.createSucceedMessage();
	}

	@GET
	@Path("level")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage level() throws Exception {
		PExpLevelList.Builder expLevelList = PExpLevelList.newBuilder();

		// 等级信息
		for (LevelInfo li : LevelHelper.getAllLevelList()) {
			PExpLevel expLevel = PExpLevel.newBuilder().setId(li.getLevel()).setTitleSc(li.getTitle_sc())
					.setTitleTc(li.getTitle_tc()).setTitleKr(li.getTitle_kr()).setTitleEn(li.getTitle_en()).build();
			expLevelList.addExpLevel(expLevel);
		}

		return ReslutUtil.createReslutMessage(expLevelList.build());
	}
}