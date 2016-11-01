package com.blemobi.library.consul;
/**
 * 
 * @author 李子才<davis.lee@blemobi.com>
 * 这是Consul服务器取信息的管理类。
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;
@Log4j
public class ConsulManager {
	
	/**
	 * 定义系统启动的模式，分别有local，test，prod模式。
	 * data[i][0]为模式名称，data[i][1]为描述.
	 * 系统启动时，分别从对应的Consul中获取对应的配置信息启动。
	 */
	private static String[][] data = {
			{"local",	"开发模式"},
			{"test", 	"测试模式"},
			{"prod",	"正式生产模式"},
			{"help",	"测试帮助信息"}
			};
	
	/**
	 * 在Consul管理类中注册登记的适配器对象，当配置信息有变更的时候，会通知到所有的适配器对象。
	 */
	private static ArrayList<ConsulChangeListener>  listener = new ArrayList<ConsulChangeListener>();
	
	/**
	 * 在Consul管理启动的标志，以保证系统初始化一次。
	 */
	private static boolean startFlag = false;  
	
	/**
	 * 在监听Consul服务器的后台监听线程。
	 */
	private static ConsulMonitorThread monitor = null;
	
	/**
	 * 启动Consul管理服务器，定期从consul服务器中抓取实时的配置信息。
	 * @param args 就是从系统main()函数中传递过来的启动参数。
	 * @param consulLoopTime 获取配置信息的间隔时间，以毫秒为单位。
	 * @throws IOException 
	 */
	public static void startService(String selfName,String[] args, long consulLoopTime){
		String selfServ = selfName.substring(0, 1).toUpperCase()+selfName.substring(1);
		if(!startFlag){
			startFlag = true;
			String env = checkStartMode(args,data);
			if(env!=null){
				if(env.equals("local")){
					log.info("System starting with local mode. ");
					log.info("Now, System reading local config file. ");
					try {
						LocalProp.setLocalEnv(selfServ+"Manager.ini");
						log.info("Read local Config File Finish!");
					} catch (IOException e) {
						log.info("Read local Config File Exception.");
						log.info("Config file ["+selfServ+"Manager.ini] not exist or content is error！");
						log.info("System exit!");
			        	log.info("Good bye!");
						System.exit(0);
					}
				}else{
					log.info("System starting with \""+env+"\" mode. Connet Local Consul Server.");
					System.setProperty("EnvMode", env);
					String addr = "127.0.0.1";
					int port = 8500;
//					String addr = "192.168.1.241";
//					int port = 8500;
					log.info("Consul Server Addr=["+addr+"] Port=["+port+"]");
					ConsulClient consulClient  = createConsul(addr, port);
					
					boolean check = checkConsulStat(consulClient,null);
					if(!check){
						log.info("Connect consul server throw an exception, System init fail!");
			        	log.info("System exit!");
			        	log.info("Good bye!");
						System.exit(0);
					}
					
		        	monitor = new ConsulMonitorThread(selfName,consulClient,consulLoopTime,listener,null);
					monitor.start();
					
				}
			}else{
				printHelp(data);
				System.exit(0);
			}
		}
	}

	/**
	 * 系统启动时监测Consul服务器状态，做一次连接检查。
	 * @param client 就是ConsulClient对象。
	 * @param token 连接consul服务器的认证信息。
	 */
	private static boolean checkConsulStat(ConsulClient client,String token) {
		try{
			String KEY_PRE_FIX_CHAT = "blemobi/sep/chat/"+System.getProperty("EnvMode", "")+"/"; 
			List<String> keys = (token==null)?client.getKVKeysOnly(KEY_PRE_FIX_CHAT).getValue():client.getKVKeysOnly(KEY_PRE_FIX_CHAT,null,token).getValue();
			return keys.size()>0;
		}catch(Exception e){
			return false;
		}
		
	}

	/**
	 * 获取系统启动的模式。
	 */
	private static String checkStartMode(String[] args, String[][] typeData) {
		String rtn = null;
		if(args.length >1 && args[0].equalsIgnoreCase("-ENV")){
			String mode = args[1].toLowerCase();
			for(String[] line:typeData){
				String key = line[0].toLowerCase();
				if(mode.equalsIgnoreCase(key)){
					rtn = mode;
					break;
				}
			}
			if(rtn!=null && rtn.equals("help")){
				rtn = null;
			}
		}
		return rtn;
	}


	/**
	 * 系统启动的打印的帮助信息提示内容。
	 */
	private static void printHelp(String[][] data) {
		StringBuffer sb = new StringBuffer();
		sb.append("usage: ChatManager -env ");
		sb.append("<");
		for(int i=0;i<data.length;i++){
			if(i>0)	sb.append("|");
			sb.append(data[i][0]);
		}
		sb.append(">\r\n");
		for(int i=0;i<data.length;i++){
			sb.append("\t-"+data[i][0]+"	"+data[i][1]+"\r\n");
		}
		log.info(sb.toString());
	}

	/**
	 * 创建连接consul服务器。
	 * @param host 是consul服务器的域名或IP地址。
	 * @param port 是consul服务器的端口。
	 * @return 返回ConsulClient对象。
	 */
	private static ConsulClient createConsul(String host, int port) {
		ConsulClient rtn = new ConsulClient(host, port);
		return rtn;
	}


	/**
	 * 提供给外部适配器注册登记。登记后适配器，都能收到Consul服务器配置信息有变更时的通知。
	 * @param adapter 需要登记的适配器。
	 */
	public static void addConsulChangeListener(ConsulChangeListener adapter){
		if(!Strings.isNullOrEmpty(System.getProperty("EnvMode"))){
			monitor.addConsulChangeListener(adapter);
			
		}else{
			LocalProp.invokeEnv(adapter);
		}
	}
	
	/**
	 * 从服务列表中移除适配器。移除后，该适配器不再收到Consul服务器配置信息变更。
	 * @param adapter 需要移除的适配器。
	 */
	public static void removeConsulChangeListener(ConsulChangeListener adapter){
		if(!Strings.isNullOrEmpty(System.getProperty("EnvMode"))){
			monitor.removeConsulChangeListener(adapter);
		}
	}
}
