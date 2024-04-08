package cn.iocoder.yudao.module.ai.enums;

import lombok.Getter;

// TODO @fansili：1）类注释要加下；2）author 和 time 用 javadoc，@author 和 @since；3）@AllArgsConstructor 使用这个注解，去掉构造方法；4）value 改成 model 字段，然后注释都写下哈；5）message 改成 name，然后注释都写下哈
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

    // TODO @fan
    private String value;

    private String message;

}
