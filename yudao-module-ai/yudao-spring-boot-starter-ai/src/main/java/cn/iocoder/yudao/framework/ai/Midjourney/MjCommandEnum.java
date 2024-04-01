package cn.iocoder.yudao.framework.ai.Midjourney;

import lombok.Getter;

/**
 * MJ 命令
 */
@Getter
public enum MjCommandEnum {

    IMAGINE("imagine", "生成图片"),
    DESCRIBE("describe", "生成描述"),
    FAST("fast", "快速生成"),
    SETTINGS("settings", "设置"),
    ASK("ask", "提问"),
    BLEND("blend", "融合"),

    ;

    MjCommandEnum(String value, String message) {
        this.value =value;
        this.message =message;
    }

    private String value;
    private String message;

}
