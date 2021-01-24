package cn.iocoder.dashboard.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * JSON 工具类
 *
 * @author 芋道源码
 */
public class JSONUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    // TODO 芋艿，需要将 Spring 的设置下进来
    public static void setObjectMapper(ObjectMapper objectMapper) {
        JSONUtils.objectMapper = objectMapper;
    }

    public static String toJSONString(Object object) {
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

}
