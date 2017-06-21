package com.blemobi.task.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/6 14:06
 */
@Getter
@Setter
@ToString
public class Order {
    private String ordNo;
    private String goodsNo;
    private String name;
    private Integer ordStatus;
    private Long crtTm;
    private String express;
    private String mobile;
    private String contact;
    private String phone;
    private String qq;
    private String address;
    private String remark;
    private String opRemark;
    private String opor;
    private Long opTm;
}
