package cn.iocoder.yudao.module.market.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 营销的商品范围枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PromotionProductScopeEnum implements IntArrayValuable {

    ALL(1, "全部商品参与"),
    SPU(2, "指定商品参与"),
    ;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(PromotionProductScopeEnum::getScope).toArray();

    /**
     * 范围值
     */
    private final Integer scope;
    /**
     * 范围名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
