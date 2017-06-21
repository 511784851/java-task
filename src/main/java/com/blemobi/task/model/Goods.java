package com.blemobi.task.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/6 14:01
 */
@Getter
@Setter
@ToString
public class Goods {
    private Integer id;
    private Long crtTm;
    private String goodsNo;
    private String name;
    private Integer goodsType;
    private Integer price;
    private Integer salesCnt;
    private Integer stock;
    private Integer stockPerDay;
    private Long onSaleTm;
    private Long offSaleTm;
    private Integer saleStatus;
    private String exchangeNm;
    private Integer totRemain;
    private Integer todayRemain;
    private Integer sort;
}



