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
public enum AiChatDrawingStatusEnum {

    SUBMIT("submit", "提交任务"),
    COMPLETE("complete", "完成"),
    FAIL("fail", "失败"),

    ;

    private String status;

    private String name;


    public static AiChatDrawingStatusEnum valueOfStatus(String status) {
        for (AiChatDrawingStatusEnum itemEnum : AiChatDrawingStatusEnum.values()) {
            if (itemEnum.getStatus().equals(status)) {
                return itemEnum;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + status);
    }
}
