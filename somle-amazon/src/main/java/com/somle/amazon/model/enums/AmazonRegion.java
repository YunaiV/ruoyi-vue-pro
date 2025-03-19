package com.somle.amazon.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/25$
 */
@AllArgsConstructor
@Getter
public enum AmazonRegion {
    EU("EU","https://advertising-api-eu.amazon.com", "https://sellingpartnerapi-eu.amazon.com"),
    FE("FE","https://advertising-api-fe.amazon.com", "https://sellingpartnerapi-fe.amazon.com"),
    NA("NA","https://advertising-api.amazon.com", "https://sellingpartnerapi-na.amazon.com");

    private final String code;
    private final String adUrl;
    private final String spUrl;

    public static AmazonRegion findByCode(String code) {
        return Arrays.stream(values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
    }
}


