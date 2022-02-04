package cn.iocoder.yudao.module.tool.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Tool 错误码枚举类
 *
 * tool 系统，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 字典类型（测试） 1003000000 ==========
    ErrorCode TEST_DEMO_NOT_EXISTS = new ErrorCode(1003000000, "测试示例不存在");

    // ========== 代码生成器 1003001000 ==========
    ErrorCode CODEGEN_TABLE_EXISTS = new ErrorCode(1003001000, "表定义已经存在");
    ErrorCode CODEGEN_IMPORT_TABLE_NULL = new ErrorCode(1003001001, "导入的表不存在");
    ErrorCode CODEGEN_IMPORT_COLUMNS_NULL = new ErrorCode(1003001002, "导入的字段不存在");
    ErrorCode CODEGEN_PARSE_SQL_ERROR = new ErrorCode(1003001003, "解析 SQL 失败，请检查");
    ErrorCode CODEGEN_TABLE_NOT_EXISTS = new ErrorCode(1003001004, "表定义不存在");
    ErrorCode CODEGEN_COLUMN_NOT_EXISTS = new ErrorCode(1003001005, "字段义不存在");
    ErrorCode CODEGEN_SYNC_COLUMNS_NULL = new ErrorCode(1003001006, "同步的字段不存在");
    ErrorCode CODEGEN_SYNC_NONE_CHANGE = new ErrorCode(1003001007, "同步失败，不存在改变");

}
