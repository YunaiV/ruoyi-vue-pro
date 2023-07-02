package cn.iocoder.yudao.module.member.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Member 错误码枚举类
 *
 * member 系统，使用 1-004-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 用户相关  1004001000============
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1004001000, "用户不存在");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1004001001, "密码校验失败");

    // ========== AUTH 模块 1004003000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1004003000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1004003001, "登录失败，账号被禁用");
    ErrorCode AUTH_TOKEN_EXPIRED = new ErrorCode(1004003004, "Token 已经过期");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1004003005, "未绑定账号，需要进行绑定");
    ErrorCode AUTH_WEIXIN_MINI_APP_PHONE_CODE_ERROR = new ErrorCode(1004003006, "获得手机号失败");

    // ========== 用户收件地址 1004004000 ==========
    ErrorCode ADDRESS_NOT_EXISTS = new ErrorCode(1004004000, "用户收件地址不存在");

    //========== 用户积分 1004005000 ==========

    // TODO @xiaqing：错误码要分段；例如说这里，积分配置、积分记录、签到配置、签到记录；分成 4 段；

    ErrorCode SIGN_IN_CONFIG_NOT_EXISTS = new ErrorCode(1004005003, "签到天数规则不存在");
    ErrorCode SIGN_IN_CONFIG_EXISTS = new ErrorCode(1004005004, "签到天数规则已存在");

    ErrorCode RECORD_NOT_EXISTS = new ErrorCode( 1004005005, "用户积分记录不存在");

    ErrorCode SIGN_IN_RECORD_NOT_EXISTS = new ErrorCode(1004005006, "用户签到积分不存在");

}
