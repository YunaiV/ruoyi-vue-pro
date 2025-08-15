package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 设备事件上报触发器匹配器
 * <p>
 * 处理设备事件上报的触发器匹配逻辑
 *
 * @author HUIHUI
 */
@Component
public class DeviceEventPostTriggerMatcher extends AbstractIotSceneRuleTriggerMatcher {

    /**
     * 设备事件上报消息方法
     */
    private static final String DEVICE_EVENT_POST_METHOD = "thing.event.post";

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.DEVICE_EVENT_POST;
    }

    @Override
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1. 基础参数校验
        if (!isBasicTriggerValid(trigger)) {
            logMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }

        // 2. 检查消息方法是否匹配
        if (!DEVICE_EVENT_POST_METHOD.equals(message.getMethod())) {
            logMatchFailure(message, trigger, "消息方法不匹配，期望: " + DEVICE_EVENT_POST_METHOD + ", 实际: " + message.getMethod());
            return false;
        }

        // 3. 检查标识符是否匹配
        String messageIdentifier = IotDeviceMessageUtils.getIdentifier(message);
        if (!isIdentifierMatched(trigger.getIdentifier(), messageIdentifier)) {
            logMatchFailure(message, trigger, "标识符不匹配，期望: " + trigger.getIdentifier() + ", 实际: " + messageIdentifier);
            return false;
        }

        // 4. 对于事件触发器，通常不需要检查操作符和值，只要事件发生即匹配
        // 但如果配置了操作符和值，则需要进行条件匹配
        if (StrUtil.isNotBlank(trigger.getOperator()) && StrUtil.isNotBlank(trigger.getValue())) {
            Object eventData = message.getData();
            if (eventData == null) {
                logMatchFailure(message, trigger, "消息中事件数据为空");
                return false;
            }

            boolean matched = evaluateCondition(eventData, trigger.getOperator(), trigger.getValue());
            if (!matched) {
                logMatchFailure(message, trigger, "事件数据条件不匹配");
                return false;
            }
        }

        logMatchSuccess(message, trigger);
        return true;
    }

    @Override
    public int getPriority() {
        return 30; // 中等优先级
    }
}
