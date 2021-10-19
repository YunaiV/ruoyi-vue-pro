package cn.iocoder.yudao.userserver.modules.member.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Member 错误码枚举类
 *
 * member 系统，使用 1-004-000-000 段
 */
public interface MbrErrorCodeConstants {

    // 用户相关
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1004000000, "用户不存在");

    // 文件相关
    ErrorCode FILE_IS_EMPTY = new ErrorCode(1004000000, "用户不存在");
}
