package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 接入网关协议枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotProtocolTypeEnum implements ArrayValuable<Integer> {

    CUSTOM(0, "自定义"),
    MODBUS(1, "Modbus"),
    OPC_UA(2, "OPC UA"),
    ZIGBEE(3, "ZigBee"),
    BLE(4, "BLE");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotProtocolTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
