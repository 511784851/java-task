package com.blemobi.task.dao.impl;

import com.blemobi.task.dao.AddrInfDAO;
import com.blemobi.task.dao.BaseDao;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/8 14:13
 */
@Log4j
@Repository("addrInfDAOImpl")
public class AddrInfDAOImpl extends BaseDao implements AddrInfDAO {
    @Override
    public Map<String, Object> getAddr(String uuid) {
        String sql = "SELECT contact, addr, email, qq, phone FROM t_addr_inf WHERE uuid = ?";
        log.debug("SQL->" + sql + ", VALUE->" + uuid);
        List<Map<String, Object>> list = this.queryForList(sql, uuid);
        if(list == null || list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    @Override
    public Boolean addAddr(Map<String, Object> param) {
        StringBuilder sql = new StringBuilder("INSERT INTO t_addr_inf (");
        StringBuilder paramSQL = new StringBuilder(" VALUES(");
        List<Object> list = new ArrayList<>();
        param.forEach((k, v) -> {
            sql.append(k).append(",");
            paramSQL.append("?,");
            list.add(v);
        });
        sql.setLength(sql.length() - 1);
        paramSQL.setLength(paramSQL.length() - 1);
        sql.append(")").append(paramSQL).append(")");
        log.debug("SQL->" + sql.toString() + ", VALUE->" + StringUtils.join(list, ","));
        return this.update(sql.toString(), list.toArray()) > 0;
    }

    @Override
    public Boolean editAddr(Map<String, Object> param, String uuid) {
        StringBuilder sql = new StringBuilder("UPDATE t_addr_inf SET ");
        List<Object> values = new ArrayList<>();
        param.forEach((k, v) -> {
            sql.append(k).append("= ? ,");
            values.add(v);
        });
        sql.setLength(sql.length() - 1);
        sql.append(" WHERE uuid = ?");
        values.add(uuid);
        log.debug("SQL->" + sql.toString() + ", VALUE->" + StringUtils.join(values, ","));
        return this.update(sql.toString(), values.toArray()) > 0;
    }
}
