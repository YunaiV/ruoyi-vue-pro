package com.somle.dingtalk.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.List;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DingTalkResponse {
    private int errcode;
    private String errmsg;
    private String result;
    private String requestId;

    public <T> List<T> getResultList(Class<T> objectClass) {
        return JSON.parseArray(result, objectClass);
    }

    public <T> T getResult(Class<T> objectClass) {
        return JSON.parseObject(result, objectClass);
    }
}
