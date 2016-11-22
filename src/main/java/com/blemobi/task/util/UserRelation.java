package com.blemobi.task.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.blemobi.library.client.AccountHttpClient;
import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.NewsHttpClient;
import com.blemobi.library.client.SocialHttpClient;
import com.blemobi.library.redis.RedisManager;
import com.blemobi.sep.probuf.AccountApiProtos.PNotifyBaseInfo;
import com.blemobi.sep.probuf.AccountApiProtos.PNotifyBaseInfoList;
import com.blemobi.sep.probuf.AccountProtos.ELevelType;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.AccountProtos.PUserBase;
import com.blemobi.sep.probuf.AccountProtos.PUserBaseList;
import com.blemobi.sep.probuf.AccountProtos.PUserList;
import com.blemobi.sep.probuf.NewsProtos.PFollowOrFansList;
import com.blemobi.sep.probuf.NewsProtos.PRecommendUser;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.sep.probuf.ResultProtos.PStringList;
import com.google.common.base.Strings;
import com.google.protobuf.ProtocolStringList;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;

@Log4j
public class UserRelation {
	public static List<Integer> levelList;

	/*
	 * 用户角色的允许范围
	 */
	static {
		levelList = new ArrayList<Integer>();
		levelList.add(ELevelType.User_VALUE);
		levelList.add(ELevelType.Vip_VALUE);
		levelList.add(ELevelType.Vipp_VALUE);
	}

	/*
	 * 获取用户基础信息
	 */
	public static PMessage getUserInfo(String uuid) throws IOException {
		String url = "/account/user/profile?from=task&uuid=" + uuid;
		BaseHttpClient httpClient = new AccountHttpClient(url, null, null);
		return httpClient.getMethod();
	}

	/*
	 * 批量获取用户基础信息
	 */
	public static List<PUserBase> getUserListInfo(String uuids) throws ClientProtocolException, IOException {
		List<PUserBase> userLIst = new ArrayList<PUserBase>();

		String url = "/v1/account/users/baseinfo?from=task&uuids=" + uuids.toString();
		BaseHttpClient httpClient = new AccountHttpClient(url, null, null);
		PMessage message = httpClient.getMethod();

		String type = message.getType();
		if ("PUserBaseList".equals(type)) {
			PUserBaseList userList = PUserBaseList.parseFrom(message.getData());
			userLIst = userList.getListList();
		} else {
			PResult result = PResult.parseFrom(message.getData());
			log.debug("批量获取用户信息失败:" + result.getErrorCode());
		}
		return userLIst;
	}

	/*
	 * 获取用户好友列表
	 */
	public static List<PUser> getFirendList(String uuid) throws ClientProtocolException, IOException {
		List<PUser> userList = new ArrayList<PUser>();

		String url = "/social/listfriends?from=task&start=0&count=100000&uuid=" + uuid;
		BaseHttpClient httpClient = new SocialHttpClient(url, null, null);
		PMessage message = httpClient.getMethod();
		String type = message.getType();
		if ("PUserList".equals(type)) {
			PUserList puserList = PUserList.parseFrom(message.getData());
			List<PUser> list = puserList.getListList();
			userList.addAll(list);
		} else {
			PResult result = PResult.parseFrom(message.getData());
			log.debug("获取用户好友列表失败:" + result.getErrorCode());
		}
		return userList;
	}

	/*
	 * 获取用户关注列表
	 */
	public static List<PRecommendUser> getFollowList(String uuid) throws ClientProtocolException, IOException {
		List<PRecommendUser> userList = new ArrayList<PRecommendUser>();

		int count = 100;
		int offset = 0;// 分页起始值
		boolean bool = true;
		do {
			String url = "/v1/news/inside/follow?from=task&offset=" + offset + "&count=" + count + "&uuidb=" + uuid;
			BaseHttpClient httpClient = new NewsHttpClient(url, null, null);
			PMessage message = httpClient.getMethod();
			String type = message.getType();
			if ("PFollowOrFansList".equals(type)) {
				PFollowOrFansList stringList = PFollowOrFansList.parseFrom(message.getData());
				List<PRecommendUser> list = stringList.getListList();
				userList.addAll(list);
				offset = stringList.getIndex();
				if (list == null || list.size() == 0) {
					bool = false;
				}
			} else {
				PResult result = PResult.parseFrom(message.getData());
				log.debug("获取用户关注列表失败:" + result.getErrorCode());
			}
		} while (bool);
		return userList;
	}

