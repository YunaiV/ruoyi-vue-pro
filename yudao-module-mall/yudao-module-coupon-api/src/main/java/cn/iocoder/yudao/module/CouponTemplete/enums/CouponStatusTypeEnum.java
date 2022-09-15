package cn.iocoder.yudao.module.CouponTemplete.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 优惠券 - 优惠券状态类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum CouponStatusTypeEnum {

    PROCESSING(1,"进行中"),
    END(2,"已结束"),
    CLOSE(3,"已关闭"),;

    /**
     * 优惠券类型
     */
    private final Integer type;
    /**
     * 优惠券类型名
     */
    private final String name;

}
