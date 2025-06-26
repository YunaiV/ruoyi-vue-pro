package cn.iocoder.yudao.module.iot.enums.ota;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT OTA 升级任务的范围枚举
 *
 * @author haohao
 */
@RequiredArgsConstructor
@Getter
public enum IotOtaUpgradeTaskStatusEnum implements ArrayValuable<Integer> {

    IN_PROGRESS(10), // 进行中：升级中
    COMPLETED(20), // 已完成：已结束，全部升级完成
    INCOMPLETE(21), // 未完成：已结束，部分升级完成
    CANCELED(30),; // 已取消：一般是主动取消任务

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotOtaUpgradeTaskStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 范围
     */
    private final Integer status;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}