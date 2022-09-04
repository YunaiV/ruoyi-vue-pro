package cn.iocoder.yudao.module.CouponTemplete.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 优惠券 - 优惠券商品使用类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum CouponGoodsTypeEnum {

    ALL(1,"全部商品可用"),
    POINT_PRODUCT(2,"指定商品可用"),
    POINT_PRODUCT_NOT(3,"指定商品不可用"),;

    /**
     * 优惠券商品使用类型
     */
    private final Integer type;
    /**
     * 优惠券商品使用类型名
     */
    private final String name;

}
