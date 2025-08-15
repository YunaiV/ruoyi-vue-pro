package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionLevelEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 设备属性条件匹配器
 * <p>
 * 处理设备属性相关的子条件匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class DevicePropertyConditionMatcher extends AbstractIotSceneRuleMatcher {

    @Override
    public MatcherType getMatcherType() {
        return MatcherType.CONDITION;
    }

    @Override
    public IotSceneRuleConditionTypeEnum getSupportedConditionType() {
        return IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY;
    }

    @Override
    public IotSceneRuleConditionLevelEnum getSupportedConditionLevel() {
        return IotSceneRuleConditionLevelEnum.SECONDARY;
    }

    @Override
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        // 1. 基础参数校验
        if (!isBasicConditionValid(condition)) {
            logConditionMatchFailure(message, condition, "条件基础参数无效");
            return false;
        }

        // 2. 检查标识符是否匹配
        String messageIdentifier = IotDeviceMessageUtils.getIdentifier(message);
        if (!isIdentifierMatched(condition.getIdentifier(), messageIdentifier)) {
            logConditionMatchFailure(message, condition, "标识符不匹配，期望: " + condition.getIdentifier() + ", 实际: " + messageIdentifier);
            return false;
        }

        // 3. 检查操作符和参数是否有效
        if (!isConditionOperatorAndParamValid(condition)) {
            logConditionMatchFailure(message, condition, "操作符或参数无效");
            return false;
        }

        // 4. 获取属性值
        Object propertyValue = message.getData();
        if (propertyValue == null) {
            logConditionMatchFailure(message, condition, "消息中属性值为空");
            return false;
        }

        // 5. 使用条件评估器进行匹配
        boolean matched = evaluateCondition(propertyValue, condition.getOperator(), condition.getParam());

        if (matched) {
            logConditionMatchSuccess(message, condition);
        } else {
            logConditionMatchFailure(message, condition, "设备属性条件不匹配");
        }

        return matched;
    }

    @Override
    public int getPriority() {
        return 25; // 中等优先级
    }

}
