package cn.iocoder.yudao.module.mp.enums.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公众号消息自动回复的匹配模式
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum MpAutoReplyMatchEnum {

    ALL(1, "完全匹配"),
    LIKE(2, "半匹配"),
    ;

    /**
     * 匹配
     */
    private final Integer match;
    /**
     * 匹配的名字
     */
    private final String name;

}
