package cn.iocoder.yudao.module.iot.gateway.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * iot gateway 错误码枚举类
 * <p>
 * iot 系统，使用 1-051-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 设备认证 1-050-001-000 ============
    ErrorCode DEVICE_AUTH_FAIL = new ErrorCode(1_051_001_000, "设备鉴权失败"); // 对应阿里云 20000
    ErrorCode DEVICE_TOKEN_EXPIRED = new ErrorCode(1_051_001_002, "token 失效。需重新调用 auth 进行鉴权，获取token"); // 对应阿里云 20001

    // ========== 设备信息 1-050-002-000 ============
    ErrorCode DEVICE_NOT_EXISTS = new ErrorCode(1_051_002_001, "设备({}/{}) 不存在");

    // ========== 设备透传与数据解析脚本 1-051-003-000 ============
    ErrorCode DEVICE_MESSAGE_RAW_METHOD_RESERVED = new ErrorCode(1_051_003_000, "透传方法只能由网关 raw 入口使用");
    ErrorCode SCRIPT_EXECUTE_FAILED = new ErrorCode(1_051_003_100, "数据解析脚本执行失败：{}");
    ErrorCode SCRIPT_TIMEOUT = new ErrorCode(1_051_003_101, "处理超时：未在截止时间内完成（排队、加载配置或脚本执行）");
    ErrorCode SCRIPT_REJECTED = new ErrorCode(1_051_003_102, "数据解析脚本繁忙或已熔断");
    ErrorCode SCRIPT_OUTPUT_INVALID = new ErrorCode(1_051_003_103, "数据解析脚本输出不合法：{}");
    ErrorCode RAW_NOT_SUPPORTED = new ErrorCode(1_051_003_105, "当前连接不支持透传下发");
    ErrorCode DEVICE_OFFLINE_OR_WRITE_FAILED = new ErrorCode(1_051_003_106, "设备不在线或写入失败");
    ErrorCode MODBUS_TIMEOUT = new ErrorCode(1_051_003_110, "Modbus 响应超时");
    ErrorCode MODBUS_EXCEPTION = new ErrorCode(1_051_003_111, "Modbus 异常响应（异常码 {}）");
    ErrorCode MODBUS_PDU_INVALID = new ErrorCode(1_051_003_112, "Modbus PDU 不合法：{}");
    ErrorCode MODBUS_QUEUE_FULL = new ErrorCode(1_051_003_113, "Modbus 事务队列已满");
    ErrorCode MODBUS_RESPONSE_INVALID = new ErrorCode(1_051_003_114, "Modbus 响应不合法");
    ErrorCode MODBUS_DISCONNECTED = new ErrorCode(1_051_003_115, "Modbus 连接已断开");

}