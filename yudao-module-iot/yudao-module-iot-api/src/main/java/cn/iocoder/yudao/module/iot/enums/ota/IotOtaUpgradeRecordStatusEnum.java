package cn.iocoder.yudao.module.iot.enums.ota;


import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT OTA 升级记录的范围枚举
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Getter
public enum IotOtaUpgradeRecordStatusEnum implements ArrayValuable<Integer> {

    PENDING(0), // 待推送
    PUSHED(10), // 已推送
    UPGRADING(20), // 升级中
    SUCCESS(30), // 升级成功
    FAILURE(40), // 升级失败
    CANCELED(50),; // 已取消

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotOtaUpgradeRecordStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 范围
     */
    private final Integer status;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}