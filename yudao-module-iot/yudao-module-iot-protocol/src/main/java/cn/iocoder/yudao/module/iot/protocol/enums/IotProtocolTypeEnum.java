package cn.iocoder.yudao.module.iot.protocol.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 协议类型枚举
 *
 * @author haohao
 */
@Getter
@AllArgsConstructor
public enum IotProtocolTypeEnum {

    /**
     * Alink 协议（阿里云物联网协议）
     */
    ALINK("alink", "Alink 协议"),

    /**
     * MQTT 原始协议
     */
    MQTT_RAW("mqtt_raw", "MQTT 原始协议"),

    /**
     * HTTP 协议
     */
    HTTP("http", "HTTP 协议"),

    /**
     * TCP 协议
     */
    TCP("tcp", "TCP 协议"),

    /**
     * UDP 协议
     */
    UDP("udp", "UDP 协议"),

    /**
     * 自定义协议
     */
    CUSTOM("custom", "自定义协议");

    /**
     * 协议编码
     */
    private final String code;

    /**
     * 协议名称
     */
    private final String name;

    /**
     * 根据编码获取协议类型
     *
     * @param code 协议编码
     * @return 协议类型枚举，如果未找到返回 null
     */
    public static IotProtocolTypeEnum getByCode(String code) {
        for (IotProtocolTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查是否为有效的协议编码
     *
     * @param code 协议编码
     * @return 如果有效返回 true，否则返回 false
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
}