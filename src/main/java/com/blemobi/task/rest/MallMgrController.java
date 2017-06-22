package com.blemobi.task.rest;

import com.alibaba.fastjson.JSONObject;
import com.blemobi.library.util.ExcelUtils;
import com.blemobi.sep.probuf.MallProtos;
import com.blemobi.sep.probuf.ResultProtos;
import com.blemobi.task.config.InstanceFactory;
import com.blemobi.task.exception.BizException;
import com.blemobi.task.exception.RestException;
import com.blemobi.task.model.Goods;
import com.blemobi.task.model.Order;
import com.blemobi.task.model.Page;
import com.blemobi.task.service.GoodsInfService;
import com.blemobi.task.service.OrderService;
import com.googlecode.protobuf.format.JsonFormat;
import com.pakulov.jersey.protobuf.internal.MediaTypeExt;
import lombok.extern.log4j.Log4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:管理平台接口
 * User: HUNTER.POON
 * Date: 2017/6/2 17:20
 */

/**
 * 添加商品、修改商品、（保存）发货信息、
 * 交易记录、设置上下架
 */
@Log4j
@Path("v1/task/mgr")
public class MallMgrController {
    private GoodsInfService goodsInfService = InstanceFactory.getInstance(GoodsInfService.class);
    private OrderService orderService = InstanceFactory.getInstance(OrderService.class);

