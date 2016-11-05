package com.blemobi.task.notify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import com.blemobi.library.client.AccountHttpClient;
import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.NewsHttpClient;
import com.blemobi.library.client.SocialHttpClient;
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
import com.google.protobuf.ProtocolStringList;

import lombok.extern.log4j.Log4j;

@Log4j
public class UserRelation {
	public static List<Integer> levelList;

	static {
		levelList = new ArrayList<Integer>();
		levelList.add(ELevelType.User_VALUE);
		levelList.add(ELevelType.Vip_VALUE);
		levelList.add(ELevelType.Vipp_VALUE);
	}

	/*
	 * 获取用户昵称，语言
	 */
	public static PMessage getUserInfo(String uuid) throws IOException {
		String url = "/account/user/profile?from=task&uuid=" + uuid;
		BaseHttpClient httpClient = new AccountHttpClient(url, null, null);
		return httpClient.getMethod();
	}

	// 批量获取用户基础信息
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

	// 获取用户好友列表
	public static List<PUser> getFirendList(String uuid) throws ClientProtocolException, IOException {
		List<PUser> userList = new ArrayList<PUser>();

		String url = "/social/listfriends?from=task&start=0&count=10000&uuid=" + uuid;
		BaseHttpClient httpClient = new SocialHttpClient(url, null, null);
		PMessage message = httpClient.getMethod();
		String type = message.getType();
		if ("PUserList".equals(type)) {
			PUserList puserList = PUserList.parseFrom(message.getData());
			userList = puserList.getListList();
		} else {
			PResult result = PResult.parseFrom(message.getData());
			log.debug("获取用户好友列表失败:" + result.getErrorCode());
		}

		return userList;
	}

	// 获取用户关注列表
	public static List<PRecommendUser> getFollowList(String uuid) throws ClientProtocolException, IOException {
		List<PRecommendUser> userList = new ArrayList<PRecommendUser>();

		int count = 100;
		int offset = 0;// 分页起始值

		String url = "/v1/news/inside/follow?from=task&offset=" + offset + "&count=" + count + "&uuidb=" + uuid;
		BaseHttpClient httpClient = new NewsHttpClient(url, null, null);
		PMessage message = httpClient.getMethod();
		String type = message.getType();
		if ("PFollowOrFansList".equals(type)) {
			PFollowOrFansList stringList = PFollowOrFansList.parseFrom(message.getData());
			List<PRecommendUser> list = stringList.getListList();
			userList.addAll(list);
		} else {
			PResult result = PResult.parseFrom(message.getData());
			log.debug("获取用户关注列表失败:" + result.getErrorCode());
		}

		return userList;
	}

	// 获取用户粉丝列表
	public static ProtocolStringList getFansList(String uuid) throws ClientProtocolException, IOException {
		int count = 100;
		int offset = 0;// 分页起始值

		String url = "/v1/news/inside/fans?from=task&offset=" + offset + "&count=" + count + "&uuid=" + uuid;
		BaseHttpClient httpClient = new NewsHttpClient(url, null, null);
		PMessage message = httpClient.getMethod();
		String type = message.getType();
		if ("PStringList".equals(type)) {
			PStringList stringList = PStringList.parseFrom(message.getData());
			return stringList.getListList();
		} else {
			PResult result = PResult.parseFrom(message.getData());
			log.debug("获取用户粉丝列表失败:" + result.getErrorCode());
		}

		return null;
	}

	/*
	 * 获取用户昵称，语言
	 */
	public static PNotifyBaseInfo getBaseInfo(String uuid) throws IOException {
		PNotifyBaseInfo notifyBaseInfo = null;
		String url = "/account/user/profile?from=task";
		BaseHttpClient httpClient = new AccountHttpClient(url, null, null);
		PMessage message = httpClient.postBodyMethod(uuid.getBytes());
		if ("PUser".equals(message.getType())) {
			PNotifyBaseInfoList motifyBaseInfoList = PNotifyBaseInfoList.parseFrom(message.getData());
			Map<String, PNotifyBaseInfo> map = motifyBaseInfoList.getMap();
			if (map != null) {
				notifyBaseInfo = map.get(uuid);
			}
		} else {
			PResult result = PResult.parseFrom(message.getData());
		}
		return notifyBaseInfo;
	}
}