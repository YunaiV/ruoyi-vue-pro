package cn.iocoder.yudao.module.iot.core.topic.auth;

import lombok.Data;

// TODO @AI：修复建议，参考 /Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-iot/yudao-module-iot-core/src/main/java/cn/iocoder/yudao/module/iot/core/topic/auth/IotSubDeviceRegisterReqDTO.java
/**
 * IoT 子设备动态注册 Response DTO
 * <p>
 * 用于 thing.sub.register 响应的设备信息
 *
 * @author 芋道源码
 */
@Data
public class IotSubDeviceRegisterRespDTO {

    /**
     * 子设备 ProductKey
     */
    private String productKey;

    /**
     * 子设备 DeviceName
     */
    private String deviceName;

    /**
     * 分配的 DeviceSecret
     */
    private String deviceSecret;

}
