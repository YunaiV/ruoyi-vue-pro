package cn.iocoder.yudao.framework.ai.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 模型平台
 *
 * @author fansili
 */
@Getter
@AllArgsConstructor
public enum AiPlatformEnum {

    // ========== 国内平台 ==========

    YI_YAN("YiYan", "文心一言"), // 百度
    QIAN_WEN("QianWen", "千问"), // 阿里
    DEEP_SEEK("DeepSeek", "DeepSeek"), // DeepSeek
    XING_HUO("XingHuo", "星火"), // 讯飞

    // ========== 国外平台 ==========

    OPENAI("OpenAI", "OpenAI"),
    OLLAMA("Ollama", "Ollama"),

    STABLE_DIFFUSION("StableDiffusion", "StableDiffusion"), // Stability AI
    MIDJOURNEY("Midjourney", "Midjourney"), // Midjourney
    SUNO("Suno", "Suno"), // Suno AI
    ;

    /**
     * 平台
     */
    private final String platform;
    /**
     * 平台名
     */
    private final String name;

    public static AiPlatformEnum validatePlatform(String platform) {
        for (AiPlatformEnum platformEnum : AiPlatformEnum.values()) {
            if (platformEnum.getPlatform().equals(platform)) {
                return platformEnum;
            }
        }
        throw new IllegalArgumentException("非法平台： " + platform);
    }

}
