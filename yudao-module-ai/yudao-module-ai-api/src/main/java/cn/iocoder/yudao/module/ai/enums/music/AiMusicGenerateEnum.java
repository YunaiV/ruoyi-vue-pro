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
public enum AiMusicGenerateEnum {

    // TODO @xin：用数字哈。项目目前枚举都是数字
    LYRIC("lyric", "歌词模式"),
    DESCRIPTION("description", "描述模式");

    /**
     * 模式
     */
    private final String mode;
    /**
     * 模式名
     */
    private final String name;

    public static AiMusicGenerateEnum valueOfMode(String mode) {
        for (AiMusicGenerateEnum modeEnum : AiMusicGenerateEnum.values()) {
            if (modeEnum.getMode().equals(mode)) {
                return modeEnum;
            }
        }
        throw new IllegalArgumentException("未知模式： " + mode);
    }

}
