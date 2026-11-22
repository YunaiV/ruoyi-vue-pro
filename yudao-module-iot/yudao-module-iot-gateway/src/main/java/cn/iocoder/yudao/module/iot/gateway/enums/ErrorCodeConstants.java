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
    ErrorCode MODBUS_BIT_WRITE_NOT_SUPPORTED = new ErrorCode(1_051_003_116, "Modbus TCP Server 暂不支持寄存器位点位写入");

    // ========== 属性设置 1-051-003-200 ============
    ErrorCode PROPERTY_SET_FAILED = new ErrorCode(1_051_003_200, "属性设置失败：{} 个属性中 {} 个失败");
    ErrorCode PROPERTY_SET_PARAMS_INVALID = new ErrorCode(1_051_003_201, "属性设置参数不合法：{}");
    ErrorCode PROPERTY_SET_POINT_NOT_EXISTS = new ErrorCode(1_051_003_202, "属性没有启用的点位配置");
    ErrorCode PROPERTY_SET_POINT_NOT_WRITABLE = new ErrorCode(1_051_003_203, "点位不可写：{}");
    ErrorCode PROPERTY_SET_VALUE_INVALID = new ErrorCode(1_051_003_204, "属性值不合法：{}");
    ErrorCode PROPERTY_SET_DEVICE_NOT_CONFIGURED = new ErrorCode(1_051_003_205, "设备没有可用的采集配置");
    ErrorCode PROPERTY_SET_POINT_CHANGED = new ErrorCode(1_051_003_206, "点位配置已变化（禁用、删除或地址、类型变更），未写入");
    ErrorCode PROPERTY_SET_CONNECTION_CHANGED = new ErrorCode(1_051_003_207, "设备连接目标已变化（IP、端口或从站号变更），未写入");

    // ========== 离线暂存 1-051-004-000 ============
    ErrorCode DEVICE_MESSAGE_SPOOL_REJECTED = new ErrorCode(1_051_004_000, "消息总线不可用，且离线暂存失败：{}");
    ErrorCode DEVICE_MESSAGE_REJECTED = new ErrorCode(1_051_004_001, "消息被消息总线拒绝（例如说超过大小限制），重试也不会成功");

    // ========== OPC UA Client 1-051-005-000 ============
    ErrorCode OPC_UA_DOWNSTREAM_NOT_SUPPORTED = new ErrorCode(1_051_005_000, "OPC UA 只支持属性设置下发，不支持服务调用、透传等下行指令");
    ErrorCode OPC_UA_DEVICE_CONFIG_REJECTED = new ErrorCode(1_051_005_001, "OPC UA 设备配置不可用：{}");
    ErrorCode OPC_UA_ENDPOINT_REJECTED = new ErrorCode(1_051_005_002, "OPC UA 端点不可用：{}");
    ErrorCode OPC_UA_SECURITY_INIT_FAILED = new ErrorCode(1_051_005_003, "OPC UA 客户端证书初始化失败：{}");
    ErrorCode OPC_UA_WRITE_SESSION_INACTIVE = new ErrorCode(1_051_005_100, "OPC UA 会话未激活（设备离线）");
    ErrorCode OPC_UA_WRITE_SESSION_EXPIRED = new ErrorCode(1_051_005_101, "OPC UA 会话已过期（写入前断线或重连），未写入");
    ErrorCode OPC_UA_WRITE_QUEUE_FULL = new ErrorCode(1_051_005_102, "OPC UA 写入排队已达上限");
    ErrorCode OPC_UA_WRITE_TIMEOUT = new ErrorCode(1_051_005_103, "OPC UA 写入超时：{}");
    ErrorCode OPC_UA_WRITE_NODE_ID_UNRESOLVED = new ErrorCode(1_051_005_104, "NodeId 无法解析（格式不正确或命名空间不存在）");
    ErrorCode OPC_UA_WRITE_NODE_NOT_WRITABLE = new ErrorCode(1_051_005_105, "节点不可写：{}");
    ErrorCode OPC_UA_WRITE_DATA_TYPE_NOT_SUPPORTED = new ErrorCode(1_051_005_106, "不支持写入的节点数据类型：{}");
    ErrorCode OPC_UA_WRITE_ATTRIBUTE_READ_FAILED = new ErrorCode(1_051_005_107, "读取节点属性失败：{}");
    ErrorCode OPC_UA_WRITE_STATUS_BAD = new ErrorCode(1_051_005_108, "OPC UA 服务端返回：{}");
    ErrorCode OPC_UA_WRITE_FAILED = new ErrorCode(1_051_005_109, "OPC UA 写入失败：{}");
    ErrorCode OPC_UA_WRITE_CONFIG_EXPIRED = new ErrorCode(1_051_005_110, "设备配置已变化（解绑、会话或点位变更），未写入");

    // ========== OPC UA Server 1-051-006-000 ==========
    ErrorCode OPC_UA_SERVER_SECURITY_INIT_FAILED = new ErrorCode(1_051_006_000, "OPC UA Server 证书初始化失败：{}");

}