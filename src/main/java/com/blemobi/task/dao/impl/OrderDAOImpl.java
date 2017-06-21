package com.blemobi.task.dao.impl;

import com.blemobi.task.dao.BaseDao;
import com.blemobi.task.dao.OrderDAO;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 13:48
 */
@Log4j
@Repository("orderDAO")
public class OrderDAOImpl extends BaseDao implements OrderDAO {
    private final static String _ORDER_FIELDS = "g.nm, g.goods_no, g.price, g.obj_key, o.id, o.g_id, o.g_type, o" +
            ".status,o.crt_tm, o.express,o.mobile,o.phone,o.contact,o.addr,o.email,o.qq,o.remark,o.op_remark,o.opor,o.op_tm";

    @Override
    public Boolean insertOrder(Map<String, Object> fields) {
        StringBuilder sql = new StringBuilder("INSERT INTO t_order_inf(");
        StringBuilder paramSQL = new StringBuilder(" VALUES(");
        List<Object> param = new ArrayList<>();
        fields.forEach((k, v) -> {
            sql.append(k).append(",");
            paramSQL.append("?,");
            param.add(v);
        });
        sql.setLength(sql.length() - 1);
        paramSQL.setLength(paramSQL.length() - 1);
        sql.append(")").append(paramSQL).append(")");
        log.debug("SQL->" + sql.toString() + ",VALUE->" + StringUtils.join(param, ","));
        return this.update(sql.toString(), param.toArray()) > 0;
    }

    @Override
    public Boolean updateExpressInf(Map<String, Object> fields, String ordId) {
        StringBuilder sql = new StringBuilder("UPDATE t_order_inf SET ");
        List<Object> values = new ArrayList<>();
        fields.forEach((k, v) -> {
            sql.append(k).append(" = ?,");
            values.add(v);
        });
        sql.setLength(sql.length() - 1);
        sql.append(" WHERE id = ?");
        values.add(ordId);
        log.debug("SQL->" + sql.toString() + ", VALUE->" + StringUtils.join(values, ","));
        return this.update(sql.toString(), values.toArray()) > 0;
    }

    @Override
    public List<Map<String, Object>> ordList(String sql, List<Object> param) {
        log.debug("SQL->" + sql + ", VALUES->" + StringUtils.join(param, ","));
        return this.queryForList(sql, param.toArray());
    }

    @Override
    public Integer ordListSize(String sql, List<Object> param) {
        log.debug("SQL->" + sql + ", VALUES->" + StringUtils.join(param, ","));
        return this.queryForObject(sql, Integer.class, param.toArray());
    }

    @Override
    public Integer getBuyCount(String uuid, int gId, long start, long end) {
        String sql = "SELECT COUNT(1) cnt FROM t_order_inf WHERE g_id = ? AND buyer = ? AND crt_tm >= ? AND crt_tm <= ?";
        return this.queryForObject(sql, Integer.class, gId, uuid, start, end);
    }

    @Override
    public Map<String, Object> ordDetail(String ordId) {
        String sql = "SELECT " + _ORDER_FIELDS + " FROM t_order_inf o JOIN " +
                "t_goods_inf g ON o.g_id=g.id WHERE o.id = ?";
        log.debug("SQL->" + sql + ", VALUES->" + ordId);
        return this.queryForMap(sql, ordId);
    }

}
