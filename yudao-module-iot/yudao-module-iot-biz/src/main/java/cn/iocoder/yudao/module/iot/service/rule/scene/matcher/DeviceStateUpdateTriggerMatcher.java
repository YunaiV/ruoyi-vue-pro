package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 设备状态更新触发器匹配器
 * <p>
 * 处理设备上下线状态变更的触发器匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class DeviceStateUpdateTriggerMatcher extends AbstractIotSceneRuleMatcher {

    // TODO @puhui999：是不是不用枚举哈；
    /**
     * 设备状态更新消息方法
     */
    private static final String DEVICE_STATE_UPDATE_METHOD = IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod();

    @Override
    public MatcherType getMatcherType() {
        return MatcherType.TRIGGER;
    }

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE;
    }

    @Override
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1. 基础参数校验
        if (!isBasicTriggerValid(trigger)) {
            logTriggerMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }

        // 2. 检查消息方法是否匹配
        if (!DEVICE_STATE_UPDATE_METHOD.equals(message.getMethod())) {
            logTriggerMatchFailure(message, trigger, "消息方法不匹配，期望: " + DEVICE_STATE_UPDATE_METHOD + ", 实际: " + message.getMethod());
            return false;
        }

        // 3. 检查操作符和值是否有效
        if (!isTriggerOperatorAndValueValid(trigger)) {
            logTriggerMatchFailure(message, trigger, "操作符或值无效");
            return false;
        }

        // 4. 获取设备状态值
        Object stateValue = message.getData();
        if (stateValue == null) {
            logTriggerMatchFailure(message, trigger, "消息中设备状态值为空");
            return false;
        }

        // 5. 使用条件评估器进行匹配
        boolean matched = evaluateCondition(stateValue, trigger.getOperator(), trigger.getValue());
        if (matched) {
            logTriggerMatchSuccess(message, trigger);
        } else {
            logTriggerMatchFailure(message, trigger, "状态值条件不匹配");
        }
        return matched;
    }

    @Override
    public int getPriority() {
        return 10; // 高优先级
    }

}
