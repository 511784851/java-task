/******************************************************************
 *
 *    
 *    Package:     com.blemobi.payment.util
 *
 *    Filename:    PropHandler.java
 *
 *    Description: TODO
 *
 *    @author:     HUNTER.POON
 *
 *    @version:    1.0.0
 *
 *    Create at:   2017年3月13日 下午7:04:27
 *
 *    Revision:
 *
 *    2017年3月13日 下午7:04:27
 *
 *****************************************************************/
package com.blemobi.task.config;

import com.blemobi.library.consul_v1.PropsUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * @ClassName PropHandler
 * @Description TODO
 * @author HUNTER.POON
 * @Date 2017年3月13日 下午7:04:27
 * @version 1.0.0
 */
@Log4j
public class PropertyConfigurer extends PropertyPlaceholderConfigurer {

	@Override
	protected String convertProperty(String propertyName, String propertyValue) {
		String kvVal = PropsUtils.getString(propertyName);
		if (!StringUtils.isEmpty(kvVal)) {
			log.info("----properties key:" + propertyName + ",OrgVal: " + propertyValue + ", KvVal:" + kvVal);
		}
		return StringUtils.isEmpty(kvVal) ? propertyValue : kvVal;
	}
}
