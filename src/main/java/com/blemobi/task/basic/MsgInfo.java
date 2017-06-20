package com.blemobi.task.basic;

/**
 * 消息ID信息
 * 
 * @author zhaoyong
 *
 */
public class MsgInfo {
	// 消息ID
	private short msgID;
	// 对应服务器名称
	private String server;

	public short getMsgID() {
		return msgID;
	}

	public void setMsgID(short msgID) {
		this.msgID = msgID;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("msgID=").append(msgID).append(", server=").append(server).toString();
	}

}