package cn.iocoder.yudao.module.promotion.enums.coupon;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 优惠劵状态枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum CouponStatusEnum implements ArrayValuable<Integer> {

    UNUSED(1, "未使用"),
    USED(2, "已使用"),
    EXPIRE(3, "已过期");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CouponStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 值
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
