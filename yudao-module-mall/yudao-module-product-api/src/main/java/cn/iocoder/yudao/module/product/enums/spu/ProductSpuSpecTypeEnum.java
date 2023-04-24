package cn.iocoder.yudao.module.product.enums.spu;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品 SPU 规格类型
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
@Deprecated
public enum ProductSpuSpecTypeEnum implements IntArrayValuable {

    RECYCLE(false, "统一规格"),
    DISABLE(true, "多规格");

    //public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ProductSpuSpecTypeEnum::getType).toArray();// TODO 暂时先这样跑起来
    public static final int[] ARRAYS = {};
    /**
     * 规格类型
     */
    private final Boolean type;
    /**
     * 规格名称
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
