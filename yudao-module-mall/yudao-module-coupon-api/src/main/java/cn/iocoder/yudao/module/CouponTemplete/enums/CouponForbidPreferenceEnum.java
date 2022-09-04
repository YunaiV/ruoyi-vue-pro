package cn.iocoder.yudao.module.CouponTemplete.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 优惠券 - 优惠叠加类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum CouponForbidPreferenceEnum {

    UN_FORBID(0,"不限制"),
    FORBID(1,"优惠券仅原价购买商品时可用");

    /**
     * 优惠券类型
     */
    private final Integer type;
    /**
     * 优惠券类型名
     */
    private final String name;

}
