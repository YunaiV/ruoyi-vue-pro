package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum.findTriggerTypeEnum;

/**
 * IoT 场景规则匹配器统一管理器
 * <p>
 * 负责管理所有匹配器（触发器匹配器和条件匹配器），并提供统一的匹配入口
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotSceneRuleMatcherManager {

    /**
     * 触发器匹配器映射表
     * Key: 触发器类型枚举
     * Value: 对应的匹配器实例
     */
    private final Map<IotSceneRuleTriggerTypeEnum, IotSceneRuleMatcher> triggerMatchers;

    /**
     * 条件匹配器映射表
     * Key: 条件类型枚举
     * Value: 对应的匹配器实例
     */
    private final Map<IotSceneRuleConditionTypeEnum, IotSceneRuleMatcher> conditionMatchers;

    /**
     * 所有匹配器列表（按优先级排序）
     */
    private final List<IotSceneRuleMatcher> allMatchers;

    public IotSceneRuleMatcherManager(List<IotSceneRuleMatcher> matchers) {
        if (CollUtil.isEmpty(matchers)) {
            log.warn("[IotSceneRuleMatcherManager][没有找到任何匹配器]");
            this.triggerMatchers = new HashMap<>();
            this.conditionMatchers = new HashMap<>();
            this.allMatchers = new ArrayList<>();
            return;
        }

        // 按优先级排序并过滤启用的匹配器
        this.allMatchers = matchers.stream()
                .filter(IotSceneRuleMatcher::isEnabled)
                .sorted(Comparator.comparing(IotSceneRuleMatcher::getPriority))
                .collect(Collectors.toList());

        // 分离触发器匹配器和条件匹配器
        List<IotSceneRuleMatcher> triggerMatchers = this.allMatchers.stream()
                .filter(matcher -> matcher.getMatcherType() == IotSceneRuleMatcher.MatcherType.TRIGGER)
                .toList();
        List<IotSceneRuleMatcher> conditionMatchers = this.allMatchers.stream()
                .filter(matcher -> matcher.getMatcherType() == IotSceneRuleMatcher.MatcherType.CONDITION)
                .toList();

        // 构建触发器匹配器映射表
        // TODO @puhui999：convertMap()
        this.triggerMatchers = triggerMatchers.stream()
                .collect(Collectors.toMap(
                        IotSceneRuleMatcher::getSupportedTriggerType,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("[IotSceneRuleMatcherManager][触发器类型({})存在多个匹配器，使用优先级更高的: {}]",
                                    existing.getSupportedTriggerType(),
                                    existing.getPriority() <= replacement.getPriority() ? existing.getMatcherName() : replacement.getMatcherName());
                            return existing.getPriority() <= replacement.getPriority() ? existing : replacement;
                        },
                        LinkedHashMap::new
                ));
        // 构建条件匹配器映射表
        this.conditionMatchers = conditionMatchers.stream()
                .collect(Collectors.toMap(
                        IotSceneRuleMatcher::getSupportedConditionType,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("[IotSceneRuleMatcherManager][条件类型({})存在多个匹配器，使用优先级更高的: {}]",
                                    existing.getSupportedConditionType(),
                                    existing.getPriority() <= replacement.getPriority() ? existing.getMatcherName() : replacement.getMatcherName());
                            return existing.getPriority() <= replacement.getPriority() ? existing : replacement;
                        },
                        LinkedHashMap::new
                ));

        // 日志输出初始化信息
        log.info("[IotSceneRuleMatcherManager][初始化完成，共加载 {} 个匹配器，其中触发器匹配器 {} 个，条件匹配器 {} 个]",
                this.allMatchers.size(), this.triggerMatchers.size(), this.conditionMatchers.size());
        this.triggerMatchers.forEach((type, matcher) ->
                log.info("[IotSceneRuleMatcherManager][触发器匹配器] 类型: {}, 匹配器: {}, 优先级: {}",
                        type, matcher.getMatcherName(), matcher.getPriority()));
        this.conditionMatchers.forEach((type, matcher) ->
                log.info("[IotSceneRuleMatcherManager][条件匹配器] 类型: {}, 匹配器: {}, 优先级: {}",
                        type, matcher.getMatcherName(), matcher.getPriority()));
    }

    /**
     * 检查触发器是否匹配消息（主条件匹配）
     *
     * @param message 设备消息
     * @param trigger 触发器配置
     * @return 是否匹配
     */
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        // TODO @puhui999：日志优化下；claude 打出来的日志风格，和项目有点不一样哈；
        if (message == null || trigger == null || trigger.getType() == null) {
            log.debug("[isMatched][参数无效] message: {}, trigger: {}", message, trigger);
            return false;
        }
        IotSceneRuleTriggerTypeEnum triggerType = findTriggerTypeEnum(trigger.getType());
        if (triggerType == null) {
            log.warn("[isMatched][未知的触发器类型: {}]", trigger.getType());
            return false;
        }
        IotSceneRuleMatcher matcher = triggerMatchers.get(triggerType);
        if (matcher == null) {
            log.warn("[isMatched][触发器类型({})没有对应的匹配器]", triggerType);
            return false;
        }

        try {
            return matcher.isMatched(message, trigger);
        } catch (Exception e) {
            log.error("[isMatched][触发器匹配异常] message: {}, trigger: {}, matcher: {}",
                    message, trigger, matcher.getMatcherName(), e);
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
            log.debug("[isConditionMatched][参数无效] message: {}, condition: {}", message, condition);
            return false;
        }

        // 根据条件类型查找对应的匹配器
        IotSceneRuleConditionTypeEnum conditionType = findConditionTypeEnum(condition.getType());
        if (conditionType == null) {
            log.warn("[isConditionMatched][未知的条件类型: {}]", condition.getType());
            return false;
        }

        IotSceneRuleMatcher matcher = conditionMatchers.get(conditionType);
        if (matcher == null) {
            log.warn("[isConditionMatched][条件类型({})没有对应的匹配器]", conditionType);
            return false;
        }

        try {
            return matcher.isMatched(message, condition);
        } catch (Exception e) {
            log.error("[isConditionMatched][条件匹配异常] message: {}, condition: {}, matcher: {}",
                    message, condition, matcher.getMatcherName(), e);
            return false;
        }
    }

    /**
     * 根据类型值查找条件类型枚举
     *
     * @param typeValue 类型值
     * @return 条件类型枚举
     */
    private IotSceneRuleConditionTypeEnum findConditionTypeEnum(Integer typeValue) {
        return Arrays.stream(IotSceneRuleConditionTypeEnum.values())
                .filter(type -> type.getType().equals(typeValue))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有支持的触发器类型
     *
     * @return 支持的触发器类型列表
     */
    public Set<IotSceneRuleTriggerTypeEnum> getSupportedTriggerTypes() {
        return new HashSet<>(triggerMatchers.keySet());
    }

    /**
     * 获取所有支持的条件类型
     *
     * @return 支持的条件类型列表
     */
    public Set<IotSceneRuleConditionTypeEnum> getSupportedConditionTypes() {
        return new HashSet<>(conditionMatchers.keySet());
    }

    // TODO @puhui999：用不到的方法，可以去掉先哈；

    /**
     * 获取指定触发器类型的匹配器
     *
     * @param triggerType 触发器类型
     * @return 匹配器实例，如果不存在则返回 null
     */
    public IotSceneRuleMatcher getTriggerMatcher(IotSceneRuleTriggerTypeEnum triggerType) {
        return triggerMatchers.get(triggerType);
    }

    /**
     * 获取指定条件类型的匹配器
     *
     * @param conditionType 条件类型
     * @return 匹配器实例，如果不存在则返回 null
     */
    public IotSceneRuleMatcher getConditionMatcher(IotSceneRuleConditionTypeEnum conditionType) {
        return conditionMatchers.get(conditionType);
    }

    // TODO @puhui999：是不是不用这个哈；直接 @Getter，单测直接处理；
    /**
     * 获取所有匹配器的统计信息
     *
     * @return 统计信息映射表
     */
    public Map<String, Object> getMatcherStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalMatchers", allMatchers.size());
        statistics.put("triggerMatchers", triggerMatchers.size());
        statistics.put("conditionMatchers", conditionMatchers.size());
        statistics.put("supportedTriggerTypes", getSupportedTriggerTypes());
        statistics.put("supportedConditionTypes", getSupportedConditionTypes());

        // 触发器匹配器详情
        Map<String, Object> triggerMatcherDetails = new HashMap<>();
        triggerMatchers.forEach((type, matcher) -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("matcherName", matcher.getMatcherName());
            detail.put("priority", matcher.getPriority());
            detail.put("enabled", matcher.isEnabled());
            triggerMatcherDetails.put(type.name(), detail);
        });
        statistics.put("triggerMatcherDetails", triggerMatcherDetails);

        // 条件匹配器详情
        Map<String, Object> conditionMatcherDetails = new HashMap<>();
        conditionMatchers.forEach((type, matcher) -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("matcherName", matcher.getMatcherName());
            detail.put("priority", matcher.getPriority());
            detail.put("enabled", matcher.isEnabled());
            conditionMatcherDetails.put(type.name(), detail);
        });
        statistics.put("conditionMatcherDetails", conditionMatcherDetails);

        return statistics;
    }

}
