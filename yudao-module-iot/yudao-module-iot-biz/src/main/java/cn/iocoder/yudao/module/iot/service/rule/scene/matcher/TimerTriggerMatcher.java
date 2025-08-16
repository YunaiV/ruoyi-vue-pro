package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 定时触发器匹配器
 * <p>
 * 处理定时触发的触发器匹配逻辑
 * 注意：定时触发器不依赖设备消息，主要用于定时任务场景
 *
 * @author HUIHUI
 */
@Component
public class TimerTriggerMatcher extends AbstractIotSceneRuleMatcher {

    @Override
    public MatcherType getMatcherType() {
        return MatcherType.TRIGGER;
    }

    @Override
    public IotSceneRuleTriggerTypeEnum getSupportedTriggerType() {
        return IotSceneRuleTriggerTypeEnum.TIMER;
    }

    @Override
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // 1. 基础参数校验
        if (!isBasicTriggerValid(trigger)) {
            logTriggerMatchFailure(message, trigger, "触发器基础参数无效");
            return false;
        }

        // 2. 检查 CRON 表达式是否存在
        if (StrUtil.isBlank(trigger.getCronExpression())) {
            logTriggerMatchFailure(message, trigger, "定时触发器缺少 CRON 表达式");
            return false;
        }

        // 3. 定时触发器通常不依赖具体的设备消息
        // 它是通过定时任务调度器触发的，这里主要是验证配置的有效性

        // 4. 可以添加 CRON 表达式格式验证
        if (!isValidCronExpression(trigger.getCronExpression())) {
            logTriggerMatchFailure(message, trigger, "CRON 表达式格式无效: " + trigger.getCronExpression());
            return false;
        }

        logTriggerMatchSuccess(message, trigger);
        return true;
    }

    /**
     * 验证 CRON 表达式格式是否有效
     *
     * @param cronExpression CRON 表达式
     * @return 是否有效
     */
    private boolean isValidCronExpression(String cronExpression) {
        try {
            // 简单的 CRON 表达式格式验证
            // 标准 CRON 表达式应该有 6 或 7 个字段（秒 分 时 日 月 周 [年]）
            String[] fields = cronExpression.trim().split("\\s+");
            return fields.length >= 6 && fields.length <= 7;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 50; // 最低优先级，因为定时触发器不依赖消息
    }

    @Override
    public boolean isEnabled() {
        // 定时触发器可以根据配置动态启用/禁用
        return true;
    }

}
