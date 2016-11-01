package com.blemobi.library.consul;

public class SocketInfo {
	private String ipAddr = null;
	private int port = 0;

	public SocketInfo(String ipAddr, int port) {
		this.ipAddr = ipAddr;
		this.port = port;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}