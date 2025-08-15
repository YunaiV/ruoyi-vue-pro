package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum.findTriggerTypeEnum;

/**
 * IoT 场景规则触发器匹配管理器
 * <p>
 * 负责管理所有触发器匹配器，并提供统一的匹配入口
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotSceneRuleTriggerMatcherManager {

    /**
     * 触发器匹配器映射表
     * Key: 触发器类型枚举
     * Value: 对应的匹配器实例
     */
    private final Map<IotSceneRuleTriggerTypeEnum, IotSceneRuleTriggerMatcher> matcherMap;

    /**
     * 所有匹配器列表（按优先级排序）
     */
    private final List<IotSceneRuleTriggerMatcher> allMatchers;

    public IotSceneRuleTriggerMatcherManager(List<IotSceneRuleTriggerMatcher> matchers) {
        if (CollUtil.isEmpty(matchers)) {
            log.warn("[IotSceneRuleTriggerMatcherManager][没有找到任何触发器匹配器]");
            this.matcherMap = new HashMap<>();
            this.allMatchers = new ArrayList<>();
            return;
        }

        // 按优先级排序并过滤启用的匹配器
        this.allMatchers = matchers.stream()
                .filter(IotSceneRuleTriggerMatcher::isEnabled)
                .sorted(Comparator.comparing(IotSceneRuleTriggerMatcher::getPriority))
                .collect(Collectors.toList());

        // 构建匹配器映射表
        this.matcherMap = this.allMatchers.stream()
                .collect(Collectors.toMap(
                        IotSceneRuleTriggerMatcher::getSupportedTriggerType,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("[IotSceneRuleTriggerMatcherManager][触发器类型({})存在多个匹配器，使用优先级更高的: {}]",
                                    existing.getSupportedTriggerType(),
                                    existing.getPriority() <= replacement.getPriority() ? existing.getMatcherName() : replacement.getMatcherName());
                            return existing.getPriority() <= replacement.getPriority() ? existing : replacement;
                        },
                        LinkedHashMap::new
                ));

        log.info("[IotSceneRuleTriggerMatcherManager][初始化完成，共加载 {} 个触发器匹配器]", this.matcherMap.size());
        this.matcherMap.forEach((type, matcher) ->
                log.info("[IotSceneRuleTriggerMatcherManager][触发器类型: {}, 匹配器: {}, 优先级: {}]",
                        type, matcher.getMatcherName(), matcher.getPriority()));
    }

    /**
     * 检查触发器是否匹配消息
     *
     * @param message 设备消息
     * @param trigger 触发器配置
     * @return 是否匹配
     */
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.Trigger trigger) {
        if (message == null || trigger == null || trigger.getType() == null) {
            log.debug("[isMatched][参数无效] message: {}, trigger: {}", message, trigger);
            return false;
        }

        // 根据触发器类型查找对应的匹配器
        IotSceneRuleTriggerTypeEnum triggerType = findTriggerTypeEnum(trigger.getType());
        if (triggerType == null) {
            log.warn("[isMatched][未知的触发器类型: {}]", trigger.getType());
            return false;
        }

        IotSceneRuleTriggerMatcher matcher = matcherMap.get(triggerType);
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
     * 获取所有支持的触发器类型
     *
     * @return 支持的触发器类型列表
     */
    public Set<IotSceneRuleTriggerTypeEnum> getSupportedTriggerTypes() {
        return new HashSet<>(matcherMap.keySet());
    }

    /**
     * 获取指定触发器类型的匹配器
     *
     * @param triggerType 触发器类型
     * @return 匹配器实例，如果不存在则返回 null
     */
    public IotSceneRuleTriggerMatcher getMatcher(IotSceneRuleTriggerTypeEnum triggerType) {
        return matcherMap.get(triggerType);
    }

    /**
     * 获取所有匹配器的统计信息
     *
     * @return 统计信息映射表
     */
    public Map<String, Object> getMatcherStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalMatchers", allMatchers.size());
        statistics.put("enabledMatchers", matcherMap.size());
        statistics.put("supportedTriggerTypes", getSupportedTriggerTypes());

        Map<String, Object> matcherDetails = new HashMap<>();
        matcherMap.forEach((type, matcher) -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("matcherName", matcher.getMatcherName());
            detail.put("priority", matcher.getPriority());
            detail.put("enabled", matcher.isEnabled());
            matcherDetails.put(type.name(), detail);
        });
        statistics.put("matcherDetails", matcherDetails);

        return statistics;
    }
}
