package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品脚本状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum IotProductScriptStatusEnum implements ArrayValuable<Integer> {

    ENABLE(0, "启用"),
    DISABLE(1, "禁用"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotProductScriptStatusEnum::getStatus)
            .toArray(Integer[]::new);

    /**
     * 状态值
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

    public static IotProductScriptStatusEnum getByStatus(Integer status) {
        return Arrays.stream(values())
                .filter(type -> type.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }

    public static boolean isEnable(Integer status) {
        return ENABLE.getStatus().equals(status);
    }

    public static boolean isDisable(Integer status) {
        return DISABLE.getStatus().equals(status);
    }
}