package cn.iocoder.yudao.module.ai.enums.drama;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 短剧音频类型的枚举
 *
 * @author 芋道源码
 */
@AllArgsConstructor
@Getter
public enum AiDramaAudioTypeEnum implements ArrayValuable<Integer> {

    SAMPLE(1, "角色样音"),
    NARRATION(2, "旁白"),
    DIALOGUE(3, "对白"),
    BGM(4, "配乐"),
    REFERENCE(5, "参考音频");

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
