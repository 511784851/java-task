package com.blemobi.task.service.impl;

import com.blemobi.library.cache.UserBaseCache;
import com.blemobi.library.client.OssHttpClient;
import com.blemobi.library.consul_v1.PropsUtils;
import com.blemobi.library.grpc.CommunityGrpcClient;
import com.blemobi.library.grpc.TaskGrpcClient;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.AccountProtos;
import com.blemobi.sep.probuf.MallProtos;
import com.blemobi.sep.probuf.OssProtos;
import com.blemobi.sep.probuf.ResultProtos;
import com.blemobi.task.dao.AddrInfDAO;
import com.blemobi.task.dao.GoodsInfDAO;
import com.blemobi.task.dao.OrderDAO;
import com.blemobi.task.exception.BizException;
import com.blemobi.task.model.Order;
import com.blemobi.task.model.Page;
import com.blemobi.task.service.OrderService;
import com.blemobi.task.util.IDUtils;
import com.blemobi.task.util.MapUtils;
import com.blemobi.tools.DateUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 13:47
 */
@Log4j
@Service("orderService")
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderDAO orderDAO;
    @Autowired
    private AddrInfDAO addrInfDAO;
    @Autowired
    private GoodsInfDAO goodsInfDAO;

    private final static String _ORDER_FIELDS = "g.nm, g.goods_no, g.price, g.obj_key, o.id, o.g_id, o.g_type, o" +
            ".status,o" +
            ".crt_tm, o" +
            ".express,o" +
            ".mobile,o" +
            ".phone,o.contact,o.addr,o.email,o.qq,o.remark,o.op_remark,o.opor,o.op_tm, o.buyer ";

    @Override
    public Page<Order> ordList(int begin, int end, int status, String type, String val, int pageSize, int page) {
        Page<Order> data = new Page<>();
        StringBuilder sql = new StringBuilder("SELECT {0} FROM t_order_inf o join " +
                "t_goods_inf g ON o.g_id=g.id WHERE 1 = 1 ");
        List<Object> paramList = new ArrayList<>();
        if (begin > 0) {
            sql.append("AND o.begin >= ? ");
            paramList.add(begin);
        }
        if (end > 0) {
            sql.append("AND o.end <= ? ");
            paramList.add(end);
        }

        if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(val)) {
            if ("orderNO".equals(type)) {
                sql.append("AND o.id = ? ");
                paramList.add(val);
            } else if ("goodsNm".equals(type)) {
                sql.append("AND g.nm LIKE ? ");
                paramList.add("%" + val + "%");
            } else if ("goodsNO".equals(type)) {
                sql.append("AND g.goods_no = ? ");
                paramList.add(val);
            } else if ("phoneNO".equals(type)) {
                sql.append("AND o.mobile = ? ");
                paramList.add(val);
            }
        }
        if (status != -1) {
            sql.append("AND o.status = ? ");
            paramList.add(status);
        }

        Integer totCnt = orderDAO.ordListSize(sql.toString().replace("{0}", "COUNT(1) AS cnt"), paramList);
        sql.append("LIMIT ?, ?");
        paramList.add((page - 1) * pageSize);
        paramList.add(pageSize);
        int totPage = totCnt % pageSize == 0 ? totCnt / pageSize : totCnt / pageSize + 1;
        data.setCurrentPage(page);
        data.setRecordPerPage(pageSize);
        data.setTotPage(totPage);
        data.setTotRecords(totCnt);
        List<Map<String, Object>> list = orderDAO.ordList(sql.toString().replace("{0}", _ORDER_FIELDS), paramList);
        List<Order> retList = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Order order = buildOrder(map);
                retList.add(order);
            }
        }
        data.setData(retList);
        return data;
    }

    private Order buildOrder(Map<String, Object> orderMap) {
        Order order = new Order();
        order.setAddress(MapUtils.getString(orderMap, "addr"));
        order.setContact(MapUtils.getString(orderMap, "contact"));
        order.setCrtTm(MapUtils.getLong(orderMap, "crt_tm"));
        order.setExpress(MapUtils.getString(orderMap, "express"));
        order.setGoodsNo(MapUtils.getString(orderMap, "goods_no"));
        order.setName(MapUtils.getString(orderMap, "nm"));
        order.setMobile(MapUtils.getString(orderMap, "mobile"));
        String opor = MapUtils.getString(orderMap, "opor");
        if(!StringUtils.isEmpty(opor.trim())) {
            try {
                AccountProtos.PUserBase userBase = UserBaseCache.get(opor);
                if (userBase != null) {
                    order.setOpor(userBase.getNickname());
                } else {
                    order.setOpor(opor);
                }
            } catch (IOException e) {
                throw new RuntimeException("用户没有找到");
            }
        }
        order.setOpRemark(MapUtils.getString(orderMap, "op_remark"));
        order.setOpTm(MapUtils.getLong(orderMap, "op_tm"));
        order.setOrdNo(MapUtils.getString(orderMap, "id"));
        order.setOrdStatus(MapUtils.getInt(orderMap, "status"));
        order.setPhone(MapUtils.getString(orderMap, "phone"));
        order.setQq(MapUtils.getString(orderMap, "qq"));
        order.setRemark(MapUtils.getString(orderMap, "remark"));
        return order;
    }

    @Override
    public ResultProtos.PMessage ordList(String uuid, int goodsType, long time) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(_ORDER_FIELDS).append(" FROM t_order_inf o JOIN " +
                "t_goods_inf g ON o.g_id=g.id WHERE 1 = 1 ");
        List<Object> param = new ArrayList<>();
        sql.append("AND o.crt_tm < ? ");
        if (time == 0) {
            param.add(System.currentTimeMillis());
        } else {
            param.add(time);
        }
        if (goodsType != -1) {
            sql.append("AND g.category = ? ");
            param.add(goodsType);
        }
        sql.append("AND o.buyer = ? ");
        param.add(uuid);
        sql.append("ORDER BY o.crt_tm DESC LIMIT 20");
        List<Map<String, Object>> mapList = orderDAO.ordList(sql.toString(), param);
        MallProtos.POrderList.Builder builder = MallProtos.POrderList.newBuilder();
        if (mapList != null && !mapList.isEmpty()) {
            List<MallProtos.POrderInf> list = new ArrayList<>();
            mapList.forEach(map -> {
                list.add(buildPOrder(map));
            });
            builder.addAllList(list);
        }
        return ReslutUtil.createReslutMessage(builder.build());
    }

    private OssProtos.PDownload getURL(String objectKey) {
        OssHttpClient ossClient = new OssHttpClient();
        try {
            ResultProtos.PMessage message = ossClient.getDownloadurls(objectKey);
            OssProtos.PDownloadArray downloadArray = OssProtos.PDownloadArray.parseFrom(message.getData());
            List<OssProtos.PDownload> list = downloadArray.getDownloadsList();
            if (list == null || list.isEmpty()) {
                return null;
            }
            return list.get(0);
        } catch (Exception ex) {
            log.error("调用获取地址出现异常", ex);
        }
        return null;
    }

    private MallProtos.POrderInf buildPOrder(Map<String, Object> map) {
        String uuid = MapUtils.getString(map, "buyer");
        AccountProtos.PUserBase buyer = null;
        try {
            buyer = UserBaseCache.get(uuid);
        } catch (IOException e) {
            throw new RuntimeException("用户没有找到");
        }
        String opor = MapUtils.getString(map, "opor");
        AccountProtos.PUserBase oporInf = null;
        if (!StringUtils.isEmpty(opor.trim())) {
            try {
                oporInf = UserBaseCache.get(opor);
            } catch (IOException e) {
                throw new RuntimeException("用户没有找到");
            }
        }
        String objKey = MapUtils.getString(map, "obj_key");
        OssProtos.PDownload download = getURL(objKey);
        MallProtos.POss pic = MallProtos.POss.newBuilder().setObjKey(objKey).setThumb(download.getThumb()).setUrl
                (download.getUrl()).build();
        MallProtos.PGoodsInf goodsInf = MallProtos.PGoodsInf.newBuilder().setId(MapUtils.getInt(map, "g_id")).setNm
                (MapUtils.getString(map, "nm"))
                .setCategory
                        (MallProtos
                                .PGoodsCategory.valueOf(MapUtils.getInt(map, "g_type")))
                .setPrice(MapUtils.getInt(map, "price")).setPic(pic).build();
        MallProtos.POrderInf ordInf = MallProtos.POrderInf.newBuilder().setAddrInf(buildAddr(map)).setBbNO(MapUtils
                .getString(map,
                "bb_no"))
                .setBuyer(buyer)
                .setCount(1)
                .setCrtTm(MapUtils.getLong(map, "crt_tm")).setExpress(MapUtils.getString(map, "express")).setGoodsInf(goodsInf)
                .setId(MapUtils.getString(map, "id")).setOpRemark(MapUtils.getString(map, "op_remark"))
                .setOpTm(MapUtils.getLong(map, "op_tm"))
                .setRemark(MapUtils.getString(map, "remark")).setStatus(MapUtils.getInt(map, "status")).build();
        if(oporInf != null){
            ordInf = ordInf.toBuilder().setOpor(oporInf).build();
        }
        return ordInf;
    }

    private MallProtos.PAddrInf buildAddr(Map<String, Object> map) {
        return MallProtos.PAddrInf.newBuilder().setAddr(MapUtils.getString(map, "addr"))
                .setContact(MapUtils.getString(map, "contact")).setEmail(MapUtils.getString(map, "email"))
                .setPhone(MapUtils.getString(map, "phone")).setQq(MapUtils.getString(map, "qq")).build();
    }

    private Map<String, Object> getAddrMap(String contact, String address, String email, String qqNO, String phone) {
        Map<String, Object> addrMap = new HashMap<>();
        if (!StringUtils.isEmpty(contact)) {
            addrMap.put("contact", contact);
        }
        if (!StringUtils.isEmpty(address)) {
            addrMap.put("addr", address);
        }
        if (!StringUtils.isEmpty(email)) {
            addrMap.put("email", email);
        }
        if (!StringUtils.isEmpty(qqNO)) {
            addrMap.put("qq", qqNO);
        }
        if (!StringUtils.isEmpty(phone)) {
            addrMap.put("phone", phone);
        }
        return addrMap;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ResultProtos.PMessage exchageGoods(String uuid, int id, String contact, String address, String email,
                                              String qqNO, String phone, String remark, int ordChann, int payChann,
                                              int bizType) {
        Map<String, Object> goodsInf = goodsInfDAO.goodsDetail(id);
        if (goodsInf == null) {
            throw new RuntimeException("未找到兑换的商品->[" + id + "]");
        }
        int category = MapUtils.getInt(goodsInf, "category");
        int limitType = MapUtils.getInt(goodsInf, "limit_typ");//限制类型 0:不限 1:按天，2:按周，周一 - 周日
        int times = MapUtils.getInt(goodsInf, "limit_times"); //限制次数
        int communityId = MapUtils.getInt(goodsInf, "exchange_cid");//可兑换的社区ID
        int level = MapUtils.getInt(goodsInf, "exchange_level");//用户所在社区的等级
        int saleStatus = MapUtils.getInt(goodsInf, "sale_status");//0:待上架，1：售中，2：下架
        int status = MapUtils.getInt(goodsInf, "status"); //0:正常、1：今日已售罄、2：已售罄 、3：库存告急
        int gold = MapUtils.getInt(goodsInf, "price");
        if (communityId > 0) {
            CommunityGrpcClient cClient = new CommunityGrpcClient();
            int right = cClient.getUsrAndCommunityMembership(uuid, communityId);
            if (right == -1) {
                throw new BizException(3101013, "你不在指定社区");
            }
        }
        // TODO: 2017/6/12 判断用户在社区的等级
        int buyTimes = -2;
        if (limitType == 1) {
            long start = DateUtils.getDayStart();
            long end = System.currentTimeMillis();
            buyTimes = orderDAO.getBuyCount(uuid, id, start, end);
            if (buyTimes >= times) {
                throw new BizException(3101009, "当日购买数量超出限制");
            }
        } else if (limitType == 2) {
            long start = DateUtils.getWeekStart();
            long end = System.currentTimeMillis();
            buyTimes = orderDAO.getBuyCount(uuid, id, start, end);
            if (buyTimes >= times) {
                throw new BizException(3101010, "当周购买数量超出限制");
            }
        }
        if (saleStatus == 0) {
            throw new BizException(3101007, "商品还未上架，不能进行交易");
        } else if (saleStatus == 2) {
            throw new BizException(3101008, "商品已下架，不能进行交易");
        }
        if (status == 1) {
            throw new BizException(3101011, "该商品今日已售罄");
        } else if (status == 2) {
            throw new BizException(3101012, "该商品已售罄");
        }

        Map<String, Object> addrInfo = addrInfDAO.getAddr(uuid);
        Map<String, Object> addrMap = getAddrMap(contact, address, email, qqNO, phone);
        if (addrInfo == null) {
            addrMap.put("uuid", uuid);
            addrInfDAO.addAddr(addrMap);
        } else {

            addrInfDAO.editAddr(addrMap, uuid);
        }
        StringBuilder updSql = new StringBuilder("UPDATE t_goods_inf SET ");
        int totStock = MapUtils.getInt(goodsInf, "tot_stock");
        int todayStock = MapUtils.getInt(goodsInf, "today_stock");

        int totRemain = MapUtils.getInt(goodsInf, "tot_remain");
        int todayRemain = MapUtils.getInt(goodsInf, "today_remain");
        if(todayRemain == 1){
            status = 1;
        }
        if(totRemain == 1){
            status = 2;
        }
        List<Object> updParam = new ArrayList<>();
        updSql.append("status = ?, ");
        updParam.add(status);
        if (totStock != -1) {
            updSql.append("tot_remain = tot_remain - 1, ");
        }
        if (todayStock != -1) {
            updSql.append("today_remain = today_remain - 1, ");
        }
        updSql.append("tot_saled = tot_saled + 1, today_saled = today_saled + 1 WHERE id = ? AND " +
                "tot_stock = ? AND today_stock = ?");
        updParam.add(id);
        updParam.add(totStock);
        updParam.add(todayStock);
        goodsInfDAO.updateStock(updSql.toString(), updParam);//更新库存
        String ordId = IDUtils.genOrdId(ordChann, payChann, bizType, uuid);
        Map<String, Object> ordMap = new HashMap<>();
        ordMap.put("id", ordId);
        ordMap.put("g_id", id);
        ordMap.put("g_type", category);
        ordMap.put("count", 1);
        ordMap.putAll(addrMap);
        ordMap.remove("uuid");
        ordMap.put("remark", remark);
        ordMap.put("buyer", uuid);
        String bbNO = " ";
        try {
            AccountProtos.PUserBase userBase = UserBaseCache.get(uuid);
            bbNO = userBase.getUserName();
        }catch(Exception ex){
            log.warn("没有获取到用户信息");
        }
        ordMap.put("bb_no", bbNO);
        ordMap.put("crt_tm", System.currentTimeMillis());
        orderDAO.insertOrder(ordMap);//添加订单
        //调用金币消耗接口
        TaskGrpcClient client = new TaskGrpcClient();
        client.exchangeGoods(gold, ordId, uuid);
        return ReslutUtil.createSucceedMessage();
    }

    @Override
    public ResultProtos.PMessage getContacts() {
        String qq = PropsUtils.getString("contact_qq");
        String email = PropsUtils.getString("contact_email");
        return ReslutUtil.createReslutMessage(MallProtos.PContacts.newBuilder().setEmail(email).setQq(qq).build());
    }

    @Override
    public ResultProtos.PMessage getGold(String uuid) {
        TaskGrpcClient client = new TaskGrpcClient();
        Integer gold = client.getGold(uuid);
        log.debug("用户UUID->" + uuid + ", 可用金币数量->" + gold);
        ResultProtos.PInt32Single ret = ResultProtos.PInt32Single.newBuilder().setVal(gold).build();
        return ReslutUtil.createReslutMessage(ret);
    }

    @Override
    public ResultProtos.PMessage getAddr(String uuid) {
        Map<String, Object> map = addrInfDAO.getAddr(uuid);
        if (map == null || map.isEmpty()) {
            return ReslutUtil.createSucceedMessage();
        }
        return ReslutUtil.createReslutMessage(buildAddr(map));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean addExpress(String uuid, String ordId, String expressInf, String describe,
                              boolean changeStatus) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("opor", uuid);
        conditions.put("express", expressInf);
        if (!StringUtils.isEmpty(describe)) {
            conditions.put("op_remark", describe);
        }
        if (changeStatus) {
            conditions.put("status", 1);
        }
        return orderDAO.updateExpressInf(conditions, ordId);
    }
}
