package com.somle.framework.common.util.collection;

import com.somle.framework.common.util.string.StrUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
* @Author Wqh
* @Description 提供与集合相关的工具方法
**/
public class MapUtils {
    private MapUtils(){}


    /**
     * 将给定映射中的键从蛇形命名法或其他格式转换为驼峰命名法。
     *
     * @param <V>      映射中值的类型
     * @param resource 包含需要转换为驼峰命名法的键的原始映射
     * @return 一个新映射，其中键已转换为驼峰命名法，值保持不变
     */
    public static <V> Map<String,V> keyConvertToCamelCase(Map<String,V> resource) {
        Map<String, V> camelCaseMap = new LinkedHashMap<>();
        for (Map.Entry<String, V> entry : resource.entrySet()) {
            String key = StrUtils.toCamelCase(entry.getKey());
            camelCaseMap.put(key, entry.getValue());
        }
        return camelCaseMap;
    }
}
