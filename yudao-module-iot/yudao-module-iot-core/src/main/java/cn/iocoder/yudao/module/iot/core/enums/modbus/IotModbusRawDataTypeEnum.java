package cn.iocoder.yudao.module.iot.core.enums.modbus;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT Modbus 原始数据类型枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotModbusRawDataTypeEnum implements ArrayValuable<String> {

    INT16("INT16", "有符号 16 位整数", 1),
    UINT16("UINT16", "无符号 16 位整数", 1),
    INT32("INT32", "有符号 32 位整数", 2),
    UINT32("UINT32", "无符号 32 位整数", 2),
    FLOAT("FLOAT", "32 位浮点数", 2),
    DOUBLE("DOUBLE", "64 位浮点数", 4),
    BOOLEAN("BOOLEAN", "布尔值（用于线圈）", 1),
    STRING("STRING", "字符串", null); // null 表示可变长度

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotModbusRawDataTypeEnum::getType)
            .toArray(String[]::new);

    /**
     * 数据类型
     */
    private final String type;
    /**
     * 名称
     */
    private final String name;
    /**
     * 寄存器数量（null 表示可变）
     */
    private final Integer registerCount;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotModbusRawDataTypeEnum getByType(String type) {
        return Arrays.stream(values())
                .filter(e -> e.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

}
