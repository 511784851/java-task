package com.blemobi.task.rest;

import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.task.exception.BizException;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;
import lombok.extern.log4j.Log4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * 统一异常处理器
 *
 * @author zhaoyong
 */
@Log4j
@Provider
public class BizExceptionMapperImpl implements ExceptionMapper<BizException> {
    /**
     * 异常处理
     */
    public Response toResponse(BizException e) {
        log.error("Payment server catch an exception, MSG=[" + e.getMessage() + "]");
        PMessage msg = ReslutUtil.createErrorMessage(e.getErrCd(), e.getMsg(), e.getExtMsg());
        return Response.ok(msg, MediaTypeExt.APPLICATION_PROTOBUF).status(200).build();
    }
}