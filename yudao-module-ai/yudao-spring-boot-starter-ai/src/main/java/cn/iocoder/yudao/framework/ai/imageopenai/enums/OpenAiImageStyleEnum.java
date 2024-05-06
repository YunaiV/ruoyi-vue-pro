package cn.iocoder.yudao.framework.ai.imageopenai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * open ai image style
 *
 * @author fansili
 * @time 2024/4/28 16:15
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum OpenAiImageStyleEnum {

    // 图像生成的风格。可为vivid（生动）或 natural（自然）。vivid会使模型偏向生成超现实和戏剧性的图像，而natural则会让模型产出更自然、不那么超现实的图像。该参数仅对dall-e-3模型有效。

    VIVID("vivid", "生动"),
    NATURAL("natural", "自然"),

    ;

    private String style;

    private String name;

    public static OpenAiImageStyleEnum valueOfStyle(String style) {
        for (OpenAiImageStyleEnum itemEnum : OpenAiImageStyleEnum.values()) {
            if (itemEnum.getStyle().equals(style)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + style);
    }
}
