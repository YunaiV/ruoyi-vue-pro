package cn.iocoder.yudao.module.product.enums.favorite;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 喜爱商品类型 枚举
 *
 * @author jason
 */
@Getter
@AllArgsConstructor
public enum ProductFavoriteTypeEnum implements IntArrayValuable {
    COLLECT(1,"收藏"),
    THUMBS_UP(2, "点赞");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ProductFavoriteTypeEnum::getType).toArray();
    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String desc;

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
