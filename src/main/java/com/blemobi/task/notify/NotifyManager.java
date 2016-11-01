package com.blemobi.task.notify;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.ChatHttpClient;
import com.blemobi.sep.probuf.AccountApiProtos.PNotifyBaseInfo;
import com.blemobi.sep.probuf.AccountProtos.PUser;
import com.blemobi.sep.probuf.AccountProtos.PUserList;

public class NotifyManager {
	private String uuid;
	private TypeEnum type;
	private String param;

	public NotifyManager(String uuid, TypeEnum type, String param) {
		this.uuid = uuid;
		this.type = type;
		this.param = param;
	}

	// 保存通知
	public void save() {
		try {
			PNotifyBaseInfo baseInfo = UserRelation.getBaseInfo(uuid);
			String nickname = baseInfo.getNickname();
			String language = baseInfo.getLanguage();

			// 推送给自己
			String content = LanguageHelper.getContent(type, language, param);
			this.send(uuid, content);

			// 推送给好友和粉丝
			if (TypeEnum.n07.toString().equals(type.toString())) {// 等级提升
				String content1 = LanguageHelper.getContent(TypeEnum.n01, language, nickname, param);
				PUserList firendList = UserRelation.getFirendList(uuid);
				List<PUser> userList = firendList.getListList();
				for (PUser user : userList) {
					this.send(user.getUuid(), content1);
				}

				String content2 = LanguageHelper.getContent(TypeEnum.n03, language, nickname, param);
				List<String> fansList = UserRelation.getFansList(uuid);
				for (String fans : fansList) {
					this.send(fans, content2);
				}
			}
		} catch (Exception e) {

		}
	}

	// 推送通知
	public void send(String uuid, String alertContent) {
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("receiverUUIDs", uuid));
			params.add(new BasicNameValuePair("title", uuid));
			params.add(new BasicNameValuePair("msgid", "9"));
			params.add(new BasicNameValuePair("description", "aaaa"));
			params.add(new BasicNameValuePair("alertContent", alertContent));
			params.add(new BasicNameValuePair("info", "cccc"));

			BaseHttpClient httpClient = new ChatHttpClient("/chat/push/msg/multi", params, null);
			httpClient.postMethod();
		} catch (Exception e) {

		}
	}
}