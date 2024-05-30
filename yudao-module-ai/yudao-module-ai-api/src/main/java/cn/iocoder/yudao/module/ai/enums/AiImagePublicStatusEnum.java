package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai绘画 public 状态
 *
 * @author fansili
 * @time 2024/4/28 17:05
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum AiImagePublicStatusEnum {

    PRIVATE("private", "私有"),
    PUBLIC("public", "公开"),

    ;

    // TODO @fan：final 一下
    private final String status;

    private final String name;


    public static AiImagePublicStatusEnum valueOfStatus(String status) {
        for (AiImagePublicStatusEnum itemEnum : AiImagePublicStatusEnum.values()) {
            if (itemEnum.getStatus().equals(status)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + status);
    }
}
