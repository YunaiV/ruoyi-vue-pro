package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IOT 接入网关协议枚举类
 * 接入网关协议, 0: 自定义, 1: Modbus, 2: OPC UA, 3: ZigBee, 4: BLE
 */
@AllArgsConstructor
@Getter
public enum IotProtocolTypeEnum implements IntArrayValuable {

    CUSTOM(0, "自定义"),
    MODBUS(1, "Modbus"),
    OPC_UA(2, "OPC UA"),
    ZIGBEE(3, "ZigBee"),
    BLE(4, "BLE");


    /**
     * 类型
     */
    private final Integer type;

    /**
     * 描述
     */
    private final String description;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotProtocolTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
