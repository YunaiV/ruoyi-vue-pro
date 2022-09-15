package cn.iocoder.yudao.module.CouponTemplete.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 优惠券 - 是否开启过期提醒
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum CouponExpireTimeTypeEnum {

    OPEN(1,"不开启"),
    CLOSE(0,"开启"),;

    /**
     * 是否开启过期提醒
     */
    private final Integer type;
    /**
     * 是否开启过期提醒
     */
    private final String name;

}
