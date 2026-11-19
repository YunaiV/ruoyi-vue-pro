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
    ErrorCode GO_VIEW_SQL_INVALID = new ErrorCode(1_003_000_001, "GoView SQL 不合法：{}");
    ErrorCode GO_VIEW_SQL_DB_NOT_SUPPORTED = new ErrorCode(1_003_000_002,
            "GoView SQL 查询不支持当前数据库（{}），仅支持 MySQL、MariaDB、PostgreSQL");
    ErrorCode GO_VIEW_SQL_TRANSACTION_NOT_SUPPORTED = new ErrorCode(1_003_000_003,
            "GoView SQL 查询不能在多数据源本地事务（@DSTransactional）中执行");
    ErrorCode GO_VIEW_SQL_ROWS_EXCEED = new ErrorCode(1_003_000_004, "GoView SQL 查询结果超过 {} 行，请缩小查询范围");

    // ========== UReport2 模块 1-003-001-000 ==========
    ErrorCode UREPORT2_FILE_NOT_EXISTS = new ErrorCode(1_003_001_000, "UReport2 报表模板不存在");

}
