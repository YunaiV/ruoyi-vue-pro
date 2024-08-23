package com.somle.framework.common.util.json;

import cn.hutool.core.collection.ListUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JSONArray extends ArrayNode {
//    public JSONArray(JsonNodeFactory nc) {
//        super(nc);
//    }
//
//    public JSONArray(JsonNodeFactory nc, ArrayNode other) {
//        super(nc);
//        this.addAll(other);
//    }

    public JSONArray() {
        super(JsonUtils.getNodeFactory());
    }


    public JSONArray(ArrayNode other) {
        super(JsonUtils.getNodeFactory());
        this.addAll(other);
    }

    public Stream<JsonNode> stream() {
        return  StreamSupport.stream(this.spliterator(), false);
    }

    private void test() {
        this.get(0);
    }
}
