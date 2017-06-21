package com.blemobi.task.dao;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 11:28
 */
public interface OrderDAO {
    /**
     * 新增订单
     * @param fields 订单数据
     * @return 新增结果
     */
    Boolean insertOrder(Map<String, Object> fields);

    /**
     * 更新快递信息
     * @param fields 快递内容
     * @param ordId 订单号
     * @return 更新结果
     */
    Boolean updateExpressInf(Map<String, Object> fields, String ordId);

    /**
     * 查询订单列表
     * @param sql
     * @param param
     * @return 订单列表
     */
    List<Map<String, Object>> ordList(String sql, List<Object> param);


    /**
     * 查询订单列表大小
     * @param sql
     * @param param
     * @return
     */
    Integer ordListSize(String sql, List<Object> param);

    /**
     * 订单详情
     * @param ordId 订单ID
     * @return
     */
    Map<String, Object> ordDetail(String ordId);

    Integer getBuyCount(String uuid, int gId, long start, long end);

}
