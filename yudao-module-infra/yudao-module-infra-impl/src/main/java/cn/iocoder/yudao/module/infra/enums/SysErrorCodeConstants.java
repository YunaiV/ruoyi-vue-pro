package cn.iocoder.yudao.module.infra.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-006-000-000 段
 */
public interface SysErrorCodeConstants {

    // ========= 文件相关 1006001000=================
    ErrorCode FILE_PATH_EXISTS = new ErrorCode(1006001000, "文件路径已存在");
    ErrorCode FILE_NOT_EXISTS = new ErrorCode(1006001002, "文件不存在");

}
