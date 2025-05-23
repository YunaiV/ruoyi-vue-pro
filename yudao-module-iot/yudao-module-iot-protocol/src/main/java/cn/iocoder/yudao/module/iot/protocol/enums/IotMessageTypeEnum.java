package cn.iocoder.yudao.module.iot.protocol.enums;

import cn.iocoder.yudao.module.iot.protocol.constants.IotTopicConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 消息类型枚举
 *
 * @author haohao
 */
@Getter
@AllArgsConstructor
public enum IotMessageTypeEnum {

    /**
     * 属性上报
     */
    PROPERTY_POST("property.post", "属性上报"),

    /**
     * 属性设置
     */
    PROPERTY_SET("property.set", "属性设置"),

    /**
     * 属性获取
     */
    PROPERTY_GET("property.get", "属性获取"),

    /**
     * 事件上报
     */
    EVENT_POST("event.post", "事件上报"),

    /**
     * 服务调用
     */
    SERVICE_INVOKE("service.invoke", "服务调用"),

    /**
     * 配置设置
     */
    CONFIG_SET("config.set", "配置设置"),

    /**
     * OTA 升级
     */
    OTA_UPGRADE("ota.upgrade", "OTA升级"),

    /**
     * 设备上线
     */
    DEVICE_ONLINE("device.online", "设备上线"),

    /**
     * 设备下线
     */
    DEVICE_OFFLINE("device.offline", "设备下线"),

    /**
     * 心跳
     */
    HEARTBEAT("heartbeat", "心跳");

    /**
     * 消息类型编码
     */
    private final String code;

    /**
     * 消息类型名称
     */
    private final String name;

    /**
     * 根据编码获取消息类型
     *
     * @param code 消息类型编码
     * @return 消息类型枚举，如果未找到返回 null
     */
    public static IotMessageTypeEnum getByCode(String code) {
        for (IotMessageTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据方法名获取消息类型
     *
     * @param method 方法名
     * @return 消息类型枚举，如果未找到返回 null
     */
    public static IotMessageTypeEnum getByMethod(String method) {
        if (method == null) {
            return null;
        }

        // 处理 thing.service.xxx 格式
        if (method.startsWith(IotTopicConstants.MethodPrefix.THING_SERVICE)) {
            String servicePart = method.substring(IotTopicConstants.MethodPrefix.THING_SERVICE.length());
            if ("property.set".equals(servicePart)) {
                return PROPERTY_SET;
            } else if ("property.get".equals(servicePart)) {
                return PROPERTY_GET;
            } else if ("config.set".equals(servicePart)) {
                return CONFIG_SET;
            } else if ("ota.upgrade".equals(servicePart)) {
                return OTA_UPGRADE;
            } else {
                return SERVICE_INVOKE;
            }
        }

        // 处理 thing.event.xxx 格式
        if (method.startsWith(IotTopicConstants.MethodPrefix.THING_EVENT)) {
            String eventPart = method.substring(IotTopicConstants.MethodPrefix.THING_EVENT.length());
            if ("property.post".equals(eventPart)) {
                return PROPERTY_POST;
            } else {
                return EVENT_POST;
            }
        }

        // 其他类型
        switch (method) {
            case IotTopicConstants.Method.DEVICE_ONLINE:
                return DEVICE_ONLINE;
            case IotTopicConstants.Method.DEVICE_OFFLINE:
                return DEVICE_OFFLINE;
            case IotTopicConstants.Method.HEARTBEAT:
                return HEARTBEAT;
            default:
                return null;
        }
    }
}