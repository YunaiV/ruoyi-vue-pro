package com.somle.eccang.model;

import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@ToString
public class EccangResponse {
    private String code;
    private String message;
    private long timestamp;
    private String version;
    private String nonceStr;
    private String signType;
    private String sign;
    private String bizContent;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EccangError {
        private String errorMsg;
        private String errorCode;
    }

    public String getBizContentString() {
        return bizContent;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EccangPage {
        private Integer total;
        private Integer totalCount;
        private JsonNode data;
        private Integer page;
        private Integer pageSize;

        public <T> List<T> getData(Class<T> objectClass) {
            return JsonUtilsX.parseArray(data,objectClass);
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


    public <T> T getBizContent(Class<T> objectClass) {
        return JsonUtilsX.parseObject(bizContent, objectClass);
    }

    public <T> List<T> getBizContentList(Class<T> objectClass) {
        return JsonUtilsX.parseArray(bizContent, objectClass);
    }
}