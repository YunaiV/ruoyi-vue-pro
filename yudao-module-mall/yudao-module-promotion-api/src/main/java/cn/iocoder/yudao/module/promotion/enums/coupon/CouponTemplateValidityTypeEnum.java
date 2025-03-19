package cn.iocoder.yudao.module.promotion.enums.coupon;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 优惠劵模板的有限期类型的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum CouponTemplateValidityTypeEnum implements ArrayValuable<Integer> {

    DATE(1, "固定日期"),
    TERM(2, "领取之后"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CouponTemplateValidityTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 值
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
