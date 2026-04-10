package cn.iocoder.yudao.module.iot.enums.ota;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT OTA 升级任务的设备范围枚举
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Getter
public enum IotOtaTaskDeviceScopeEnum implements ArrayValuable<Integer> {

    ALL(1), // 全部设备：只包括当前产品下的设备，不包括未来创建的设备
    SELECT(2); // 指定设备

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotOtaTaskDeviceScopeEnum::getScope).toArray(Integer[]::new);

    /**
     * 范围
     */
    private final Integer scope;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
