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
    ErrorCode THINK_MODEL_FUNCTION_IDENTIFIER_EXISTS = new ErrorCode(1_050_002_002, "存在重复的功能标识符。");
    ErrorCode THINK_MODEL_FUNCTION_NAME_EXISTS = new ErrorCode(1_050_002_003, "存在重复的功能名称。");
    ErrorCode THINK_MODEL_FUNCTION_IDENTIFIER_INVALID = new ErrorCode(1_050_002_003, "产品物模型标识无效");

    // ========== IoT 设备 1-050-003-000 ============
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_050_003_000, "设备不存在");
    ErrorCode DEVICE_NAME_EXISTS = new ErrorCode(1_050_003_001, "设备名称在同一产品下必须唯一");
    ErrorCode DEVICE_HAS_CHILDREN = new ErrorCode(1_050_003_002, "有子设备，不允许删除");
    ErrorCode DEVICE_NAME_CANNOT_BE_MODIFIED = new ErrorCode(1_050_003_003, "设备名称不能修改");
    ErrorCode DEVICE_PRODUCT_CANNOT_BE_MODIFIED = new ErrorCode(1_050_003_004, "产品不能修改");
    ErrorCode DEVICE_INVALID_DEVICE_STATUS = new ErrorCode(1_050_003_005, "无效的设备状态");

}
