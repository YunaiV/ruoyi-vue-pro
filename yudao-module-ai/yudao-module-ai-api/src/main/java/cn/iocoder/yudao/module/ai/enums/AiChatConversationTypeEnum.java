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
public enum AiChatConversationTypeEnum {

    // roleChat、userChat
    ROLE_CHAT("roleChat", "角色对话"),
    USER_CHAT("userChat", "用户对话"),

    ;

    private String type;

    private String name;

    public static AiChatConversationTypeEnum valueOfType(String type) {
        for (AiChatConversationTypeEnum itemEnum : AiChatConversationTypeEnum.values()) {
            if (itemEnum.getType().equals(type)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + type);
    }

}
