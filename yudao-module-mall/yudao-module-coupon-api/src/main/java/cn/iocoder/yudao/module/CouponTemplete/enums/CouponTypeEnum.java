package cn.iocoder.yudao.module.CouponTemplete.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 优惠券 - 优惠券类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum CouponTypeEnum {

    REWARD(1,"满减"),
    DISCOUNT(2,"折扣"),
    RANDOW(3,"随机"),;

    /**
     * 优惠券类型
     */
    private final Integer type;
    /**
     * 优惠券类型名
     */
    private final String name;

}
