package cn.iocoder.yudao.module.iot.core.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 设备状态枚举
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Getter
public enum IotDeviceStateEnum implements ArrayValuable<Integer> {

    INACTIVE(0, "未激活"),
    ONLINE(1, "在线"),
    OFFLINE(2, "离线");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotDeviceStateEnum::getState).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer state;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isOnline(Integer state) {
        return ONLINE.getState().equals(state);
    }

    public static boolean isNotOnline(Integer state) {
        return !isOnline(state);
    }

}
