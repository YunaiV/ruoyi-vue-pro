package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 定位方式枚举类
 *
 * @author alwayssuper
 */
@AllArgsConstructor
@Getter
public enum IotLocationTypeEnum implements ArrayValuable<Integer> {
    MANUAL(0, "手动定位"),
    IP(1, "IP定位"),
    MODULE(2, "定位模块定位");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotLocationTypeEnum::getType).toArray(Integer[]::new);

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
