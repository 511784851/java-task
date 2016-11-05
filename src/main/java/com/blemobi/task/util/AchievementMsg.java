package com.blemobi.task.util;

import com.blemobi.library.client.AchievementHttpClient;
import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.sep.probuf.AchievementProtos.PAchievementAction;
import com.blemobi.sep.probuf.AchievementProtos.PAchievementActions;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;

import lombok.extern.log4j.Log4j;

@Log4j
public class AchievementMsg {
	private String uuid;
	private int msgid;
	private int value;

	public AchievementMsg(String uuid, int msgid, int value) {
		this.uuid = uuid;
		this.msgid = msgid;
		this.value = value;
	}

	// 保存通知
	public void notifyMsg() {
		try {
			log.debug("用户[" + uuid + "]达成了成就-> msgid:" + msgid + "; value:" + value);

			PAchievementAction pushMsg = PAchievementAction.newBuilder().setUuid(uuid).setMsgid(msgid).setValue(value)
					.build();

			PAchievementActions achievementActions = PAchievementActions.newBuilder().addArray(pushMsg).build();

			send(achievementActions);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 添加通知
	public void send(PAchievementActions achievementActions) {
		try {
			PMessage messageaa = PMessage.newBuilder().setType("PAchievementActions")
					.setData(achievementActions.toByteString()).build();
			BaseHttpClient httpClient = new AchievementHttpClient("/v1/achievement/inside/action?from=task", null,
					null);
			PMessage message = httpClient.postBodyMethod(messageaa.toByteArray(), "application/x-protobuf");
			log.debug("用户[" + uuid + "]达成了成就-> msgid:" + msgid + "; value:" + value + " - message：" + message);
			if ("PResult".equals(message.getType())) {
				PResult result = PResult.parseFrom(message.getData());
				log.debug("用户[" + uuid + "]达成了成就-> msgid:" + msgid + "; value:" + value + " - code："
						+ result.getErrorCode() + "; msg：" + result.getErrorMsg());
			}
		} catch (Exception e) {

		}
	}
}