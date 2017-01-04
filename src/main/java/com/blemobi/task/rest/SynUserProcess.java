package com.blemobi.task.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.library.cache.UserBaseCache;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.AccountProtos.PUserBaseList;
import com.blemobi.sep.probuf.ResultProtos.PBinaryMsg;
import com.blemobi.sep.probuf.ResultProtos.PBinaryMsgList;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.msg.SubscribeMsgPool;
import com.blemobi.task.util.Constant;
import com.blemobi.task.util.UserRelation;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

import redis.clients.jedis.Jedis;

@Path("/v1/task/inside")
public class SynUserProcess {
	/**
	 * 用户信息变更同步
	 * 
	 * @param binaryMsgList
	 *            用户数据
	 * @return
	 * @throws InvalidProtocolBufferException
	 */
	@POST
	@Path("msgpush/consumer")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage consumer(PBinaryMsgList binaryMsgList) throws InvalidProtocolBufferException {
		Jedis jedis = RedisManager.getRedis();
		List<PBinaryMsg> list = binaryMsgList.getListList();
		for (PBinaryMsg binaryMsg : list) {
			if (binaryMsg.getMsgType() != 2) {
				continue;
			}
			PUserBaseList userBaseListArray = PUserBaseList.parseFrom(binaryMsg.getMsgData());
			for (PUserBase user : userBaseListArray.getListList()) {
				String uuid = user.getUUID();
				UserBaseCache.put(uuid, user);// 更新缓存
				if (!UserRelation.levelList.contains(user.getLevel())) {
					delVOData(jedis, uuid);
				}
			}
		}
		RedisManager.returnResource(jedis);
		return ReslutUtil.createSucceedMessage();
	}

	/**
	 * 清楚VO用户任务数据
	 * 
	 * @param jedis
	 * @param uuid
	 */
	private void delVOData(Jedis jedis, String uuid) {
		String userInfoKey = Constant.GAME_USER_INFO + uuid;
		// 删除用户基础信息
		jedis.del(userInfoKey);
		// 删除用户主线任务信息
		jedis.del(Constant.GAME_TASK_MAIN + uuid);
		// 删除用户日常任务信息
		Set<String> set = jedis.keys(Constant.GAME_TASK_DAILY + uuid + "*");
		for (String key : set) {
			jedis.del(key);
		}
		// 删除消息订阅以及取消消息订阅
		Map<String, String> userMsgids = jedis.hgetAll(Constant.GAME_MSGID + uuid);
		jedis.del(Constant.GAME_MSGID + uuid);
		for (String msgid : userMsgids.keySet()) {
			SubscribeMsgPool.add(uuid, Integer.parseInt(msgid), 0);// 消息订阅（取消）
		}
	}
}