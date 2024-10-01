package cn.iocoder.yudao.module.iot.enums.product;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IOT 产品功能（物模型）类型枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotProductFunctionTypeEnum implements IntArrayValuable {

    PROPERTY(1, "属性"),
    SERVICE(2, "服务"),
    EVENT(3, "事件");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotProductFunctionTypeEnum::getType).toArray();

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
