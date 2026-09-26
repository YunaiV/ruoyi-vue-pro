package cn.iocoder.yudao.module.ai.service.billing.pricing;

import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DefaultPricingStrategy} 的单元测试
 *
 * 用例与 AiCostCalculatorTest 一致，验证策略模式结果不变
 */
class DefaultPricingStrategyTest {

    private final DefaultPricingStrategy strategy = new DefaultPricingStrategy();

    @Test
    void testCalculateCost_nullPricing() {
        AiPricingContext ctx = AiPricingContext.builder()
                .promptTokens(1000).completionTokens(500).build();
        assertEquals(0L, strategy.calculateCost(ctx));
    }

    @Test
    void testCalculateCost_allNullTokens() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceReasoningPer1m(0L);

        AiPricingContext ctx = AiPricingContext.builder().pricing(pricing).build();
        assertEquals(0L, strategy.calculateCost(ctx));
    }

    @Test
    void testCalculateCost_standardTwoTier() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceReasoningPer1m(0L);

        AiPricingContext ctx = AiPricingContext.builder()
                .promptTokens(1000).completionTokens(500).pricing(pricing).build();
        assertEquals(6000L, strategy.calculateCost(ctx));
    }

    @Test
    void testCalculateCost_fourTierWithCacheAndReasoning() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(10_000_000L);
        pricing.setPriceCachedPer1m(2_000_000L);
        pricing.setPriceOutPer1m(20_000_000L);
        pricing.setPriceReasoningPer1m(40_000_000L);

        AiPricingContext ctx = AiPricingContext.builder()
                .promptTokens(1000).completionTokens(800)
                .cachedTokens(300).reasoningTokens(200)
                .pricing(pricing).build();
        assertEquals(27600L, strategy.calculateCost(ctx));
    }

    @Test
    void testCalculateCost_cachedPriceZeroFallsBackToInputPrice() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(10_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceOutPer1m(20_000_000L);
        pricing.setPriceReasoningPer1m(0L);

        AiPricingContext ctx = AiPricingContext.builder()
                .promptTokens(1000).completionTokens(500).cachedTokens(300)
                .pricing(pricing).build();
        assertEquals(20000L, strategy.calculateCost(ctx));
    }

    @Test
    void testCalculateCost_cachedExceedsPrompt_noNegative() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(10_000_000L);
        pricing.setPriceCachedPer1m(2_000_000L);
        pricing.setPriceOutPer1m(20_000_000L);
        pricing.setPriceReasoningPer1m(0L);

        AiPricingContext ctx = AiPricingContext.builder()
                .promptTokens(500).completionTokens(100).cachedTokens(800)
                .pricing(pricing).build();
        assertEquals(3600L, strategy.calculateCost(ctx));
    }

    @Test
    void testCalculateCost_largeTokenCount() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceReasoningPer1m(0L);

        AiPricingContext ctx = AiPricingContext.builder()
                .promptTokens(1_000_000).completionTokens(500_000)
                .pricing(pricing).build();
        assertEquals(6_000_000L, strategy.calculateCost(ctx));
    }

}
