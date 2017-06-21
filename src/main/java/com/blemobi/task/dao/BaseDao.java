package com.blemobi.task.dao;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 13:20
 */
public abstract class BaseDao extends JdbcTemplate {

    @Resource
    public void setDs(DataSource ds) {
        super.setDataSource(ds);
    }
}
