package cn.iocoder.yudao.adminserver.modules.system.enums.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登陆日志的类型枚举
 */
@Getter
@AllArgsConstructor
public enum SysLoginLogTypeEnum {

    LOGIN_USERNAME(100), // 使用账号登录
    LOGIN_SOCIAL(101), // 使用社交登陆

    LOGOUT_SELF(200),  // 自己主动登出
    LOGOUT_TIMEOUT(201), // 超时登出
    LOGOUT_DELETE(202), // 强制退出
    ;

    /**
     * 日志类型
     */
    private final Integer type;

}