    private void wrap(Exception ex){
        log.error("", ex);
        if(ex instanceof BizException){
            BizException be = (BizException) ex;
            throw new RestException(be.getErrCd(), be.getMsg());
        }else{
            throw new RestException(1001012, "系统繁忙");
        }
    }
    /**
     * 新增商品
     *
     * @param uuid          当前用户UUID
     * @param token         会话
     * @param name          商品名称
     * @param category      分类0; //实物1; //虚拟物品
     * @param price         售价
     * @param objkey        图片
     * @param describe      描述
     * @param otherDescribe 其它说明
     * @param stock         库存 -1不限
     * @param communityId   社区ID -1不限
     * @param level         会员等级 -1不限
     * @param tag           标签0; //常规1; //限量2; //专属3; //特价
     * @param limitType     限制类型0; //不限制1; //按天限制2; //按周限制
     * @param times         次数 1，2选项
     * @param saleStatus    出售状态
     * @param onoffType     上下架类型 0; //默认1; //时间段
     * @param begin         上架时间 0没设置
     * @param end           下架时间 0没设置
     * @param serialNO      排序
     * @return
     */
    @POST
    @Path("addGoods")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_JSON)
    public String createGoodsInf(@CookieParam("uuid") String uuid, @CookieParam
            ("token") String token, @FormParam("name") String name, @FormParam("category") Integer category,
                                 @FormParam("price") Integer price, @FormParam("objkey") String objkey, @FormParam
                                         ("describe") String describe, @FormParam("otherDescribe") String
                                         otherDescribe, @FormParam("stock") int stock, @FormParam("limitCnt") int
                                         limitCnt,
                                 @FormParam("communityId") Integer communityId, @FormParam("level") Integer level,
                                 @FormParam("tag") Integer tag, @FormParam("limitType") int limitType, @FormParam
                                         ("saleStatus") int saleStatus, @FormParam("times") int times, @FormParam
                                         ("onoffType") int onoffType, @FormParam
                                         ("begin") long begin, @FormParam("end") long end, @FormParam("serialNO")
                                         Integer serialNO) {
        log.debug("createGoodsInf");
        try {
            Boolean ret = goodsInfService.createGoods(uuid, name, category, price, objkey, describe, otherDescribe,
                    stock, limitCnt,
                    communityId,
                    level, tag, limitType, times, saleStatus, onoffType, begin, end, serialNO);
            JSONObject json = new JSONObject();
            if (!ret) {
                json.put("code", 1001012);
            } else {
                json.put("code", 0);
            }
            return json.toJSONString();
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }

    /**
     * 编辑商品
     *
     * @param uuid          当前用户UUID
     * @param token         会话
     * @param id            商品ID
     * @param name          商品名称
     * @param category      分类0; //实物1; //虚拟物品
     * @param price         售价
     * @param objkey        图片
     * @param describe      描述
     * @param otherDescribe 其它说明
     * @param stock         库存 -1不限
     * @param communityId   社区ID -1不限
     * @param level         会员等级 -1不限
     * @param tag           标签0; //常规1; //限量2; //专属3; //特价
     * @param limitType     限制类型0; //不限制1; //按天限制2; //按周限制
     * @param times         次数 1，2选项
     * @param saleStatus    出售状态
     * @param onoffType     上下架类型 0; //默认1; //时间段
     * @param begin         上架时间 0没设置
     * @param end           下架时间 0没设置
     * @param serialNO      排序
     * @return
     */
    @POST
    @Path("editGoods")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_JSON)
    public String editGoods(@CookieParam("uuid") String uuid, @CookieParam
            ("token") String token, @FormParam("id") Integer id, @FormParam("name") String name, @FormParam("category")
                                    Integer category, @FormParam("price") Integer price, @FormParam("objkey") String
                                    objkey, @FormParam("describe") String describe, @FormParam("otherDescribe") String
                                    otherDescribe, @FormParam("stock") int stock, @FormParam("limitCnt") int
                                    limitCnt, @FormParam
                                    ("communityId") Integer communityId, @FormParam("level") Integer level,
                            @FormParam("tag") int tag, @FormParam("limitType") int limitType, @FormParam
                                    ("times") int times, @FormParam("saleStatus") int saleStatus, @FormParam
                                    ("onoffType") int onoffType, @FormParam
                                    ("begin") long begin, @FormParam("end") long end, @FormParam("serialNO")
                                    Integer serialNO) {
        log.debug("editGoods");
        try {
            Boolean ret = goodsInfService.updateGoods(id, uuid, name, category, price, objkey, describe,
                    otherDescribe, stock, limitCnt,
                    communityId, level, tag, limitType, times, saleStatus, onoffType, begin, end, serialNO);
            JSONObject json = new JSONObject();
            if (!ret) {
                json.put("code", 1001012);
            } else {
                json.put("code", 0);
            }
            return json.toJSONString();
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }

    /**
     * 更新商品状态
     *
     * @param uuid
     * @param token
     * @param id         商品ID
     * @param saleStatus 商品上下架状态 1;//出售中 2;//已下架
     * @return
     */
    @POST
    @Path("changeGoodsStatus")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_JSON)
    public String changeGoodsStatus(@CookieParam("uuid") String uuid, @CookieParam
            ("token") String token, @FormParam("id") Integer id, @FormParam("saleStatus") Integer saleStatus) {
        log.debug("changeGoodsStatus");
        try {
            Boolean ret = goodsInfService.changeGoodsStatus(uuid, id, saleStatus);
            JSONObject json = new JSONObject();
            if (!ret) {
                json.put("code", 1001012);
            } else {
                json.put("code", 0);
            }
            return json.toJSONString();
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }

    @POST
    @Path("updateSerial")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_JSON)
    public String updateSerial(@CookieParam("uuid") String uuid, @CookieParam
            ("token") String token, @FormParam("id[]") List<Integer> idList, @FormParam("serial[]") List<Integer>
                                       serialList) {
        log.debug("updateSerial");
        serialList.forEach(serial -> {
            if (serial < 1) {
                log.error("序号只能为正整数");
                throw new RestException(3101012, "序号只能为正整数");
            }
        });
        if(idList.size() != serialList.size()){
            log.error("ID与序号不能一一对应");
            throw new RestException(1001012, "系统繁忙");
        }
        try {
            Boolean ret = goodsInfService.updateSerial(idList, serialList);
            JSONObject json = new JSONObject();
            if (!ret) {
                json.put("code", 1001012);
            } else {
                json.put("code", 0);
            }
            return json.toJSONString();
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }

    /**
     * 商品列表
     *
     * @param uuid       当前用户UUID
     * @param token      会话
     * @param category   商品分类 -1 不限
     * @param name       商品名称
     * @param saleStatus 出售状态
     * @param pageSize   50，100，150，200
     * @param page       当前页面首页传1
     * @return
     */
    @GET
    @Path("goodsList")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_JSON)
    public String goodsList(@CookieParam("uuid") String uuid, @CookieParam
            ("token") String token, @QueryParam("saleStatus") Integer saleStatus, @QueryParam("category") Integer
                                    category, @QueryParam("name") String name, @QueryParam("begin") Long begin,
                            @QueryParam("end") Long end,
                            @QueryParam("pageSize") int pageSize, @QueryParam("page") int
                                    page, @QueryParam("sort") String sort) {
        log.debug("goodsList");
        try {
            Page<Goods> pageData = goodsInfService.goodsList(begin, end, saleStatus, category, name, pageSize, page, sort);
            String json = JSONObject.toJSONString(pageData);
            log.debug(json);
            return json;
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }

    @GET
    @Path("goodsExport")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_OCTET_STREAM)
    public Response goodsExport(@CookieParam("uuid") String uuid, @CookieParam
            ("token") String token, @QueryParam("saleStatus") Integer saleStatus, @QueryParam("category") Integer
                                        category, @QueryParam("name") String name, @QueryParam("begin") Long begin,
                                @QueryParam("end") Long end, @QueryParam("sort") String sort) {
        log.debug("goodsExport");
        try {
            List<Map<String, Object>> data = goodsInfService.goodsList(begin, end, saleStatus, category, name, sort);
            List<String> titleList = new ArrayList<>();
            List<String> headerList = new ArrayList<>();
            titleList.add("创建日期");
            titleList.add("商品编号");
            titleList.add("商品名称");
            titleList.add("商品类型");
            titleList.add("兑换金币");
            titleList.add("已售数量");
            titleList.add("总库存");
            titleList.add("每日库存");
            titleList.add("上架时间");
            titleList.add("排序");
            titleList.add("销售状态");
            headerList.add("crt_tm");
            headerList.add("goods_no");
            headerList.add("nm");
            headerList.add("category");
            headerList.add("price");
            headerList.add("tot_saled");
            headerList.add("tot_remain");
            headerList.add("today_remain");
            headerList.add("begin");
            headerList.add("serial_no");
            headerList.add("sale_status");
            ByteArrayInputStream stream = ExcelUtils.exportExcelWithTitle("商品列表", data, titleList, headerList);
            Response.ResponseBuilder responseBuilder = Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            responseBuilder.type(MediaType.APPLICATION_OCTET_STREAM_TYPE);
            String fileNm = new String("商品列表".getBytes("utf-8"), "ISO-8859-1");
            return responseBuilder.header("content-disposition", "inline;filename=" + fileNm + ".xls").build();
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }

    /**
     * 订单列表
     *
     * @param uuid     当前用户UUID
     * @param token    会话
     * @param begin    下单开始时间
     * @param end      下单结束时间
     * @param status   订单状态
     * @param type     查询字段类型 ""：忽略，orderNO，goodsNm，phoneNO
     * @param val      根据type传入对应的keywords
     * @param pageSize 50，100，150，200
     * @param page     当前页面首页传1
     * @return
     */
    @GET
    @Path("ordList")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_JSON)
    public String ordList(@CookieParam("uuid") String uuid, @CookieParam
            ("token") String token, @QueryParam("begin") int begin, @QueryParam("end") int end, @QueryParam
                                  ("status") int status, @QueryParam("type") String type, @QueryParam("val") String val,
                          @QueryParam("pageSize") int pageSize, @QueryParam("page") int page) {
        log.debug("ordList");
        try {
            Page<Order> pageData = orderService.ordList(begin, end, status, type, val, pageSize, page);
            String json = JSONObject.toJSONString(pageData);
            log.debug(json);
            return json;
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }


    /**
     * 获取分享商品详情
     *
     * @param id
     * @return
     */
    @GET
    @Path("shareGoods")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_JSON)
    public String shareGoods(@QueryParam("id") int id) {
        log.debug("shareGoods");
        try {
            ResultProtos.PMessage message = goodsInfService.goodsDetail(id);
            MallProtos.PGoodsInf goodsInf = MallProtos.PGoodsInf.parseFrom(message.getData());
            String json = JsonFormat.printToString(goodsInf);
            log.debug(json);
            return json;
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }

    /**
     * 添加物流信息
     *
     * @param uuid         当前用户UUID
     * @param token        会话
     * @param ordId        订单号
     * @param expressInf   物流信息
     * @param describe     描述
     * @param changeStatus 是否更改为已发货
     * @return
     */
    @POST
    @Path("addExpress")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaTypeExt.APPLICATION_JSON)
    public String addExpress(@CookieParam("uuid") String uuid, @CookieParam("token") String token,
                             @FormParam("ordId") String ordId, @FormParam("expressInf") String expressInf, @FormParam("describe")
                                     String describe, @FormParam("changeStatus") boolean changeStatus) {
        log.debug("addExpress");
        try {
            Boolean ret = orderService.addExpress(uuid, ordId, expressInf, describe, changeStatus);
            JSONObject json = new JSONObject();
            if (!ret) {
                json.put("code", 1001012);
            } else {
                json.put("code", 0);
            }
            return json.toJSONString();
        } catch (Exception ex) {
            wrap(ex);
        }
        return null;
    }
}
