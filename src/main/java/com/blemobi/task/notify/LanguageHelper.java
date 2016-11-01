package com.blemobi.task.notify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import com.blemobi.task.util.Constant;

public class LanguageHelper {
	private static Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();

	/*
	 * 第一次加载此类的时候缓存多语言模版数据到内存中
	 */
	static {
		try {
			// 读取多语言模版数据配置文件
			Configuration config = new XMLConfiguration(
					LanguageHelper.class.getResource(""));
			List<?> types = config.getList("languages.language.type");
			List<?> zh_cns = config.getList("languages.language.zh_cn");
			List<?> zh_tws = config.getList("languages.language.zh_tw");
			List<?> en_uss = config.getList("languages.language.en_us");
			List<?> ko_krs = config.getList("languages.language.ko_kr");

			for (int i = 0; i < types.size(); i++) {
				Map<String, String> languageMap = new HashMap<String, String>();
				languageMap.put("zh_cn", zh_cns.get(i).toString());// 中文简体
				languageMap.put("zh_tw", zh_tws.get(i).toString());// 中文繁体
				languageMap.put("en_us", en_uss.get(i).toString());// 英文
				languageMap.put("ko_kr", ko_krs.get(i).toString());// 韩文
				map.put(types.get(i).toString(), languageMap);
			}
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * 获取对应语言文本
	 */
	public static String getContent(TypeEnum type, String language, String... params) {
		Map<String, String> languageMap = map.get(type.toString());
		String content = languageMap.get(language);

		for (int i = 0; i < params.length; i++) {
			content.replace("[" + i + "]", params[i]);// 替换[x]内容
		}
		return content;
	}
}