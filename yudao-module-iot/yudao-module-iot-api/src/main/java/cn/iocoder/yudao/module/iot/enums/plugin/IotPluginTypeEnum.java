package cn.iocoder.yudao.module.iot.enums.plugin;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 插件类型枚举
 *
 * @author haohao
 */
@AllArgsConstructor
@Getter
public enum IotPluginTypeEnum implements ArrayValuable<Integer> {

    NORMAL(0, "普通插件"),
    DEVICE(1, "设备插件");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotPluginTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
