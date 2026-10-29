package cn.iocoder.yudao.module.iot.enums;

/**
 * IoT 权限标识常量
 *
 * 只收录业务代码中动态校验的权限标识，与菜单配置的权限标识保持一致；接口上的 {@code @PreAuthorize} 仍直接书写权限标识
 *
 * @author 芋道源码
 */
public interface IotPermissionConstants {

    /**
     * 设备消息的透传下发（raw.push）：可以直接驱动 Modbus 写，需要独立的按钮权限
     */
    String DEVICE_MESSAGE_RAW_SEND = "iot:device:message-raw-send";

}
