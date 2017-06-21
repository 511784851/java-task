/******************************************************************
 *
 *    
 *    Package:     com.blemobi.payment.excepiton
 *
 *    Filename:    BizException.java
 *
 *    Description: TODO
 *
 *    @author:     HUNTER.POON
 *
 *    @version:    1.0.0
 *
 *    Create at:   2017年2月28日 下午3:33:34
 *
 *    Revision:
 *
 *    2017年2月28日 下午3:33:34
 *
 *****************************************************************/
package com.blemobi.task.exception;


/**
 * @ClassName BizException
 * @Description TODO
 * @author HUNTER.POON
 * @Date 2017年2月28日 下午3:33:34
 * @version 1.0.0
 */
public class BizException extends RuntimeException {
    private int errCd;
    private String msg;
    private String extMsg = "";
    public BizException(int errCd, String msg){
        super();
        this.errCd = errCd;
        this.msg = msg;
    }

    public BizException(int errCd, String msg, String extMsg) {
        super();
        this.errCd = errCd;
        this.msg = msg;
        this.extMsg = extMsg;
    }

    public int getErrCd() {
        return errCd;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public String getExtMsg() {
        return extMsg;
    }
    
}
