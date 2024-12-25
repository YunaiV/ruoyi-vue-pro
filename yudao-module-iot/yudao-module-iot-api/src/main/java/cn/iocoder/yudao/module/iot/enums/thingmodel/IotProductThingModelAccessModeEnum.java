package cn.iocoder.yudao.module.iot.enums.thingmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IOT 产品物模型属性读取类型枚举
 *
 * @author ahh
 */
@AllArgsConstructor
@Getter
public enum IotProductThingModelAccessModeEnum {

    READ_ONLY("r"),
    READ_WRITE("rw");

    private final String mode;

}
