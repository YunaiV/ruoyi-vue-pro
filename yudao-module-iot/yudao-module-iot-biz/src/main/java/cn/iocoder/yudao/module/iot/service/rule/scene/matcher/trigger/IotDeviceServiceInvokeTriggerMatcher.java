package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 设备服务调用触发器匹配器：处理设备服务调用的触发器匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class IotDeviceServiceInvokeTriggerMatcher implements IotSceneRuleTriggerMatcher {

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE;
    }

    @Override
    public boolean matches(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1.1 基础参数校验
        if (!IotSceneRuleMatcherHelper.isBasicTriggerValid(trigger)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }
        // 1.2 检查消息方法是否匹配
        if (!IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod().equals(message.getMethod())) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息方法不匹配，期望: " + IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod() + ", 实际: " + message.getMethod());
            return false;
        }
        // 1.3 检查标识符是否匹配
        String messageIdentifier = IotDeviceMessageUtils.getIdentifier(message);
        if (!IotSceneRuleMatcherHelper.isIdentifierMatched(trigger.getIdentifier(), messageIdentifier)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "标识符不匹配，期望: " + trigger.getIdentifier() + ", 实际: " + messageIdentifier);
            return false;
        }

        // 2. 检查是否配置了参数条件
        if (hasParameterCondition(trigger)) {
            return matchParameterCondition(message, trigger);
        }

        // 3. 无参数条件时，标识符匹配即成功
        IotSceneRuleMatcherHelper.logTriggerMatchSuccess(message, trigger);
        return true;
    }

    /**
     * 判断触发器是否配置了参数条件
     *
     * @param trigger 触发器配置
     * @return 是否配置了参数条件
     */
    private boolean hasParameterCondition(IotSceneRuleDO.Trigger trigger) {
        return StrUtil.isNotBlank(trigger.getOperator()) && StrUtil.isNotBlank(trigger.getValue());
    }

    /**
     * 匹配参数条件
     *
     * @param message 设备消息
     * @param trigger 触发器配置
     * @return 是否匹配
     */
    private boolean matchParameterCondition(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1.1 从消息中提取服务调用的输入参数
        Map<String, Object> inputParams = IotDeviceMessageUtils.extractServiceInputParams(message);
        // TODO @puhui999：要考虑 empty 的情况么？
        if (inputParams == null) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息中缺少服务输入参数");
            return false;
        }
        // 1.2 获取要匹配的参数值（使用 identifier 作为参数名）
        Object paramValue = inputParams.get(trigger.getIdentifier());
        if (paramValue == null) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "服务输入参数中缺少指定参数: " + trigger.getIdentifier());
            return false;
        }

        // 2. 使用条件评估器进行匹配
        boolean matched = IotSceneRuleMatcherHelper.evaluateCondition(paramValue, trigger.getOperator(), trigger.getValue());
        if (matched) {
            IotSceneRuleMatcherHelper.logTriggerMatchSuccess(message, trigger);
        } else {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "服务输入参数条件不匹配");
        }
        return matched;
    }

    @Override
    public int getPriority() {
        return 40; // 较低优先级
    }

}
