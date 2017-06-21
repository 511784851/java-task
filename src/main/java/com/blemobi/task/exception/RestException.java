/******************************************************************
 *
 *    
 *    Package:     com.blemobi.payment.excepiton
 *
 *    Filename:    RestException.java
 *
 *    Description: TODO
 *
 *    @author:     HUNTER.POON
 *
 *    @version:    1.0.0
 *
 *    Create at:   2017年4月13日 下午2:16:02
 *
 *    Revision:
 *
 *    2017年4月13日 下午2:16:02
 *
 *****************************************************************/
package com.blemobi.task.exception;

import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName RestException
 * @Description TODO
 * @author HUNTER.POON
 * @Date 2017年4月13日 下午2:16:02
 * @version 1.0.0
 */
public class RestException extends RuntimeException {
    private int ID;
    private String Str;
    public RestException(int errCd, String msg){
        super();
        this.ID = errCd;
        this.Str = msg;
    }
    public String getJsonString(){
        JSONObject json = new JSONObject();
        json.put("ID", ID);
        json.put("Str", Str);
        return json.toJSONString();
    }
}
