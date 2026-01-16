package cn.iocoder.yudao.module.iot.enums.device;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

// TODO @AI：如果枚举需要共享，可以拿到 /Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-iot/yudao-module-iot-core/src/main/java/cn/iocoder/yudao/module/iot/core/enums 里
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
    STRING("STRING", "字符串", -1); // -1 表示可变长度

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
    // TODO @AI：去掉 default 会好点。null 表示可变；
    /**
     * 默认寄存器数量（-1 表示可变）
     */
    private final Integer defaultRegisterCount;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    // TODO @AI：如果不用，可以删除掉
    /**
     * 根据类型获取枚举
     *
     * @param type 类型
     * @return 枚举
     */
    public static IotModbusRawDataTypeEnum getByType(String type) {
        return Arrays.stream(values())
                .filter(e -> e.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

}
