package com.somle.amazon.controller.vo;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AmazonSpOrderRespVO {
    private OrderList payload;
    private String errors;

    @Data
    @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
    public static class OrderList {
        private JsonNode orders;
        private String nextToken;
        private LocalDateTime lastUpdatedBefore;
        private LocalDateTime createdBefore;
    }
}
