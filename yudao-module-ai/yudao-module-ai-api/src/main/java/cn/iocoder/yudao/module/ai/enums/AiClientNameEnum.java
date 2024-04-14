package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai client 名字
 *
 * 这个需要根据配置文件起的来决定
 *
 * @author fansili
 * @time 2024/4/14 16:02
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum AiClientNameEnum {

    QIAN_WEN("qianWen", "千问模型!"),
    YI_YAN_3_5_8K("yiYan3_5_8k", "文心一言(3.5-8k)"),
    XING_HUO("xingHuo", "星火模型!"),

    ;

    private String name;

    private String message;

    public static AiClientNameEnum valueOfName(String name) {
        for (AiClientNameEnum nameEnum : AiClientNameEnum.values()) {
            if (nameEnum.getName().equals(name)) {
                return nameEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + name);
    }
}
