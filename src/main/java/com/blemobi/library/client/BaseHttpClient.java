package com.blemobi.library.client;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.blemobi.library.consul.SocketInfo;
import com.blemobi.sep.probuf.ResultProtos;
import com.blemobi.sep.probuf.ResultProtos.PMessage;

import lombok.extern.log4j.Log4j;

/**
 * @author 赵勇<andy.zhao@blemobi.com> 远程调用类
 */
@Log4j
public abstract class BaseHttpClient {
	private String basePath;
	private List<NameValuePair> params;
	private Cookie[] cookies;
	protected StringBuffer url;
	protected SocketInfo socketInfo;

	/**
	 * 构造方法
	 * 
	 * @param basePath
	 *            服务路径
	 * @param params
	 *            参数信息
	 * @param cookies
	 *            cookies信息
	 */
	protected BaseHttpClient(String basePath, List<NameValuePair> params, Cookie[] cookies) {
		this.basePath = basePath;
		this.params = params;
		this.cookies = cookies;
	}

	/**
	 * post方式
	 * 
	 * @return PMessage PMessage对象
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public PMessage postMethod() throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url.toString());

		if (params != null) {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			httpPost.setEntity(entity);// 设置参数
		}

		return execute(httpPost);
	}

	/**
	 * get方式调用
	 * 
	 * @return PMessage PMessage对象
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public PMessage getMethod() throws ClientProtocolException, IOException {
		resetGetUrl();// 生成带参数的url
		log.debug("Exec getMethod() request url = [" + url + "]");
		HttpGet httpGet = new HttpGet(url.toString());

		return execute(httpGet);
	}

	/**
	 * post方式传递body内容调用
	 * 
	 * @param body
	 *            body内容
	 * @return PMessage PMessage对象
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
//	public PMessage postBodyMethod(byte[] body) throws IOException {
//		URL httpUrl = new URL(url.toString());
//		HttpURLConnection urlConnection = (HttpURLConnection) httpUrl.openConnection();
//
//		urlConnection.setDoInput(true);
//		urlConnection.setDoOutput(true);
//		urlConnection.setUseCaches(false);
//
//		urlConnection.setRequestProperty("Content-type", "form-data");
//		urlConnection.setRequestProperty("accept", "application/x-protobuf");
//		urlConnection.setRequestMethod("POST");
//
//		if (cookies != null) {
//			urlConnection.setRequestProperty("Cookie", getCookie());
//		}
//
//		OutputStream out = urlConnection.getOutputStream();
//		out.write(body);
//		out.flush();
//		out.close();
//
//		InputStream inputStream = urlConnection.getInputStream();
//		String encoding = urlConnection.getContentEncoding();
//		String bodyString = IOUtils.toString(inputStream, encoding);
//
//		ResultProtos.PMessage message = ResultProtos.PMessage.parseFrom(bodyString.getBytes());
//		return message;
//	}
	public PMessage postBodyMethod(byte[] body) throws IOException {
		return postBodyMethod(body,"form-data");
	}
	
	public PMessage postBodyMethod(byte[] body,String contentType) throws IOException {
		HttpPost httpPost = new HttpPost(url.toString());
		if ((body != null) && (body.length >0)) {
			ByteArrayEntity entity = new ByteArrayEntity(body, ContentType.create(contentType));
			httpPost.setEntity(entity);// 设置参数
		}
		return execute(httpPost);
	}
	
	
	

	/**
	 * 调用
	 * 
	 * @param httpRequestBase
	 *            http请求信息
	 * @return PMessage PMessage对象
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private PMessage execute(HttpRequestBase httpRequestBase) throws ClientProtocolException, IOException {
		if (cookies != null) {
			httpRequestBase.setHeader("Cookie", getCookie());
		}
		HttpClient client = HttpClients.custom().setConnectionManager(HttpClientPool.getManager()).build();
		HttpResponse response = client.execute(httpRequestBase);
		HttpEntity entity = response.getEntity();
		byte[] data = EntityUtils.toByteArray(entity);
		ResultProtos.PMessage message = ResultProtos.PMessage.parseFrom(data);

		return message;
	}

	// 生成GET带参数的完整url
	private void resetGetUrl() {
		if (params != null) {
			int i = 0;
			if (url.indexOf("?") < 0) {
				url.append("?");
			} else {
				i = 1;
			}
			for (NameValuePair nvp : params) {
				if (i > 0) {
					url.append("&");
				}
				url.append(nvp.getName());
				url.append("=");
				url.append(nvp.getValue());
				i++;
			}
		}
	}

	// 生成服务的URL
	protected void createUrl() {
		url = new StringBuffer("http://");
		url.append(socketInfo.getIpAddr());
		url.append(":");
		url.append(socketInfo.getPort());
		url.append(basePath);
	}

	// 设置cookie参数
	private String getCookie() {
		StringBuilder sb = new StringBuilder();
		for (Cookie ck : cookies) {
			sb.append(ck.getName()).append('=').append(ck.getValue()).append(";");
		}
		return sb.toString();
	}
}
