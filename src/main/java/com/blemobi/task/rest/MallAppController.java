package com.blemobi.task.rest;

import com.blemobi.sep.probuf.ResultProtos;
import com.blemobi.task.config.InstanceFactory;
import com.blemobi.task.service.GoodsInfService;
import com.blemobi.task.service.OrderService;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;
import lombok.extern.log4j.Log4j;

import javax.ws.rs.*;

/**
 * Description: app 端接口
 * User: HUNTER.POON
 * Date: 2017/6/2 17:19
 */
@Log4j
@Path("v1/task/front")
public class MallAppController {
    private GoodsInfService goodsInfService = InstanceFactory.getInstance(GoodsInfService.class);
    private OrderService orderService = InstanceFactory.getInstance(OrderService.class);

    /**
     * 获取每个分类下商品的数量
     *
     * @param uuid  当前用户ID
     * @param token 会话token
     * @return 返回实物商品、虚拟商品的数量
     */
    @GET
    @Path("typeStatistics")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage goodsTypeStatistics(@CookieParam("uuid") String uuid, @CookieParam("token") String token) {
        log.debug("查看分类下商品数量");
        return goodsInfService.categoryStatics();
    }

    /**
     * 根据条件获取商品列表
     *
     * @param uuid     当前用户ID
     * @param token    当前用户会话
     * @param category 分类0：实物商品、1：虚拟商品
     * @param serialNo 最后一个ID
     * @return 返回商品列表
     */
    @GET
    @Path("goodsList")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage goodsList(@CookieParam("uuid") String uuid, @CookieParam("token") String token, @QueryParam("category") int
            category, @QueryParam("serialNo") int serialNo) {
        log.debug("goodsList");
        return goodsInfService.goodsList(category, serialNo);
    }


    /**
     * 客户端我购买的商品列表
     *
     * @param uuid
     * @param token
     * @param category
     * @param time
     * @return
     */
    @GET
    @Path("ordList")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage ordList(@CookieParam("uuid") String uuid, @CookieParam
            ("token") String token, @QueryParam("category") int category, @QueryParam("time") long time) {
        log.debug("ordList");
        return orderService.ordList(uuid, category, time);
    }

    /**
     * 根据ID获取商品详情
     *
     * @param uuid  当前用户ID
     * @param token 当前用户会话
     * @param id    商品ID
     * @return 返回商品详情
     */
    @GET
    @Path("detail")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage detail(@CookieParam("uuid") String uuid, @CookieParam("token") String token,
                                        @QueryParam("id") int id) {
        log.debug("detail");
        return goodsInfService.goodsDetail(id);
    }

    /**
     * 兑换实物商品
     *
     * @param uuid     当前用户UUID
     * @param token    会话
     * @param id       实物ID
     * @param contact  联系人
     * @param address  收货地址
     * @param phone    联系电话
     * @param remark   特别要求
     * @param ordChann 渠道 iOS下单：0；安卓下单：1；H5下单：2
     * @param payChann 支付渠道：金币支付0；支付宝支付：1；微信支付：2；银联支付：3
     * @param bizType  业务类型：虚拟兑换 0；实物兑换：1；虚拟购买：2；实物购买：3
     * @return 兑换结果
     */

    @POST
    @Path("exchangeReal")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage exchageReal(@CookieParam("uuid") String uuid, @CookieParam("token") String token,
                                             @FormParam("id") int id, @FormParam("contact") String contact, @FormParam
                                                     ("address") String address, @FormParam("phone") String phone,
                                             @FormParam("remark") String remark, @FormParam("excChannel") int ordChann,
                                             @FormParam("payChannel") int payChann, @FormParam("bizType") int bizType) {
        log.debug("exchageReal");
        return orderService.exchageGoods(uuid, id, contact, address, null, null, phone, remark, ordChann, payChann,
                bizType);
    }

    /**
     * 兑换虚拟商品
     *
     * @param uuid     当前用户UUID
     * @param token    会话
     * @param id       实物ID
     * @param email    邮箱地址
     * @param qqNO     qq号码
     * @param phone    联系电话
     * @param remark   特别要求
     * @param ordChann 渠道 iOS下单：0；安卓下单：1；H5下单：2
     * @param payChann 支付渠道：金币支付0；支付宝支付：1；微信支付：2；银联支付：3
     * @param bizType  业务类型：虚拟兑换 0；实物兑换：1；虚拟购买：2；实物购买：3
     * @return 兑换结果
     */
    @POST
    @Path("exchangeVirtual")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage exchageVirtual(@CookieParam("uuid") String uuid, @CookieParam("token") String token,
                                                @FormParam("id") int id, @FormParam("email") String email, @FormParam
                                                        ("qqNO") String qqNO, @FormParam("phone") String phone,
                                                @FormParam("remark") String remark, @FormParam("excChannel") int ordChann,
                                                @FormParam("payChannel") int payChann, @FormParam("bizType") int bizType) {
        log.debug("exchageVirtual");
        return orderService.exchageGoods(uuid, id, null, null, email, qqNO, phone, remark, ordChann, payChann,
                bizType);
    }

    /**
     * 获取收货信息
     *
     * @param uuid
     * @param token
     * @return
     */
    @GET
    @Path("getAddr")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage getAddr(@CookieParam("uuid") String uuid, @CookieParam("token") String token) {
        log.debug("getAddr");
        return orderService.getAddr(uuid);
    }

    @GET
    @Path("getGold")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage getGold(@CookieParam("uuid") String uuid, @CookieParam("token") String token) {
        log.debug("getGold");
        return orderService.getGold(uuid);
    }

    /**
     * 获取收货信息
     *
     * @param uuid
     * @param token
     * @return
     */
    @GET
    @Path("getContacts")
    @Produces(MediaTypeExt.APPLICATION_PROTOBUF)
    public ResultProtos.PMessage getContacts(@CookieParam("uuid") String uuid, @CookieParam("token") String token) {
        log.debug("getContacts");
        return orderService.getContacts();
    }
}
