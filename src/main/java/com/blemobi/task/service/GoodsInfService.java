package com.blemobi.task.service;

import com.blemobi.sep.probuf.ResultProtos;
import com.blemobi.task.model.Goods;
import com.blemobi.task.model.Page;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 13:46
 */
public interface GoodsInfService {
    Boolean createGoods(String uuid, String name, Integer category, Integer price, String objkey, String describe,
                        String otherDescribe,
                        int stock, int limitCnt, Integer communityId, Integer level, Integer tag, int limitType, int
                                times,
                        int
                                saleStatus, int onoffType, long begin, long end, Integer serialNo);

    Boolean updateGoods(Integer id, String uuid, String name, Integer category, Integer price, String objkey, String
            describe, String otherDescribe, int stock, int limitCnt, Integer communityId, Integer level, Integer tag, int limitType,
                        int times, int
                                saleStatus, int onoffType, long begin, long end, Integer serialNo);

    Page<Goods> goodsList(Long begin, Long end, Integer saleStatus, Integer category, String name, int pageSize, int
            page, String sort);

    List<Map<String, Object>> goodsList(Long begin, Long end, Integer saleStatus, Integer category, String name, String sort);

    Boolean changeGoodsStatus(String uuid, Integer id, Integer saleStatus);

    Boolean updateSerial(List<Integer> idList, List<Integer> serialList);

    ResultProtos.PMessage goodsList(int category, int lastId);

    ResultProtos.PMessage goodsDetail(int goodsId);

    ResultProtos.PMessage categoryStatics();

    List<Integer> getAutoOnList();

    List<Integer> getAutoOffList();

    List<Map<String, Object>> getNeedResetGoodsList();

    Boolean updateStock(Integer id, Integer todayStock);
}
