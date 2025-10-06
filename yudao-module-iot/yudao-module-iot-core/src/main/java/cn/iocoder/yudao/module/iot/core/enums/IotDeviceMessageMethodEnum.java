package cn.iocoder.yudao.module.iot.core.enums;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * IoT 设备消息的方法枚举
 *
 * @author haohao
 */
@Getter
@AllArgsConstructor
public enum IotDeviceMessageMethodEnum implements ArrayValuable<String> {

    // ========== 设备状态 ==========

    STATE_UPDATE("thing.state.update", "设备状态更新", true),

    // TODO 芋艿：要不要加个 ping 消息；

    // ========== 设备属性 ==========
    // 可参考：https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services

    PROPERTY_POST("thing.property.post", "属性上报", true),
    PROPERTY_SET("thing.property.set", "属性设置", false),

    // ========== 设备事件 ==========
    // 可参考：https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services

    EVENT_POST("thing.event.post", "事件上报", true),

    // ========== 设备服务调用 ==========
    // 可参考：https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services

    SERVICE_INVOKE("thing.service.invoke", "服务调用", false),

    // ========== 设备配置 ==========
    // 可参考：https://help.aliyun.com/zh/iot/user-guide/remote-configuration-1

    CONFIG_PUSH("thing.config.push", "配置推送", false),

    // ========== OTA 固件 ==========
    // 可参考：https://help.aliyun.com/zh/iot/user-guide/perform-ota-updates

    OTA_UPGRADE("thing.ota.upgrade", "OTA 固定信息推送", false),
    OTA_PROGRESS("thing.ota.progress", "OTA 升级进度上报", true),
    ;

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotDeviceMessageMethodEnum::getMethod)
            .toArray(String[]::new);

    /**
     * 不进行 reply 回复的方法集合
     */
    public static final Set<String> REPLY_DISABLED = SetUtils.asSet(
            STATE_UPDATE.getMethod(),
            OTA_PROGRESS.getMethod() // 参考阿里云，OTA 升级进度上报，不进行回复
    );

    private final String method;

    private final String name;

    private final Boolean upstream;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotDeviceMessageMethodEnum of(String method) {
        return ArrayUtil.firstMatch(item -> item.getMethod().equals(method),
                IotDeviceMessageMethodEnum.values());
    }

    public static boolean isReplyDisabled(String method) {
        return REPLY_DISABLED.contains(method);
    }

}
