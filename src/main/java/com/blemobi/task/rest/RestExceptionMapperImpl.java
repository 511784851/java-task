/******************************************************************
 *
 *    
 *    Package:     com.blemobi.payment.rest
 *
 *    Filename:    RestExceptionMapperImpl.java
 *
 *    Description: TODO
 *
 *    @author:     HUNTER.POON
 *
 *    @version:    1.0.0
 *
 *    Create at:   2017年4月13日 下午2:15:32
 *
 *    Revision:
 *
 *    2017年4月13日 下午2:15:32
 *
 *****************************************************************/
package com.blemobi.task.rest;

import com.blemobi.task.exception.RestException;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;
import lombok.extern.log4j.Log4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @ClassName RestExceptionMapperImpl
 * @Description TODO
 * @author HUNTER.POON
 * @Date 2017年4月13日 下午2:15:32
 * @version 1.0.0
 */
@Log4j
@Provider
public class RestExceptionMapperImpl implements ExceptionMapper<RestException> {

    @Override
    public Response toResponse(RestException exception) {
        log.error("Payment server catch an exception, MSG=[" + exception + "]");
        return Response.ok(exception.getJsonString(), MediaTypeExt.APPLICATION_JSON).status(200).build();
    }

}
