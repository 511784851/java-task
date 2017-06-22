package com.blemobi.task.service.impl;

import com.blemobi.library.client.OssHttpClient;
import com.blemobi.library.consul_v1.PropsUtils;
import com.blemobi.library.grpc.CommunityGrpcClient;
import com.blemobi.library.util.ReslutUtil;
import com.blemobi.sep.probuf.CommunityProtos;
import com.blemobi.sep.probuf.MallProtos;
import com.blemobi.sep.probuf.OssProtos;
import com.blemobi.sep.probuf.ResultProtos;
import com.blemobi.task.dao.GoodsInfDAO;
import com.blemobi.task.exception.BizException;
import com.blemobi.task.model.Goods;
import com.blemobi.task.model.Page;
import com.blemobi.task.service.GoodsInfService;
import com.blemobi.task.util.IDUtils;
import com.blemobi.task.util.MapUtils;
import com.blemobi.tools.DateUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 13:47
 */
@Log4j
@Service("goodsInfService")
public class GoodsInfServiceImpl implements GoodsInfService {
    @Autowired
    private GoodsInfDAO goodsInfDAO;
    private static final long _YEAR = 360 * 24 * 60 * 60 * 1000l;

    private void checkGoods(Integer category, Integer price, String objkey, String name, String describe, Integer
            stock, Integer saleStatus) {
        if (category < 0 || category > 1) {
            throw new BizException(3101000, "请选择商品类型");
        }
        if (stock == 0 || stock < -1) {
            throw new BizException(3101001, "请填写库存总数");
        }
        if (price < 1) {
            throw new BizException(3101002, "请填写销售价");
        }
        if (StringUtils.isEmpty(objkey)) {
            throw new BizException(3101003, "请上传商品图片");
        }
        if (StringUtils.isEmpty(name)) {
            throw new BizException(3101004, "请填写商品名称");
        }
        if (StringUtils.isEmpty(describe)) {
            throw new BizException(3101005, "请填写商品描述");
        }
        if (saleStatus < 0 || saleStatus > 2) {
            throw new BizException(3101006, "请选择销售状态");
        }
    }

