package com.blemobi.task.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;

import lombok.extern.log4j.Log4j;

/**
 * 统一异常处理器
 * 
 * @author zhaoyong
 *
 */
@Log4j
@Provider
public class ExceptionImpl implements ExceptionMapper<Exception> {
	/**
	 * 异常处理
	 */
	public Response toResponse(Exception e) {
		log.error("Task server catch an exception, MSG=[" + e.getMessage() + "]");
		e.printStackTrace();
		PMessage msg = ReslutUtil.createErrorMessage(1001012, "系统繁忙");
		return Response.ok(msg, MediaTypeExt.APPLICATION_PROTOBUF).status(200).build();
	}
}