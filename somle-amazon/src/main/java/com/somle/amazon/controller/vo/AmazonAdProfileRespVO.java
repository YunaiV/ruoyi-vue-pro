package com.somle.amazon.controller.vo;

import lombok.Data;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/25$
 */

@Data
public class AmazonAdProfileRespVO {
    private Long profileId;
    private String countryCode;
    private String currencyCode;
    private Double dailyBudget;
    private String timezone;
    private AccountInfo accountInfo;

    @Data
    public static class AccountInfo {
        private String marketplaceStringId;
        private String id;
        private String type;
        private String name;
        private boolean validPaymentMethod;
        private String subType;
    }
}
