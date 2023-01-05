package cn.iocoder.yudao.module.mp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Mp 错误码枚举类
 *
 * mp 系统，使用 1-006-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 用户相关  1004001000============
    ErrorCode WX_ACCOUNT_NOT_EXISTS = new ErrorCode(1006001000, "公众号账户不存在");
    ErrorCode WX_ACCOUNT_FANS_NOT_EXISTS = new ErrorCode(1006001001, "粉丝账号不存在");
    ErrorCode COMMON_NOT_EXISTS = new ErrorCode(1006001002, "用户不存在");

}
