package cn.iocoder.yudao.framework.common.util.json;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// if use inner property to store node, then ObjectMapper.writeValueAsString(JSONObject) will be wrong
public class JSONObject extends ObjectNode {
//    public JSONObject(JsonNodeFactory nc) {
//        super(nc);
//    }
//
//    public JSONObject(JsonNodeFactory nc, ObjectNode other) {
//        super(nc);
//        this.setAll(other);
//    }

    public JSONObject() {
        super(JsonUtilsX.getNodeFactory());
    }

    public JSONObject(ObjectNode other) {
        super(JsonUtilsX.getNodeFactory());
        this.setAll(other);
    }

    public JSONObject(JsonNode other) {
        super(JsonUtilsX.getNodeFactory());
        other.fields().forEachRemaining(entry -> this.set(entry.getKey(), entry.getValue()));
    }


    public List<Map.Entry<String, JsonNode>> entrySet() {
        return _children.entrySet().stream().toList();
    }

    public void put(String fieldName, Object v) {
        this.putPOJO(fieldName, v);
    }

    public String getString(String fieldName) {
        var value = this.get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof NullNode) {
            return null;
        }
        return value.asText();
    }

    public BigDecimal getBigDecimal(String fieldName) {
        var value = this.get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof NullNode) {
            return null;
        }
        return new BigDecimal(value.asText());
    }

    public List<String> getStringList(String fieldName) {
        return StreamSupport.stream(this.get(fieldName).spliterator(), false)
            .filter(JsonNode::isTextual) // Ensure the element is a text node
            .map(JsonNode::asText) // Extract text value
            .collect(Collectors.toList()); // Collect into a list
    }


    public List<Integer> getIntegerList(String fieldName) {
        return StreamSupport.stream(this.get(fieldName).spliterator(), false)
            .filter(JsonNode::isInt) // Ensure the element is a text node
            .map(JsonNode::asInt) // Extract text value
            .collect(Collectors.toList()); // Collect into a list
    }

    public Integer getInteger(String fieldName) {
        var value = this.get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof NullNode) {
            return null;
        }
        return value == null ? null : value.asInt();
    }

    public Long getLong(String fieldName) {
        var value = this.get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof NullNode) {
            return null;
        }
        return value == null ? null : value.asLong();
    }

    public JSONArray getJSONArray(String fieldName) {
        JsonNode value = this.get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof NullNode) {
            return null;
        }
        return new JSONArray((ArrayNode) value);
    }

    public JSONObject getJSONObject(String fieldName) {
        JsonNode value = this.get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof NullNode) {
            return null;
        }
        return new JSONObject((ObjectNode) value);
    }


    private void test(String fieldName) {
        this.get(1);
    }
//    public JSONObject(ObjectNode node) {
//        this.setAll(node);
//    }
//    public ObjectNode node;

//    public JSONObject() {
//        this.node = JsonUtils.newNode();
//    }
//
//    public JSONObject(ObjectNode node) {
//        this.node = node;
//    }
//
//    public void put(String fieldName, String v) {
//        node.put(fieldName, v);
//    }
//
//
//    public void put(String fieldName, Object v) {
//        node.putPOJO(fieldName, v);
//    }
//
//    public List<Map.Entry<String, JsonNode>> entrySet() {
//        return ListUtil.toList(node.fields());
//    }
}
