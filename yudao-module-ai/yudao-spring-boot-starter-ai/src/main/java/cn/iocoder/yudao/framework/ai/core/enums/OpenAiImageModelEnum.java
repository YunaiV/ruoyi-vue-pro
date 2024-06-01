package cn.iocoder.yudao.framework.ai.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO 芋艿：待梳理
/**
 * open ai
 *
 * @author fansili
 * @time 2024/4/28 14:21
 * @since 1.0
 */
@AllArgsConstructor
@Getter
@Deprecated
public enum OpenAiImageModelEnum {

    DALL_E_2("dall-e-2", "dall-e-2"),

    DALL_E_3("dall-e-3", "dall-e-3")

    ;

    private String model;

    private String name;

    public static OpenAiImageModelEnum valueOfModel(String model) {
        for (OpenAiImageModelEnum itemEnum : OpenAiImageModelEnum.values()) {
            if (itemEnum.getModel().equals(model)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + model);
    }
}
