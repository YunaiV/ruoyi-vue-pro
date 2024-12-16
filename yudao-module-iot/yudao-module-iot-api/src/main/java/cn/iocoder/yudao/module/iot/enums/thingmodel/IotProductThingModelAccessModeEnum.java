package cn.iocoder.yudao.module.iot.enums.thingmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IOT 访问方式枚举类
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
