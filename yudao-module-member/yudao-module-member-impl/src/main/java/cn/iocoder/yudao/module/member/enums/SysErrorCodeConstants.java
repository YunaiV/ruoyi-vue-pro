package cn.iocoder.yudao.module.member.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-005-000-000 段
 */
public interface SysErrorCodeConstants {

    // ========== AUTH 模块 1005000000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1005000000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1005000001, "登录失败，账号被禁用");
    ErrorCode AUTH_LOGIN_FAIL_UNKNOWN = new ErrorCode(1005000002, "登录失败"); // 登录失败的兜底，未知原因
    ErrorCode AUTH_TOKEN_EXPIRED = new ErrorCode(1005000003, "Token 已经过期");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1005000004, "未绑定账号，需要进行绑定");

    // ========== SMS CODE 模块 1005001000 ==========
    ErrorCode USER_SMS_CODE_NOT_FOUND = new ErrorCode(1005001000, "验证码不存在");
    ErrorCode USER_SMS_CODE_EXPIRED = new ErrorCode(1005001001, "验证码已过期");
    ErrorCode USER_SMS_CODE_USED = new ErrorCode(1005001002, "验证码已使用");
    ErrorCode USER_SMS_CODE_NOT_CORRECT = new ErrorCode(1005001003, "验证码不正确");
    ErrorCode USER_SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY = new ErrorCode(1005001004, "超过每日短信发送数量");
    ErrorCode USER_SMS_CODE_SEND_TOO_FAST = new ErrorCode(1005001005, "短信发送过于频率");
    ErrorCode USER_SMS_CODE_IS_EXISTS = new ErrorCode(1005001006, "手机号已被使用");
    ErrorCode USER_SMS_CODE_IS_UNUSED = new ErrorCode(1005001006, "验证码未被使用");

    // ========== 用户模块 1005002000 ==========
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1005002001, "用户不存在");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1005002003, "密码校验失败");
}
