package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧视觉资产类型的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaVisualAssetKindEnum implements ArrayValuable<Integer> {

    PROP(1, "道具"),
    SHOT_FRAME(2, "关键帧"),
    ARCHIVED_PROP(3, "历史道具"),
    ARCHIVED_SHOT_FRAME(4, "历史关键帧");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaVisualAssetKindEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
