package cn.iocoder.yudao.module.iot.core.enums;

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
//    PROPERTY("property"), // 设备属性：可参考 https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services 设备属性、事件、服务
    EVENT("event"), // 设备事件：可参考 https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services 设备属性、事件、服务
    SERVICE("service"), // 设备服务：可参考 https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services 设备属性、事件、服务
    CONFIG("config"), // 设备配置：可参考 https://help.aliyun.com/zh/iot/user-guide/remote-configuration-1 远程配置
    OTA("ota"), // 设备 OTA：可参考 https://help.aliyun.com/zh/iot/user-guide/ota-update OTA 升级
    REGISTER("register"), // 设备注册：可参考 https://help.aliyun.com/zh/iot/user-guide/register-devices 设备身份注册
    TOPOLOGY("topology"),; // 设备拓扑：可参考 https://help.aliyun.com/zh/iot/user-guide/manage-topological-relationships 设备拓扑

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
