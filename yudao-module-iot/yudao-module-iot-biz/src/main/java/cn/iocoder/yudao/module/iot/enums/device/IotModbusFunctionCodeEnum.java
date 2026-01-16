package cn.iocoder.yudao.module.iot.enums.device;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

// TODO @AI：如果枚举需要共享，可以拿到 /Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-iot/yudao-module-iot-core/src/main/java/cn/iocoder/yudao/module/iot/core/enums 里
/**
 * IoT Modbus 功能码枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotModbusFunctionCodeEnum implements ArrayValuable<Integer> {

    READ_COILS(1, "读线圈", true, 5, 15),
    READ_DISCRETE_INPUTS(2, "读离散输入", false, null, null),
    READ_HOLDING_REGISTERS(3, "读保持寄存器", true, 6, 16),
    READ_INPUT_REGISTERS(4, "读输入寄存器", false, null, null);

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotModbusFunctionCodeEnum::getCode)
            .toArray(Integer[]::new);

    /**
     * 功能码
     */
    private final Integer code;
    /**
     * 名称
     */
    private final String name;
    /**
     * 是否支持写操作
     */
    private final Boolean writable;
    /**
     * 单个写功能码
     */
    private final Integer writeSingleCode;
    /**
     * 多个写功能码
     */
    private final Integer writeMultipleCode;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 根据功能码获取枚举
     *
     * @param code 功能码
     * @return 枚举
     */
    public static IotModbusFunctionCodeEnum valueOf(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

}
