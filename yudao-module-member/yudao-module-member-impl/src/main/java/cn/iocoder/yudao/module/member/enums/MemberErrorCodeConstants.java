package cn.iocoder.yudao.module.member.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Member 错误码枚举类
 *
 * member 系统，使用 1-004-000-000 段
 */
public interface MemberErrorCodeConstants {

    // ==========用户相关  1004001000============
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1004001000, "用户不存在");

    // ==========文件相关 1004002000 ===========
    ErrorCode FILE_IS_EMPTY = new ErrorCode(1004002000, "文件为空");

}
