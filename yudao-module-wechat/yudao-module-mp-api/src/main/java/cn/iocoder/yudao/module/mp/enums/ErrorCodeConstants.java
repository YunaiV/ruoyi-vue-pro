package cn.iocoder.yudao.module.mp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Member 错误码枚举类
 *
 * wechatMp 系统，使用 1-004-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 用户相关  1004001000============
    ErrorCode WX_ACCOUNT_NOT_EXISTS = new ErrorCode(1004001000, "用户不存在");
    ErrorCode WX_ACCOUNT_FANS_NOT_EXISTS = new ErrorCode(1004001000, "用户不存在");
    ErrorCode COMMON_NOT_EXISTS = new ErrorCode(1004001000, "用户不存在");

}
