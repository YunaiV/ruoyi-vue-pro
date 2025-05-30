package com.somle.esb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlatformEnum {

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
    WAYFAIR(false, null),
    CDISCOUNT(true, null),
    TIKTOK(true, null),
    AUTONOMOUS(true, null),
    LAZADA(true, null),
    DSV(true, null),
    HOME24(true, null),
    SHOPEE(true, "https://www.shopee.com.my/");


    private boolean isSyncProfile;
    private String siteURL;

    /**
     * 是否匹配任意一个
     **/
    public boolean isAnyMatch(PlatformEnum... platforms) {
        boolean matched = false;
        for (PlatformEnum platform : platforms) {
            if (platform == this) {
                matched = true;
                break;
            }
        }
        return matched;
    }

}