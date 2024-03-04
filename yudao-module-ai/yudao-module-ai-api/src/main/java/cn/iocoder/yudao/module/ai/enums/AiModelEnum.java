package cn.iocoder.yudao.module.ai.enums;

import lombok.Getter;

/**
 * author: fansili
 * time: 2024/3/4 12:36
 */
@Getter
public enum AiModelEnum {

    OPEN_AI_GPT_3_5("gpt-3.5-turbo", "GPT3.5"),
    OPEN_AI_GPT_4("gpt-4-turbo", "GPT4")

    ;

    AiModelEnum(String value, String message) {
        this.value = value;
        this.message = message;
    }

    private String value;

    private String message;
}
