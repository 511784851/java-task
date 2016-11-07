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
		Thread.currentThread();
	}

	static {
		new SubscribeThread().start();
	}

	public void run() {
		Jedis jedis = RedisManager.getLongRedis();
		while (true) {
			// 队列中取出一个成员
			PSubscribe subscribe = queue.poll();
			if (subscribe == null) {
				// log.debug("没有消息订阅");
				try {
					Thread.sleep(1000);
				} catch (Exception e) {

				}
			} else {
				String uuid = subscribe.getUuid();
				int msgid = subscribe.getMsgid();
				long time = subscribe.getTime();

				String server = TaskHelper.getServerByMsgid(msgid);
				String logStr = "订阅时间=[" + System.currentTimeMillis() + "] - uuid=[" + uuid + "],msgid=[" + msgid
						+ "],time=[" + time + "],server=[" + server + "]";

				if (Strings.isNullOrEmpty(server)) {
					log.debug("消息订阅没有对应的逻辑服务器 -> " + logStr);
					continue;
				}

				String oldTimeStr = jedis.hget(Constant.GAME_MSGID + uuid, msgid + "");
				logStr += ",oldTime=[" + oldTimeStr + "]";

				// 检查是否需要重新订阅
				boolean bool = isSubscribe(time, oldTimeStr);
				if (bool) {
					try {
						PResult result = subscribe(subscribe);
						if (result.getErrorCode() == 0) {
							jedis.hset(Constant.GAME_MSGID + uuid, msgid + "", time + "");
							log.debug("消息订阅成功 -> " + logStr);
						} else {
							log.error("消息订阅失败 -> " + logStr + " error: " + result.getErrorCode());
						}
					} catch (Exception e) {
						log.error("消息订阅异常 -> " + logStr);
						// e.printStackTrace();
					}
				} else {
					log.debug("消息不需要订阅 -> " + logStr);
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

		String basePath = "/";
		if (!"chat".equals(server)) {
			basePath += "v1/";
		}

		basePath += server + "/inside/task/msg/subscribe?from=task";
		BaseHttpClient httpClient = new CommonHttpClient(server, basePath, null, null);
		PMessage message = httpClient.postBodyMethod(subscribeArray.toByteArray(), "application/x-protobuf");
		PResult result = PResult.parseFrom(message.getData());

		return result;
	}
}