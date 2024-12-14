package cn.iocoder.yudao.module.iot.enums.plugin;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 插件类型枚举
 *
 * @author haohao
 */
@Getter
public enum IotPluginTypeEnum implements IntArrayValuable {

    NORMAL(0, "普通插件"),
    DEVICE(1, "设备插件");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotPluginTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;

    /**
     * 类型名
     */
    private final String name;

    IotPluginTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    public static IotPluginTypeEnum fromType(Integer type) {
        for (IotPluginTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isValidType(Integer type) {
        return fromType(type) != null;
    }

    @Override
    public int[] array() {
        return ARRAYS;
    }
}
