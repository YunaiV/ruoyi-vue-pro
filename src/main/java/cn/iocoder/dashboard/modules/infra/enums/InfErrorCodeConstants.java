package cn.iocoder.dashboard.modules.infra.enums;

import cn.iocoder.dashboard.common.exception.ErrorCode;

/**
 * Infra 错误码枚举类
 *
 * system 系统，使用 1-001-000-000 段
 */
public interface InfErrorCodeConstants {

    // ========== 参数配置 1001000000 ==========
    ErrorCode CONFIG_NOT_FOUND = new ErrorCode(1001000001, "参数配置不存在");
    ErrorCode CONFIG_NAME_DUPLICATE = new ErrorCode(1001000002, "参数配置 key 重复");
    ErrorCode CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE = new ErrorCode(1001000003, "不能删除类型为系统内置的参数配置");
    ErrorCode CONFIG_GET_VALUE_ERROR_IF_SENSITIVE = new ErrorCode(1001000004, "不允许获取敏感配置到前端");

}
