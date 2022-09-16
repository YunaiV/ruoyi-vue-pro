package cn.iocoder.yudao.module.product.enums.spu;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品 SPU 状态
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ProductSpuStatusEnum implements IntArrayValuable {

    RECYCLE(-1, "回收站"),
    DISABLE(0, "下架"),
    ENABLE(1, "上架"),;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ProductSpuStatusEnum::getStyle).toArray();

    /**
     * 状态
     */
    private final Integer style;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
