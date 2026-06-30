package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiCostCalculator} 的单元测试
 */
class AiCostCalculatorTest {

    @Test
    void testCalculateCost_nullPricing() {
        assertEquals(0L, AiCostCalculator.calculateCost(1000, 500, null, null, null));
    }

    @Test
    void testCalculateCost_allNullTokens() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceReasoningPer1m(0L);
        assertEquals(0L, AiCostCalculator.calculateCost(null, null, null, null, pricing));
    }

    @Test
    void testCalculateCost_standardTwoTier() {
        // DeepSeek-V3: 输入 2元/百万, 输出 8元/百万
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);  // 2元 = 2,000,000微元
        pricing.setPriceOutPer1m(8_000_000L); // 8元 = 8,000,000微元
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceReasoningPer1m(0L);

        // 1000 输入 + 500 输出
        // cost = 1000 * 2,000,000 / 1,000,000 + 500 * 8,000,000 / 1,000,000
        //      = 2000 + 4000 = 6000 微元 = 0.006 元
        long cost = AiCostCalculator.calculateCost(1000, 500, null, null, pricing);
        assertEquals(6000L, cost);
    }

    @Test
    void testCalculateCost_fourTierWithCacheAndReasoning() {
        // 四档计费
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(10_000_000L);       // 10元/百万
        pricing.setPriceCachedPer1m(2_000_000L);     // 2元/百万（缓存命中）
        pricing.setPriceOutPer1m(20_000_000L);       // 20元/百万
        pricing.setPriceReasoningPer1m(40_000_000L); // 40元/百万（推理）

        // promptTokens=1000, completionTokens=800, cachedTokens=300, reasoningTokens=200
        // uncachedInput = 1000 - 300 = 700
        // standardOutput = 800 - 200 = 600
        // cost = 700 * 10,000,000 / 1,000,000 + 300 * 2,000,000 / 1,000,000
        //      + 600 * 20,000,000 / 1,000,000 + 200 * 40,000,000 / 1,000,000
        //      = 7000 + 600 + 12000 + 8000 = 27600 微元
        long cost = AiCostCalculator.calculateCost(1000, 800, 300, 200, pricing);
        assertEquals(27600L, cost);
    }

    @Test
    void testCalculateCost_cachedPriceZeroFallsBackToInputPrice() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(10_000_000L);
        pricing.setPriceCachedPer1m(0L); // 0 表示不区分，退化为 priceIn
        pricing.setPriceOutPer1m(20_000_000L);
        pricing.setPriceReasoningPer1m(0L);

        // promptTokens=1000, cachedTokens=300
        // uncachedInput = 700, cached = 300 (用 priceIn 计费)
        // cost = 700 * 10 + 300 * 10 + 500 * 20 = 7000 + 3000 + 10000 = 20000
        long cost = AiCostCalculator.calculateCost(1000, 500, 300, null, pricing);
        assertEquals(20000L, cost);
    }

    @Test
    void testCalculateCost_cachedExceedsPrompt_noNegative() {
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(10_000_000L);
        pricing.setPriceCachedPer1m(2_000_000L);
        pricing.setPriceOutPer1m(20_000_000L);
        pricing.setPriceReasoningPer1m(0L);

        // cachedTokens > promptTokens → uncachedInput = max(0, 500-800) = 0
        long cost = AiCostCalculator.calculateCost(500, 100, 800, null, pricing);
        // cost = 0 * 10 + 800 * 2 + 100 * 20 = 0 + 1600 + 2000 = 3600
        assertEquals(3600L, cost);
    }

    @Test
    void testCalculateCost_largeTokenCount() {
        // 模拟大量 token（百万级）
        AiModelPricingDO pricing = new AiModelPricingDO();
        pricing.setPriceInPer1m(2_000_000L);
        pricing.setPriceOutPer1m(8_000_000L);
        pricing.setPriceCachedPer1m(0L);
        pricing.setPriceReasoningPer1m(0L);

        // 1,000,000 输入 + 500,000 输出
        // cost = 1,000,000 * 2 + 500,000 * 8 = 2,000,000 + 4,000,000 = 6,000,000 微元 = 6 元
        long cost = AiCostCalculator.calculateCost(1_000_000, 500_000, null, null, pricing);
        assertEquals(6_000_000L, cost);
    }

}
