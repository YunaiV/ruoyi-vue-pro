package com.somle.amazon.controller.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/25$
 */

@Data
public class AmazonAdAccountRespVO {
    private List<AdsAccount> adsAccounts;

    @Data
    public static class AdsAccount {
        private String accountName;
        private String adsAccountId;
        private List<AlternateId> alternateIds;
        private List<String> countryCodes;
        private String status;
    }

    @Data
    public static class AlternateId {
        private String countryCode;
        private Long profileId;
        private String entityId;
    }
}
