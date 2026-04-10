package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品的设备类型
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotProductDeviceTypeEnum implements ArrayValuable<Integer> {

    DIRECT(0, "直连设备"),
    GATEWAY_SUB(1, "网关子设备"),
    GATEWAY(2, "网关设备");

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 描述
     */
    private final String description;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotProductDeviceTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 判断是否是网关
     *
     * @param type 类型
     * @return 是否是网关
     */
    public static boolean isGateway(Integer type) {
        return GATEWAY.getType().equals(type);
    }

    /**
     * 判断是否是网关子设备
     *
     * @param type 类型
     * @return 是否是网关子设备
     */
    public static boolean isGatewaySub(Integer type) {
        return GATEWAY_SUB.getType().equals(type);
    }

}
