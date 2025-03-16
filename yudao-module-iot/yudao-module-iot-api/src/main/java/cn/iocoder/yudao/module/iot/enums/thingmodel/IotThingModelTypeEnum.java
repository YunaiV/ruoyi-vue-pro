package cn.iocoder.yudao.module.iot.enums.thingmodel;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品功能（物模型）类型枚举类
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotThingModelTypeEnum implements ArrayValuable<Integer> {

    PROPERTY(1, "属性"),
    SERVICE(2, "服务"),
    EVENT(3, "事件");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotThingModelTypeEnum::getType).toArray(Integer[]::new);

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
