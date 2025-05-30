package com.somle.lazada.model.reps;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LazadaRefreshTokenResp {
    // 主类字段
    private String accessToken;
    private String country;
    private String refreshToken;
    private String accountId;
    private String code;
    private List<CountryUserInfo> countryUserInfoList;
    private String accountPlatform;
    private String refreshExpiresIn;
    private String expiresIn;
    private String requestId;
    private String account;

    // 嵌套对象定义
    @Data
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CountryUserInfo {
        private String country;
        private String userId;
        private String sellerId;
        private String shortCode;
    }
}