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
public enum ChatRoleClassifyEnum {

    WRITING("writing", "写作"),

    ENTERTAINMENT("entertainment", "娱乐"),

    ;


    private String classify;

    private String name;


    public static ChatRoleClassifyEnum valueOfClassify(String classify) {
        for (ChatRoleClassifyEnum itemEnum : ChatRoleClassifyEnum.values()) {
            if (itemEnum.getClassify().equals(classify)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + classify);
    }
}
