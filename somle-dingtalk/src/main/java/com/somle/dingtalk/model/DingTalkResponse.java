package com.somle.dingtalk.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import lombok.Data;
import java.util.List;


@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DingTalkResponse {
    private int errcode;
    private String errmsg;
    private JsonNode result;
    private String requestId;

    public <T> List<T> getResultList(Class<T> objectClass) {
        return JsonUtils.parseArray(result, objectClass);
    }

    public <T> T getResult(Class<T> objectClass) {
        return JsonUtils.parseObject(result, objectClass);
    }
}
