package com.blemobi.library.consul;

import java.util.ArrayList;
import java.util.List;

public class ServiceInfo {
	private SocketInfo[] allSocketInfo = new SocketInfo[0];
	private List<String> errorList = new ArrayList<String>();

	public SocketInfo[] getAllSocketInfo() {
		return this.allSocketInfo;
	}

	public  void setAllSocketInfo(SocketInfo[] allSocketInfo) {
		this.allSocketInfo = allSocketInfo;
	}

	public List<String> getErrorList() {
		return this.errorList;
	}

	public void reportErrorServer(String addr, int port) {
		this.errorList.add(addr + "-" + port);
	}

	public  void clearErrorServer() {
		this.errorList.clear();
	}

}