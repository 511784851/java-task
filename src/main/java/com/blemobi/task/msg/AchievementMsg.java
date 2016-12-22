package com.blemobi.task.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.blemobi.library.client.AchievementHttpClient;
import com.blemobi.sep.probuf.AchievementProtos.PAchievementAction;
import com.blemobi.sep.probuf.AchievementProtos.PAchievementActions;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;

import lombok.extern.log4j.Log4j;

/*
 * 成就消息
 */
@Log4j
public class AchievementMsg extends Thread {
	private static Queue<PAchievementAction> queue = new LinkedList<PAchievementAction>();

	private AchievementMsg() {

	}

	/*
	 * 添加消息到队列
	 */
	public static void add(String uuid, int msgid, int value) {
		PAchievementAction achievementAction = PAchievementAction.newBuilder().setUuid(uuid).setMsgid(msgid)
				.setValue(value).build();
		queue.add(achievementAction);
	}

	static {
		new AchievementMsg().start();
	}

	/*
	 * 线程处理队列消息
	 */
	public void run() {
		while (true) {
			try {
				PAchievementAction achievementAction = queue.poll();
				if (achievementAction == null) {
					Thread.sleep(500);
					continue;
				}
				// 单次最多处理100条消息
				List<PAchievementAction> list = new ArrayList<PAchievementAction>();
				list.add(achievementAction);
				log.debug("用户[" + achievementAction.getUuid() + "]达成了成就 -> " + achievementAction.toString());
				for (int i = 0; i < 99; i++) {
					achievementAction = queue.poll();
					if (achievementAction == null) {
						break;
					}
					list.add(achievementAction);
					log.debug("用户[" + achievementAction.getUuid() + "]达成了成就 -> " + achievementAction.toString());
				}
				// 发送成就消息
				send(list);
			} catch (Exception e) {
				log.error("成就队列处理异常");
				e.printStackTrace();
			}
		}
	}

	/*
	 * 发送成就消息
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