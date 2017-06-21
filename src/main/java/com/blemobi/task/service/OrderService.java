package com.blemobi.task.service;

import com.blemobi.sep.probuf.ResultProtos;
import com.blemobi.task.model.Order;
import com.blemobi.task.model.Page;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/5 13:46
 */
public interface OrderService {
    Page<Order> ordList(int begin, int end, int status, String type, String val, int pageSize, int page);

    ResultProtos.PMessage ordList(String uuid, int goodsType, long time);

    ResultProtos.PMessage exchageGoods(String uuid, int id, String contact, String address, String email, String qqNO,
                                       String phone, String remark, int ordChann, int payChann, int bizType);

    ResultProtos.PMessage getAddr(String uuid);

    ResultProtos.PMessage getGold(String uuid);

    ResultProtos.PMessage getContacts();

    Boolean addExpress(String uuid, String ordId, String expressInf, String describe, boolean changeStatus);

}
