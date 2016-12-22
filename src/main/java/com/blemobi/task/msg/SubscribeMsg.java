package com.blemobi.task.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.blemobi.library.client.CommonHttpClient;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.sep.probuf.TaskProtos.PSubscribe;
import com.blemobi.task.basic.TaskHelper;
import com.blemobi.task.util.Constant;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/*
 * 订阅消息
 */
@Log4j
public class SubscribeMsg extends Thread {
	private static Queue<PSubscribe> queue = new LinkedList<PSubscribe>();
	private Jedis jedis;

	private SubscribeMsg() {

	}

	/*
	 * 添加消息到队列
	 */
	public static void add(String uuid, int msgid, long time) {
		queue.add(PSubscribe.newBuilder().setUuid(uuid).setMsgid(msgid).setTime(time).build());
	}

	private void add(PSubscribe subscribe) {
		queue.add(subscribe);
	}

	static {
		new SubscribeMsg().start();
	}

	/*
	 * 线程处理队列消息
	 */
	public void run() {
		jedis = RedisManager.getLongRedis();
		while (true) {
			try {
				PSubscribe subscribe = queue.poll();
				if (subscribe == null) {
					Thread.sleep(500);
					continue;
				}
				// 单次最多处理200条消息，并根据服务器分组
				Map<String, List<PSubscribe>> serverMap = new HashMap<String, List<PSubscribe>>();
				step(serverMap, subscribe);
				for (int i = 0; i < 200; i++) {
					subscribe = queue.poll();
					if (subscribe == null) {
						break;
					}
					step(serverMap, subscribe);
				}
				// 消息订阅
				send(serverMap);
			} catch (Exception e) {
				log.error("消息订阅处理异常");
				e.printStackTrace();
			}
		}
	}

	/*
	 * 校验消息是否需要订阅，以及订阅处理
	 */
	private void step(Map<String, List<PSubscribe>> serverMap, PSubscribe subscribe) {
		String uuid = subscribe.getUuid();
		int msgid = subscribe.getMsgid();
		long time = subscribe.getTime();

		String server = TaskHelper.getServerByMsgid(msgid);
		String logStr = server + ",uuid=[" + uuid + "],msgid=[" + msgid + "],time=[" + time + "]";

		if (Strings.isNullOrEmpty(server)) {
			log.debug("消息订阅没有对应的逻辑服务器 -> " + logStr);
			return;
		}

		String oldTimeStr = jedis.hget(Constant.GAME_MSGID + uuid, msgid + "");
		logStr += ",oldTime=[" + oldTimeStr + "]";

		// 检查是否需要重新订阅
		boolean bool = isSubscribe(time, oldTimeStr);
		if (bool) {
			log.debug("消息需要订阅 -> " + logStr);
			List<PSubscribe> serList = serverMap.get(server);
			if (serList == null) {
				serList = new ArrayList<PSubscribe>();
			}
			serList.add(subscribe);
			serverMap.put(server, serList);
		} else {
			log.debug("消息不需要订阅 -> " + logStr);
		}
	}

	/*
	 * 检查是否需要重新订阅
	 */
	private boolean isSubscribe(long time, String oldTimeStr) {
		if (Strings.isNullOrEmpty(oldTimeStr)) {// 还没有订阅
			return true;
		}
		long oldTime = Long.parseLong(oldTimeStr);
		if (oldTime == -1) {// 已订阅永久有效
			return false;
		}
		if (oldTime < time) {// 订阅已失效
			return true;
		}
		return false;
	}

	/*
	 * 发送订阅消息
	 */
	private void send(Map<String, List<PSubscribe>> serverMap) throws IOException {
		for (String server : serverMap.keySet()) {
			List<PSubscribe> list = serverMap.get(server);
			try {
				String basePath = "chat".equals(server) ? "/" : "/v1/";
				basePath += server + "/inside/task/msg/subscribe?from=";
				CommonHttpClient httpClient = new CommonHttpClient(server);
				PMessage message = httpClient.subscribe(basePath, list);
				PResult result = PResult.parseFrom(message.getData());
				if (result.getErrorCode() == 0) {
					log.debug("[" + server + "]消息订阅成功: " + list.size());
					for (PSubscribe subscribe : list) {
						jedis.hset(Constant.GAME_MSGID + subscribe.getUuid(), subscribe.getMsgid() + "",
								subscribe.getTime() + "");
					}
				} else {
					log.debug("[" + server + "]消息订阅失败: " + list.size() + " -> " + result.getErrorCode());
					for (PSubscribe subscribe : list) {
						add(subscribe);
					}
				}
			} catch (Exception e) {
				log.debug("[" + server + "]消息订阅异常: " + list.size() + " -> " + e.getMessage());
				for (PSubscribe subscribe : list) {
					add(subscribe);
				}
				e.printStackTrace();
			}
		}
	}
}