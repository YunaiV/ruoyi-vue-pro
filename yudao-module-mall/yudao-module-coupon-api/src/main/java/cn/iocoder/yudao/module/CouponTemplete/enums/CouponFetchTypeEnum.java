package cn.iocoder.yudao.module.CouponTemplete.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 优惠券 - 领取是否无限制 0
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum CouponFetchTypeEnum {

    LIMIT(1,"限制"),
    NOT_LIMIT(0,"不限制"),;

    /**
     * 是否开启过期提醒
     */
    private final Integer type;
    /**
     * 是否开启过期提醒
     */
    private final String name;

}
