package cn.iocoder.dashboard.modules.system.enums;

import cn.iocoder.dashboard.common.exception.ErrorCode;

/**
 * 错误码枚举类
 *
 * system 系统，使用 1-002-000-000 段
 */
public interface SysErrorCodeConstants {

    // ========== AUTH 模块 1002000000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1002000000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1002000001, "登录失败，账号被禁用");
    ErrorCode AUTH_LOGIN_FAIL_UNKNOWN = new ErrorCode(1002000002, "登录失败"); // 登陆失败的兜底，位置原因

    // ========== TOKEN 模块 1002001000 ==========
    ErrorCode TOKEN_EXPIRED = new ErrorCode(1002001000, "Token 已经过期");
    ErrorCode TOKEN_PARSE_FAIL = new ErrorCode(1002001001, "Token 解析失败");

}
