package cn.iocoder.yudao.module.wms.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * WMS 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 *
 * @author Administrator
 */
public interface ErrorCodeConstants {

    // ========== WMS_EXTERNAL_STORAGE 外部存储库 2_001_000_000 ==========
    ErrorCode EXTERNAL_STORAGE_NAME_DUPLICATE = new ErrorCode(2_001_000_000, "名称已存在");
    ErrorCode EXTERNAL_STORAGE_CODE_DUPLICATE = new ErrorCode(2_001_000_001, "代码已存在");
    ErrorCode EXTERNAL_STORAGE_NOT_EXISTS = new ErrorCode(2_001_000_002, "外部存储库不存在");

    // ========== WMS_WAREHOUSE 仓库 2_001_100_000 ==========
    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(2_001_100_000, "供应商不存在");
}