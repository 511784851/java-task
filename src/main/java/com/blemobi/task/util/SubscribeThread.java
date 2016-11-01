package com.blemobi.task.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.CommonHttpClient;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.sep.probuf.TaskProtos.PSubscribe;
import com.blemobi.sep.probuf.TaskProtos.PSubscribeArray;
import com.blemobi.task.basic.TaskHelper;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * 消息订阅
 */
@Log4j
public class SubscribeThread extends Thread {
	private static Queue<PSubscribe> queue = new LinkedList<PSubscribe>();

	private SubscribeThread() {

	}

	public static void addQueue(String uuid, int msgid, long time) {
		queue.add(PSubscribe.newBuilder().setUuid(uuid).setMsgid(msgid).setTime(time).build());
		SubscribeThread.interrupted();
	}

	static {
		new SubscribeThread().start();
	}

	public void run() {
		Jedis jedis = RedisManager.getRedis();
		while (true) {
			// 队列中取出一个成员
			PSubscribe subscribe = queue.poll();
			if (subscribe == null) {
				RedisManager.returnResource(jedis);
				log.debug("没有消息订阅");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					log.debug("消息订阅线程被唤醒了");
				}
				jedis = RedisManager.getRedis();
			} else {
				String uuid = subscribe.getUuid();
				int msgid = subscribe.getMsgid();
				long time = subscribe.getTime();
				try {
					String oldTimeStr = jedis.hget(Constant.GAME_MSGID + uuid, msgid + "");
					String logStr = "uuid=[" + uuid + "],msgid=[" + msgid + "],time=[" + time + "],oldTime=["
							+ oldTimeStr + "]";
					log.error("有消息订阅 -> " + logStr);
					// 检查是否需要重新订阅
					boolean bool = isSubscribe(time, oldTimeStr);
					if (bool) {
						PResult result = subscribe(subscribe);
						if (result.getErrorCode() == 0) {
							jedis.hset(Constant.GAME_MSGID + uuid, msgid + "", time + "");
							log.debug("消息订阅成功 -> " + logStr);
						} else {
							// addQueue(uuid, msgid, time);// 放回队列
							log.error("消息订阅失败 -> uuid=[" + logStr + " error: " + result.getErrorCode());
						}
					} else {
						log.debug("消息不需要订阅 -> " + logStr);
					}
				} catch (Exception e) {
					// addQueue(uuid, msgid, time);// 放回队列
					log.error("消息订阅异常: " + e.getMessage());
					e.printStackTrace();
				}
			}

		}
	}

	/*
	 * 检查是否需要重新订阅
	 */
	private boolean isSubscribe(long time, String oldTimeStr) {
		boolean bool = false;
		if (Strings.isNullOrEmpty(oldTimeStr)) {// 还没有订阅
			bool = true;
		} else if (!"-1".equals(oldTimeStr)) {// 已订阅(排除已订阅永久有效)
			long oldTime = Long.parseLong(oldTimeStr);
			if (oldTime < time) {// 订阅已失效，需要重新订阅
				bool = true;
			}
		}
		return bool;
	}

	/*
	 * 订阅
	 */
	private PResult subscribe(PSubscribe subscribe) throws IOException {
		PSubscribeArray subscribeArray = PSubscribeArray.newBuilder().addSubscribe(subscribe).build();

		String server = TaskHelper.getServerByMsgid(subscribe.getMsgid());
		String basePath = "/v1/" + server + "/inside/task/msg/subscribe?from=gamification";
		BaseHttpClient httpClient = new CommonHttpClient(server, basePath, null, null);
		PMessage message = httpClient.postBodyMethod(subscribeArray.toByteArray());
		PResult result = PResult.parseFrom(message.getData());

		return result;
	}
}