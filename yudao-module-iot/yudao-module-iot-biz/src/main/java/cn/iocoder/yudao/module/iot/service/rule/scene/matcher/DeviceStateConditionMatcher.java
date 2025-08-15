package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionLevelEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 设备状态条件匹配器
 * <p>
 * 处理设备状态相关的子条件匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class DeviceStateConditionMatcher extends AbstractIotSceneRuleMatcher {

    @Override
    public MatcherType getMatcherType() {
        return MatcherType.CONDITION;
    }

    @Override
    public IotSceneRuleConditionTypeEnum getSupportedConditionType() {
        return IotSceneRuleConditionTypeEnum.DEVICE_STATE;
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

        // 2. 检查操作符和参数是否有效
        if (!isConditionOperatorAndParamValid(condition)) {
            logConditionMatchFailure(message, condition, "操作符或参数无效");
            return false;
        }

        // 3. 获取设备状态值
        // 设备状态通常在消息的 data 字段中
        Object stateValue = message.getData();
        if (stateValue == null) {
            logConditionMatchFailure(message, condition, "消息中设备状态值为空");
            return false;
        }

        // 4. 使用条件评估器进行匹配
        boolean matched = evaluateCondition(stateValue, condition.getOperator(), condition.getParam());

        if (matched) {
            logConditionMatchSuccess(message, condition);
        } else {
            logConditionMatchFailure(message, condition, "设备状态条件不匹配");
        }

        return matched;
    }

    @Override
    public int getPriority() {
        return 30; // 中等优先级
    }

}
