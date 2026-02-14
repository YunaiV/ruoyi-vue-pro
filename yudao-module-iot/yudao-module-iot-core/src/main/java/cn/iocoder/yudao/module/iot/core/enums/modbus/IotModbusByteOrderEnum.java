package cn.iocoder.yudao.module.iot.core.enums.modbus;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT Modbus 字节序枚举
 *
 * @author 芋道源码
 */
@Getter
@RequiredArgsConstructor
public enum IotModbusByteOrderEnum implements ArrayValuable<String> {

    AB("AB", "大端序（16位）", 2),
    BA("BA", "小端序（16位）", 2),
    ABCD("ABCD", "大端序（32位）", 4),
    CDAB("CDAB", "大端字交换（32位）", 4),
    DCBA("DCBA", "小端序（32位）", 4),
    BADC("BADC", "小端字交换（32位）", 4);

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotModbusByteOrderEnum::getOrder)
            .toArray(String[]::new);

    /**
     * 字节序
     */
    private final String order;
    /**
     * 名称
     */
    private final String name;
    /**
     * 字节数
     */
    private final Integer byteCount;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotModbusByteOrderEnum getByOrder(String order) {
        return Arrays.stream(values())
                .filter(e -> e.getOrder().equals(order))
                .findFirst()
                .orElse(null);
    }

}
