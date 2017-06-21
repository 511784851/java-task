package com.blemobi.task.bat;

import com.blemobi.task.config.InstanceFactory;
import com.blemobi.task.service.GoodsInfService;
import com.blemobi.task.util.MapUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/13 10:58
 */
@Log4j
public class ResetStockJob implements Job {
    private GoodsInfService goodsInfService = InstanceFactory.getInstance(GoodsInfService.class);
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        List<Map<String, Object>> list = goodsInfService.getNeedResetGoodsList();
        log.debug("需要每日重置限购数量的商品列表【" + StringUtils.join(list, ",") + "】");
        list.forEach(map -> {
            Integer id = MapUtils.getInt(map, "id");
            Integer totRemain = MapUtils.getInt(map, "tot_remain");
            Integer todayStock = MapUtils.getInt(map, "today_stock");
            if(totRemain != -1){
                todayStock = totRemain > todayStock ? todayStock : totRemain;
            }
            log.debug("重置商品->" + id + "库存为->" + todayStock);
            goodsInfService.updateStock(id, todayStock);
        });
    }
}
