package cn.iocoder.yudao.module.iot.enums.thingmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO @puhui999：加个 ArrayValuable
/**
 * IOT 产品物模型参数是输入参数还是输出参数枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum IotThingModelParamDirectionEnum {

    INPUT("input"), // 输入参数
    OUTPUT("output"); // 输出参数

    private final String direction;

}
