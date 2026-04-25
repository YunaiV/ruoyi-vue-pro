package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PricingStrategyAgent — Phase 10 动态定价（替代死价格）。
 *
 * <h3>定价公式</h3>
 * <pre>
 * price = totalCost × 2.2 + trendBoost + marketAdjust
 *
 * trendBoost   = 同品类历史均价 × 0.1（市场热度溢价）
 * marketAdjust = 高端市场（EU/ME）+20元；CN市场 0
 *
 * fallback（无成本数据）：同品类历史均价 × 0.95（略低于均价抢量）
 * 绝对fallback：299 元
 * </pre>
 *
 * <h3>与 PricingAgent 的区别</h3>
 * <ul>
 *   <li>PricingAgent — 利润率驱动（cost × (1+rate)），阶梯批发价</li>
 *   <li>PricingStrategyAgent — 市场驱动（趋势溢价 + 市场调整），用于 Phase 10 最终定价</li>
 * </ul>
 */
@Component
public class PricingStrategyAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(PricingStrategyAgent.class);

    private static final BigDecimal DEFAULT_PRICE      = new BigDecimal("299");
    private static final BigDecimal BASE_MULTIPLIER    = new BigDecimal("2.2");
    private static final BigDecimal TREND_BOOST_RATIO  = new BigDecimal("0.1");
    private static final BigDecimal MARKET_ADJUST_LUXURY = new BigDecimal("20");

    @Resource private DeepayMetricsMapper  metricsMapper;
    @Resource private DeepayProductMapper  productMapper;

    @Override
    public Context run(Context ctx) {
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[PricingStrategyAgent] shouldProduce=false，跳过");
            return ctx;
        }

        BigDecimal cost     = resolveCost(ctx);
        BigDecimal avgPrice = fetchAvgPrice(ctx.category);
        BigDecimal trend    = calcTrendBoost(avgPrice);
        BigDecimal market   = calcMarketAdjust(ctx.market);

        ctx.trendBoost   = trend;
        ctx.marketAdjust = market;

        BigDecimal finalPrice;

        if (cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            // 主路径：成本驱动 + 溢价调整
            finalPrice = cost.multiply(BASE_MULTIPLIER)
                    .add(trend)
                    .add(market)
                    .setScale(0, RoundingMode.CEILING);
        } else if (avgPrice != null && avgPrice.compareTo(BigDecimal.ZERO) > 0) {
            // fallback：低于均价 5% 抢量
            finalPrice = avgPrice.multiply(new BigDecimal("0.95"))
                    .add(market)
                    .setScale(0, RoundingMode.CEILING);
        } else {
            finalPrice = DEFAULT_PRICE;
        }

        ctx.price    = finalPrice;
        // basePrice 永远是 EUR（内部基准），analytics/利润统一用此字段
        ctx.basePrice = finalPrice;

        // 落库更新
        if (StringUtils.hasText(ctx.productId)) {
            try {
                productMapper.updatePrice(Long.parseLong(ctx.productId), finalPrice);
                if (cost != null) {
                    productMapper.updateCostPrice(Long.parseLong(ctx.productId), cost);
                }
            } catch (Exception e) {
                log.warn("[PricingStrategyAgent] 落库失败 productId={}", ctx.productId, e);
            }
        }

        log.info("[PricingStrategyAgent] 定价完成 cost={} trendBoost={} marketAdj={} finalPrice={} category={} market={}",
                cost, trend, market, finalPrice, ctx.category, ctx.market);
        return ctx;
    }

    // ----------------------------------------------------------------

    private BigDecimal resolveCost(Context ctx) {
        if (ctx.totalCost != null && ctx.totalCost.compareTo(BigDecimal.ZERO) > 0) return ctx.totalCost;
        if (ctx.costPrice != null && ctx.costPrice.compareTo(BigDecimal.ZERO) > 0) return ctx.costPrice;
        return null;
    }

    private BigDecimal fetchAvgPrice(String category) {
        if (!StringUtils.hasText(category)) return null;
        try {
            return metricsMapper.selectAvgPriceByCategory(category);
        } catch (Exception e) {
            log.debug("[PricingStrategyAgent] 查询历史均价失败 category={}", category, e);
            return null;
        }
    }

    private BigDecimal calcTrendBoost(BigDecimal avgPrice) {
        if (avgPrice == null || avgPrice.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return avgPrice.multiply(TREND_BOOST_RATIO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcMarketAdjust(String market) {
        if ("EU".equalsIgnoreCase(market) || "ME".equalsIgnoreCase(market)) {
            return MARKET_ADJUST_LUXURY;
        }
        return BigDecimal.ZERO;
    }
}
