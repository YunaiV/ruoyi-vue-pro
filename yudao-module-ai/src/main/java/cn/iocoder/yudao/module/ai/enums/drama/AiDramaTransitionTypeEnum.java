package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧成片转场类型的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaTransitionTypeEnum implements ArrayValuable<Integer> {

    NONE(0, "无", null),
    DISSOLVE(1, "溶解", "dissolve"),
    FADE(2, "黑场淡入淡出", "fadeblack"),
    SLIDE(3, "滑动", "slideleft");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaTransitionTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;
    /**
     * FFmpeg xfade 的 transition 参数；无转场时为空
     */
    private final String xfade;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static AiDramaTransitionTypeEnum of(Integer type) {
        return Arrays.stream(values()).filter(item -> item.getType().equals(type)).findFirst().orElse(NONE);
    }

}
