package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 数据校验级别枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotValidateTypeEnum implements ArrayValuable<Integer> {

    WEAK(0, "弱校验"),
    NONE(1, "免校验");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotValidateTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
