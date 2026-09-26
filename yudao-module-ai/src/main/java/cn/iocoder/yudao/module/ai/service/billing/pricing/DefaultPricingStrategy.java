package cn.iocoder.yudao.module.ai.service.billing.pricing;

import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelPricingDO;
import cn.iocoder.yudao.module.ai.enums.billing.AiPricingStrategyTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 默认四档计费策略
 *
 * 逻辑与 {@link cn.iocoder.yudao.module.ai.service.billing.AiCostCalculator} 完全一致。
 *
 * @author 芋道源码
 */
@Component
public class DefaultPricingStrategy implements AiPricingStrategy {

    @Override
    public String getType() {
        return AiPricingStrategyTypeEnum.DEFAULT.getType();
    }

    @Override
    public long calculateCost(AiPricingContext ctx) {
        AiModelPricingDO pricing = ctx.getPricing();
        if (pricing == null) {
            return 0L;
        }
        int prompt = nullToZero(ctx.getPromptTokens());
        int completion = nullToZero(ctx.getCompletionTokens());
        int cached = nullToZero(ctx.getCachedTokens());
        int reasoning = nullToZero(ctx.getReasoningTokens());

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
