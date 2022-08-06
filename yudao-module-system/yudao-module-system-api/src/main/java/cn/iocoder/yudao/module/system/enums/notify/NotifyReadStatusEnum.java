package cn.iocoder.yudao.module.system.enums.notify;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 站内信阅读状态枚举类
 *
 * @author xrcoder
 */
@Getter
@AllArgsConstructor
public enum NotifyReadStatusEnum {

    UNREAD(0),
    READ(1);

    /**
     * 类型
     */
    private final Integer status;
}
