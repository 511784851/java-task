package com.blemobi.task.util;

import com.blemobi.library.redis.RedisManager;
import com.blemobi.task.exception.BizException;
import com.blemobi.tools.DateUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Jedis;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/12 12:01
 */
@Log4j
public final class IDUtils {
    private static final String _GOODS_PREFIX = "BBS";
    private static final String _LOCK_KEY = "GOODS_NO_LOCK";
    private static final int _MONTH = 31 * 24 * 60 * 60;
    /*
        订单号（15位）=下单渠道（1位）+支付渠道（1位）+业务类型（1位）+时间信息（4位）+UUID（后4位）+随机码（4位）
        下单渠道：iOS下单：0；安卓下单：1；H5下单：2
        支付渠道：金币支付0；支付宝支付：1；微信支付：2；银联支付：3
        业务类型：虚拟兑换 0；实物兑换：1；虚拟购买：2；实物购买：3
        时间信息：“月份+日”
         */
    public static String genOrdId(int ordChann, int payChann, int bizTyp, String uuid){
        String randomStr = RandomStringUtils.randomNumeric(4);
        String uuid4 = uuid.substring(uuid.length() - 4);
        String ordId = String.format("%d%d%d%s%s%s", ordChann, payChann, bizTyp, DateUtils.getMMDDHHMM(), uuid4, randomStr);
        return ordId;
    }

    public static String getGoodsNO() {
        Jedis jedis = RedisManager.getRedis();
        try {
            String goodsNo = _GOODS_PREFIX + DateUtils.getYYMM();
            String key = _GOODS_PREFIX + DateUtils.getMM();
            Integer no = 1;
            jedis.setnx(_LOCK_KEY, "lock");
            jedis.expire(_LOCK_KEY, 2);
            if (jedis.exists(key)) {
                no = Integer.parseInt(jedis.get(key)) + 1;
            }
            jedis.incrBy(key, 1);
            jedis.expire(key, _MONTH);
            return goodsNo + StringUtils.leftPad(no + "", 4, "0");
        } catch (Exception ex) {
            log.error("getGoodsNO", ex);
            throw new BizException(1, "getGoodsNO");
        } finally {
            jedis.del(_LOCK_KEY);
            RedisManager.returnResource(jedis);
        }
    }
    public static void main(String[] args) {
        System.out.printf(genOrdId(0,0,0,"242634254r2342"));
    }
}
