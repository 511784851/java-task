package com.blemobi.task.dao.impl;

import com.blemobi.task.dao.BaseDao;
import com.blemobi.task.dao.GoodsInfDAO;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 13:17
 */
@Log4j
@Repository("goodsInfDAO")
public class GoodsInfDAOImpl extends BaseDao implements GoodsInfDAO {
    private static final String _GOODS_FIELDS = "id,category,nm,price,obj_key,`describe`,other_describe,tot_stock,tot_saled,tot_remain," +
            "today_stock,today_saled,today_remain,exchange_cid,exchange_level,limit_typ,limit_times,serial_no," +
            "goods_no,tag,sale_status,status,on_off_typ,`begin`,`end`,crt_tm,crt_usr,upd_tm,upd_usr";
    @Override
    public Boolean updateSerialNo(int serialNo) {
        String sql = "UPDATE t_goods_inf SET serial_no = serial_no + 1 WHERE serial_no >= ?";
        return this.update(sql, serialNo) > 1;
    }

    @Override
    public Boolean existsSerialNo(Integer id, int serialNo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) cnt FROM t_goods_inf WHERE serial_no = ? ");
        List<Object> param = new ArrayList<>();
        param.add(serialNo);
        if(id != null){
            sql.append("AND id <> ?");
            param.add(id);
        }
        return this.queryForObject(sql.toString(), param.toArray(), Integer.class) > 0;
    }

    /**
     * 新增商品信息
     *
     * @param param 商品信息
     * @return 添加结果
     */
    @Override
    public Boolean insertGoods(Map<String, Object> param) {
        StringBuilder sql = new StringBuilder("INSERT INTO t_goods_inf (");
        StringBuilder pStr = new StringBuilder();
        List<Object> values = new ArrayList<>();
        param.forEach((k, v) ->{
            sql.append("`").append(k).append("`,");
            pStr.append("?,");
            values.add(v);
        });
        sql.setLength(sql.length() - 1);
        pStr.setLength(pStr.length() - 1);
        sql.append(") VALUES(").append(pStr).append(")");
        log.debug("SQL -> " + sql.toString() + ", param-> " + StringUtils.join(values, ","));
        return this.update(sql.toString(), values.toArray())  == 1;
    }

    /**
     * 更新商品信息
     *
     * @param fields 更新字段
     * @param id     条件
     * @return 更新结果
     */
    @Override
    public Boolean updateGoods(Map<String, Object> fields, Integer id) {
        StringBuilder sql = new StringBuilder("UPDATE t_goods_inf SET ");
        List<Object> values = new ArrayList<>();
        fields.forEach((k, v) ->{
            sql.append("`").append(k).append("` = ?,");
            values.add(v);
        });
        sql.setLength(sql.length() - 1);
        sql.append(" WHERE id = ? ");
        values.add(id);
        log.debug("SQL -> " + sql.toString() + ", param-> " + StringUtils.join(values, ","));
        return this.update(sql.toString(), values.toArray())  == 1;
    }

    @Override
    public Boolean updateStock(Integer id, Integer todayStock, String resetDate) {
        String sql = "UPDATE t_goods_inf SET today_stock = ?, today_saled = 0, today_remain = ?, reset_date = ? WHERE" +
                " id = ?";
        return this.update(sql, todayStock, todayStock, resetDate, id) > 0;
    }

    /**
     * 商品列表
     *
     * @param conditions 查询条件
     * @return 商品列表
     */
    @Override
    public List<Map<String, Object>> goodsList(Map<String, Object> conditions, String sort) {
        Map<String, Object> data = buildListSQL(conditions);
        String sql = data.get("SQL").toString();
        List<Object> param = (List<Object>)data.get("DATA");
        String fields = _GOODS_FIELDS;
        sql = sql.replace("{0}", fields);
        if(StringUtils.isEmpty(sort)) {
            sql += " ORDER BY crt_tm DESC";
        }else{
            sql += " ORDER BY serial_no " + sort;
        }
        if(conditions.containsKey("start") && conditions.containsKey("limit")) {
            param.add(conditions.get("start"));
            param.add(conditions.get("limit"));
            sql += " LIMIT ?, ?";
        }
        log.debug("SQL -> " + sql + ", PARAM -> " + StringUtils.join(param, ","));
        return this.queryForList(sql, param.toArray());
    }

    private Map<String, Object> buildListSQL(Map<String, Object> map){
        StringBuilder sql = new StringBuilder("SELECT {0} FROM t_goods_inf WHERE 1 = 1 ");
        List<Object> param = new ArrayList<>();
        map.forEach((k, v) -> {
            if("limit,start".indexOf(k) > -1){
                return;
            }
            if(k.equals("nm")){
                param.add( "%" + v + "%");
                sql.append("AND ").append(k).append(" LIKE ? ");
            }else if(k.equals("begin")){
                sql.append("AND crt_tm >= ? ");
                param.add(v);
            }else if(k.equals("end")){
                sql.append("AND crt_tm <= ? ");
                param.add(v);
            }else{
                sql.append("AND ").append(k).append(" = ? ");
                param.add(v);
            }
        });
        Map<String, Object> ret = new HashMap<>();
        ret.put("SQL", sql);
        ret.put("DATA", param);
        return  ret;
    }
    @Override
    public Integer goodsRecordSize(Map<String, Object> conditions) {
        Map<String, Object> data = buildListSQL(conditions);
        String sql = data.get("SQL").toString();
        List<Object> param = (List<Object>)data.get("DATA");
        sql = sql.replace("{0}", "COUNT(1) AS CNT");
        log.debug("SQL -> " + sql + ", PARAM -> " + StringUtils.join(param, ","));
        return this.queryForObject(sql, param.toArray(), Integer.class);
    }

    @Override
    public List<Map<String, Object>> goodsList(Integer category, Integer lastId) {
        String sql = " SELECT " + _GOODS_FIELDS + " FROM t_goods_inf WHERE category = ? AND serial_no > ? ORDER " +
                "BY serial_no ASC LIMIT 20";
        return this.queryForList(sql, category, lastId);
    }

    /**
     * 商品详情
     *
     * @param id 商品主键
     * @return 商品详情
     */
    @Override
    public Map<String, Object> goodsDetail(Integer id) {
        String sql = "SELECT " + _GOODS_FIELDS + " FROM t_goods_inf WHERE id = ?";
        return this.queryForMap(sql, id);
    }

    /**
     * 更新库存
     *
     * @param sql sql
     * @param param 参数列表
     * @return 更新结果
     */
    @Override
    public Boolean updateStock(String sql, List<Object> param) {
        log.debug("SQL->" + sql + ", VALUE->" + StringUtils.join(param, ","));
        return this.update(sql, param.toArray()) > 0;
    }

    @Override
    public Boolean updateSerial2(Integer id, Integer serial) {
        String sql1= "UPDATE t_goods_inf SET serial_no = serial_no + 1 WHERE serial_no >= ?";
        this.update(sql1, serial);
        String sql = "UPDATE t_goods_inf SET serial_no = ? where id = ? ";
        return this.update(sql, serial, id) > 0;
    }

    @Override
    public Boolean updateSerial(Integer id, Integer serial) {
        String sql = "UPDATE t_goods_inf SET serial_no = ? where id = ? ";
        return this.update(sql, serial, id) > 0;
    }

    /**
     * 更新上下架状态
     *
     * @param id     商品ID
     * @param status 上下架状态
     * @return 更新结果
     */
    @Override
    public Boolean updateSaleStatus(Integer id, Integer status) {
        String sql = "UPDATE t_goods_inf SET sale_status = ? where id = ? ";
        return this.update(sql, status, id) > 0;
    }

    @Override
    public List<Map<String, Object>> categoriesStatics() {
        return null;
    }

    @Override
    public List<Integer> getAutoOnList(long currentTm) {
        String sql = "SELECT id FROM t_goods_inf WHERE on_off_typ = 1 AND sale_status = 0 AND begin < ? AND end > ?";
        return this.queryForList(sql, new Object[]{currentTm,currentTm}, Integer.class);
    }

    @Override
    public List<Integer> getAutoOffList(long currentTm) {
        String sql = "SELECT id FROM t_goods_inf WHERE sale_status IN(0,1) AND end < ?";
        return this.queryForList(sql, new Object[]{currentTm}, Integer.class);
    }

    @Override
    public List<Map<String, Object>> getNeedResetGoodsList(String resetDate) {
        String sql = "SELECT id, tot_remain, today_stock FROM t_goods_inf WHERE sale_status = 1 AND status <> 2 AND " +
                "today_stock <> -1 AND (tot_remain > 0 OR tot_remain = -1) AND reset_date <> ?";
        return this.queryForList(sql, resetDate);
    }
}