	/*
	 * 获取用户粉丝列表
	 */
	public static List<String> getFansList(String uuid) throws ClientProtocolException, IOException {
		List<String> userList = new ArrayList<String>();
		int count = 100;
		int offset = 0;// 分页起始值
		boolean bool = true;
		do {
			String url = "/v1/news/inside/fans?from=task&offset=" + offset + "&count=" + count + "&uuid=" + uuid;
			BaseHttpClient httpClient = new NewsHttpClient(url, null, null);
			PMessage message = httpClient.getMethod();
			String type = message.getType();
			if ("PStringList".equals(type)) {
				PStringList stringList = PStringList.parseFrom(message.getData());
				ProtocolStringList list = stringList.getListList();
				userList.addAll(list);
				if (list == null || list.size() == 0) {
					bool = false;
				}
				offset += count;
			} else {
				PResult result = PResult.parseFrom(message.getData());
				log.debug("获取用户粉丝列表失败:" + result.getErrorCode());
			}
		} while (bool);
		return userList;
	}

	/*
	 * 获取用户语言
	 */
	public static String getLanguage(String uuid) throws IOException {
		String language = "";
		String url = "/v1/account/user/notifybases?from=task";
		BaseHttpClient httpClient = new AccountHttpClient(url, null, null);
		PMessage message = httpClient.postBodyMethod(uuid.getBytes());
		if ("PNotifyBaseInfoList".equals(message.getType())) {
			PNotifyBaseInfoList motifyBaseInfoList = PNotifyBaseInfoList.parseFrom(message.getData());
			Map<String, PNotifyBaseInfo> map = motifyBaseInfoList.getMap();
			PNotifyBaseInfo notifyBaseInfo = map.get(uuid);
			language = notifyBaseInfo.getLanguage();
			log.debug("用户语言：" + language);
		} else if ("PResult".equals(message.getType())) {
			PResult result = PResult.parseFrom(message.getData());
			log.error("获取用户语言出错：" + result.getErrorCode() + " - " + result.getErrorMsg());
		}
		return language;
	}

	/*
	 * 服务启动时可校验VO用户
	 */
	public static void delVO() throws ClientProtocolException, IOException {
		Jedis jedis = RedisManager.getRedis();
		Set<String> set = jedis.keys(Constant.GAME_USER_INFO + "*");
		String uuids = "";
		for (String key : set) {
			String uuid = key.substring(Constant.GAME_USER_INFO.length());
			if (uuids.length() > 0) {
				uuids += ",";
			}
			uuids += uuid;
		}
		int count = 0;
		List<PUserBase> userBaseList = UserRelation.getUserListInfo(uuids);
		for (PUserBase userBase : userBaseList) {
			if (!UserRelation.levelList.contains(userBase.getLevel())) {
				String uuid = userBase.getUUID();
				jedis.del(Constant.GAME_USER_INFO + uuid);
				jedis.del(Constant.GAME_TASK_MAIN + uuid);
				jedis.del(Constant.GAME_MSGID + uuid);
				count++;
			}
		}
		log.debug("清除VO用户数量：" + count);
	}

	/*
	 * 服务启动时，可校验用户基础信息是否完善
	 */
	public static void loadInfo() throws ClientProtocolException, IOException {
		Jedis jedis = RedisManager.getRedis();
		Set<String> sets = jedis.keys(Constant.GAME_USER_INFO + "*");
		String uuids = "";
		for (String key : sets) {
			String uuid = key.substring(Constant.GAME_USER_INFO.length());
			Map<String, String> map = jedis.hgetAll(key);
			if (Strings.isNullOrEmpty(map.get("nickname"))) {
				if (uuids.length() > 0) {
					uuids += ",";
				}
				uuids += uuid;
			}
		}
		int count = 0;
		List<PUserBase> userBaseList = UserRelation.getUserListInfo(uuids);
		for (PUserBase userBase : userBaseList) {
			String uuid = userBase.getUUID();
			String userInfoKey = Constant.GAME_USER_INFO + uuid;
			jedis.hset(userInfoKey, "nickname", userBase.getNickname());
			jedis.hset(userInfoKey, "headimg", userBase.getHeadImgURL());
			jedis.hset(userInfoKey, "language", userBase.getLanguage());
			jedis.hset(userInfoKey, "levelType", userBase.getLevel() + "");
			count++;
		}
		log.debug("重新获取用户基础信息：" + count);
	}
}