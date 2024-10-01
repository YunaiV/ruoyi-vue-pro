package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IOT 联网方式枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotNetTypeEnum implements IntArrayValuable {

    WIFI(0, "Wi-Fi"),
    CELLULAR(1, "Cellular"),
    ETHERNET(2, "Ethernet"),
    OTHER(3, "其他");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotNetTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
