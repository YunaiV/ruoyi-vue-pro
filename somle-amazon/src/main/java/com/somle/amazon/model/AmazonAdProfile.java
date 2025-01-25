package com.somle.amazon.model;

import lombok.Data;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/25$
 */

@Data
public class AmazonAdProfile {
    private long profileId;
    private String countryCode;
    private String currencyCode;
    private double dailyBudget;
    private String timezone;
    private AccountInfo accountInfo;

    @Data
    public static class AccountInfo {
        private String marketplaceStringId;
        private String id;
        private String type;
        private String name;
        private boolean validPaymentMethod;
    }
}
