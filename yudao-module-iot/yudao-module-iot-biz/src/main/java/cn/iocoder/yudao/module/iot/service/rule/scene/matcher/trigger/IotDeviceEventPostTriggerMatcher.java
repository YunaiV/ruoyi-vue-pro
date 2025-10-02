package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import org.springframework.stereotype.Component;

/**
 * 设备事件上报触发器匹配器：处理设备事件上报的触发器匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class IotDeviceEventPostTriggerMatcher implements IotSceneRuleTriggerMatcher {

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.DEVICE_EVENT_POST;
    }

    @Override
    public boolean matches(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1.1 基础参数校验
        if (!IotSceneRuleMatcherHelper.isBasicTriggerValid(trigger)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }

        // 1.2 检查消息方法是否匹配
        if (!IotDeviceMessageMethodEnum.EVENT_POST.getMethod().equals(message.getMethod())) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息方法不匹配，期望: " +
                    IotDeviceMessageMethodEnum.EVENT_POST.getMethod() + ", 实际: " + message.getMethod());
            return false;
        }

        // 1.3 检查标识符是否匹配
        String messageIdentifier = IotDeviceMessageUtils.getIdentifier(message);
        if (!IotSceneRuleMatcherHelper.isIdentifierMatched(trigger.getIdentifier(), messageIdentifier)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "标识符不匹配，期望: " +
                    trigger.getIdentifier() + ", 实际: " + messageIdentifier);
            return false;
        }

        // 2. 对于事件触发器，通常不需要检查操作符和值，只要事件发生即匹配
        // 但如果配置了操作符和值，则需要进行条件匹配
        if (StrUtil.isNotBlank(trigger.getOperator()) && StrUtil.isNotBlank(trigger.getValue())) {
            Object eventData = message.getData();
            if (eventData == null) {
                IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "消息中事件数据为空");
                return false;
            }

            boolean matched = IotSceneRuleMatcherHelper.evaluateCondition(eventData, trigger.getOperator(), trigger.getValue());
            if (!matched) {
                IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "事件数据条件不匹配");
                return false;
            }
        }

        IotSceneRuleMatcherHelper.logTriggerMatchSuccess(message, trigger);
        return true;
    }

    @Override
    public int getPriority() {
        return 30; // 中等优先级
    }

}
