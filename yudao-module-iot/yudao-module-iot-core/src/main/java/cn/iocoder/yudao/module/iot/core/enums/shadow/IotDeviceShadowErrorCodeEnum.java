package cn.iocoder.yudao.module.iot.core.enums.shadow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 设备影子设备侧错误码枚举
 * <p>
 * 用于回填 update_reply.payload.content，不走 CommonResult
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
    SHADOW_EMPTY(407, "影子内容为空"),
    PROPERTY_COUNT_EXCEEDED(408, "影子属性数超过限制"),
    VERSION_CONFLICT(409, "影子版本冲突"),
    SERVER_ERROR(500, "服务端异常");

    private final Integer code;

    private final String message;

}
