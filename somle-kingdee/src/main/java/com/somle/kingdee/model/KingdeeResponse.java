package com.somle.kingdee.model;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeResponse {
    private JsonNode data;
    private String description;
    private String errcode;


    public <T> T getData(Class<T> objectClass) {
        return JsonUtils.parseObject(data, objectClass);
    }

    public <T> List<T> getDataList(Class<T> objectClass) {
        return JsonUtils.parseArray(data, objectClass);
    }
}