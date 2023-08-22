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
    ErrorCode USER_MOBILE_NOT_EXISTS = new ErrorCode(1004001001, "手机号未注册用户");
    ErrorCode USER_MOBILE_USED = new ErrorCode(1004001002, "修改手机失败，该手机号({})已经被使用");

    // ========== AUTH 模块 1004003000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1004003000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1004003001, "登录失败，账号被禁用");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1004003005, "未绑定账号，需要进行绑定");
    ErrorCode AUTH_WEIXIN_MINI_APP_PHONE_CODE_ERROR = new ErrorCode(1004003006, "获得手机号失败");
    ErrorCode AUTH_MOBILE_USED = new ErrorCode(1004003007, "手机号已经被使用");

    // ========== 用户收件地址 1004004000 ==========
    ErrorCode ADDRESS_NOT_EXISTS = new ErrorCode(1004004000, "用户收件地址不存在");

    //========== 用户标签 1004006000 ==========
    ErrorCode TAG_NOT_EXISTS = new ErrorCode(1004006000, "用户标签不存在");
    ErrorCode TAG_NAME_EXISTS = new ErrorCode(1004006001, "用户标签已经存在");
    ErrorCode TAG_HAS_USER = new ErrorCode(1004006002, "用户标签下存在用户，无法删除");

    //========== 积分配置 1004007000 ==========

    //========== 积分记录 1004008000 ==========

    //========== 签到配置 1004009000 ==========
    ErrorCode SIGN_IN_CONFIG_NOT_EXISTS = new ErrorCode(1004009000, "签到天数规则不存在");
    ErrorCode SIGN_IN_CONFIG_EXISTS = new ErrorCode(1004009001, "签到天数规则已存在");

    //========== 签到配置 1004010000 ==========


    //========== 用户等级 1004011000 ==========
    ErrorCode LEVEL_NOT_EXISTS = new ErrorCode(1004011000, "用户等级不存在");
    ErrorCode LEVEL_NAME_EXISTS = new ErrorCode(1004011001, "用户等级名称[{}]已被使用");
    ErrorCode LEVEL_VALUE_EXISTS = new ErrorCode(1004011002, "用户等级值[{}]已被[{}]使用");
    ErrorCode LEVEL_EXPERIENCE_MIN = new ErrorCode(1004011003, "升级经验必须大于上一个等级[{}]设置的升级经验[{}]");
    ErrorCode LEVEL_EXPERIENCE_MAX = new ErrorCode(1004011004, "升级经验必须小于下一个等级[{}]设置的升级经验[{}]");
    ErrorCode LEVEL_HAS_USER = new ErrorCode(1004011005, "用户等级下存在用户，无法删除");

    ErrorCode LEVEL_LOG_NOT_EXISTS = new ErrorCode(1004011100, "用户等级记录不存在");
    ErrorCode EXPERIENCE_LOG_NOT_EXISTS = new ErrorCode(1004011200, "用户经验记录不存在");
    ErrorCode LEVEL_REASON_NOT_EXISTS = new ErrorCode(1004011300, "用户等级调整原因不能为空");

    //========== 用户分组 1004012000 ==========
    ErrorCode GROUP_NOT_EXISTS = new ErrorCode(1004012000, "用户分组不存在");
    ErrorCode GROUP_HAS_USER = new ErrorCode(1004012001, "用户分组下存在用户，无法删除");

}
