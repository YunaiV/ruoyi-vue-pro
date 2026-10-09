package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;

/**
 * AI 四档计费工具类
 *
 * 计费公式（四档）：
 * cost = (promptTokens - cachedTokens) * priceInPer1m / 1,000,000
 *      + cachedTokens * priceCachedPer1m / 1,000,000
 *      + (completionTokens - reasoningTokens) * priceOutPer1m / 1,000,000
 *      + reasoningTokens * priceReasoningPer1m / 1,000,000
 *
 * 注意：promptTokens 已包含 cachedTokens，completionTokens 已包含 reasoningTokens，
 * 不能重复计算。price_cached_per_1m=0 时视为等于 price_in_per_1m，
 * price_reasoning_per_1m=0 时视为等于 price_out_per_1m。
 *
 * @author 芋道源码
 * @deprecated 请使用 {@link cn.iocoder.yudao.module.ai.service.billing.pricing.DefaultPricingStrategy}
 */
@Deprecated
public class AiCostCalculator {

    private AiCostCalculator() {
    }

    /**
     * 计算调用费用（微元）
     *
     * @param promptTokens     输入 token 总量（含缓存命中部分），可为 null
     * @param completionTokens 输出 token 总量（含推理部分），可为 null
     * @param cachedTokens     缓存命中 token 数，可为 null
     * @param reasoningTokens  推理/思考 token 数，可为 null
     * @param pricing          计费配置，可为 null（无配置时返回 0）
     * @return 费用（微元）
     */
    public static long calculateCost(Integer promptTokens, Integer completionTokens,
                                     Integer cachedTokens, Integer reasoningTokens,
                                     AiModelPricingDO pricing) {
        if (pricing == null) {
            return 0L;
        }
        int prompt = nullToZero(promptTokens);
        int completion = nullToZero(completionTokens);
        int cached = nullToZero(cachedTokens);
        int reasoning = nullToZero(reasoningTokens);

        // 获取四档单价（微元/100万tokens）
        long priceIn = nullToZero(pricing.getPriceInPer1m());
        long priceCached = nullToZero(pricing.getPriceCachedPer1m());
        long priceOut = nullToZero(pricing.getPriceOutPer1m());
        long priceReasoning = nullToZero(pricing.getPriceReasoningPer1m());

        // 0 表示不区分，退化为标准价
        if (priceCached == 0) {
            priceCached = priceIn;
        }
        if (priceReasoning == 0) {
            priceReasoning = priceOut;
        }

        // 防止负数（cached 不应超过 prompt，reasoning 不应超过 completion）
        int uncachedInput = Math.max(prompt - cached, 0);
        int standardOutput = Math.max(completion - reasoning, 0);

        // 使用 double 中间计算避免整数除法截断，最终四舍五入到微元
        double cost = (double) uncachedInput * priceIn / 1_000_000
                + (double) cached * priceCached / 1_000_000
                + (double) standardOutput * priceOut / 1_000_000
                + (double) reasoning * priceReasoning / 1_000_000;
        return Math.round(cost);
    }

    private static int nullToZero(Integer value) {
        return value != null ? value : 0;
    }

    private static long nullToZero(Long value) {
        return value != null ? value : 0L;
    }

}
