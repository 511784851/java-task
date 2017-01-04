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
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

/**
 * 订阅消息
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class SubscribeMsg extends Thread {
	private Queue<PSubscribe> queue = new LinkedList<PSubscribe>();
	private Jedis jedis;

	/**
	 * 消息处理间隔时间（单位：毫秒）
	 */
	private long sleep_time = 5 * 60 * 1000;

	/**
	 * 消息处理失败要放到的队列
	 */
	private SubscribeMsg otherSubscribeMsg;

	/**
	 * 构造方法
	 * 
	 * @param sleep_time
	 * @param errorSubscribeMsg
	 */
	public SubscribeMsg(long sleep_time, SubscribeMsg otherSubscribeMsg) {
		this.sleep_time = sleep_time;
		this.otherSubscribeMsg = otherSubscribeMsg;
	}

	/**
	 * 添加消息到队列
	 * 
	 * @param uuid
	 * @param msgid
	 * @param time
	 */
	public void add(String uuid, int msgid, long time) {
		queue.add(PSubscribe.newBuilder().setUuid(uuid).setMsgid(msgid).setTime(time).build());
	}

	private void addAll(List<PSubscribe> subscribes) {
		queue.addAll(subscribes);
	}

	public void run() {
		while (true) {
			if (sleep_time > 0) {
				try {
					Thread.sleep(sleep_time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				jedis = RedisManager.getLongRedis();
				makeMsg();
			} catch (Exception e) {
				log.error("消息订阅处理异常:" + e.getMessage());
				e.printStackTrace();
			} finally {
				RedisManager.returnResource(jedis);
			}
		}
	}

	/**
	 * 处理队列消息
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void makeMsg() throws InterruptedException, IOException {
		while (true) {
			int len = queue.size();
			if (len == 0) {
				Thread.sleep(500);
			} else {
				int n = 0;// 一次最多处理的消息数量
				// 单次最多处理200条消息，并根据服务器分组
				Map<String, List<PSubscribe>> serverMap = new HashMap<String, List<PSubscribe>>();
				for (int i = 0; i < 200; i++) {
					PSubscribe subscribe = queue.poll();
					if (subscribe == null)
						break;
					step(serverMap, subscribe);
					n++;
				}
				// 消息订阅
				send(serverMap);
				if (n > len)
					break;
			}
		}
	}

	/**
	 * 校验消息是否需要订阅，以及订阅处理
	 * 
	 * @param serverMap
	 * @param subscribe
	 */
	private void step(Map<String, List<PSubscribe>> serverMap, PSubscribe subscribe) {
		String uuid = subscribe.getUuid();
		int msgid = subscribe.getMsgid();
		long time = subscribe.getTime();

		// 消息对应的服务器
		String server = TaskHelper.getServerByMsgid(msgid);
		if (Strings.isNullOrEmpty(server)) {
			return;
		}

		// 检查是否需要重新订阅
		String oldTimeStr = jedis.hget(Constant.GAME_MSGID + uuid, msgid + "");
		boolean bool = isSubscribe(time, oldTimeStr);
		if (bool) {
			groupMsg(serverMap, subscribe, server);
		}
	}

	/**
	 * 消息订阅根据服务器分组
	 * 
	 * @param serverMap
	 * @param subscribe
	 * @param server
	 */
	private void groupMsg(Map<String, List<PSubscribe>> serverMap, PSubscribe subscribe, String server) {
		List<PSubscribe> serList = serverMap.get(server);
		if (serList == null) {
			serList = new ArrayList<PSubscribe>();
		}
		serList.add(subscribe);
		serverMap.put(server, serList);
	}

	/**
	 * 检查是否需要重新订阅
	 * 
	 * @param time
	 * @param oldTimeStr
	 * @return
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

	/**
	 * 发送订阅消息
	 * 
	 * @param serverMap
	 * @throws IOException
	 */
	private void send(Map<String, List<PSubscribe>> serverMap) throws IOException {
		for (String server : serverMap.keySet()) {
			List<PSubscribe> list = serverMap.get(server);
			try {
				PResult result = subscribe(server, list);
				if (result.getErrorCode() == 0) {
					log.debug("[" + server + "]消息订阅成功: " + list.size());
					logTime(list);
				} else {
					log.error("[" + server + "]消息订阅失败: " + list.size() + " -> " + result.getErrorCode());
					otherSubscribeMsg.addAll(list);
				}
			} catch (Exception e) {
				log.error("[" + server + "]消息订阅异常: " + list.size() + " -> " + e.getMessage());
				otherSubscribeMsg.addAll(list);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 订阅
	 * 
	 * @param server
	 * @param list
	 * @return
	 * @throws IOException
	 * @throws InvalidProtocolBufferException
	 */
	private PResult subscribe(String server, List<PSubscribe> list) throws IOException, InvalidProtocolBufferException {
		String basePath = "chat".equals(server) ? "/" : "/v1/";
		basePath += server + "/inside/task/msg/subscribe?from=";
		CommonHttpClient httpClient = new CommonHttpClient(server);
		PMessage message = httpClient.subscribe(basePath, list);
		return PResult.parseFrom(message.getData());
	}

	/**
	 * 订阅成功，保存订阅时间
	 * 
	 * @param list
	 */
	private void logTime(List<PSubscribe> list) {
		for (PSubscribe subscribe : list) {
			jedis.hset(Constant.GAME_MSGID + subscribe.getUuid(), subscribe.getMsgid() + "", subscribe.getTime() + "");
		}
	}
}