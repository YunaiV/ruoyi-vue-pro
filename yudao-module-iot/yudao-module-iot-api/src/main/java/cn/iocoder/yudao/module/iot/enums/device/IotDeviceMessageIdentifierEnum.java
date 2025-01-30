package cn.iocoder.yudao.module.iot.enums.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * IoT 设备消息标识符枚举
 */
@Getter
@RequiredArgsConstructor
public enum IotDeviceMessageIdentifierEnum {

    PROPERTY_GET("get"), // 下行
    PROPERTY_SET("set"), // 下行
    PROPERTY_REPORT("report"), // 上行

    STATE_ONLINE("online"), // 上行
    STATE_OFFLINE("offline"), // 上行

    SERVICE_REPLY_SUFFIX("_reply"); // TODO 上行 or 下行


    /**
     * 标志符
     */
    private final String identifier;

}
