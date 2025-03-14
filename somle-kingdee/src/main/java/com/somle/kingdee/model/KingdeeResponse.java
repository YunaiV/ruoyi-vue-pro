package com.somle.kingdee.model;

import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class KingdeeResponse {
    private JsonNode data;
    private String description;
    private String errcode;


    public <T> T getData(Class<T> objectClass) {
        return JsonUtilsX.parseObject(data, objectClass);
    }

    public <T> List<T> getDataList(Class<T> objectClass) {
        return JsonUtilsX.parseArray(data, objectClass);
    }
}