package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IOT 数据校验级别枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotValidateTypeEnum implements IntArrayValuable {

    WEAK(0, "弱校验"),
    NONE(1, "免校验");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotValidateTypeEnum::getType).toArray();

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String description;

    @Override
    public int[] array() {
        return ARRAYS;
    }

}
