package com.somle.framework.common.util.json;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

// if use inner property to store node, then ObjectMapper.writeValueAsString(JSONObject) will be wrong
public class JSONObject extends ObjectNode{
//    public JSONObject(JsonNodeFactory nc) {
//        super(nc);
//    }
//
//    public JSONObject(JsonNodeFactory nc, ObjectNode other) {
//        super(nc);
//        this.setAll(other);
//    }

    public JSONObject() {
        super(JsonUtils.getNodeFactory());
    }

    public JSONObject(ObjectNode other) {
        super(JsonUtils.getNodeFactory());
        this.setAll(other);
    }


    public List<Map.Entry<String, JsonNode>> entrySet() {
        return _children.entrySet().stream().toList();
    }

    public void put(String fieldName, Object v) {
        this.putPOJO(fieldName, v);
    }

    public String getString(String fieldName) {
        return this.get(fieldName).asText();
    }

    public Integer getInteger(String fieldName) {
        return this.get(fieldName).asInt();
    }

    public JSONArray getJSONArray(String fieldName) {
        return new JSONArray((ArrayNode) this.get(fieldName));
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
