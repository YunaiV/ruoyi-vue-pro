package cn.iocoder.yudao.framework.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

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

    ;

    private String platform;
    private String name;

    public static AiPlatformEnum valueOfPlatform(String platform) {
        for (AiPlatformEnum itemEnum : AiPlatformEnum.values()) {
            if (itemEnum.getPlatform().equals(platform)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + platform);
    }

}
