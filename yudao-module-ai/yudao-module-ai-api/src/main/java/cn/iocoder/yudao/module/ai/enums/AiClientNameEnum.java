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
    YI_YAN("yiYan", "一言模型!"),
    XING_HUO("xingHuo", "星火模型!"),

    ;

    private String name;

    private String message;
}
