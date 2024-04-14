package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对话类型
 * 创建对话、继续对话
 *
 * @author fansili
 * @time 2024/4/14 18:15
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum ChatConversationTypeEnum {

    NEW("new", "新建对话"),
    CONTINUE("continue", "继续对话"),

    ;

    private String type;

    private String name;

    public static ChatConversationTypeEnum valueOfType(String type) {
        for (ChatConversationTypeEnum itemEnum : ChatConversationTypeEnum.values()) {
            if (itemEnum.getType().equals(type)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + type);
    }

}
