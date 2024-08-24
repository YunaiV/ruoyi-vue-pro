package com.somle.framework.common.util.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.somle.framework.common.util.json.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //忽略未列出的property
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 忽略 null 值 (不然VO的序列化很麻烦）
        objectMapper.registerModules(new JavaTimeModule()); // 解决 LocalDateTime 的序列化
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

//    /**
//     * 初始化 objectMapper 属性
//     * <p>
//     * 通过这样的方式，使用 Spring 创建的 ObjectMapper Bean
//     *
//     * @param objectMapper ObjectMapper 对象
//     */
//    public static void init(ObjectMapper objectMapper) {
//        JsonUtils.objectMapper = objectMapper;
//    }
    public static JsonNodeFactory getNodeFactory() {
        return objectMapper.getNodeFactory();
    }

    public static JSONObject newObject() {
        return new JSONObject();
    }




//    public static ObjectNode newObject() {
//        return objectMapper.createObjectNode();
//    }

    @SneakyThrows
    public static String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    public static String toJsonString(JSONObject jsonObject) {
        return jsonObject.toString();
    }


    public static JSONObject toJSONObject(Object object) {
        return new JSONObject(objectMapper.valueToTree(object));
    }

    @SneakyThrows
    public static <T> T parseObject(JsonNode node, Class<T> clazz) {
        return objectMapper.treeToValue(node, clazz);
    }


    @SneakyThrows
    public static <T> List<T> parseArray(JsonNode node, Class<T> clazz) {
        return  objectMapper.readerForListOf(clazz).readValue(node);
    }

    @SneakyThrows
    public static <T> List<T> parseArray(JSONArray jsonArray, Class<T> clazz) {
        return parseArray((ArrayNode) jsonArray, clazz);
    }

    @SneakyThrows
    public static <T> T parseObject(JSONObject jsonObject, Class<T> clazz) {
        return parseObject((ObjectNode) jsonObject, clazz);
    }

    @SneakyThrows
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        if (clazz == JSONObject.class) {
            return clazz.getConstructor(ObjectNode.class).newInstance(JsonUtils.parseObject(text, ObjectNode.class));
        } else if (clazz == JSONArray.class) {
            return clazz.getConstructor(ArrayNode.class).newInstance(JsonUtils.parseObject(text, ArrayNode.class));
        } else {
            return objectMapper.readValue(text, clazz);
        }
    }



    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("json parse err,json:{}", text, e);
            throw new RuntimeException(e);
        }
    }

//    /**
//     * 将字符串解析成指定类型的对象
//     * 使用 {@link #parseObject(String, Class)} 时，在@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) 的场景下，
//     * 如果 text 没有 class 属性，则会报错。此时，使用这个方法，可以解决。
//     *
//     * @param text 字符串
//     * @param clazz 类型
//     * @return 对象
//     */
//    public static <T> T parseObject2(String text, Class<T> clazz) {
//        if (StrUtil.isEmpty(text)) {
//            return null;
//        }
//        return JSONUtil.toBean(text, clazz);
//    }

}
