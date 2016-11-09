package com.blemobi.task.notify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.ChatHttpClient;
import com.blemobi.library.client.NotificationHttpClient;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.NotificationApiProtos.PGameMsgMeta;
import com.blemobi.sep.probuf.NotificationApiProtos.PPushMsg;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.task.basic.LevelHelper;

import lombok.extern.log4j.Log4j;

@Log4j
public class NotifyManager {
	private String uuid;
	private int level;

	public NotifyManager(String uuid, int level) {
		this.uuid = uuid;
		this.level = level;
	}

	// 保存通知
	public void notifyMsg() {
		try {
			log.debug("用户[" + uuid + "]经验等级升级了 -> " + level);
			PGameMsgMeta gameMsgMeta = PGameMsgMeta.newBuilder().setTp(1).setUuid(uuid).setLevel(level).build();

			PPushMsg.Builder pushMsgBuilder = PPushMsg.newBuilder().setFromUuid(uuid).setType("achievement_task")
					.setTime(System.currentTimeMillis()).setData(gameMsgMeta.toByteString());

			pushMsgBuilder.addToUuids(uuid);

			// 粉丝列表
			List<String> stringList = UserRelation.getFansList(uuid);
			log.debug("用户[" + uuid + "]经验等级升级了 -> " + level + " 粉丝数量：" + stringList.size());
			for (String uuid : stringList) {
				pushMsgBuilder.addToUuids(uuid);
			}

			// 好友列表
			List<PUser> firends = UserRelation.getFirendList(uuid);
			log.debug("用户[" + uuid + "]经验等级升级了 -> " + level + " 好友数量：" + firends.size());
			for (PUser user : firends) {
				String ouuid = user.getUuid();
				if (!stringList.contains(ouuid)) {
					pushMsgBuilder.addToUuids(ouuid);
				}
			}

			send(pushMsgBuilder.build());
			push();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 推送通知
	public void push() {
		try {
			String language = UserRelation.getLanguage(uuid);
			String levelName = LevelHelper.getLevelInfoByLevel(level).getTitle(language);
			String conent = getContent(levelName, language);

			Map<String, String> info = new HashMap<String, String>();
			info.put("MsgType", "tl");
			info.put("Title", "BB");
			info.put("Id", level + "");
			info.put("Name", levelName);

			String infoString = JSONObject.toJSONString(info);

			Map<String, String> param = new HashMap<String, String>();
			param.put("info", infoString);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("receiverUUIDs", uuid));
			params.add(new BasicNameValuePair("title", "BB"));
			params.add(new BasicNameValuePair("msgid", "10"));
			params.add(new BasicNameValuePair("description", "d"));
			params.add(new BasicNameValuePair("alertContent", conent));
			params.add(new BasicNameValuePair("info", infoString));

			BaseHttpClient httpClient = new ChatHttpClient("/chat/push/msg/multi", params, null);
			PMessage message = httpClient.postMethod();
			if ("PResult".equals(message.getType())) {
				PResult result = PResult.parseFrom(message.getData());
				log.debug("推送结果:" + result.getErrorCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 添加通知
	public void send(PPushMsg pushMsg) {
		try {
			BaseHttpClient httpClient = new NotificationHttpClient("/v1/notification/inside/msg?from=task", null, null);
			PMessage message = httpClient.postBodyMethod(pushMsg.toByteArray());
			log.debug("用户[" + uuid + "]经验等级升级了 -> " + level + " message：" + message);
			if ("PResult".equals(message.getType())) {
				PResult result = PResult.parseFrom(message.getData());
				log.debug("用户[" + uuid + "]经验等级升级了 -> " + level + " code：" + result.getErrorCode() + "; msg："
						+ result.getErrorMsg());
			}
		} catch (Exception e) {

		}
	}

	/*
	 * 获取用户语音对应的推送文字内容
	 */
	private String getContent(String name, String language) throws IOException {
		String content = "";
		if ("zh-tw".equals(language)) {// 中文繁体
			content = "您的经验等级升级为" + name;
		} else if ("en-us".equals(language)) {// 英文
			content = "Your EXP has been upgraded to " + name;
		} else if ("ko-kr".equals(language)) {// 韩文
			content = "현재 경험등급이" + name;
		} else {// 中文简体（默认）
			content = "您的经验等级升级为" + name;
		}
		return content;
	}
}