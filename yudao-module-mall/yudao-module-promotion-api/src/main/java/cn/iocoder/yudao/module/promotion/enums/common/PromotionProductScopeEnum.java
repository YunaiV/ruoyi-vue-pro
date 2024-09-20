package cn.iocoder.yudao.module.promotion.enums.common;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 营销的商品范围枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PromotionProductScopeEnum implements IntArrayValuable {

    ALL(1, "全部商品"),
    SPU(2, "指定商品"),
    CATEGORY(3, "指定品类");

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

    public static boolean isAll(Integer scope) {
        return Objects.equals(scope, ALL.scope);
    }

    public static boolean isSpu(Integer scope) {
        return Objects.equals(scope, SPU.scope);
    }

    public static boolean isCategory(Integer scope) {
        return Objects.equals(scope, CATEGORY.scope);
    }

}
