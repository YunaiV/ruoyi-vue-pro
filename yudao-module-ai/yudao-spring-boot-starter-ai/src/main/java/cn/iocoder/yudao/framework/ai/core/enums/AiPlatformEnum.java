package cn.iocoder.yudao.framework.ai.core.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// TODO 芋艿：这块，看看要不要调整下；
/**
 * ai 模型平台
 *
 * author: fansili
 * time: 2024/3/11 10:12
 */
@Getter
@AllArgsConstructor
public enum AiPlatformEnum {

    YI_YAN("yiyan", "一言"),
    QIAN_WEN("qianwen", "千问"),
    XING_HUO("xinghuo", "星火"),
    OPENAI("OpenAI", "OpenAI"),
    OPEN_AI_DALL("dall", "dall"),
    MIDJOURNEY("midjourney", "midjourney"),

    ;

    private String platform;
    private String name;

    public static List<AiPlatformEnum> CHAT_PLATFORM_LIST = Lists.newArrayList(
            AiPlatformEnum.YI_YAN,
            AiPlatformEnum.QIAN_WEN,
            AiPlatformEnum.XING_HUO,
            AiPlatformEnum.OPENAI
    );

    public static List<AiPlatformEnum> IMAGE_PLATFORM_LIST = Lists.newArrayList(
            AiPlatformEnum.OPEN_AI_DALL,
            AiPlatformEnum.MIDJOURNEY
    );

    public static AiPlatformEnum validatePlatform(String platform) {
        for (AiPlatformEnum platformEnum : AiPlatformEnum.values()) {
            if (platformEnum.getPlatform().equals(platform)) {
                return platformEnum;
            }
        }
        throw new IllegalArgumentException("非法平台： " + platform);
    }

}
