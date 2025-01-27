package cn.iocoder.yudao.module.iot.enums.plugin;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 插件状态枚举
 *
 * @author haohao
 */
@Getter
public enum IotPluginStatusEnum implements ArrayValuable<Integer> {

    STOPPED(0, "停止"),
    RUNNING(1, "运行");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotPluginStatusEnum::getStatus).toArray(Integer[]::new);

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
        return Arrays.stream(values())
                .filter(value -> value.getStatus().equals(state))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isValidState(Integer state) {
        return fromState(state) != null;
    }

    public static boolean contains(Integer status) {
        return Arrays.stream(values()).anyMatch(e -> e.getStatus().equals(status));
    }

}
