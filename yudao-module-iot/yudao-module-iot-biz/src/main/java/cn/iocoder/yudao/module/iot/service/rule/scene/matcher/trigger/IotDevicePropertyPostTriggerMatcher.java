package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import org.springframework.stereotype.Component;

/**
 * 设备属性上报触发器匹配器：处理设备属性数据上报的触发器匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class IotDevicePropertyPostTriggerMatcher implements IotSceneRuleTriggerMatcher {

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST;
    }

    @Override
    public boolean matches(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1.1 基础参数校验
        if (!IotSceneRuleMatcherHelper.isBasicTriggerValid(trigger)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }

        // 1.2 检查消息方法是否匹配
        if (!IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod().equals(message.getMethod())) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息方法不匹配，期望: " +
                    IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod() + ", 实际: " + message.getMethod());
            return false;
        }

        // 1.3 检查消息中是否包含触发器指定的属性标识符
        // 注意：属性上报可能同时上报多个属性，所以需要判断 trigger.getIdentifier() 是否在 message 的 params 中
        if (IotDeviceMessageUtils.notContainsIdentifier(message, trigger.getIdentifier())) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息中不包含属性: " +
                    trigger.getIdentifier());
            return false;
        }

        // 1.4 检查操作符和值是否有效
        if (!IotSceneRuleMatcherHelper.isTriggerOperatorAndValueValid(trigger)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "操作符或值无效");
            return false;
        }

        // 2.1 获取属性值 - 使用工具类方法正确提取属性值
        Object propertyValue = IotDeviceMessageUtils.extractPropertyValue(message, trigger.getIdentifier());
        if (propertyValue == null) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息中属性值为空或未找到指定属性");
            return false;
        }

        // 2.2 使用条件评估器进行匹配
        boolean matched = IotSceneRuleMatcherHelper.evaluateCondition(propertyValue, trigger.getOperator(), trigger.getValue());
        if (matched) {
            IotSceneRuleMatcherHelper.logTriggerMatchSuccess(message, trigger);
        } else {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "属性值条件不匹配");
        }
        return matched;
    }

    @Override
    public int getPriority() {
        return 20; // 中等优先级
    }

}
