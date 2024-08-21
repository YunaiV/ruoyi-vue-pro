package com.somle.kingdee.model;

import java.util.List;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeResponse {
    private String data;
    private String description;
    private String errcode;

    public JSONObject getData() {
        return JSON.parseObject(data, JSONObject.class);
    }

    public <T> T getData(Class<T> objectClass) {
        return JSON.parseObject(data, objectClass);
    }

    public <T> List<T> getDataList(Class<T> objectClass) {
        return JSON.parseArray(data, objectClass);
    }
}
