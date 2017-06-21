package com.blemobi.task.dao;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 11:20
 */
public interface GoodsInfDAO {
    /**
     * 新增商品信息
     *
     * @param param 商品信息
     * @return 添加结果
     */
    Boolean insertGoods(Map<String, Object> param);

    Boolean existsSerialNo(Integer id, int serialNo);

    Boolean updateSerialNo(int serialNo);
    /**
     * 更新商品信息
     *
     * @param fields 更新字段
     * @param id     条件
     * @return 更新结果
     */
    Boolean updateGoods(Map<String, Object> fields, Integer id);

    /**
     * 商品列表
     *
     * @param conditions 查询条件
     * @return 商品列表
     */
    List<Map<String, Object>> goodsList(Map<String, Object> conditions, String sort);

    /**
     * 商品列表总条数
     *
     * @param conditions
     * @return
     */
    Integer goodsRecordSize(Map<String, Object> conditions);

    /**
     * 商品列表
     *
     * @param category 分类
     * @param lastId   最后一条ID
     * @return
     */
    List<Map<String, Object>> goodsList(Integer category, Integer lastId);

    /**
     * 商品详情
     *
     * @param id 商品主键
     * @return 商品详情
     */
    Map<String, Object> goodsDetail(Integer id);

    /**
     * 更新库存
     *
     * @param sql sql
     * @param param 参数列表
     * @return 更新结果
     */
    Boolean updateStock(String sql, List<Object> param);

    /**
     * 更新上下架状态
     *
     * @param Id     商品ID
     * @param status 上下架状态
     * @return 更新结果
     */
    Boolean updateSaleStatus(Integer Id, Integer status);

    Boolean updateSerial(Integer id, Integer serial);
    Boolean updateSerial2(Integer id, Integer serial);

    /**
     * 类别商品统计
     *
     * @return
     */
    List<Map<String, Object>> categoriesStatics();

    /**
     * 获取自动上架商品列表
     * @param currentTm 当前时间
     * @return
     */
    List<Integer> getAutoOnList(long currentTm);

    /**
     * 获取自动下架商品列表
     * @param currentTm 当前时间
     * @return
     */
    List<Integer> getAutoOffList(long currentTm);

    List<Map<String, Object>> getNeedResetGoodsList(String resetDate);

    public Boolean updateStock(Integer id, Integer todayStock, String resetDate);
}
