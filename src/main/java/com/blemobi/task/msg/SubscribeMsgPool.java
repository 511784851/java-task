package com.blemobi.task.msg;

/**
 * 消息队列管理类
 * 
 * @author zhaoyong
 *
 */
public class SubscribeMsgPool {
	/**
	 * 错误消息处理间隔时间（单位：毫秒）
	 */
	private static long error_sleep_time = 5 * 60 * 1000;

	private static SubscribeMsg newSubscribeMsg;
	private static SubscribeMsg errorSubscribeMsg;

	/**
	 * 初始化
	 */
	static {
		newSubscribeMsg = new SubscribeMsg(0, errorSubscribeMsg);
		newSubscribeMsg.start();
		errorSubscribeMsg = new SubscribeMsg(error_sleep_time, errorSubscribeMsg);
		errorSubscribeMsg.start();
	}

	/**
	 * 添加订阅消息
	 * 
	 * @param uuid
	 * @param msgid
	 * @param time
	 */
	public static void add(String uuid, int msgid, long time) {
		newSubscribeMsg.add(uuid, msgid, time);
	}
}