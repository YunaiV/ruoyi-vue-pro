package cn.iocoder.dashboard.modules.system.enums.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登陆日志的类型枚举
 */
@Getter
@AllArgsConstructor
public enum SysLoginLogTypeEnum {

    LOGIN(1),
    LOGOUT(2);

    /**
     * 日志类型
     */
    private final Integer type;

}
