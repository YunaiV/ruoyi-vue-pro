package cn.iocoder.yudao.module.ai.enums.music;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 音乐状态的枚举
 *
 * @author xiaoxin
 */
@AllArgsConstructor
@Getter
public enum AiMusicStatusEnum {

    // TODO @xin：用数字哈。项目目前枚举都是数字
    // @xin 文档中无失败这个返回值
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
