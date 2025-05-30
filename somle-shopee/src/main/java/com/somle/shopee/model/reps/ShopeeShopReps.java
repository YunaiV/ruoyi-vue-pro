package com.somle.shopee.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ShopeeShopReps {
    // 基础字段
    private String error;
    private String message;
    private String requestId;
    private Long authTime;        // 时间戳类型建议用Long
    private Long expireTime;
    private String shopName;
    private String region;
    private String status;
    private String shopFulfillmentFlag;

    // 布尔标识字段
    private boolean isCb;
    private boolean isUpgradedCbsc;
    private boolean isSip;
    private boolean isMainShop;
    private boolean isDirectShop;

    // 嵌套对象字段
    private Long merchantId;
    private List<Object> sipAffiShops;
    private List<LinkedDirectShop> linkedDirectShopList;
    private Long linkedMainShopId;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class LinkedDirectShop {
        private Long directShopId;
        private String directShopRegion;
    }
}