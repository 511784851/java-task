package com.blemobi.task.bat;

import com.blemobi.task.config.InstanceFactory;
import com.blemobi.task.service.GoodsInfService;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/13 09:43
 */
@Log4j
public class OnOffJob implements Job {
    private GoodsInfService goodsInfService = InstanceFactory.getInstance(GoodsInfService.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("开始扫描需要上架的商品列表");
        List<Integer> onList = goodsInfService.getAutoOnList();
        log.debug("需要上架的商品列表->[" + StringUtils.join(onList, ",") + "]");
        log.debug("开始扫描需要下架的商品列表");
        List<Integer> offList = goodsInfService.getAutoOffList();
        log.debug("需要下架的商品列表->[" + StringUtils.join(offList, ",") + "]");
        onGoods(onList);
        offGoods(offList);
    }

    private void onGoods(List<Integer> list){
        list.forEach(id ->{
            log.debug("商品->【" + id + "】上架");
            goodsInfService.changeGoodsStatus(null, id, 1);
        });
    }

    private void offGoods(List<Integer> list){
        list.forEach(id -> {
            log.debug("商品->【" + id + "】下架");
            goodsInfService.changeGoodsStatus(null, id, 2);
        });
    }
}
