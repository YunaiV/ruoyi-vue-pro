package com.somle.amazon.controller.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/25$
 */
@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AmazonAuthReqVO {
    private String grantType;
    // expires in 5 minutes
    private String code;
    private String refreshToken;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;
}
