package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * chat角色 可见范围
 *
 * @author fansili
 * @time 2024/4/24 16:44
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum AiChatRoleVisibilityEnum {

    PUBLIC("public", "公开"),
    PRIVATE("private", "私有的"),

    ;

    private String type;

    private String name;


    public static AiChatRoleVisibilityEnum valueOfType(String type) {
        for (AiChatRoleVisibilityEnum itemEnum : AiChatRoleVisibilityEnum.values()) {
            if (itemEnum.getType().equals(type)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + type);
    }

}
