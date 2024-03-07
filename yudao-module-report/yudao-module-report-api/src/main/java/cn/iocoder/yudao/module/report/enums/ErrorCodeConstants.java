package cn.iocoder.yudao.module.report.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Report 错误码枚举类
 *
 * report 系统，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== GoView 模块 1-003-000-000 ==========
    ErrorCode GO_VIEW_PROJECT_NOT_EXISTS = new ErrorCode(1_003_000_000, "GoView 项目不存在");

    // ========== UREPORT 模块 1-003-001-000 ==========
    ErrorCode UREPORT_DATA_NOT_EXISTS = new ErrorCode(1_003_001_001, "Ureport2 报表不存在");
    ErrorCode UREPORT_DATABASE_NOT_EXISTS = new ErrorCode(1_003_001_002, "Ureport2 报表数据源不存在");

}
