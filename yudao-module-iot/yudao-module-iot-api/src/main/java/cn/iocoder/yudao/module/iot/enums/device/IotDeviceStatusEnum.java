package cn.iocoder.yudao.module.iot.enums.device;

import lombok.Getter;

/**
 * IoT 设备状态枚举
 * 设备状态：0 - 未激活，1 - 在线，2 - 离线，3 - 已禁用
 */
@Getter
public enum IotDeviceStatusEnum {

    INACTIVE(0, "未激活"),
    ONLINE(1, "在线"),
    OFFLINE(2, "离线"),
    DISABLED(3, "已禁用");

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    IotDeviceStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public static IotDeviceStatusEnum fromStatus(Integer status) {
        for (IotDeviceStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isValidStatus(Integer status) {
        return fromStatus(status) != null;
    }
}
