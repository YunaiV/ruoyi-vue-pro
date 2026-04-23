package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CostEstimateAgent — 基于面料和设计细节复杂度估算生产成本（Phase 8）。
 *
 * <p>规则：
 * <ul>
 *   <li>baseCost 由面料决定：丝绸 80 / 羊毛 70 / 牛仔 50 / 针织 45 / 棉 30 / 其他 40</li>
 *   <li>detailMultiplier 由细节数量决定：1个→1.0 / 2个→1.2 / 3+个→1.4</li>
 *   <li>estimatedCost = baseCost × detailMultiplier，保留 2 位小数</li>
 *   <li>suggestPrice = estimatedCost × 2.5（2.5 倍加价）</li>
 * </ul>
 * </p>
 */
@Component
public class CostEstimateAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(CostEstimateAgent.class);

    /** 建议售价相对成本的加价倍数（2.5 倍） */
    private static final BigDecimal PRICE_MARKUP_MULTIPLIER = new BigDecimal("2.5");

    @Override
    public Context run(Context ctx) {
        try {
            int baseCost = baseCostFromFabric(ctx.fabric);
            double multiplier = detailMultiplier(ctx.designDetails);

            BigDecimal estimatedCost = BigDecimal.valueOf(baseCost * multiplier)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal suggestPrice = estimatedCost
                    .multiply(PRICE_MARKUP_MULTIPLIER)
                    .setScale(2, RoundingMode.HALF_UP);

            ctx.cost = estimatedCost;
            ctx.suggestPrice = suggestPrice;

            if (ctx.costPrice == null) {
                ctx.costPrice = estimatedCost;
            }
            if (ctx.price == null) {
                ctx.price = suggestPrice;
            }

            log.info("[CostEstimateAgent] fabric={} details={} cost={} suggestPrice={}",
                    ctx.fabric, ctx.designDetails, estimatedCost, suggestPrice);
        } catch (Exception e) {
            log.warn("[CostEstimateAgent] 成本估算异常，跳过", e);
        }
        return ctx;
    }

    private int baseCostFromFabric(String fabric) {
        if (!StringUtils.hasText(fabric)) return 40;
        switch (fabric) {
            case "丝绸": return 80;
            case "羊毛": return 70;
            case "牛仔": return 50;
            case "针织": return 45;
            case "棉":   return 30;
            default:     return 40;
        }
    }

    private double detailMultiplier(String designDetails) {
        if (!StringUtils.hasText(designDetails)) return 1.0;
        // 细节数量 = 逗号数量 + 1
        int detailCount = designDetails.split(",").length;
        if (detailCount >= 3) return 1.4;
        if (detailCount == 2) return 1.2;
        return 1.0;
    }

}
