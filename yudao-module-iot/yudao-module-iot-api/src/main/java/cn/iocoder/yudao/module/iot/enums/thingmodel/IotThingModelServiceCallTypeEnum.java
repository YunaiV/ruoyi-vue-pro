package cn.iocoder.yudao.module.iot.enums.thingmodel;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 产品物模型服务调用方式枚举
 *
 * @author HUIHUI
 */
@AllArgsConstructor
@Getter
public enum IotThingModelServiceCallTypeEnum implements ArrayValuable<String> {

    ASYNC("async"), // 异步调用
    SYNC("sync"); // 同步调用

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotThingModelServiceCallTypeEnum::getType).toArray(String[]::new);

    private final String type;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
