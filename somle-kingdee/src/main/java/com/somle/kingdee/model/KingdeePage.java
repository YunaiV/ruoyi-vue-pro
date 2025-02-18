package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePage {
    private Integer count;
    private Integer currentPageSize;
    private Integer page;
    private Integer pageSize;
    private JsonNode rows;
    private Integer totalPage;

//    public <T> List<T> getRows(Class<T> objectClass) {
//        return JsonUtilsSomle.parseArray(rows.toString(), objectClass);
//    }

    public <T> List<T> getRowsList(Class<T> objectClass) {
        return JsonUtils.parseArray(rows, objectClass);
    }
}
