package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition.IotSceneRuleConditionMatcher;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger.IotSceneRuleTriggerMatcher;

/**
 * IoT 场景规则匹配器基础接口：定义所有匹配器的通用行为，包括优先级、名称和启用状态
 *
 * - {@link IotSceneRuleTriggerMatcher} 触发器匹配器
 * - {@link IotSceneRuleConditionMatcher} 条件匹配器
 *
 * @author HUIHUI
 */
public interface IotSceneRuleMatcher {

    /**
     * 获取匹配优先级（数值越小优先级越高）
     * <p>
     * 用于在多个匹配器支持同一类型时确定优先级
     *
     * @return 优先级数值
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 是否启用该匹配器
     * <p>
     * 可用于动态开关某些匹配器
     *
     * @return 是否启用
     */
    default boolean isEnabled() {
        return true;
    }

}
