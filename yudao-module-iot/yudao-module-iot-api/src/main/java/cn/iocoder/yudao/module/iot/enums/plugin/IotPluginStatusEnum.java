package cn.iocoder.yudao.module.iot.enums.plugin;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 插件状态枚举
 *
 * @author haohao
 */
@Getter
public enum IotPluginStatusEnum implements IntArrayValuable {

    STOPPED(0, "停止"),
    RUNNING(1, "运行");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotPluginStatusEnum::getStatus).toArray();

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

    IotPluginStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public static IotPluginStatusEnum fromState(Integer state) {
        for (IotPluginStatusEnum value : values()) {
            if (value.getStatus().equals(state)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isValidState(Integer state) {
        return fromState(state) != null;
    }

    public static boolean contains(Integer status) {
        return Arrays.stream(values()).anyMatch(e -> e.getStatus().equals(status));
    }

    @Override
    public int[] array() {
        return new int[0];
    }
}
