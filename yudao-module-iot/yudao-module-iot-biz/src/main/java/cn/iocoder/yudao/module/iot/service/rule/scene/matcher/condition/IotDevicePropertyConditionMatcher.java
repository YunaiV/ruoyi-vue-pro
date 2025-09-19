package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;


import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import org.springframework.stereotype.Component;


/**
 * 设备属性条件匹配器：处理设备属性相关的子条件匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class IotDevicePropertyConditionMatcher implements IotSceneRuleConditionMatcher {

    @Override
    public IotSceneRuleConditionTypeEnum getSupportedConditionType() {
        return IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY;
    }

    @Override
    public boolean matches(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        // 1.1 基础参数校验
        if (!IotSceneRuleMatcherHelper.isBasicConditionValid(condition)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "条件基础参数无效");
            return false;
        }

        // 1.2 检查标识符是否匹配
        String messageIdentifier = IotDeviceMessageUtils.getIdentifier(message);
        if (!IotSceneRuleMatcherHelper.isIdentifierMatched(condition.getIdentifier(), messageIdentifier)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "标识符不匹配，期望: " + condition.getIdentifier() + ", 实际: " + messageIdentifier);
            return false;
        }

        // 1.3 检查操作符和参数是否有效
        if (!IotSceneRuleMatcherHelper.isConditionOperatorAndParamValid(condition)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "操作符或参数无效");
            return false;
        }

        // 2.1. 获取属性值 - 使用工具类方法正确提取属性值
        Object propertyValue = IotDeviceMessageUtils.extractPropertyValue(message, condition.getIdentifier());
        if (propertyValue == null) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "消息中属性值为空或未找到指定属性");
            return false;
        }

        // 2.2 使用条件评估器进行匹配
        boolean matched = IotSceneRuleMatcherHelper.evaluateCondition(propertyValue, condition.getOperator(), condition.getParam());
        if (matched) {
            IotSceneRuleMatcherHelper.logConditionMatchSuccess(message, condition);
        } else {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "设备属性条件不匹配");
        }
        return matched;
    }

    @Override
    public int getPriority() {
        return 25; // 中等优先级
    }

}
