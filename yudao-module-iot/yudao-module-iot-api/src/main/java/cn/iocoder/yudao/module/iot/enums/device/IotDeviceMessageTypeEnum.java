package cn.iocoder.yudao.module.iot.enums.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * IoT 设备消息类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum IotDeviceMessageTypeEnum {

    STATE("state"), // 设备状态
    PROPERTY("property"), // 设备属性
    EVENT("event"); // 设备事件

    /**
     * 属性
     */
    private final String type;

}
