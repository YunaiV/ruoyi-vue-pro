package cn.iocoder.yudao.module.iot.enums.thingmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IOT 产品物模型参数是输入参数还是输出参数枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum IotProductThingModelParamDirectionEnum {

    INPUT("input"), // 输入参数
    OUTPUT("output"); // 输出参数

    private final String direction;

}
