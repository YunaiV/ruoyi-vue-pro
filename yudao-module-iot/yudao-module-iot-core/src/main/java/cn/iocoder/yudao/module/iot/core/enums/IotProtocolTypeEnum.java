package cn.iocoder.yudao.module.iot.core.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 协议类型枚举
 *
 * 用于定义传输层协议类型
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotProtocolTypeEnum implements ArrayValuable<String> {

    TCP("tcp"),
    UDP("udp"),
    WEBSOCKET("websocket"),
    HTTP("http"),
    MQTT("mqtt"),
    EMQX("emqx"),
    COAP("coap"),
    MODBUS_TCP_CLIENT("modbus_tcp_client"),
    MODBUS_TCP_SERVER("modbus_tcp_server");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotProtocolTypeEnum::getType).toArray(String[]::new);

    /**
     * 类型
     */
    private final String type;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotProtocolTypeEnum of(String type) {
        return ArrayUtil.firstMatch(e -> e.getType().equals(type), values());
    }

}
