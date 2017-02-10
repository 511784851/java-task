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
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.sep.probuf.ResultProtos.PStringList;
import com.blemobi.task.basic.LevelHelper;

import lombok.extern.log4j.Log4j;

/**
 * 等级提升通知和推送消息
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class NotifyMsg extends Thread {
	private static Queue<PGameMsgMeta> queue = new LinkedList<PGameMsgMeta>();

	private NotifyMsg() {

	}

	/**
	 * 添加消息到队列
	 * 
	 * @param uuid
	 * @param level
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

				// 推送给自己
				push(uuid, level);
				// 通知消息
				sendMyMsg(uuid, level + "");
				sendOtherMsg(uuid, level + "");
			} catch (Exception e) {
				log.error("通知和推送异常");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 通知消息的接受者-好友、粉丝
	 * 
	 * @param uuid
	 * @param level
	 * @throws IOException
	 */
	private void sendOtherMsg(String uuid, String level) throws IOException {
		List<String> list = notifyUsers(uuid);
		PStringList stringList = PStringList.newBuilder().addAllList(list).build();
		send(stringList, uuid, "301", level);
	}

	/**
	 * 通知消息的接受者-自己
	 * 
	 * @param uuid
	 * @param level
	 * @throws IOException
	 */
	private void sendMyMsg(String uuid, String level) throws IOException {
		PStringList stringList = PStringList.newBuilder().addList(uuid).build();
		send(stringList, "", "101", level);
	}

	/**
	 * 通知消息接受者
	 * 
	 * @param pushMsgBuilder
	 * @param uuid
	 * @throws IOException
	 */
	private List<String> notifyUsers(String uuid) throws IOException {
		List<String> fansList = getAllFansList(uuid);
		List<PUserBase> firendList = getAllFriendList(uuid);

		for (PUserBase user : firendList) {
			String firendUUID = user.getUUID();
			if (!fansList.contains(firendUUID)) {
				fansList.add(firendUUID);
			}
		}
		return fansList;
	}

	/**
	 * 获取全部好友
	 * 
	 * @param uuid
	 * @return
	 * @throws IOException
	 */
	private List<PUserBase> getAllFriendList(String uuid) throws IOException {
		SocialHttpClient socialHttpClient = new SocialHttpClient();
		return socialHttpClient.getAllFriendList(uuid);
	}

	/**
	 * 获取全部粉丝
	 * 
	 * @param uuid
	 * @return
	 * @throws IOException
	 */
	private List<String> getAllFansList(String uuid) throws IOException {
		NewsHttpClient newsHttpClient = new NewsHttpClient();
		return newsHttpClient.getAllFansList(uuid);
	}

	/**
	 * 推送消息
	 * 
	 * @param uuid
	 * @param level
	 * @throws IOException
	 */
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

	/**
	 * 通知消息
	 * 
	 * @param pushMsg
	 * @throws IOException
	 */
	private void send(PStringList stringList, String uuid, String type, String task) throws IOException {
		NotificationHttpClient httpClient = new NotificationHttpClient();
		PMessage message = httpClient.msg(stringList, uuid, type, task);
		PResult result = PResult.parseFrom(message.getData());
		if (result.getErrorCode() != 0) {
			log.error("发送通知消息失败  -> " + result.getErrorCode());
		}
	}

	/**
	 * 获取用户语音对应的推送文字内容
	 * 
	 * @param name
	 * @param language
	 * @return
	 * @throws IOException
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