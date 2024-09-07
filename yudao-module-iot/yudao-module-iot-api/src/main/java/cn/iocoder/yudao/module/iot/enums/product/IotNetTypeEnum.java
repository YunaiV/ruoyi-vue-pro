package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IOT 联网方式枚举类
 * 联网方式, 0: Wi-Fi, 1: Cellular, 2: Ethernet, 3: 其他
 */
@AllArgsConstructor
@Getter
public enum IotNetTypeEnum implements IntArrayValuable {

    WIFI(0, "Wi-Fi"),
    CELLULAR(1, "Cellular"),
    ETHERNET(2, "Ethernet"),
    OTHER(3, "其他");


    /**
     * 类型
     */
    private final Integer type;

    /**
     * 描述
     */
    private final String description;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotNetTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
