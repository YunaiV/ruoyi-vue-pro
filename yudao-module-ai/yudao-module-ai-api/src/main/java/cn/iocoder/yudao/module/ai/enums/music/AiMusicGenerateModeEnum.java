package cn.iocoder.yudao.module.ai.enums.music;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 音乐生成模式的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiMusicGenerateModeEnum implements ArrayValuable<Integer> {

    DESCRIPTION(1, "描述模式"),
    LYRIC(2, "歌词模式");

    /**
     * 模式
     */
    private final Integer mode;
    /**
     * 模式名
     */
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AiMusicGenerateModeEnum::getMode).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
