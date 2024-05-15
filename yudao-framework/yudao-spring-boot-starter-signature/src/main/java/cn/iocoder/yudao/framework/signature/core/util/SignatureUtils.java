package cn.iocoder.yudao.framework.signature.core.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class SignatureUtils {

    @SuppressWarnings("unchecked")
    public static TreeMap<String, Object> traverseMap(Map<String, Object> map) {
        TreeMap<String, Object> result = new TreeMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                result.put(key, traverseMap((Map<String, Object>) value));
            } else if (value instanceof List) {
                result.put(key, traverseList((List<Object>) value));
            } else {
                result.put(key, value);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> traverseList(List<Object> list) {
        List<Object> result = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof Map) {
                result.add(traverseMap((Map<String, Object>) obj));
            } else if (obj instanceof List) {
                result.add(traverseList((List<Object>) obj));
            } else {
                result.add(obj);
            }
        }

        return result;
    }

    /**********************************JsonNode************************************/

    public static TreeMap<String, Object> traverseMap(JsonNode node) {
        TreeMap<String, Object> result = new TreeMap<>();

        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = entry.getKey();
            if (entry.getValue().isObject()) {
                result.put(key, traverseMap(entry.getValue()));
            } else if (entry.getValue().isArray()) {
                result.put(key, traverseList(entry.getValue()));
            } else {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    public static List<Object> traverseList(JsonNode node) {
        List<Object> result = new ArrayList<>();

        for (JsonNode childNode : node) {
            if (childNode.isObject()) {
                result.add(traverseMap(childNode));
            } else if (childNode.isArray()) {
                result.add(traverseList(childNode));
            } else {
                result.add(childNode);
            }
        }
        return result;
    }
}

