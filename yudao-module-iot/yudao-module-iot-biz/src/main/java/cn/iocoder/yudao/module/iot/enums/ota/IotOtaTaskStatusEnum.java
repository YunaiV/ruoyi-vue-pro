package cn.iocoder.yudao.module.iot.enums.ota;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT OTA 升级任务的状态
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotOtaTaskStatusEnum implements ArrayValuable<Integer> {

    IN_PROGRESS(10), // 进行中（升级中）
    END(20), // 已结束（包括全部成功、部分成功）
    CANCELED(30),; // 已取消（一般是主动取消任务）

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotOtaTaskStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}