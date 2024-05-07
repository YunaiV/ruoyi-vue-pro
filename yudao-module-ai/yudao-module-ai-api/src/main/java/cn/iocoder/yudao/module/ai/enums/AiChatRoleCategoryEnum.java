package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天role 分类
 *
 * @author fansili
 * @time 2024/4/24 16:41
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum AiChatRoleCategoryEnum {

    WRITING("writing", "写作"),

    ENTERTAINMENT("entertainment", "娱乐"),

    ;


    private String category;

    private String name;


    public static AiChatRoleCategoryEnum valueOfCategory(String category) {
        for (AiChatRoleCategoryEnum itemEnum : AiChatRoleCategoryEnum.values()) {
            if (itemEnum.getCategory().equals(category)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + category);
    }
}
