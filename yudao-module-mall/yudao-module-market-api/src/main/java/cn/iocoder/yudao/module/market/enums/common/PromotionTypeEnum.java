package cn.iocoder.yudao.module.market.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 营销类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PromotionTypeEnum implements IntArrayValuable {

    DISCOUNT(1, "限时折扣"),
    REWARD(2, "满减送"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PromotionTypeEnum::getType).toArray();

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
