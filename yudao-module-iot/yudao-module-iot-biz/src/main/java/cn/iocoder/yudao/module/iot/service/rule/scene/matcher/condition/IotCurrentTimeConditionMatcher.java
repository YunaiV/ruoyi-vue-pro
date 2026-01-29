package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotSceneRuleTimeHelper;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 当前时间条件匹配器：处理时间相关的子条件匹配逻辑
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotCurrentTimeConditionMatcher implements IotSceneRuleConditionMatcher {

    @Override
    public IotSceneRuleConditionTypeEnum getSupportedConditionType() {
        return IotSceneRuleConditionTypeEnum.CURRENT_TIME;
    }

    @Override
    public boolean matches(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        // 1.1 基础参数校验
        if (!IotSceneRuleMatcherHelper.isBasicConditionValid(condition)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "条件基础参数无效");
            return false;
        }

        // 1.2 检查操作符和参数是否有效
        if (!IotSceneRuleMatcherHelper.isConditionOperatorAndParamValid(condition)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "操作符或参数无效");
            return false;
        }

        // 1.3 验证操作符是否为支持的时间操作符
        String operator = condition.getOperator();
        IotSceneRuleConditionOperatorEnum operatorEnum = IotSceneRuleConditionOperatorEnum.operatorOf(operator);
        if (operatorEnum == null) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "无效的操作符: " + operator);
            return false;
        }

        if (IotSceneRuleTimeHelper.isTimeOperator(operatorEnum)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "不支持的时间操作符: " + operator);
            return false;
        }

        // 2.1 执行时间匹配
        boolean matched = IotSceneRuleTimeHelper.executeTimeMatching(operatorEnum, condition.getParam());

        // 2.2 记录匹配结果
        if (matched) {
            IotSceneRuleMatcherHelper.logConditionMatchSuccess(message, condition);
        } else {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "时间条件不匹配");
        }

        return matched;
    }

    @Override
    public int getPriority() {
        return 40; // 较低优先级
    }

}
