package com.somle.dingtalk.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
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
        return JsonUtilsX.parseArray(result, objectClass);
    }

    public <T> T getResult(Class<T> objectClass) {
        return JsonUtilsX.parseObject(result, objectClass);
    }

}
