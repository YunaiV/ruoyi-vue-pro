package com.somle.esb.enums.oms;

import lombok.Getter;

@Getter
public enum SalesPlatformEnum {

    AMAZON(true, "https://www.amazon.com/"),
    JINGDONG(false, null),
    SHOPIFY(true, "https://fitueyes.com"),
    BESTBUY(false, null),
    MANOMANO(false, null),
    MATOMO(false, null),
    OTTO(false, null),
    RAKUTEN(false, null),
    WALMART(false, null),
    WANGDIAN(false, null),
    WAYFAIR(false, null);

    private String siteURL;
    private boolean isSyncProfile;

    private SalesPlatformEnum(boolean isSyncProfile, String siteURL) {
        this.isSyncProfile = isSyncProfile;
        this.siteURL = siteURL;
    }

    /**
     * 是否匹配任意一个
     **/
    public boolean isAnyMatch(SalesPlatformEnum... platforms) {
        boolean matched = false;
        for (SalesPlatformEnum platform : platforms) {
            if (platform == this) {
                matched = true;
                break;
            }
        }
        return matched;
    }

}
