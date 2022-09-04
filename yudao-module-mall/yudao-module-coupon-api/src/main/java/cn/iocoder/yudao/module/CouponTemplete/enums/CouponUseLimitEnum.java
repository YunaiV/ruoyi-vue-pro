package cn.iocoder.yudao.module.CouponTemplete.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 优惠券使用类型 - 优惠券使用类型类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum CouponUseLimitEnum {

    HAS_LIMIT(1,"无门槛"),
    NO_LIMIT(2,"有门槛"),;

    /**
     * 优惠券使用类型
     */
    private final Integer type;
    /**
     * 优惠券使用类型名
     */
    private final String name;

}
