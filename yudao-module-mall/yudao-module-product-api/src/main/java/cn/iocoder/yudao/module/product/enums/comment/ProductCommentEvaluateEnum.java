package cn.iocoder.yudao.module.product.enums.comment;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品评论的评价枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ProductCommentEvaluateEnum implements IntArrayValuable {

    GOOD(1, "好评"),
    BAD(2, "差评"),
    MIDDLE(2, "中评"),;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ProductCommentEvaluateEnum::getEvaluate).toArray();

    /**
     * 评价
     */
    private final Integer evaluate;
    /**
     * 评价名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
