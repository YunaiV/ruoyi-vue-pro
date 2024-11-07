package cn.iocoder.yudao.module.iot.enums.device;

import cn.iocoder.yudao.framework.common.core.IntArrayValuable;
import lombok.Getter;

import java.util.Arrays;

/**
 * IoT 设备状态枚举
 *
 * @author haohao
 */
@Getter
public enum IotDeviceStatusEnum implements IntArrayValuable {

    INACTIVE(0, "未激活"),
    ONLINE(1, "在线"),
    OFFLINE(2, "离线"),
    DISABLED(3, "已禁用");

    public static final int[] ARRAYS = Arrays.stream(values()).mapToInt(IotDeviceStatusEnum::getStatus).toArray();

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
    public int[] array() {
        return ARRAYS;
    }

}
