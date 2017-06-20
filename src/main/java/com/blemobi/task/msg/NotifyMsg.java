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

import com.blemobi.library.client.ChatHttpClient;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;

import lombok.extern.log4j.Log4j;

/**
 * 等级提升通知和推送消息
 * 
 * @author zhaoyong
 *
 */
@Log4j
public class NotifyMsg implements Runnable {

	private static Queue<String[]> queue = new LinkedList<String[]>();

	private NotifyMsg() {

	}

	/**
	 * 添加消息到队列
	 * 
	 * @param uuid
	 * @param content
	 */
	public static void add(String uuid, String content) {
		queue.add(new String[] { uuid, content });
	}

	static {
		new Thread(() -> new NotifyMsg()).start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				String[] array = queue.poll();
				if (array == null) {
					Thread.sleep(200);
					continue;
				}
				push(array[0], array[1]);
			} catch (Exception e) {
				log.error("通知和推送异常");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 推送消息
	 * 
	 * @param uuid
	 * @param content
	 * @throws IOException
	 */
	private void push(String uuid, String content) throws IOException {
		Map<String, String> info = new HashMap<String, String>();
		info.put("MsgType", "TGC");
		info.put("Title", "BB");
		info.put("Name", content);

		String infoString = JSONObject.toJSONString(info);

		Map<String, String> param = new HashMap<String, String>();
		param.put("info", infoString);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("receiverUUIDs", uuid));
		params.add(new BasicNameValuePair("title", "BB"));
		params.add(new BasicNameValuePair("msgid", "TGC"));
		params.add(new BasicNameValuePair("description", "BB"));
		params.add(new BasicNameValuePair("alertContent", content));
		params.add(new BasicNameValuePair("info", infoString));

		ChatHttpClient httpClient = new ChatHttpClient();
		PMessage message = httpClient.multi(params);
		PResult result = PResult.parseFrom(message.getData());
		if (result.getErrorCode() != 0) {
			log.error("推送消息失败  -> " + result.getErrorCode());
		}
	}

}