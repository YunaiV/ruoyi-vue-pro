package cn.iocoder.yudao.module.iot.enums.device;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 设备状态枚举
 *
 * @author haohao
 */
@Getter
public enum IotDeviceStatusEnum implements ArrayValuable<Integer> {

    INACTIVE(0, "未激活"),
    ONLINE(1, "在线"),
    OFFLINE(2, "离线"),
    DISABLED(3, "已禁用");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotDeviceStatusEnum::getStatus).toArray(Integer[]::new);

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

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
