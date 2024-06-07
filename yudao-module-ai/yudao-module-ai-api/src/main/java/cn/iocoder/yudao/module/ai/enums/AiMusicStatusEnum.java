package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author xiaoxin
 * @Date 2024/6/5
 */
@AllArgsConstructor
@Getter
public enum AiMusicStatusEnum {

    SUBMITTED("submitted", "已提交"),
    QUEUED("queued", "排队中"),
    STREAMING("streaming", "进行中"),
    COMPLETE("complete", "完成");

    /**
     * 状态
     */
    private final String status;
    /**
     * 状态名
     */
    private final String name;

    public static AiMusicStatusEnum valueOfStatus(String status) {
        for (AiMusicStatusEnum statusEnum : AiMusicStatusEnum.values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("未知会话状态： " + status);
    }

}
