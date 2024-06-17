package cn.iocoder.yudao.module.ai.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO @xin：这个类，挪到 enums/music 包下；
// TODO @xin：1）@author 这个是标准的 javadoc；2）@date 可以不要哈；3）可以加下枚举类的注释
/**
 * @Author xiaoxin
 * @Date 2024/6/5
 */
@AllArgsConstructor
@Getter
public enum AiMusicStatusEnum {

    // TODO @xin：是不是收敛成，只有 3 个：进行中，成功，失败；类似 AiImageStatusEnum

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
