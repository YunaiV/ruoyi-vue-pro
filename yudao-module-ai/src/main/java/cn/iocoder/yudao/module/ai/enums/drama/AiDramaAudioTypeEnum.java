package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧音频类型的枚举
 *
 * 角色样音的「选用」没有单独字段，用类型区分：同一角色最多一条 {@link #SAMPLE_SELECTED}，其余为 {@link #SAMPLE}
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaAudioTypeEnum implements ArrayValuable<Integer> {

    SAMPLE(1, "角色样音"),
    SAMPLE_SELECTED(2, "选用样音"),
    NARRATION(3, "旁白");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiDramaAudioTypeEnum::getType).toArray(Integer[]::new);

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
