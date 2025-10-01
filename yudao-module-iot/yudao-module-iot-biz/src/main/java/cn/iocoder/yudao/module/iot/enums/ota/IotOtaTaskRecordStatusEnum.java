package cn.iocoder.yudao.module.iot.enums.ota;


import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * IoT OTA 升级任务记录的状态枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotOtaTaskRecordStatusEnum implements ArrayValuable<Integer> {

    PENDING(0), // 待推送
    PUSHED(10), // 已推送
    UPGRADING(20), // 升级中
    SUCCESS(30), // 升级成功
    FAILURE(40), // 升级失败
    CANCELED(50),; // 升级取消

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotOtaTaskRecordStatusEnum::getStatus).toArray(Integer[]::new);

    public static final Set<Integer> IN_PROCESS_STATUSES = SetUtils.asSet(
            PENDING.getStatus(),
            PUSHED.getStatus(),
            UPGRADING.getStatus());

    public static final List<Integer> PRIORITY_STATUSES = Arrays.asList(
            SUCCESS.getStatus(),
            PENDING.getStatus(), PUSHED.getStatus(), UPGRADING.getStatus(),
            FAILURE.getStatus(), CANCELED.getStatus());

    /**
     * 状态
     */
    private final Integer status;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static IotOtaTaskRecordStatusEnum of(Integer status) {
        return ArrayUtil.firstMatch(o -> o.getStatus().equals(status), values());
    }

}