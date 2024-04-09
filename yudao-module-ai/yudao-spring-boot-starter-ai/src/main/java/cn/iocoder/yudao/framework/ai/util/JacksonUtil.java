package cn.iocoder.yudao.framework.ai.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.io.IOException;

// TODO @fansili：看看能不能用 JsonUtils
/**
 * Jackson工具类
 *
 * author: fansili
 * time: 2024/3/17 10:13
 */
public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化 ObjectMapper 以美化输出（即格式化JSON内容）
     */
    static {
        // 美化输出（缩进）
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 忽略值为 null 的属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 配置一个模块来将 Long 类型转换为 String 类型
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        objectMapper.registerModule(module);
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 需要序列化的Java对象
     * @return 序列化后的 JSON 字符串
     * @throws JsonProcessingException 当 JSON 序列化过程中出现错误时抛出异常
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型 Class 对象
     * @param <T>   泛型类型参数
     * @return 反序列化后的 Java 对象
     * @throws IOException 当 JSON 解析过程中出现错误时抛出异常
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将对象转换为格式化的 JSON 字符串（已启用 INDENT_OUTPUT 功能，所以所有方法都会返回格式化后的 JSON）
     *
     * @param obj 需要序列化的Java对象
     * @return 格式化后的 JSON 字符串
     * @throws JsonProcessingException 当 JSON 序列化过程中出现错误时抛出异常
     */
    public static String toFormattedJson(Object obj) {
        // 已在类初始化时设置了 SerializationFeature.INDENT_OUTPUT，此处无需额外操作
        return toJson(obj);
    }
}
