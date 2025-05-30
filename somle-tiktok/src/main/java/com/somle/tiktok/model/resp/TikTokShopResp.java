package com.somle.tiktok.model.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TikTokShopResp {
    private int code;
    private String message;
    private ResponseData data;
    private String requestId;

    @Data
    @NoArgsConstructor
    public static class ResponseData {
        private List<Shop> shops;
    }

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Shop {
        private String cipher;
        private String code;
        private String id;

        private String name;
        private String region;
        private String sellerType;
    }
}