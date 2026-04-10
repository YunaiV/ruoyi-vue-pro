package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import org.springframework.stereotype.Component;

/**
 * 设备状态更新触发器匹配器：处理设备上下线状态变更的触发器匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class IotDeviceStateUpdateTriggerMatcher implements IotSceneRuleTriggerMatcher {

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE;
    }

    @Override
    public boolean matches(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1.1 基础参数校验
        if (!IotSceneRuleMatcherHelper.isBasicTriggerValid(trigger)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }

        // 1.2 检查消息方法是否匹配
        if (!IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod().equals(message.getMethod())) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息方法不匹配，期望: " +
                    IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod() + ", 实际: " + message.getMethod());
            return false;
        }

        // 1.3 检查操作符和值是否有效
        if (!IotSceneRuleMatcherHelper.isTriggerOperatorAndValueValid(trigger)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "操作符或值无效");
            return false;
        }

        // 2.1 获取设备状态值 - 使用工具类方法正确提取状态值
        // 对于状态更新消息，状态值通过 getIdentifier 获取（实际是从 params.state 字段）
        String stateIdentifier = IotDeviceMessageUtils.getIdentifier(message);
        if (stateIdentifier == null) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息中设备状态值为空");
            return false;
        }

        // 2.2 使用条件评估器进行匹配
        // 状态值通常是字符串或数字，直接使用标识符作为状态值
        boolean matched = IotSceneRuleMatcherHelper.evaluateCondition(stateIdentifier, trigger.getOperator(), trigger.getValue());
        if (matched) {
            IotSceneRuleMatcherHelper.logTriggerMatchSuccess(message, trigger);
        } else {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "状态值条件不匹配");
        }
        return matched;
    }

    @Override
    public int getPriority() {
        return 10; // 高优先级
    }

}
