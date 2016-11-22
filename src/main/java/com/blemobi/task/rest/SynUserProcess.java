package com.blemobi.task.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.blemobi.library.redis.RedisManager;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.AccountProtos.PUserBaseList;
import com.blemobi.sep.probuf.ResultProtos.PBinaryMsg;
import com.blemobi.sep.probuf.ResultProtos.PBinaryMsgList;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.msg.SubscribeMsg;
import com.blemobi.task.util.Constant;
import com.blemobi.task.util.UserRelation;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/**
 * @author andy.zhao@blemobi.com 消息订阅
 */
@Log4j
@Path("/v1/task/inside")
public class SynUserProcess {

	/**
	 * 用户信息变更同步
	 * 
	 * @throws InvalidProtocolBufferException
	 */
	@POST
	@Path("msgpush/consumer")
	@Produces(MediaTypeExt.APPLICATION_PROTOBUF)
	public PMessage consumer(PBinaryMsgList binaryMsgList) throws InvalidProtocolBufferException {
		log.debug("有用户信息变更的同步数据");
		Jedis jedis = RedisManager.getRedis();
		List<PBinaryMsg> list = binaryMsgList.getListList();
		for (PBinaryMsg binaryMsg : list) {
			if (binaryMsg.getMsgType() != 2) {
				continue;
			}
			PUserBaseList userBaseListArray = PUserBaseList.parseFrom(binaryMsg.getMsgData());
			for (PUserBase user : userBaseListArray.getListList()) {
				String uuid = user.getUUID();
				log.debug("有用户信息变更的同步数据了 uuid=" + uuid);
				try {
					String userInfoKey = Constant.GAME_USER_INFO + uuid;
					boolean bool = jedis.exists(userInfoKey);
					if (bool) {// 已初始化
						jedis.hset(userInfoKey, "nickname", user.getNickname());
						jedis.hset(userInfoKey, "headimg", user.getHeadImgURL());
						jedis.hset(userInfoKey, "language", user.getLanguage());
						jedis.hset(userInfoKey, "levelType", user.getLevel() + "");
						if (!UserRelation.levelList.contains(user.getLevel())) {
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
								SubscribeMsg.add(uuid, Integer.parseInt(msgid), 0);// 消息订阅（取消）
							}
						}
					}
				} catch (Exception e) {
					log.debug("有用户信息变更的同步数据异常了 uuid=" + uuid);
					e.printStackTrace();
				}
			}
		}
		RedisManager.returnResource(jedis);
		return ReslutUtil.createSucceedMessage();
	}
}