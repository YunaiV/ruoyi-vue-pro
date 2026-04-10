package cn.iocoder.yudao.module.iot.enums.rule;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 场景流转的触发类型枚举
 *
 * 为什么不直接使用 IotDeviceMessageMethodEnum 呢？
 * 原因是，物模型属性上报，存在批量上报的情况，不只对应一个 method！！！
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotSceneRuleTriggerTypeEnum implements ArrayValuable<Integer> {

    /**
     * 设备上下线变更
     *
     * 对应 IotDeviceMessageMethodEnum.STATE_UPDATE
     */
    DEVICE_STATE_UPDATE(1),
    /**
     * 物模型属性上报
     *
     * 对应 IotDeviceMessageMethodEnum.DEVICE_PROPERTY_POST
     */
    DEVICE_PROPERTY_POST(2),
    /**
     * 设备事件上报
     *
     * 对应 IotDeviceMessageMethodEnum.DEVICE_EVENT_POST
     */
    DEVICE_EVENT_POST(3),
    /**
     * 设备服务调用
     *
     * 对应 IotDeviceMessageMethodEnum.DEVICE_SERVICE_INVOKE
     */
    DEVICE_SERVICE_INVOKE(4),

    /**
     * 定时触发
     */
    TIMER(100)

    ;

    private final Integer type;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(IotSceneRuleTriggerTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static IotSceneRuleTriggerTypeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

}
