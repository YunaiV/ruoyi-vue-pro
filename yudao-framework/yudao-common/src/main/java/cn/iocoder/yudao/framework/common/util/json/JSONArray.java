package cn.iocoder.yudao.framework.common.util.json;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

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
        super(JsonUtilsX.getNodeFactory());
    }


    public JSONArray(ArrayNode other) {
        super(JsonUtilsX.getNodeFactory());
        this.addAll(other);
    }

    public Stream<JsonNode> stream() {
        return  StreamSupport.stream(this.spliterator(), false);
    }

    private void test() {
        this.get(0);
    }
}
