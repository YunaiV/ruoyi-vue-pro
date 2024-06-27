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

    IN_PROGRESS(10, "进行中"),
    SUCCESS(20, "已完成");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

}
