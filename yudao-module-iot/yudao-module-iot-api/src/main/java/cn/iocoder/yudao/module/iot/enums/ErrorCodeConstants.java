package cn.iocoder.yudao.module.iot.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * iot 错误码枚举类
 * <p>
 * iot 系统，使用 1-050-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 产品相关 1-050-001-000 ============
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_050_001_000, "产品不存在");
    ErrorCode PRODUCT_KEY_EXISTS = new ErrorCode(1_050_001_001, "产品标识已经存在");
    ErrorCode PRODUCT_STATUS_NOT_DELETE = new ErrorCode(1_050_001_002, "产品状是发布状态，不允许删除");
    ErrorCode PRODUCT_STATUS_NOT_ALLOW_THING_MODEL = new ErrorCode(1_050_001_003, "产品状是发布状态，不允许操作物模型");
    ErrorCode PRODUCT_DEVICE_NOT_EXISTS = new ErrorCode(1_050_001_004, "产品设备类型不存在");

    // ========== 产品物模型 1-050-002-000 ============
    ErrorCode THING_MODEL_NOT_EXISTS = new ErrorCode(1_050_002_000, "产品物模型不存在");
    ErrorCode THING_MODEL_EXISTS_BY_PRODUCT_KEY = new ErrorCode(1_050_002_001, "ProductKey 对应的产品物模型已存在");
    ErrorCode THING_MODEL_IDENTIFIER_EXISTS = new ErrorCode(1_050_002_002, "存在重复的功能标识符。");
    ErrorCode THING_MODEL_NAME_EXISTS = new ErrorCode(1_050_002_003, "存在重复的功能名称。");
    ErrorCode THING_MODEL_IDENTIFIER_INVALID = new ErrorCode(1_050_002_003, "产品物模型标识无效");

    // ========== 设备 1-050-003-000 ============
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_050_003_000, "设备不存在");
    ErrorCode DEVICE_NAME_EXISTS = new ErrorCode(1_050_003_001, "设备名称在同一产品下必须唯一");
    ErrorCode DEVICE_HAS_CHILDREN = new ErrorCode(1_050_003_002, "有子设备，不允许删除");
    ErrorCode DEVICE_KEY_EXISTS = new ErrorCode(1_050_003_003, "设备标识已经存在");
    ErrorCode DEVICE_GATEWAY_NOT_EXISTS = new ErrorCode(1_050_003_004, "网关设备不存在");
    ErrorCode DEVICE_NOT_GATEWAY = new ErrorCode(1_050_003_005, "设备不是网关设备");
    ErrorCode DEVICE_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_050_003_006, "导入设备数据不能为空！");

    // ========== 产品分类 1-050-004-000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_050_004_000, "产品分类不存在");

    // ========== 设备分组 1-050-005-000 ==========
    ErrorCode DEVICE_GROUP_NOT_EXISTS = new ErrorCode(1_050_005_000, "设备分组不存在");
    ErrorCode DEVICE_GROUP_DELETE_FAIL_DEVICE_EXISTS = new ErrorCode(1_050_005_001, "设备分组下存在设备，不允许删除");

    // ========== 插件信息 1-050-006-000 ==========
    ErrorCode PLUGIN_INFO_NOT_EXISTS = new ErrorCode(1_050_006_000, "插件信息不存在");
    ErrorCode PLUGIN_INSTALL_FAILED = new ErrorCode(1_050_006_001, "插件安装失败");
    ErrorCode PLUGIN_INSTALL_FAILED_FILE_NAME_NOT_MATCH = new ErrorCode(1_050_006_002, "插件安装失败，文件名与原插件id不匹配");
    ErrorCode PLUGIN_INFO_DELETE_FAILED_RUNNING = new ErrorCode(1_050_006_003, "请先停止插件");
    ErrorCode PLUGIN_STATUS_INVALID = new ErrorCode(1_050_006_004, "插件状态无效");

    // ========== 插件实例 1-050-007-000 ==========
    ErrorCode PLUGIN_INSTANCE_NOT_EXISTS = new ErrorCode(1_050_007_000, "插件实例不存在");

}