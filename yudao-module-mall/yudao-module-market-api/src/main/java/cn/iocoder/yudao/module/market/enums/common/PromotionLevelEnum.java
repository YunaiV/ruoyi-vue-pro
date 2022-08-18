package cn.iocoder.yudao.module.market.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 营销的级别枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PromotionLevelEnum implements IntArrayValuable {

    ORDER(1, "订单级"),
    SKU(2, "商品级"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PromotionLevelEnum::getLevel).toArray();

    /**
     * 级别值
     */
    private final Integer level;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
