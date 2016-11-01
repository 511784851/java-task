package com.blemobi.library.consul;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import lombok.extern.log4j.Log4j;
@Log4j
public class LocalProp {
	private static HashMap<String,String> propInfo = new HashMap<String,String>();
	private static HashMap<String,SocketInfo[]> services = new HashMap<String,SocketInfo[]>();
	
	public static void invokeEnv(ConsulChangeListener adapter) {
		for(Entry<String, SocketInfo[]> serv:services.entrySet()){
			adapter.onServiceChange(serv.getKey(), serv.getValue());
		}
		adapter.onEnvChange(propInfo);
	}

	public static void setLocalEnv(String filePath) throws IOException {
		
		String path = System.getProperty("user.dir")+File.separator+filePath;
		InputStream in =new FileInputStream(path);

		Properties fileProp = new Properties();
		fileProp.load(in);
		
		log.info("--- Start list properties ---");

		String prefix = "service_";
		String postfix_addr = "_addr";
		String postfix_port = "_port";

		for(Entry<Object, Object> p:fileProp.entrySet()){
			String key = (String)(p.getKey());
			String value = (String)p.getValue();
			if(key.startsWith(prefix) && (key.endsWith(postfix_addr)) && (key.length() > (prefix.length()+postfix_addr.length()))){
				//如果是服务IP地址的信息
				String serviceName = key.substring(0,(key.length()-postfix_addr.length())).substring(prefix.length());
				SocketInfo[] si = services.get(serviceName);
				if(si==null){
					si = new SocketInfo[]{ new SocketInfo("nullAddr",-1)};
					services.put(serviceName, si);
				}
				si[0].setIpAddr(value);
			}else if(key.startsWith(prefix) && (key.endsWith(postfix_port)) && (key.length() > (prefix.length()+postfix_port.length()))){
				//如果是服务端口的信息
				String serviceName = key.substring(0,(key.length()-postfix_port.length())).substring(prefix.length());
				SocketInfo[] si = services.get(serviceName);
				if(si==null){
					si = new SocketInfo[]{ new SocketInfo("nullAddr",-1)};
					services.put(serviceName, si);
				}
				si[0].setPort(Integer.parseInt(value));
			}else{
				propInfo.put(key, value);
			}
		}
	}

}
