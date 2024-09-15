package cn.iocoder.yudao.module.iot.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * iot 错误码枚举类
 * <p>
 * iot 系统，使用 1-050-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== IoT 产品相关  1-050-001-000 ============
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_050_001_000, "产品不存在");
    ErrorCode PRODUCT_IDENTIFICATION_EXISTS = new ErrorCode(1_050_001_001, "产品标识已经存在");
    ErrorCode PRODUCT_STATUS_NOT_DELETE = new ErrorCode(1_050_001_002, "产品状是发布状态，不允许删除");

    // ========== IoT 产品物模型 1-050-002-000 ============
    ErrorCode THINK_MODEL_FUNCTION_NOT_EXISTS = new ErrorCode(1_050_002_000, "产品物模型不存在");
    ErrorCode THINK_MODEL_FUNCTION_EXISTS_BY_PRODUCT_KEY = new ErrorCode(1_050_002_001, "ProductKey 对应的产品物模型已存在");
    ErrorCode THINK_MODEL_FUNCTION_EXISTS_BY_IDENTIFIER = new ErrorCode(1_050_002_002, "产品物模型标识已存在");
}
