package cn.iocoder.yudao.module.iot.enums.device;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 设备消息类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum IotDeviceMessageTypeEnum implements ArrayValuable<String> {

    STATE("state"), // 设备状态
    PROPERTY("property"), // 设备属性
    EVENT("event"), // 设备事件
    SERVICE("service"); // 设备服务

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotDeviceMessageTypeEnum::getType).toArray(String[]::new);

    /**
     * 属性
     */
    private final String type;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
