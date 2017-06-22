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
    private Integer id;//商品主键
    private String name; //商品名称
    private Integer category;//商品分类
    private Integer price;// 金币价格
    private String objKey;// 图片objKey
    private String url;// 图片URL
    private String thumb;// 缩略图URL
    private Integer stock;// 总库存
    private String describe;// 描述
    private String otherDescribe;// 其它描述
    private Integer limitCnt;//限购数量
    private Integer communityId;//兑换社区ID -1 不限制，0:全部社区
    private Integer level;// 兑换限制等级 -1 不限制
    private Integer tag;//标签，-1：没有标签
    private Integer limitType;//限购类型 0:不限制，1:天，2:周
    private Integer times;// 次数
    private Integer saleStatus;//出售状态
    private Integer onoffType;//上下架类型
    private Long onSaleTm;//上架时间 ms
    private Long offSaleTm;//下架时间 ms
    private Integer sort;//商品排序
    private Long crtTm;// 创建时间
    private String goodsNo;//商品编号
    private Integer salesCnt;//总售出数量
    private Integer stockPerDay;//今日库存
    private String exchangeNm;//兑换的社区名称，
    private Integer totRemain;//总剩余
    private Integer todayRemain;//今日剩余
}



