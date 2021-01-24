package cn.iocoder.dashboard.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

/**
 * JSON 工具类
 *
 * @author 芋道源码
 */
public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化 objectMapper 属性
     *
     * 通过这样的方式，使用 Spring 创建的 ObjectMapper Bean
     *
     * @param objectMapper ObjectMapper 对象
     */
    public static void init(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        try {
            return objectMapper.readValue(text, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object parseObject(String text, TypeReference<Set<Long>> typeReference) {
        try {
            return objectMapper.readValue(text, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
