package cn.iocoder.yudao.module.iot.enums.plugin;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 插件状态枚举
 *
 * @author haohao
 */
@RequiredArgsConstructor
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

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
