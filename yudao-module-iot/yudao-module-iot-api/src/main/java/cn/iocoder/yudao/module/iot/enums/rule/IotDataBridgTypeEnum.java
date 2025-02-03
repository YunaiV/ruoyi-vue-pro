package cn.iocoder.yudao.module.iot.enums.rule;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Iot 数据桥接的类型枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotDataBridgTypeEnum implements ArrayValuable<Integer> {

    HTTP(1),
    TCP(2),
    WEBSOCKET(3),

    MQTT(10),

    DATABASE(20),
    REDIS(21),

    ROCKETMQ(30),
    RABBITMQ(31),
    KAFKA(32)
    ;

    private final Integer type;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotDataBridgTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
