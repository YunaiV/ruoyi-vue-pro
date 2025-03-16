package cn.iocoder.yudao.module.iot.enums.thingmodel;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;


/**
 * IoT 产品物模型参数是输入参数还是输出参数枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum IotThingModelParamDirectionEnum implements ArrayValuable<String> {

    INPUT("input"), // 输入参数
    OUTPUT("output"); // 输出参数

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotThingModelParamDirectionEnum::getDirection).toArray(String[]::new);

    private final String direction;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
