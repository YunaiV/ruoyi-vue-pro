package cn.iocoder.dashboard.util.json;

import com.alibaba.fastjson.JSON;

/**
 * JSON 工具类
 *
 * @author 芋道源码
 */
public class JSONUtils {

    public static String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes, clazz);
    }

}
