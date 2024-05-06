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
public enum AiChatRoleEnableEnum {

    OPEN("open", "公开"),
    CLOSE("close", "关闭"),

    ;

    private String type;

    private String name;


    public static AiChatRoleEnableEnum valueOfType(String type) {
        for (AiChatRoleEnableEnum itemEnum : AiChatRoleEnableEnum.values()) {
            if (itemEnum.getType().equals(type)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + type);
    }

}
