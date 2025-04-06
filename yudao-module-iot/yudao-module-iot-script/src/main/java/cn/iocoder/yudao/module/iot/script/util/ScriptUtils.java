package cn.iocoder.yudao.module.iot.script.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 脚本工具类，提供给脚本执行环境使用的工具方法
 */
@Slf4j
public class ScriptUtils {

    /**
     * 单例实例
     */
    private static final ScriptUtils INSTANCE = new ScriptUtils();

    /**
     * 获取单例实例
     *
     * @return 工具类实例
     */
    public static ScriptUtils getInstance() {
        return INSTANCE;
    }

    private ScriptUtils() {
        // 私有构造函数
    }

    /**
     * 字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空
     */
    public boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不为空
     */
    public boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    /**
     * 将对象转为 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串
     */
    public String toJson(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }

    /**
     * 将 JSON 字符串转为 Map
     *
     * @param json JSON 字符串
     * @return Map 对象
     */
    public Map<String, Object> parseJson(String json) {
        if (StrUtil.isEmpty(json)) {
            return MapUtil.newHashMap();
        }
        try {
            return JSONUtil.toBean(json, Map.class);
        } catch (Exception e) {
            log.error("JSON 解析失败: {}", json, e);
            return MapUtil.newHashMap();
        }
    }

    /**
     * 类型转换
     *
     * @param value 值
     * @param type  目标类型
     * @param <T>   泛型
     * @return 转换后的值
     */
    public <T> T convert(Object value, Class<T> type) {
        return Convert.convert(type, value);
    }

    /**
     * 从 Map 中获取值
     *
     * @param map Map 对象
     * @param key 键
     * @return 值
     */
    public Object get(Map<String, Object> map, String key) {
        return MapUtil.get(map, key, Object.class);
    }

    /**
     * 从 Map 中获取字符串
     *
     * @param map Map 对象
     * @param key 键
     * @return 字符串值
     */
    public String getString(Map<String, Object> map, String key) {
        return MapUtil.getStr(map, key);
    }

    /**
     * 从 Map 中获取整数
     *
     * @param map Map 对象
     * @param key 键
     * @return 整数值
     */
    public Integer getInt(Map<String, Object> map, String key) {
        return MapUtil.getInt(map, key);
    }

    /**
     * 从 Map 中获取布尔值
     *
     * @param map Map 对象
     * @param key 键
     * @return 布尔值
     */
    public Boolean getBool(Map<String, Object> map, String key) {
        return MapUtil.getBool(map, key);
    }

    /**
     * 从 Map 中获取双精度浮点数
     *
     * @param map Map 对象
     * @param key 键
     * @return 双精度浮点数值
     */
    public Double getDouble(Map<String, Object> map, String key) {
        return MapUtil.getDouble(map, key);
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 时间戳
     */
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}