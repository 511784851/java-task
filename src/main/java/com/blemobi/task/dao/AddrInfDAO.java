package com.blemobi.task.dao;

import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/8 14:12
 */
public interface AddrInfDAO {

    public Map<String, Object> getAddr(String uuid);

    public Boolean addAddr(Map<String, Object> param);

    public Boolean editAddr(Map<String, Object> param, String uuid);
}
