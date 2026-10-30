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

    TCP("tcp", true),
    UDP("udp", false),
    WEBSOCKET("websocket", true),
    HTTP("http", false),
    MQTT("mqtt", true),
    EMQX("emqx", true),
    COAP("coap", false),
    MODBUS_TCP_CLIENT("modbus_tcp_client", true),
    MODBUS_TCP_SERVER("modbus_tcp_server", true);

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotProtocolTypeEnum::getType).toArray(String[]::new);

    /**
     * 类型
     */
    private final String type;
    /**
     * 是否是连接型协议：在线状态由连接的建立和断开决定；其它（HTTP、CoAP、UDP）为活跃型，由离线任务按最后活跃时间判断
     */
    private final boolean connectionBased;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotProtocolTypeEnum of(String type) {
        return ArrayUtil.firstMatch(e -> e.getType().equals(type), values());
    }

    /**
     * @return 是否是 Modbus 协议（client、server）：透传字节按请求 PDU 校验
     */
    public boolean isModbus() {
        return this == MODBUS_TCP_CLIENT || this == MODBUS_TCP_SERVER;
    }

}
