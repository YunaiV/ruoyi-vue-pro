package cn.iocoder.yudao.module.ai.service.billing.pricing;

import cn.iocoder.yudao.module.ai.enums.billing.AiPricingStrategyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 计费策略管理器
 *
 * 自动收集所有 {@link AiPricingStrategy} Bean，按 type 建索引。
 * null/空串/未知类型均回退到 DEFAULT 策略。
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class AiPricingStrategyManager {

    private final Map<String, AiPricingStrategy> strategyMap;
    private final AiPricingStrategy defaultStrategy;

    public AiPricingStrategyManager(List<AiPricingStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(AiPricingStrategy::getType, Function.identity()));
        this.defaultStrategy = this.strategyMap.get(AiPricingStrategyTypeEnum.DEFAULT.getType());
        if (this.defaultStrategy == null) {
            throw new IllegalStateException("未找到 DEFAULT 计费策略，请确保 DefaultPricingStrategy 已注册");
        }
        log.info("[AiPricingStrategyManager] 已加载 {} 个计费策略: {}", strategies.size(),
                strategyMap.keySet());
    }

    /**
     * 获取计费策略
     *
     * @param strategyType 策略类型，null/空串/未知类型均回退到 DEFAULT
     * @return 计费策略
     */
    public AiPricingStrategy getStrategy(String strategyType) {
        if (strategyType == null || strategyType.isEmpty()) {
            return defaultStrategy;
        }
        return strategyMap.getOrDefault(strategyType, defaultStrategy);
    }

}
