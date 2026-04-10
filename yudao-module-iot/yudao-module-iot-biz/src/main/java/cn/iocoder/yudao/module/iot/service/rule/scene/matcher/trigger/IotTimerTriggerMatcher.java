package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import org.quartz.CronExpression;
import org.springframework.stereotype.Component;

/**
 * 定时触发器匹配器：处理定时触发的触发器匹配逻辑
 *
 * 注意：定时触发器不依赖设备消息，主要用于定时任务场景
 *
 * @author HUIHUI
 */
@Component
public class IotTimerTriggerMatcher implements IotSceneRuleTriggerMatcher {

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.TIMER;
    }

    @Override
    public boolean matches(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1.1 基础参数校验
        if (!IotSceneRuleMatcherHelper.isBasicTriggerValid(trigger)) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }

        // 1.2 检查 CRON 表达式是否存在
        if (StrUtil.isBlank(trigger.getCronExpression())) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "定时触发器缺少 CRON 表达式");
            return false;
        }

        // 1.3 定时触发器通常不依赖具体的设备消息
        // 它是通过定时任务调度器触发的，这里主要是验证配置的有效性
        if (!CronExpression.isValidExpression(trigger.getCronExpression())) {
            IotSceneRuleMatcherHelper.logTriggerMatchFailure(message, trigger, "CRON 表达式格式无效: " + trigger.getCronExpression());
            return false;
        }

        IotSceneRuleMatcherHelper.logTriggerMatchSuccess(message, trigger);
        return true;
    }

    @Override
    public int getPriority() {
        return 50; // 最低优先级，因为定时触发器不依赖消息
    }

}
