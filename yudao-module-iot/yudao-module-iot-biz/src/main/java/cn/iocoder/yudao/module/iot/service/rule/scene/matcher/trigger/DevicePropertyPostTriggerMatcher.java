package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.AbstractIotSceneRuleMatcher;
import org.springframework.stereotype.Component;

/**
 * 设备属性上报触发器匹配器
 * <p>
 * 处理设备属性数据上报的触发器匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class DevicePropertyPostTriggerMatcher extends AbstractIotSceneRuleMatcher {

    /**
     * 设备属性上报消息方法
     */
    // TODO @puhui999：是不是不用枚举哈？直接使用 IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod()
    private static final String DEVICE_PROPERTY_POST_METHOD = IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod();

    @Override
    public MatcherTypeEnum getMatcherType() {
        return MatcherTypeEnum.TRIGGER;
    }

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST;
    }

    @Override
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1. 基础参数校验
        if (!isBasicTriggerValid(trigger)) {
            logTriggerMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }

        // 2. 检查消息方法是否匹配
        if (!DEVICE_PROPERTY_POST_METHOD.equals(message.getMethod())) {
            logTriggerMatchFailure(message, trigger, "消息方法不匹配，期望: " + DEVICE_PROPERTY_POST_METHOD + ", 实际: " + message.getMethod());
            return false;
        }

        // 3. 检查标识符是否匹配
        String messageIdentifier = IotDeviceMessageUtils.getIdentifier(message);
        if (!isIdentifierMatched(trigger.getIdentifier(), messageIdentifier)) {
            logTriggerMatchFailure(message, trigger, "标识符不匹配，期望: " + trigger.getIdentifier() + ", 实际: " + messageIdentifier);
            return false;
        }

        // 4. 检查操作符和值是否有效
        if (!isTriggerOperatorAndValueValid(trigger)) {
            logTriggerMatchFailure(message, trigger, "操作符或值无效");
            return false;
        }

        // 5. 获取属性值
        Object propertyValue = message.getData();
        if (propertyValue == null) {
            logTriggerMatchFailure(message, trigger, "消息中属性值为空");
            return false;
        }

        // 6. 使用条件评估器进行匹配
        boolean matched = evaluateCondition(propertyValue, trigger.getOperator(), trigger.getValue());
        if (matched) {
            logTriggerMatchSuccess(message, trigger);
        } else {
            logTriggerMatchFailure(message, trigger, "属性值条件不匹配");
        }
        return matched;
    }

    @Override
    public int getPriority() {
        return 20; // 中等优先级
    }

}
