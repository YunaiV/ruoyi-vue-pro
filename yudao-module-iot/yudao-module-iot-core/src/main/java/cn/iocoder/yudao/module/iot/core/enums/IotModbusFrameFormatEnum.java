package cn.iocoder.yudao.module.iot.core.enums;

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
public enum IotModbusFrameFormatEnum implements ArrayValuable<String> {

    MODBUS_TCP("modbus_tcp", "Modbus TCP"),
    MODBUS_RTU("modbus_rtu", "Modbus RTU");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotModbusFrameFormatEnum::getFormat)
            .toArray(String[]::new);

    /**
     * 格式
     */
    private final String format;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
