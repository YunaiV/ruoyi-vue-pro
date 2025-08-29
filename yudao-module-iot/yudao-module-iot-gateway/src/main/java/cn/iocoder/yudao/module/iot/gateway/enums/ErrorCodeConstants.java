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

}