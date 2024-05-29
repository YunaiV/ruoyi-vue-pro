package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai绘画状态
 *
 * @author fansili
 * @time 2024/4/28 17:05
 * @since 1.0
 */
@AllArgsConstructor
@Getter
public enum AiImageStatusEnum {

    IN_PROGRESS("10", "进行中"),
    COMPLETE("20", "完成"),
    FAIL("30", "失败"),

    ;

    // TODO @fan：final 一下
    private final String status;

    private final String name;


    public static AiImageStatusEnum valueOfStatus(String status) {
        for (AiImageStatusEnum itemEnum : AiImageStatusEnum.values()) {
            if (itemEnum.getStatus().equals(status)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + status);
    }
}
