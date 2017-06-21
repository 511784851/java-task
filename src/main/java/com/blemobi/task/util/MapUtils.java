package com.blemobi.task.util;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Description:
 * User: HUNTER.POON
 * Date: 2017/6/7 09:37
 */
public final class MapUtils {
    private static Object getObj(Map<?,?> map, String key){
        return map.get(key);
    }

    public static String getString(Map<?, ?> map, String key){
        Object obj = getObj(map, key);
        return obj == null ? "" : obj.toString();
    }

    public static Integer getInt(Map<?, ?> map, String key){
        String str = getString(map, key);
        return StringUtils.isEmpty(str) ? null : Integer.parseInt(str);
    }

    public static Double getDouble(Map<?, ?> map, String key){
        String str = getString(map, key);
        return StringUtils.isEmpty(str) ? null : Double.parseDouble(str);
    }

    public static Float getFloat(Map<?, ?> map, String key){
        String str = getString(map, key);
        return StringUtils.isEmpty(str) ? null : Float.parseFloat(str);
    }

    public static Long getLong(Map<?, ?> map, String key){
        String str = getString(map, key);
        return StringUtils.isEmpty(str) ? null : Long.parseLong(str);
    }
}
