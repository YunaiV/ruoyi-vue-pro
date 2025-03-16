package cn.iocoder.yudao.module.iot.enums.rule;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 数据桥接的类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotDataBridgeTypeEnum implements ArrayValuable<Integer> {

    HTTP(1, "HTTP"),
    TCP(2, "TCP"),
    WEBSOCKET(3, "WEBSOCKET"),

    MQTT(10, "MQTT"),

    DATABASE(20, "DATABASE"),
    REDIS_STREAM(21, "REDIS_STREAM"),

    ROCKETMQ(30, "ROCKETMQ"),
    RABBITMQ(31, "RABBITMQ"),
    KAFKA(32, "KAFKA");

    private final Integer type;

    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotDataBridgeTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
