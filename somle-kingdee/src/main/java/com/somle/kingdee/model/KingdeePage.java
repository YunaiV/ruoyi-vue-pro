package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePage {
    private Integer count;
    private Integer currentPageSize;
    private Integer page;
    private Integer pageSize;
    private JsonNode rows;
    private Integer totalPage;

    public <T> List<T> getRowsList(Class<T> objectClass) {
        return JsonUtilsX.parseArray(rows, objectClass);
    }

    public int getTotal() {
        Integer rowCount = 1;
        rowCount = Objects.requireNonNullElse(totalPage, rowCount);
        return rowCount;
    }

    public boolean isLastPage() {
        return page == null || (page * pageSize) >= totalPage;
    }

    public boolean hasNext() {
        return page != null && (page * pageSize) < totalPage;
    }
}
