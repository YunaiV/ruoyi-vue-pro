package cn.iocoder.yudao.module.iot.core.enums.modbus;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT Modbus 数据帧格式枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotModbusFrameFormatEnum implements ArrayValuable<Integer> {

    MODBUS_TCP(1),
    MODBUS_RTU(2);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotModbusFrameFormatEnum::getFormat)
            .toArray(Integer[]::new);

    /**
     * 格式
     */
    private final Integer format;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
