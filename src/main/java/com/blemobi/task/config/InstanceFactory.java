/******************************************************************
 *
 *    
 *    Package:     com.blemobi.payment.util
 *
 *    Filename:    IstanceFactory.java
 *
 *    Description: TODO
 *
 *    @author:     HUNTER.POON
 *
 *    @version:    1.0.0
 *
 *    Create at:   2017年2月18日 下午4:20:57
 *
 *    Revision:
 *
 *    2017年2月18日 下午4:20:57
 *
 *****************************************************************/
package com.blemobi.task.config;

import lombok.extern.log4j.Log4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @ClassName IstanceFactory
 * @Description 实例工厂
 * @author HUNTER.POON
 * @Date 2017年2月18日 下午4:20:57
 * @version 1.0.0
 */
@Log4j
public final class InstanceFactory {
    private static ApplicationContext context;
    private InstanceFactory(){}
    public static <T> T getInstance(String beanName){
        if(context == null){
            initContext();
        }
        return (T)context.getBean(beanName);
    }
    
    public static <T> T getInstance(Class<T> clz){
        if(context == null){
            initContext();
        }
        return context.getBean(clz);
    }
    
    private synchronized static void initContext(){
        if(context != null){
            return;
        }
        log.debug("init spring context.");
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }
}
