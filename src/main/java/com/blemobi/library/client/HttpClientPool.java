package com.blemobi.library.client;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.blemobi.library.consul.BaseService;
import com.google.common.base.Strings;

public class HttpClientPool {
	private static HttpClientConnectionManager instance = getInit();
	
	private static HttpClientConnectionManager getInit() {
		int maxTotal = getConfigValue("http_client_connection_count",200);
		int maxPerRoute = getConfigValue("http_client_max_per_route_count",20);
		PoolingHttpClientConnectionManager rtn = new PoolingHttpClientConnectionManager();
    	//连接池最大生成连接数
    	rtn.setMaxTotal(maxTotal);
    	// 默认设置route最大连接数
    	rtn.setDefaultMaxPerRoute(maxPerRoute);
    	return rtn;
	}
	
	private static int getConfigValue(String key,int defaultValue){
		String strValue = BaseService.getProperty(key);
		if(Strings.isNullOrEmpty(strValue)){
			return defaultValue;
		}else{
			try{
				int v = Integer.parseInt(strValue);
				return (v > 0)? v: defaultValue;
			}catch(Exception e){
				return defaultValue;
			}
		}
	}
	
	public static HttpClientConnectionManager getManager() {
		return instance;
	}
}
