package com.somle.tiktok.model.resp;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TikTokAuthResp {

    private int code;
    private String message;

    private DataObject data;

    private String requestId;

    @NoArgsConstructor
    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DataObject {

        private String accessToken;


        private long accessTokenExpireIn;


        private String refreshToken;


        private long refreshTokenExpireIn;


        private String openId;


        private String sellerName;


        private String sellerBaseRegion;


        private int userType;

        private List<String> grantedScopes;
    }
}