package cn.iocoder.yudao.module.ai.enums.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 音乐状态的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiMusicGenerateModeEnum {

    LYRIC(1, "歌词模式"),
    DESCRIPTION(2, "描述模式");

    /**
     * 模式
     */
    private final Integer mode;
    /**
     * 模式名
     */
    private final String name;

}
