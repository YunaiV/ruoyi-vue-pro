package cn.iocoder.yudao.module.iot.core.enums.shadow;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 设备影子回复的错误码枚举
 * <p>
 * 用于设备影子回复（Topic：thing/shadow/update_reply）的 code、msg；
 * 管理后台接口的业务异常，使用 biz 模块的 ErrorCodeConstants
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
