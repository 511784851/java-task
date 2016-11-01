package com.blemobi.library.exception;

/**
 * 
 * @author 李子才<davis.lee@blemobi.com> 这是聊天系统的Exception的基础类，系统中的其他异常都从这类基础上继承下来。
 */

@SuppressWarnings("serial")
public class BaseException extends Exception {

	private int errorCode = 0;
	private String errorMsg = null;
	private String extraInfo = null;

	/**
	 * 构造函数ChatException。
	 */
	public BaseException(int errorCode, String errorMsg) {
		this(errorCode, errorMsg, "");
	}

	/**
	 * 构造函数ChatException。
	 */
	public BaseException(int errorCode, String errorMsg, String extraInfo) {
		super(errorMsg);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.extraInfo = extraInfo;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
