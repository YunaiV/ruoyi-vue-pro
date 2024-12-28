package cn.iocoder.yudao.module.iot.enums.thingmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IOT 产品物模型服务调用方式枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum IotThingModelServiceCallTypeEnum {

    ASYNC("async"), // 异步调用
    SYNC("sync"); // 同步调用

    private final String type;

}
