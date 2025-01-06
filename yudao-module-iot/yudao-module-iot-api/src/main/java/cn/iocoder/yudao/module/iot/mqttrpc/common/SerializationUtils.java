package cn.iocoder.yudao.module.iot.mqttrpc.common;

import cn.hutool.json.JSONUtil;

/**
 * 序列化工具类
 *
 */
public class SerializationUtils {

    public static String serialize(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        return JSONUtil.toBean(json, clazz);
    }

}