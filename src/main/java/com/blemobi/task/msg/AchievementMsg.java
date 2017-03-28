package com.blemobi.task.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.blemobi.library.client.AchievementHttpClient;
import com.blemobi.sep.probuf.AchievementApiProtos.PAchievementAction;
import com.blemobi.sep.probuf.AchievementApiProtos.PAchievementActions;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;

import lombok.extern.log4j.Log4j;

/**
 * 成就消息
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class AchievementMsg extends Thread {
	private static Queue<PAchievementAction> queue = new LinkedList<PAchievementAction>();

	private AchievementMsg() {

	}

	/**
	 * 添加消息到队列
	 * 
	 * @param uuid
	 * @param msgid
	 * @param value
	 */
	public static void add(String uuid, int msgid, int value) {
		PAchievementAction achievementAction = PAchievementAction.newBuilder().setUuid(uuid).setMsgid(msgid)
				.setValue(value).build();
		queue.add(achievementAction);
	}

	static {
		new AchievementMsg().start();
	}

	/**
	 * 线程处理队列消息
	 */
	public void run() {
		while (true) {
			try {
				int len = queue.size();
				if (len == 0) {
					Thread.sleep(500);
					continue;
				}
				List<PAchievementAction> list = getMoreActionList();
				send(list);
			} catch (Exception e) {
				log.error("成就队列处理异常");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 单次最多处理100条消息
	 * 
	 * @param list
	 */
	private List<PAchievementAction> getMoreActionList() {
		List<PAchievementAction> list = new ArrayList<PAchievementAction>();
		for (int i = 0; i < 100; i++) {
			PAchievementAction achievementAction = queue.poll();
			if (achievementAction == null)
				break;
			list.add(achievementAction);
		}
		return list;
	}

	/**
	 * 发送成就消息
	 * 
	 * @param list
	 * @throws IOException
	 */
	private void send(List<PAchievementAction> list) throws IOException {
		PAchievementActions achievementActions = PAchievementActions.newBuilder().addAllArray(list).build();
		AchievementHttpClient httpClient = new AchievementHttpClient();
		PMessage message = httpClient.action(achievementActions);
		PResult result = PResult.parseFrom(message.getData());
		if (result.getErrorCode() != 0) {
			log.error("成就消息发送失败  -> " + result.getErrorCode());
		}
	}
}