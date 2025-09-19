package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import org.springframework.stereotype.Component;

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

        // 2. 对于服务调用触发器，通常只需要匹配服务标识符即可
        // 不需要检查操作符和值，因为服务调用本身就是触发条件
        // TODO @puhui999: 服务调用时校验输入参数是否匹配条件？
        IotSceneRuleMatcherHelper.logTriggerMatchSuccess(message, trigger);
        return true;
    }

    @Override
    public int getPriority() {
        return 40; // 较低优先级
    }

}
