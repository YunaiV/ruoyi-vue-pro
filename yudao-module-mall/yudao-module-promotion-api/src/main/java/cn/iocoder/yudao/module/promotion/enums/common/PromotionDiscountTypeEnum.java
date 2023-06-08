package cn.iocoder.yudao.module.promotion.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 优惠类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PromotionDiscountTypeEnum implements IntArrayValuable {

    PRICE(1, "满减"), // 具体金额
    PERCENT(2, "折扣"), // 百分比
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PromotionDiscountTypeEnum::getType).toArray();

    /**
     * 优惠类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
