package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IOT 产品的设备类型
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotProductDeviceTypeEnum implements IntArrayValuable {

    DIRECT(0, "直连设备"),
    GATEWAY_CHILD(1, "网关子设备"),
    GATEWAY(2, "网关设备");

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 描述
     */
    private final String description;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotProductDeviceTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
