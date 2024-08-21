package com.somle.eccang.model;

import lombok.Data;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONType;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EccangResponse {
    private String code;
    private String message;
    private long timestamp;
    private String version;
    private String nonceStr;
    private String signType;
    private String sign;
    private String bizContent;

    public BizContent getBizContent() {
        return JSON.parseObject(bizContent, BizContent.class);
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BizContent {
        private Integer total;
        private Integer totalCount;
        private JSONArray data;
        private Integer page;
        private Integer pageSize;

        public <T> List<T> getData(Class<T> objectClass) {
            return data.toJavaList(objectClass);
        }

        public int getTotal() {
            Integer rowCount = 1;
            rowCount = Objects.requireNonNullElse(total, rowCount);
            rowCount = Objects.requireNonNullElse(totalCount, rowCount);
            return rowCount;
        }

        public boolean isLastPage() {
            return page == null || (page * pageSize) >= total;
        }

        public boolean hasNext() {
            return page != null && (page * pageSize) < total;
        }

        // public int getPage() {
        //     return Integer.valueOf(page);
        // }

        // public int getPageSize() {
        //     return Integer.valueOf(pageSize);
        // }
        
        //custom
        // private JSONObject headers;
    }
}