    private Map<String, Object> getGoodsMap(String uuid, String name, Integer category, Integer price, String objkey, String describe,
                                            String otherDescribe, int stock, int limitCnt, Integer communityId,
                                            Integer level, Integer tag, int limitType, int times, int
                                                    saleStatus, int onoffType, long begin, long end, int serialNo) {
        checkGoods(category, price, objkey, name, describe, stock, saleStatus);
        Map<String, Object> param = new HashMap<>();
        param.put("category", category);
        param.put("nm", name);
        param.put("price", price);
        param.put("obj_key", objkey);
        param.put("describe", describe);
        if (!StringUtils.isEmpty(otherDescribe)) {
            param.put("other_describe", otherDescribe);
        }
        param.put("tot_stock", stock);
        param.put("tot_remain", stock);
        if (limitCnt != -1) {
            param.put("today_stock", limitCnt);
            param.put("today_remain", limitCnt);
        }
        param.put("exchange_cid", communityId);
        CommunityGrpcClient cClient = new CommunityGrpcClient();
        String cNm = " ";
        if (communityId > 0) {
            CommunityProtos.PCommunityBaseList list = cClient.getCommunityBaseList(ResultProtos.PStringList.newBuilder().addList
                    (communityId + "").build());
            cNm = list.getList(0).getName();
        }
        param.put("exchange_nm", cNm);
        param.put("exchange_level", level);
        param.put("limit_typ", limitType);
        param.put("limit_times", times);
        if(tag != null) {
            param.put("tag", tag);
        }
        param.put("sale_status", saleStatus);
        param.put("status", 0);
        param.put("on_off_typ", onoffType);
        long now = DateUtils.getNowMins();
        if (onoffType == 0) {
            param.put("begin", now);
            param.put("end", now + _YEAR);
        } else {
            param.put("begin", DateUtils.getMins(begin));
            param.put("end", DateUtils.getMins(end));
        }
        param.put("upd_tm", now);
        param.put("upd_usr", uuid);
        param.put("serial_no", serialNo);
        return param;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean createGoods(String uuid, String name, Integer category, Integer price, String objkey, String describe, String otherDescribe,
                               int stock, int limitCnt, Integer communityId, Integer level, Integer tag, int limitType,
                               int times, int
                                       saleStatus, int onoffType, long begin, long end, Integer serialNo) {
        Map<String, Object> goodsMap = getGoodsMap(uuid, name, category, price, objkey, describe, otherDescribe,
                stock, limitCnt, communityId,
                level, tag, limitType, times, saleStatus, onoffType, begin, end, serialNo);
        goodsMap.put("crt_tm", System.currentTimeMillis());
        goodsMap.put("crt_usr", uuid);
        goodsMap.put("goods_no", IDUtils.getGoodsNO());
        Boolean ret = goodsInfDAO.existsSerialNo(null, serialNo);
        if (ret) {
            goodsInfDAO.updateSerialNo(serialNo);
        }
        return goodsInfDAO.insertGoods(goodsMap);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean updateGoods(Integer id, String uuid, String name, Integer category, Integer price, String objkey, String describe, String otherDescribe,
                               int stock, int limitCnt, Integer communityId, Integer level, Integer tag, int limitType,
                               int times, int
                                       saleStatus, int onoffType, long begin, long end, Integer serialNo) {
        Boolean ret = goodsInfDAO.existsSerialNo(id, serialNo);
        if (ret) {
            goodsInfDAO.updateSerialNo(serialNo);
        }
        Map<String, Object> param = getGoodsMap(uuid, name, category, price, objkey, describe, otherDescribe,
                stock, limitCnt,
                communityId,
                level, tag, limitType, times, saleStatus, onoffType, begin, end, serialNo);
        Map<String, Object> info = goodsInfDAO.goodsDetail(id);
        int saledCnt = MapUtils.getInt(info, "tot_saled");
        param.put("tot_remain", stock - saledCnt);
        if (limitCnt != -1) {
            param.put("today_stock", limitCnt);
            int todaySaled = MapUtils.getInt(info, "today_saled");
            param.put("today_remain", limitCnt - todaySaled);
        }

        return goodsInfDAO.updateGoods(param, id);
    }

    @Override
    public List<Map<String, Object>> goodsList(Long begin, Long end, Integer saleStatus, Integer category, String name, String sort) {
        Map<String, Object> conditions = new HashMap<>();
        if (saleStatus != null) {
            conditions.put("sale_status", saleStatus);
        }
        if (category != null) {
            conditions.put("category", category);
        }
        if (!StringUtils.isEmpty(name)) {
            conditions.put("nm", name);
        }
        if(begin != null){
            conditions.put("begin", begin);
        }
        if(end != null){
            conditions.put("end", end);
        }
        List<Map<String, Object>> ret = goodsInfDAO.goodsList(conditions, sort);
        if(ret != null && ret.size() > 0){
            ret.forEach(map -> {
                map.put("crt_tm", DateUtils.getDate17(new Date(MapUtils.getLong(map, "crt_tm"))));
                String cate = MapUtils.getInt(map, "category") == 0 ? "周边实物" : "虚拟物品";
                map.put("category", cate);
                String start = DateUtils.getDate17(new Date(MapUtils.getLong(map, "begin")));
                String off = DateUtils.getDate17(new Date(MapUtils.getLong(map, "end")));
                map.put("begin", start + "-" + off);
                String saleStatusStr = "待上架";
                Integer status = MapUtils.getInt(map, "sale_status");
                if(status == 1){
                    saleStatusStr = "出售中";
                }else if(status == 2){
                    saleStatusStr = "已下架";
                }
                map.put("sale_status", saleStatusStr);
            });
        }
        return ret;
    }

    @Override
    public Page<Goods> goodsList(Long begin, Long end, Integer saleStatus, Integer category, String name, int pageSize, int page, String
            sort) {
        Page<Goods> data = new Page<>();
        Map<String, Object> conditions = new HashMap<>();
        if (saleStatus != null) {
            conditions.put("sale_status", saleStatus);
        }
        if (category != null) {
            conditions.put("category", category);
        }
        if (!StringUtils.isEmpty(name)) {
            conditions.put("nm", name);
        }
        if(begin != null){
            conditions.put("begin", begin);
        }
        if(end != null){
            conditions.put("end", end);
        }
        Integer totCnt = goodsInfDAO.goodsRecordSize(conditions);
        conditions.put("limit", pageSize);
        conditions.put("start", (page - 1) * pageSize);
        Integer totPage = totCnt % pageSize != 0 ? totCnt / pageSize + 1 : totCnt / pageSize;
        data.setTotPage(totPage);
        data.setTotRecords(totCnt);
        List<Map<String, Object>> ret = goodsInfDAO.goodsList(conditions, sort);
        List<Goods> retList = new ArrayList<>();
        if (ret != null && ret.size() > 0) {
            for (Map<String, Object> map : ret) {
            	Goods g = new Goods();
            	g.setId(MapUtils.getInt(map, "id"));
            	g.setName(MapUtils.getString(map, "nm"));
            	g.setCategory(MapUtils.getInt(map, "category"));
            	g.setPrice(MapUtils.getInt(map, "price"));
            	String objKey = MapUtils.getString(map, "obj_key");
            	g.setObjKey(objKey);
            	OssProtos.PDownload urls = getURL(objKey);
                String url = "", thumb = "";
                if (urls != null) {
                    url = urls.getUrl();
                    thumb = urls.getThumb();
                }
            	g.setUrl(url);
            	g.setThumb(thumb);
            	g.setStock(MapUtils.getInt(map, "tot_stock"));
            	g.setDescribe(MapUtils.getString(map, "describe"));
            	g.setOtherDescribe(MapUtils.getString(map, "other_describe"));
            	g.setLimitCnt(MapUtils.getInt(map, "today_stock"));
            	g.setLevel(MapUtils.getInt(map, "exchange_level"));
            	g.setTag(MapUtils.getInt(map, "tag"));
            	g.setLimitType(MapUtils.getInt(map, "limit_typ"));
            	g.setTimes(MapUtils.getInt(map, "times"));
            	g.setSaleStatus(MapUtils.getInt(map, "sale_status"));
            	g.setOnoffType(MapUtils.getInt(map, "on_off_typ"));
            	g.setOnSaleTm(MapUtils.getLong(map, "begin"));
            	g.setOffSaleTm(MapUtils.getLong(map, "end"));
            	g.setSort(MapUtils.getInt(map, "serial_no"));
            	g.setCrtTm(MapUtils.getLong(map, "crt_tm"));
            	g.setGoodsNo(MapUtils.getString(map, "goods_no"));
            	g.setSalesCnt(MapUtils.getInt(map, "tot_saled"));
            	g.setStockPerDay(MapUtils.getInt(map, "today_stock"));
            	g.setExchangeNm(MapUtils.getString(map, "exchange_nm"));
            	g.setTotRemain(MapUtils.getInt(map, "tot_remain"));
            	g.setTodayRemain(MapUtils.getInt(map, "today_remain"));
                retList.add(g);
            }
        }
        data.setCurrentPage(page);
        data.setRecordPerPage(pageSize);
        data.setData(retList);
        return data;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean updateSerial(List<Integer> idList, List<Integer> serialList) {
        for (int idx = 0; idx < idList.size(); idx++) {
            Integer id = idList.get(idx);
            Integer serial = serialList.get(idx);
            Boolean ret = goodsInfDAO.existsSerialNo(id, serial);
            log.debug(String.format("id->%d,serial->%d,exists->", id, serial) + ret);
            if (ret) {
                goodsInfDAO.updateSerial2(id, serial);
            } else {
                goodsInfDAO.updateSerial(id, serial);
            }
        }
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean changeGoodsStatus(String uuid, Integer id, Integer saleStatus) {
        return goodsInfDAO.updateSaleStatus(id, saleStatus);
    }

    @Override
    public ResultProtos.PMessage goodsList(int category, int lastId) {
        List<Map<String, Object>> ret = goodsInfDAO.goodsList(category, lastId);
        MallProtos.PGoodsList.Builder listBuilder = MallProtos.PGoodsList.newBuilder();
        if (ret != null && !ret.isEmpty()) {
            List<MallProtos.PGoodsInf> list = new ArrayList<>();
            for (Map<String, Object> map : ret) {
                MallProtos.PGoodsInf inf = buildGoodsInf(map);
                list.add(inf);
            }
            listBuilder.addAllList(list);
        }
        return ReslutUtil.createReslutMessage(listBuilder.build());
    }

    private MallProtos.PGoodsInf buildGoodsInf(Map<String, Object> inf) {
        String objKey = MapUtils.getString(inf, "objkey");
        OssProtos.PDownload urls = getURL(objKey);
        String url = "", thumb = "";
        if (urls != null) {
            url = urls.getUrl();
            thumb = urls.getThumb();
        }
        MallProtos.POss pic = MallProtos.POss.newBuilder().setObjKey(objKey).setUrl(url).setThumb(thumb).build();
        MallProtos.POnOff onoff = MallProtos.POnOff.newBuilder().setType(MallProtos.POnOffType.valueOf(MapUtils.getInt(inf, "on_off_typ")))
                .setBegin(MapUtils.getLong(inf, "begin"))
                .setEnd(MapUtils.getLong(inf, "end")).build();
        MallProtos.PLimit limit = MallProtos.PLimit.newBuilder().setTimes(MapUtils.getInt(inf, "limit_times"))
                .setType(MallProtos.PLimitType.valueOf(MapUtils.getInt(inf, "limit_typ"))).build();
        MallProtos.PGoodsInf goodsInf = MallProtos.PGoodsInf.newBuilder().setCategoryValue(MapUtils.getInt(inf,
                "category")).setCrtTm(MapUtils.getLong(inf, "crt_tm")).setDescribe(MapUtils.getString(inf,
                "describe")).setOtherDescribe(MapUtils.getString(inf, "other_describe"))
                .setExchangeCommunityId(MapUtils.getInt(inf, "exchange_cid")).setExchangeLevel(MapUtils.getInt(inf, "exchange_level"))
                .setId(MapUtils.getInt(inf, "id")).setLimit(limit).setNm(MapUtils.getString(inf, "nm")).setOnoff(onoff)
                .setPic(pic).setPrice(MapUtils.getInt(inf, "price")).setTotRemain(MapUtils.getInt(inf, "tot_remain"))
                .setSaleStatus(MallProtos.PSaleStatus.valueOf(MapUtils.getInt(inf, "sale_status")))
                .setStatus(MapUtils.getInt(inf, "status")).setTotStock(MapUtils.getInt(inf, "tot_stock"))
                .setTodayRemain(MapUtils.getInt(inf, "today_remain")).setExchangeCommunityNm(MapUtils.getString(inf,
                        "exchange_nm")).setTodayStock(MapUtils.getInt(inf, "today_stock")).setShareLink(PropsUtils
                        .getString("share_link") + MapUtils.getInt(inf, "id")).build();
        int tag = MapUtils.getInt(inf, "tag");
        if (tag != -1) {
            goodsInf = goodsInf.toBuilder().setTag(MallProtos.PTag.valueOf(tag)).build();
        }
        return goodsInf;
    }

    @Override
    public ResultProtos.PMessage goodsDetail(int goodsId) {
        Map<String, Object> inf = goodsInfDAO.goodsDetail(goodsId);
        if (inf == null || inf.isEmpty()) {
            log.error("获取商品详情失败 goodsId->" + goodsId);
            throw new BizException(1001012, "");
        }
        return ReslutUtil.createReslutMessage(buildGoodsInf(inf));
    }

    @Override
    public ResultProtos.PMessage categoryStatics() {
        List<Map<String, Object>> list = goodsInfDAO.categoriesStatics();
        MallProtos.PCategoryStaticsList.Builder builder = MallProtos.PCategoryStaticsList.newBuilder();
        MallProtos.PCategoryStatics.Builder b1 = MallProtos.PCategoryStatics.newBuilder().setCategory(MallProtos.PGoodsCategory.REAL).setCount(0);
        MallProtos.PCategoryStatics.Builder b2 = MallProtos.PCategoryStatics.newBuilder().setCategory(MallProtos.PGoodsCategory.VIRTUAL).setCount(0);
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Integer category = MapUtils.getInt(map, "category");
                Integer count = MapUtils.getInt(map, "cnt");
                if (category == 0) {
                    b1.setCount(count);
                } else {
                    b2.setCount(count);
                }
            }
        }
        builder.addList(0, b1).addList(1, b2);
        return ReslutUtil.createReslutMessage(builder.build());
    }

    @Override
    public List<Integer> getAutoOnList() {
        return goodsInfDAO.getAutoOnList(System.currentTimeMillis());
    }

    @Override
    public List<Map<String, Object>> getNeedResetGoodsList() {
        return goodsInfDAO.getNeedResetGoodsList(DateUtils.getYYYYMMDD());
    }

    @Override
    public List<Integer> getAutoOffList() {
        return goodsInfDAO.getAutoOffList(System.currentTimeMillis());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean updateStock(Integer id, Integer todayStock) {
        return goodsInfDAO.updateStock(id, todayStock, DateUtils.getYYYYMMDD());
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

}
