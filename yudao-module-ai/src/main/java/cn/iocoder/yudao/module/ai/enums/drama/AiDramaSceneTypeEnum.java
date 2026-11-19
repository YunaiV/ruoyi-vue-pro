package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧分镜场景类型的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaSceneTypeEnum implements ArrayValuable<Integer> {

    DAILY(1, "日常"),
    EMOTION(2, "情感"),
    ACTION(3, "动作"),
    EPIC(4, "史诗"),
    SUSPENSE(5, "悬疑");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaSceneTypeEnum::getType).toArray(Integer[]::new);

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
