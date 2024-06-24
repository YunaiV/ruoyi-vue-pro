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

    // @xin 文档中无失败这个返回值
    STREAMING("10", "进行中"),
    COMPLETE("20", "完成");

    /**
     * 状态
     */
    private final String status;

    /**
     * 状态名
     */
    private final String name;

}
