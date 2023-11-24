package cn.iocoder.yudao.module.report.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Report 错误码枚举类
 *
 * report 系统，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== AUTH 模块 1-003-000-000 ==========
    ErrorCode GO_VIEW_PROJECT_NOT_EXISTS = new ErrorCode(1_003_000_000, "GoView 项目不存在");

    ErrorCode UREPORT_FILE_NOT_EXISTS = new ErrorCode(1_003_001_000, "打印文件不存在,请检查Ureport报表文件");
    ErrorCode UREPORT_FILE_EXISTS = new ErrorCode(1_003_001_001, "报表名字已存在，请修改后再保存");
}
