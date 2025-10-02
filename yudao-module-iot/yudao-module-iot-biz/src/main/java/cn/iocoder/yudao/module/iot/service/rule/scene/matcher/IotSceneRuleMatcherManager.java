package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition.IotSceneRuleConditionMatcher;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger.IotSceneRuleTriggerMatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IoT 场景规则匹配器统一管理器：负责管理所有匹配器（触发器匹配器和条件匹配器），并提供统一的匹配入口
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotSceneRuleMatcherManager {

    /**
     * 触发器匹配器映射表
     */
    private final Map<IotSceneRuleTriggerTypeEnum, IotSceneRuleTriggerMatcher> triggerMatchers;

    /**
     * 条件匹配器映射表
     */
    private final Map<IotSceneRuleConditionTypeEnum, IotSceneRuleConditionMatcher> conditionMatchers;

    public IotSceneRuleMatcherManager(List<IotSceneRuleMatcher> matchers) {
        if (CollUtil.isEmpty(matchers)) {
            log.warn("[IotSceneRuleMatcherManager][没有找到任何匹配器]");
            this.triggerMatchers = new HashMap<>();
            this.conditionMatchers = new HashMap<>();
            return;
        }

        // 1.1 按优先级排序并过滤启用的匹配器
        List<IotSceneRuleMatcher> allMatchers = matchers.stream()
                .filter(IotSceneRuleMatcher::isEnabled)
                .sorted(Comparator.comparing(IotSceneRuleMatcher::getPriority))
                .toList();
        // 1.2 分离触发器匹配器和条件匹配器
        List<IotSceneRuleTriggerMatcher> triggerMatchers = allMatchers.stream()
                .filter(matcher -> matcher instanceof IotSceneRuleTriggerMatcher)
                .map(matcher -> (IotSceneRuleTriggerMatcher) matcher)
                .toList();
        List<IotSceneRuleConditionMatcher> conditionMatchers = allMatchers.stream()
                .filter(matcher -> matcher instanceof IotSceneRuleConditionMatcher)
                .map(matcher -> (IotSceneRuleConditionMatcher) matcher)
                .toList();

        // 2.1 构建触发器匹配器映射表
        this.triggerMatchers = convertMap(triggerMatchers, IotSceneRuleTriggerMatcher::getSupportedTriggerType,
                Function.identity(),
                (existing, replacement) -> {
                    log.warn("[IotSceneRuleMatcherManager][触发器类型({})存在多个匹配器，使用优先级更高的: {}]",
                            existing.getSupportedTriggerType(),
                            existing.getPriority() <= replacement.getPriority() ?
                                    existing.getSupportedTriggerType() : replacement.getSupportedTriggerType());
                    return existing.getPriority() <= replacement.getPriority() ? existing : replacement;
                }, LinkedHashMap::new);
        // 2.2 构建条件匹配器映射表
        this.conditionMatchers = convertMap(conditionMatchers, IotSceneRuleConditionMatcher::getSupportedConditionType,
                Function.identity(),
                (existing, replacement) -> {
                    log.warn("[IotSceneRuleMatcherManager][条件类型({})存在多个匹配器，使用优先级更高的: {}]",
                            existing.getSupportedConditionType(),
                            existing.getPriority() <= replacement.getPriority() ?
                                    existing.getSupportedConditionType() : replacement.getSupportedConditionType());
                    return existing.getPriority() <= replacement.getPriority() ? existing : replacement;
                },
                LinkedHashMap::new);

        // 3. 日志输出初始化信息
        log.info("[IotSceneRuleMatcherManager][初始化完成，共加载({})个匹配器，其中触发器匹配器({})个，条件匹配器({})个]",
                allMatchers.size(), this.triggerMatchers.size(), this.conditionMatchers.size());
        this.triggerMatchers.forEach((type, matcher) ->
                log.info("[IotSceneRuleMatcherManager][触发器匹配器类型: ({}), 优先级: ({})] ", type, matcher.getPriority()));
        this.conditionMatchers.forEach((type, matcher) ->
                log.info("[IotSceneRuleMatcherManager][条件匹配器类型: ({}), 优先级: ({})]", type, matcher.getPriority()));
    }

    /**
     * 检查触发器是否匹配消息（主条件匹配）
     *
     * @param message 设备消息
     * @param trigger 触发器配置
     * @return 是否匹配
     */
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        if (message == null || trigger == null || trigger.getType() == null) {
            log.debug("[isMatched][message({}) trigger({}) 参数无效]", message, trigger);
            return false;
        }
        IotSceneRuleTriggerTypeEnum triggerType = IotSceneRuleTriggerTypeEnum.typeOf(trigger.getType());
        if (triggerType == null) {
            log.warn("[isMatched][triggerType({}) 未知的触发器类型]", trigger.getType());
            return false;
        }
        IotSceneRuleTriggerMatcher matcher = triggerMatchers.get(triggerType);
        if (matcher == null) {
            log.warn("[isMatched][triggerType({}) 没有对应的匹配器]", triggerType);
            return false;
        }

        try {
            return matcher.matches(message, trigger);
        } catch (Exception e) {
            log.error("[isMatched][触发器匹配异常] message: {}, trigger: {}", message, trigger, e);
            return false;
        }
    }

    /**
     * 检查子条件是否匹配消息
     *
     * @param message   设备消息
     * @param condition 触发条件
     * @return 是否匹配
     */
    public boolean isConditionMatched(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        if (message == null || condition == null || condition.getType() == null) {
            log.debug("[isConditionMatched][message({}) condition({}) 参数无效]", message, condition);
            return false;
        }

        // 1. 根据条件类型查找对应的匹配器
        IotSceneRuleConditionTypeEnum conditionType = IotSceneRuleConditionTypeEnum.typeOf(condition.getType());
        if (conditionType == null) {
            log.warn("[isConditionMatched][conditionType({}) 未知的条件类型]", condition.getType());
            return false;
        }
        IotSceneRuleConditionMatcher matcher = conditionMatchers.get(conditionType);
        if (matcher == null) {
            log.warn("[isConditionMatched][conditionType({}) 没有对应的匹配器]", conditionType);
            return false;
        }

        // 2. 执行匹配逻辑
        try {
            return matcher.matches(message, condition);
        } catch (Exception e) {
            log.error("[isConditionMatched][message({}) condition({}) 条件匹配异常]", message, condition, e);
            return false;
        }
    }

}
