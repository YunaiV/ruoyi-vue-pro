package cn.iocoder.yudao.module.iot.enums.rule;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
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
public enum IotSceneRuleActionTypeEnum implements ArrayValuable<Integer> {

    /**
     * 设备属性设置
     *
     * 对应 {@link IotDeviceMessageMethodEnum#PROPERTY_SET}
     */
    DEVICE_PROPERTY_SET(1),
    /**
     * 设备服务调用
     *
     * 对应 {@link IotDeviceMessageMethodEnum#SERVICE_INVOKE}
     */
    DEVICE_SERVICE_INVOKE(2),

    /**
     * 告警触发
     */
    ALERT_TRIGGER(100),
    /**
     * 告警恢复
     */
    ALERT_RECOVER(101),

    ;

    private final Integer type;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotSceneRuleActionTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
