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
    TCP(2, "TCP"), // TODO @puhui999：待实现；
    WEBSOCKET(3, "WebSocket"), // TODO @puhui999：待实现；

    MQTT(10, "MQTT"), // TODO 待实现；

    DATABASE(20, "Database"), // TODO @puhui999：待实现；可以简单点，对应的表名是什么，字段先固定了。
    REDIS(21, "Redis"),

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
