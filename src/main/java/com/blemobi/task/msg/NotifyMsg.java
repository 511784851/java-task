package com.blemobi.task.msg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;

import com.blemobi.library.cache.UserBaseCache;
import com.blemobi.library.client.ChatHttpClient;
import com.blemobi.library.client.NewsHttpClient;
import com.blemobi.library.client.NotificationHttpClient;
import com.blemobi.library.client.SocialHttpClient;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.NotificationApiProtos.PGameMsgMeta;
import com.blemobi.sep.probuf.NotificationApiProtos.PPushMsg;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.task.basic.LevelHelper;

import lombok.extern.log4j.Log4j;

/*
 * 等级提升通知和推送消息
 */
@Log4j
public class NotifyMsg extends Thread {
	private static Queue<PGameMsgMeta> queue = new LinkedList<PGameMsgMeta>();

	private NotifyMsg() {

	}

	/*
	 * 添加消息到队列
	 */
	public static void add(String uuid, int level) {
		PGameMsgMeta gameMsgMeta = PGameMsgMeta.newBuilder().setTp(1).setUuid(uuid).setLevel(level).build();
		queue.add(gameMsgMeta);
	}

	static {
		new NotifyMsg().start();
	}

	public void run() {
		while (true) {
			try {
				PGameMsgMeta gameMsgMeta = queue.poll();
				if (gameMsgMeta == null) {
					Thread.sleep(1000);
					continue;
				}
				String uuid = gameMsgMeta.getUuid();
				int level = gameMsgMeta.getLevel();
				log.debug("用户[" + uuid + "]经验等级升级了 -> " + level);

				PPushMsg.Builder pushMsgBuilder = PPushMsg.newBuilder().setFromUuid(uuid).setType("achievement_task")
						.setTime(System.currentTimeMillis()).setData(gameMsgMeta.toByteString());

				notifyUsers(pushMsgBuilder, uuid);// 通知消息的接受者-自己、好友、粉丝

				send(pushMsgBuilder.build());// 通知消息
				push(uuid, level);// 推送给自己
			} catch (Exception e) {
				log.error("通知和推送异常");
				e.printStackTrace();
			}
		}
	}

	/*
	 * 通知消息接受者
	 */
	private void notifyUsers(PPushMsg.Builder pushMsgBuilder, String uuid) throws IOException {
		pushMsgBuilder.addToUuids(uuid);// 通知消息的接受者-自己

		List<PUserBase> firendList = getAllFriendList(uuid);
		List<String> fansList = getAllFansList(uuid);

		for (PUserBase user : firendList) {
			pushMsgBuilder.addToUuids(user.getUUID());// 消息的接受者-好友
			fansList.remove(user.getUUID());// 排除同时是好友也是粉丝重复的通知
		}

		for (String _uuid : fansList) {
			pushMsgBuilder.addToUuids(_uuid);// 消息的接受者-粉丝
		}
	}

	/*
	 * 获取全部好友
	 */
	private List<PUserBase> getAllFriendList(String uuid) throws IOException {
		SocialHttpClient socialHttpClient = new SocialHttpClient();
		return socialHttpClient.getAllFriendList(uuid);
	}

	/*
	 * 获取全部粉丝
	 */
	private List<String> getAllFansList(String uuid) throws IOException {
		NewsHttpClient newsHttpClient = new NewsHttpClient();
		return newsHttpClient.getAllFansList(uuid);
	}

	// 推送消息
	private void push(String uuid, int level) throws IOException {
		String language = UserBaseCache.get(uuid).getLanguage();
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

		ChatHttpClient httpClient = new ChatHttpClient();
		PMessage message = httpClient.multi(params);
		PResult result = PResult.parseFrom(message.getData());
		if (result.getErrorCode() != 0) {
			log.error("推送消息失败  -> " + result.getErrorCode());
		}
	}

	// 通知消息
	private void send(PPushMsg pushMsg) throws IOException {
		NotificationHttpClient httpClient = new NotificationHttpClient();
		PMessage message = httpClient.msg(pushMsg);
		PResult result = PResult.parseFrom(message.getData());
		if (result.getErrorCode() != 0) {
			log.error("发送通知消息失败  -> " + result.getErrorCode());
		}
	}

	/*
	 * 获取用户语音对应的推送文字内容
	 */
	private String getContent(String name, String language) throws IOException {
		if ("zh-tw".equals(language)) {// 中文繁体
			return "您的经验等级升级为" + name;
		} else if ("en-us".equals(language)) {// 英文
			return "Your EXP has been upgraded to " + name;
		} else if ("ko-kr".equals(language)) {// 韩文
			return "현재 경험등급이" + name;
		} else {// 中文简体（默认）
			return "您的经验等级升级为" + name;
		}
	}
}