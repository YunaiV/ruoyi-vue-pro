package com.somle.amazon.controller.vo;


import lombok.Data;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/30$
 */
@Data
public class AmazonSpMarketplaceParticipationVO {
    private AmazonSpMarketplaceVO marketplace;
    private String storeName;
    private Participation participation;

    @Data
    public static class Participation {
        private boolean isParticipating;
        private boolean hasSuspendedListings;
    }
}


