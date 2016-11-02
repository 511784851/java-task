package com.blemobi.task.notify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.blemobi.library.client.AccountHttpClient;
import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.NewsHttpClient;
import com.blemobi.sep.probuf.AccountApiProtos.PNotifyBaseInfo;
import com.blemobi.sep.probuf.AccountApiProtos.PNotifyBaseInfoList;
import com.blemobi.sep.probuf.AccountProtos.ELevelType;
import com.blemobi.sep.probuf.AccountProtos.PUserList;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.blemobi.sep.probuf.ResultProtos.PStringList;
import com.google.protobuf.ProtocolStringList;

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

	// 获取用户好友列表
	public static PUserList getFirendList(String uuid) throws ClientProtocolException, IOException {
		PUserList userList = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("from", "chat"));
		params.add(new BasicNameValuePair("uuid", uuid));
		params.add(new BasicNameValuePair("count", "10000"));

		BaseHttpClient httpClient = new NewsHttpClient("/social/listfriends", params, null);
		PMessage message = httpClient.getMethod();
		String type = message.getType();
		if ("PUserList".equals(type)) {
			userList = PUserList.parseFrom(message.getData());
		} else {
			PResult result = PResult.parseFrom(message.getData());
		}

		return userList;
	}

	// 获取用户粉丝列表
	public static List<String> getFansList(String uuid) throws ClientProtocolException, IOException {
		List<String> uuidList = new ArrayList<String>();

		int count = 100;
		int offset = 0;// 分页起始值
		do {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("uuid", uuid));
			params.add(new BasicNameValuePair("count", count + ""));
			params.add(new BasicNameValuePair("offset", offset + ""));

			BaseHttpClient httpClient = new NewsHttpClient("/v1/news/inside/fans", params, null);
			PMessage message = httpClient.getMethod();
			String type = message.getType();
			if ("PStringList".equals(type)) {
				PStringList stringList = PStringList.parseFrom(message.getData());
				ProtocolStringList list = stringList.getListList();
				uuidList.addAll(list);
				if (list != null && list.size() == count) {
					// 还有下一页数据，继续获取
					offset += count;
				}
			} else {
				PResult result = PResult.parseFrom(message.getData());
			}
		} while (offset > 0);

		return uuidList;
	}
}
