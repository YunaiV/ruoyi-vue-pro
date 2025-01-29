package cn.iocoder.yudao.module.iot.enums.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * IoT 设备消息标识符枚举
 */
@Getter
@RequiredArgsConstructor
public enum IotDeviceMessageIdentifierEnum {

    PROPERTY_GET("get"),
    PROPERTY_SET("set"),
    PROPERTY_REPORT("report"),

    STATE_ONLINE("online"),
    STATE_OFFLINE("offline");


    /**
     * 标志符
     */
    private final String identifier;

}
