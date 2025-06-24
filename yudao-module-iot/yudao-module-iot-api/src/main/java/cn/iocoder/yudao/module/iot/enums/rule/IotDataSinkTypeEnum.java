package cn.iocoder.yudao.module.iot.enums.rule;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 数据目的的类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotDataSinkTypeEnum implements ArrayValuable<Integer> {

    HTTP(1, "HTTP"),
    TCP(2, "TCP"),
    WEBSOCKET(3, "WebSocket"),

    MQTT(10, "MQTT"),

    DATABASE(20, "Database"),
    // TODO @芋艿：改成 Redis；通过 execute 通用化；
    REDIS_STREAM(21, "Redis Stream"),

    ROCKETMQ(30, "RocketMQ"),
    RABBITMQ(31, "RabbitMQ"),
    KAFKA(32, "Kafka");

    private final Integer type;

    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotDataSinkTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
