package cn.iocoder.yudao.framework.common.util.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.*;
import java.util.stream.StreamSupport;

/**
 * 索迈自定义的Json工具类
 **/
@Slf4j
public class JsonUtilsX {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); //忽略未列出的property
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 忽略 null 值 (不然VO的序列化很麻烦）
        objectMapper.registerModules(new JavaTimeModule()); // 解决 LocalDateTime 的序列化
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static JsonNodeFactory getNodeFactory() {
        return objectMapper.getNodeFactory();
    }

    public static JSONObject newObject() {
        return new JSONObject();
    }

    // convert pojo to string
    @SneakyThrows
    public static String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    // convert json to string
    @SneakyThrows
    public static String toJsonString(JSONObject jsonObject) {
        return jsonObject.toString();
    }

    //convert pojo to json
    public static JSONObject toJSONObject(Object object) {
        return new JSONObject(objectMapper.valueToTree(object));
    }



    @SneakyThrows
    public static Map<String, String> toStringMap(JSONObject json) {
        Map<String, String> map = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> fields = json.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            map.put(field.getKey(), field.getValue().asText());
        }
        return map;
    }

    public static Map<String, String> toStringMap(Object pojo) {
        return toStringMap(toJSONObject(pojo));
    }


    public static MultiValuedMap<String, String> toMultiStringMap(JSONObject json) {
        MultiValuedMap<String, String> multiMap = new ArrayListValuedHashMap<>();

        json.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (value.isArray()) {
                value.forEach(arrayElement -> multiMap.put(key, arrayElement.asText()));
            } else {
                multiMap.put(key, value.asText());
            }
        });

        return multiMap;
    }

    public static MultiValuedMap<String, String> toMultiStringMap(Object pojo) {
        return toMultiStringMap(toJSONObject(pojo));
    }

    //convert text to JsonNode
    @SneakyThrows
    public static JsonNode parseJson(String text) {
        return objectMapper.readTree(text);
    }

    //convert json node to pojo
    @SneakyThrows
    public static <T> T parseObject(JsonNode node, Class<T> clazz) {
        if (clazz == JSONObject.class) {
            return parseObject(node.toString(), clazz);
        } else {
            return objectMapper.treeToValue(node, clazz);
        }
    }

    //convert json to pojo
    @SneakyThrows
    public static <T> T parseObject(JSONObject jsonObject, Class<T> clazz) {
        return parseObject((ObjectNode) jsonObject, clazz);
    }

    //convert string to pojo
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

    @SneakyThrows
    public static <T> List<T> parseArray(JsonNode node, Class<T> clazz) {
        if (clazz == JSONArray.class) {
            return parseArray(node.toString(), clazz);
        } else {
            return  objectMapper.readerForListOf(clazz).readValue(node);
        }

    }

    @SneakyThrows
    public static <T> List<T> parseArray(JSONArray jsonArray, Class<T> clazz) {
        return parseArray((ArrayNode) jsonArray, clazz);
    }


    @SneakyThrows
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(text, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
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
