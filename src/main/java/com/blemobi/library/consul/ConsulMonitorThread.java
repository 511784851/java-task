package com.blemobi.library.consul;
/**
 * 
 * @author 李子才<davis.lee@blemobi.com>
 * 这是Consul服务器的取信息的线程。
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
@Log4j
public class ConsulMonitorThread extends Thread {
	
	//连接consul服务器的对象
	private ConsulClient consulClient = null;
	
	//适配器队列。
	private ArrayList<ConsulChangeListener> allListener = null;

	//依赖的服务的信息记录表
	HashMap<String, SocketInfo[]> allService = new HashMap<String, SocketInfo[]>();
	
	//Consul中的Key-value的值的map
	Map<String,String> propInfo = new HashMap();

	//访问consul服务器的Token
	private String token = null;

	//访问consul服务器的间隔时间，默认值.会被Constant.ConsulIntervalTime的值替代！
	//private long loopTime = 1000 * 60 * 30 * 1L;
	private long loopTime = 1000 * 5L;

	private String selfName = null;

	/**
	 * 构造函数ConsulMonitorThread。
	 * @param consulClient 是consul连接对象
	 * @param loopTime 连接的间隔时间
	 * @param allListener 适配器对象。
	 * @param token 访问consul服务器的token认证信息。
	 */
	public ConsulMonitorThread(String selfName, ConsulClient consulClient, long loopTime, ArrayList<ConsulChangeListener> allListener, String token) {
		this.selfName  = selfName;
		this.consulClient  = consulClient;
		this.allListener  = allListener;
		this.loopTime  = loopTime;
		this.token   = token;
		try {doNotifyAllListener();} catch (Exception e) {}//触发更新一次
	}

	/**
	 * 创建“依赖服务(account和social)”的存储空间。
	 * @param serviceNameList 服务名称的列表。
	 * @return 返回Map,<String, String[][]> 的记录模式，其中String[][]的String[i][0]为IP地址，String[i][1]为端口 。
	 */
	private HashMap<String, SocketInfo[]> makeAllService(String[] serviceNameList) {
		java.util.HashMap<String, SocketInfo[]> rtn = new java.util.HashMap<String, SocketInfo[]>();
		for(String name:serviceNameList){ 
			rtn.put(name, new SocketInfo[0]);
		}
		return rtn;
	}

	/**
	 * 线程类的run()函数。
	 */
	public void run() {
		while(true){
			// 循环调用显示consul信息 
			try {sleep(loopTime);} catch (InterruptedException e) {}
			try {doNotifyAllListener();} catch (Exception e) {}
		}

	}


	/**
	 * 从consul服务器获取信息变更，并且通知到所有的适配器对象。
	 */
	private void doNotifyAllListener() {
		
		Map<String, String> prop = getEvnInfo(consulClient,selfName ); 
		String[] allServiceName = getAllServiceName(consulClient);
		log.debug("list consul Env(key-value)");
		for(Entry<String, String> kv: prop.entrySet()){
			propInfo.put(kv.getKey(), kv.getValue());
		}
		
		for(String serviceName:allServiceName){
			if(!Strings.isNullOrEmpty(serviceName)){
				SocketInfo[] info = getServiceInfo(consulClient,serviceName); 
				allService.put(serviceName, info);//有变化时则更新
			}
		}
		
		for(ConsulChangeListener listener:allListener){
			for(Entry<String, SocketInfo[]> kv:allService.entrySet()){
				listener.onServiceChange(kv.getKey(), kv.getValue());
			}
			
			listener.onEnvChange(propInfo);
		}
				
	}
	
	private String[] getAllServiceName(ConsulClient consul) {
		List<String> names = new ArrayList<String>();
		Response<Map<String, List<String>>> servs = consul.getCatalogServices(QueryParams.DEFAULT);
		for (String serviceId : servs.getValue().keySet()) {
			names.add(serviceId);
		}
		
		String[] rtn = new String[names.size()];
		names.toArray(rtn);
		return rtn;
	}

	
	/**
	 * 从consul服务器获取到的信息，通知到某个适配器对象，一般是用到某个适配器首次加入管理是调用。
	 * @param listener 某个适配器对象。
	 */
	private void doNotifyOneListener(ConsulChangeListener listener) {
		for(Entry<String, SocketInfo[]> sdf:allService.entrySet()){
			listener.onServiceChange(sdf.getKey(), sdf.getValue());
			
		}
		listener.onEnvChange(propInfo);
	}
	

	/**
	 * 比对两个数组内的数据内容相同，并且不考虑排序问题。
	 * @param frist 第一个数组对象。
	 * @param second 第二个数组对象。
	 * @return 返回布尔值，两个数组中的内容相同时为true。
	 */
	private boolean checkServiceInfoSame(SocketInfo[] frist, SocketInfo[] second) {
		boolean rtn = true;
		if(frist.length == second.length){
			for(int i=0;i<frist.length;i++){
				boolean inFlag = checkElementInArray(frist[i],second);
				if(!inFlag){//元素不在集合里面，则返回false
					rtn = false;
					break;
				}
			}
		}else{
			rtn = false;
		}
		return rtn;
	}

	/**
	 * 检查元素是否在数组里面。
	 * @param element 集合中某个元素。
	 * @param array 大集合。
	 * @return 返回布尔值，如果元素存在于集合内，则为true。
	 */
	private boolean checkElementInArray(SocketInfo element, SocketInfo[] array) {
		for(int i=0;i<array.length;i++){
			if(element.getIpAddr().equals(array[i].getIpAddr()) && (element.getPort()==array[i].getPort()) ){
				return true;
			}
		}
		return false;
	}

	/**
	 * 比对两个属性类的数据内容相同
	 * @param frist 第一个属性类对象。
	 * @param second 第二个属性类对象。
	 * @return 返回布尔值，如果两个map的内容相同，则为true。
	 */
	private boolean checkPopSame(Map<String, String> frist, Map<String, String> second) {
		boolean rtn = true;
		if(frist.size()==second.size()){
			String[] keys = new String[frist.size()];
			frist.keySet().toArray(keys); // 第1个map的key值放入keys中去. 
			for(int i=0;i<keys.length;i++){
				String key = keys[i];
				String value = frist.get(key); // 根据第i个key,找到对应的value
				boolean inFlag = checkElementInMap(key,value,second);  // 检查第一个key-value对是否存在第2个map中.
				if(!inFlag){//元素不在集合里面，则返回false
					rtn = false;
					break;
				}
			}
		}else{
			rtn = false;
		}
		return rtn;
	}

	/**
	 * 检查<key,value>是否在某个map里面
	 * @param key 是key键。
	 * @param value 是key键对应的值。
	 * @param porp 是Map属性类。
	 * @return 返回布尔值，如果该<key-value>,存在于集合内，则为true。
	 */
	private boolean checkElementInMap(String key,String value, Map<String, String> porp) {
		String mv = porp.get(key);
		return mv.equals(value);
	}

	/**
	 * 获取consul服务器中的key-value的信息内容。
	 * @param client 访问consul服务的对象。
	 * @return 返回<key-value>的map对象。
	 */
	private Map<String, String> getEvnInfo(ConsulClient client,String selfName) {
		String KEY_PRE_FIX_CHAT = "blemobi/sep/"+selfName+"/"+System.getProperty("EnvMode", "")+"/"; 
		
		//log.info(KEY_PRE_FIX_CHAT);
		
		Map rtn = new java.util.HashMap<String, String>();
		List<String> keys = (token==null)?consulClient.getKVKeysOnly(KEY_PRE_FIX_CHAT).getValue():consulClient.getKVKeysOnly(KEY_PRE_FIX_CHAT,null,token).getValue();

		int size = keys.size();
		int preLen = KEY_PRE_FIX_CHAT.length();
		for(int i=0;i<size;i++){ // foreach key 
			String key = keys.get(i);
			if(!key.endsWith("/")){
				try{
					String value = getConsulKVValue(client,key); // 获取value
					rtn.put(key.substring(preLen), value); // 保存key-value ,return 
				}catch(Exception e){

				}
			}
		}
		return rtn;
	}
	
	/**
	 * 获取consul服务器中的key-value的信息内容。
	 * @param client 访问consul服务的对象。
	 * @param key 指定的对应的key的名称。
	 * @return 返回对应的value的值。
	 */
	private String getConsulKVValue(ConsulClient client, String key) {
		byte[] data = null;
		if (token == null || token.equals("")) {
			data = client.getKVBinaryValue(key).getValue().getValue();
		} else {
			data = client.getKVBinaryValue(key, this.token).getValue().getValue();
		}

		if(data==null){
			return "";
		}else{
			return new String(data);
		}
	}


	/**
	 * 获取consul服务器中的的各服务的服务器列表内容。
	 * @param client 访问consul服务的对象。
	 * @param name 服务的名称。
	 * @return 返回对应的服务的服务器列表。String[i][0]为IP地址，String[i][1]为端口。
	 */
	private SocketInfo[] getServiceInfo(ConsulClient client,String name) {
		SocketInfo[] rtn = new SocketInfo[0];  

		String param = (token==null)?"":"?token="+token;

		Response<List<CatalogService>> response = client.getCatalogService(name+param, null);

		List<CatalogService> nodes = response.getValue();


		if (nodes.size() != 0) {
			CatalogService[] service = new CatalogService[nodes.size()];
			nodes.toArray(service);
			rtn = new SocketInfo[service.length];
			for(int i=0;i<service.length;i++){
				rtn[i] = new SocketInfo(service[i].getServiceAddress(),service[i].getServicePort());
			}
		} else {
			log.error("Please Notice！  Consul Service["+name+ "] Count = 0");
		}
		return rtn;
	}

	/**
	 * 提供给外部适配器注册登记。登记后适配器，都能收到Consul服务器配置信息有变更时的通知。
	 * @param adapter 需要登记的适配器。
	 */
	public void addConsulChangeListener(ConsulChangeListener adapter) {
		doNotifyOneListener(adapter);
		allListener.add(adapter);
	}
	
	/**
	 * 从服务列表中移除适配器。移除后，该适配器不再收到Consul服务器配置信息变更。
	 * @param adapter 需要移除的适配器。
	 */
	public void removeConsulChangeListener(ConsulChangeListener adapter) {
		allListener.remove(adapter);
	}

}
