package cn.iocoder.yudao.module.iot.enums.device;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TODO @芋艿：需要添加对应的 DTO，以及上下行的链路，网关、网关服务、设备等
/**
 * IoT 设备消息标识符枚举
 */
@Deprecated
@Getter
@RequiredArgsConstructor
public enum IotDeviceMessageIdentifierEnum {

    PROPERTY_GET("get"), // 下行
    PROPERTY_SET("set"), // 下行
    PROPERTY_REPORT("report"), // 上行

    STATE_ONLINE("online"), // 上行
    STATE_OFFLINE("offline"), // 上行

    CONFIG_GET("get"), // 上行 TODO 芋艿：【讨论】暂时没有上行的场景
    CONFIG_SET("set"), // 下行

    SERVICE_INVOKE("${identifier}"), // 下行
    SERVICE_REPLY_SUFFIX("_reply"), // 芋艿：TODO 芋艿：【讨论】上行 or 下行

    OTA_UPGRADE("upgrade"), // 下行
    OTA_PULL("pull"), // 上行
    OTA_PROGRESS("progress"), // 上行
    OTA_REPORT("report"), // 上行

    REGISTER_REGISTER("register"), // 上行
    REGISTER_REGISTER_SUB("register_sub"), // 上行
    REGISTER_UNREGISTER_SUB("unregister_sub"), // 下行

    TOPOLOGY_ADD("topology_add"), // 下行;
    ;

    /**
     * 标志符
     */
    private final String identifier;

}