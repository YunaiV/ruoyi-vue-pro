package cn.iocoder.yudao.module.iot.core.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT Modbus 模式枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotModbusModeEnum implements ArrayValuable<Integer> {

    POLLING(1, "云端轮询"),
    ACTIVE_REPORT(2, "主动上报");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotModbusModeEnum::getMode)
            .toArray(Integer[]::new);

    /**
     * 模式
     */
    private final Integer mode;
    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
