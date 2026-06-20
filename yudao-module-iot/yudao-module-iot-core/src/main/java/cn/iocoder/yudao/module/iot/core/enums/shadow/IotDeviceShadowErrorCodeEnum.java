package cn.iocoder.yudao.module.iot.core.enums.shadow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 设备影子设备侧错误码枚举
 * <p>
 * 设备侧 wire code，仅用于回填 {@code update_reply.code/msg}，不走 {@link cn.iocoder.yudao.framework.common.pojo.CommonResult}。
 * 服务端内部异常请使用 {@link cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants}（如 {@code DEVICE_SHADOW_NOT_ENABLED}）。
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum IotDeviceShadowErrorCodeEnum {

    JSON_FORMAT_ERROR(400, "JSON 格式错误"),
    METHOD_MISSING(401, "缺少 method"),
    STATE_MISSING(402, "缺少 state"),
    REPORTED_MISSING(404, "缺少 reported"),
    REPORTED_EMPTY(405, "reported 为空"),
    INVALID_METHOD(406, "无效内层 method"),
    PROPERTY_COUNT_EXCEEDED(408, "影子属性数超过限制"),
    VERSION_CONFLICT(409, "影子版本冲突"),
    SERVER_ERROR(500, "服务端异常");

    private final Integer code;

    private final String message;

}
