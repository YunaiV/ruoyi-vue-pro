package cn.iocoder.yudao.module.iot.enums.rule;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 规则场景的触发类型枚举
 *
 * 设备触发，定时触发
 */
@RequiredArgsConstructor
@Getter
public enum IotRuleSceneActionTypeEnum implements ArrayValuable<Integer> {

    DEVICE_CONTROL(1), // 设备执行
    ALERT(2), // 告警执行
    DATA_BRIDGE(3); // 桥接执行

    private final Integer type;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotRuleSceneActionTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
