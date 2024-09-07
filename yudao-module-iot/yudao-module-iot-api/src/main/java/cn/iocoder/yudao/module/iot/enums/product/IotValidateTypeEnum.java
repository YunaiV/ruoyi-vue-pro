package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IOT 数据校验级别枚举类
 * 数据校验级别,  0: 弱校验, 1: 免校验
 */
@AllArgsConstructor
@Getter
public enum IotValidateTypeEnum implements IntArrayValuable {

    WEAK(0, "弱校验"),
    NONE(1, "免校验");


    /**
     * 类型
     */
    private final Integer type;

    /**
     * 描述
     */
    private final String description;

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotValidateTypeEnum::getType).toArray();

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
