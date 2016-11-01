package com.blemobi.library.util;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.google.protobuf.GeneratedMessage;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

import lombok.extern.log4j.Log4j;

/**
 * @author 赵勇<andy.zhao@blemobi.com> 返回PMessage数据封装类
 */

@Log4j
public class ReslutUtil {

	private static final int magic = 9833;// 固定值
	private static final String errorType = "PResult";// 返回PResult数据

	/**
	 * 返回PMessage对象数据
	 * 
	 * @param message
	 *            PMessage对象
	 * @return PMessage 返回message对象数据
	 */
	public static PMessage createReslutMessage(GeneratedMessage message) {
		char charSubClassKey = '$';
		char charPackage = '.';

		String packageAndClass = message.getClass().getName();

		int subIndex = packageAndClass.lastIndexOf(charSubClassKey);
		int packageIndex = packageAndClass.lastIndexOf(charPackage);

		int lassPoint = (subIndex > 0) ? subIndex : packageIndex;
		String className = packageAndClass.substring(lassPoint + 1);

		return createReslutMessage(className, message);
	}

	private static PMessage createReslutMessage(String type, GeneratedMessage message) {
		log.debug("type：" + type + "；data：" + message);
		return PMessage.newBuilder().setMagic(magic).setType(type).setData(message.toByteString()).build();
	}

	/**
	 * 返回PResult数据
	 * 
	 * @param errorCode
	 *            错误码
	 * @param errorMsg
	 *            错误简要描述
	 * @param extraInfo
	 *            扩展信息
	 * @return PMessage 返回message对象数据
	 */
	public static PMessage createSucceedMessage() {
		return createSucceedMessage("success");
	}

	public static PMessage createSucceedMessage(String errorMsg) {
		return createSucceedMessage(errorMsg, "");
	}

	public static PMessage createSucceedMessage(String errorMsg, String extraInfo) {
		return createErrorMessage(0, errorMsg, extraInfo);
	}

	public static PMessage createErrorMessage(int errorCode, String errorMsg) {
		return createErrorMessage(errorCode, errorMsg, "");
	}

	public static PMessage createErrorMessage(int errorCode, String errorMsg, String extraInfo) {
		PResult result = createPResultMessage(errorCode, errorMsg, extraInfo);
		return ReslutUtil.createReslutMessage(errorType, result);
	}

	public static PResult createPResultMessage() {
		return createPResultMessage(0, "success", "");
	}

	public static PResult createPResultMessage(int errorCode, String errorMsg) {
		return createPResultMessage(errorCode, errorMsg, "");
	}

	public static PResult createPResultMessage(int errorCode, String errorMsg, String extraInfo) {
		PResult result = PResult.newBuilder().setErrorCode(errorCode).setErrorMsg(errorMsg).setExtraInfo(extraInfo)
				.build();
		log.debug("json：" + result);
		return result;
	}

	/**
	 * ServletResponse返回包处理
	 * 
	 * @param response
	 *            response对象
	 * @param errorCode
	 *            返回码
	 * @param errorMsg
	 *            返回码描述
	 * @return void 返回无
	 * @throws IOException
	 */
	public static void createResponse(ServletResponse response, int errorCode, String errorMsg) throws IOException {
		PMessage message = createErrorMessage(errorCode, errorMsg);
		byte[] data = message.toByteArray();

		response.setContentType(MediaTypeExt.APPLICATION_PROTOBUF);
		response.setContentLength(data.length);
		ServletOutputStream out = response.getOutputStream();
		out.write(data);
		out.flush();
		out.close();
	}
}
