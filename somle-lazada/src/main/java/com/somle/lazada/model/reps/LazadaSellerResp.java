package com.somle.lazada.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LazadaSellerResp {
    private String code;
    private ResponseData data;
    private String requestId;

    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ResponseData {
        private String nameCompany;
        private String logoUrl;
        private String name;
        private String verified;
        private String location;
        private String marketplaceEaseMode;
        private String sellerId;
        private String email;
        private String shortCode;
        private String cb;
        private String status;
    }
}