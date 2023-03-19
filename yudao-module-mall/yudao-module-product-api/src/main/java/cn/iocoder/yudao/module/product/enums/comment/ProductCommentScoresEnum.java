package cn.iocoder.yudao.module.product.enums.comment;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品评论的星级枚举
 *
 * @author wangzhs
 */
@Getter
@AllArgsConstructor
public enum ProductCommentScoresEnum implements IntArrayValuable {

    ONE(1, "1星"),
    TWO(2, "2星"),
    THREE(3, "3星"),
    FOUR(4, "4星"),
    FIVE(5, "5星");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(ProductCommentScoresEnum::getScores).toArray();

    /**
     * 星级
     */
    private final Integer scores;

    /**
     * 星级名
     */
    private final String name;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
