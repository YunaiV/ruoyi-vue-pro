package cn.iocoder.yudao.module.ai.enums.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO @xiaoxin：这个也叫 AiMusicGenerateModeEnum 吧。虽然长，但是和项目统一一点。
/**
 * AI 音乐状态的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiMusicGenerateEnum {

    LYRIC("1", "歌词模式"),
    DESCRIPTION("2", "描述模式");

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
