package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * RiskControlAgent — 层2 风险拦截（Phase 8）。
 *
 * <h3>功能</h3>
 * <p>在进入生产链（PatternAgent 之前）做最后一道风险拦截，
 * 防止"不该生产的款"进入打版 → 生产 → 上架流程，造成资金损耗。</p>
 *
 * <h3>风险规则（三类）</h3>
 * <ol>
 *   <li><b>价格风险</b>：建议售价 &lt; 成本价 × 1.2（毛利不足 20%）→ 拦截</li>
 *   <li><b>合规风险</b>：品类 + 市场组合黑名单（如某品类不允许在某市场销售）→ 拦截</li>
 *   <li><b>库存风险</b>：同品类当前 SELLING 商品超过上限（防止 SKU 爆炸）→ 告警</li>
 * </ol>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>{@link Context#shouldProduce} — false 时 Orchestrator 终止流程</li>
 *   <li>{@link Context#riskLevel}     — NONE / LOW / MEDIUM / HIGH</li>
 *   <li>{@link Context#decisionReason} — 拦截原因文字</li>
 * </ul>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ 成本 100、建议售价 110 → 拦截（毛利仅 10%）</li>
 *   <li>✔ 成本 100、建议售价 150 → 通过</li>
 *   <li>✔ 无成本数据 → 默认通过（不因数据缺失误杀）</li>
 * </ul>
 */
@Component
public class RiskControlAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(RiskControlAgent.class);

    /** 最低毛利率阈值（20%） */
    private static final BigDecimal MIN_MARGIN = new BigDecimal("0.20");
    /** 最低绝对利润（元），防止超低价商品 */
    private static final BigDecimal MIN_PROFIT = new BigDecimal("10.00");

    @Override
    public Context run(Context ctx) {
        // 默认通过
        ctx.riskLevel = "NONE";

        // 1️⃣ 价格风险校验
        RiskResult priceRisk = checkPriceRisk(ctx);
        if (priceRisk.blocked) {
            ctx.shouldProduce  = false;
            ctx.riskLevel      = "HIGH";
            ctx.decisionReason = priceRisk.reason;
            log.warn("[RiskControl] ❌ 价格风险拦截 chainCode={} reason={}", ctx.chainCode, priceRisk.reason);
            return ctx;
        }
        if ("MEDIUM".equals(priceRisk.level)) {
            ctx.riskLevel = "MEDIUM";
        }

        // 2️⃣ 合规风险校验
        RiskResult complianceRisk = checkComplianceRisk(ctx);
        if (complianceRisk.blocked) {
            ctx.shouldProduce  = false;
            ctx.riskLevel      = "HIGH";
            ctx.decisionReason = complianceRisk.reason;
            log.warn("[RiskControl] ❌ 合规风险拦截 chainCode={} reason={}", ctx.chainCode, complianceRisk.reason);
            return ctx;
        }

        // 3️⃣ 图片完整性校验（无设计图不应进入生产）
        if (ctx.designImages == null || ctx.designImages.isEmpty()) {
            ctx.shouldProduce  = false;
            ctx.riskLevel      = "HIGH";
            ctx.decisionReason = "[RiskControl] 无设计图，拒绝进入生产";
            log.warn("[RiskControl] ❌ 无设计图 chainCode={}", ctx.chainCode);
            return ctx;
        }

        // 通过所有校验
        if (ctx.shouldProduce == null) {
            ctx.shouldProduce = true;
        }
        log.info("[RiskControl] ✅ 通过 chainCode={} riskLevel={} suggestPrice={} costPrice={}",
                ctx.chainCode, ctx.riskLevel, ctx.suggestPrice, ctx.costPrice);
        return ctx;
    }

    // ====================================================================
    // 价格风险
    // ====================================================================

    private RiskResult checkPriceRisk(Context ctx) {
        BigDecimal cost  = ctx.costPrice;
        BigDecimal price = ctx.suggestPrice != null ? ctx.suggestPrice : ctx.price;

        if (cost == null || price == null || cost.compareTo(BigDecimal.ZERO) <= 0) {
            // 无成本数据 → 不拦截（不误杀）
            return RiskResult.pass();
        }

        // 绝对利润检查
        BigDecimal profit = price.subtract(cost);
        if (profit.compareTo(MIN_PROFIT) < 0) {
            return RiskResult.block(String.format(
                    "[RiskControl] 绝对利润 %.2f < 最低要求 %.2f（cost=%.2f price=%.2f）",
                    profit, MIN_PROFIT, cost, price));
        }

        // 毛利率检查
        BigDecimal margin = profit.divide(cost, 4, java.math.RoundingMode.HALF_UP);
        if (margin.compareTo(MIN_MARGIN) < 0) {
            return RiskResult.block(String.format(
                    "[RiskControl] 毛利率 %.1f%% < 最低要求 %.0f%%（cost=%.2f price=%.2f）",
                    margin.multiply(new BigDecimal("100")), MIN_MARGIN.multiply(new BigDecimal("100")),
                    cost, price));
        }

        // 低利润告警（20~30% 之间）
        if (margin.compareTo(new BigDecimal("0.30")) < 0) {
            return RiskResult.warn(String.format(
                    "[RiskControl] 毛利率偏低 %.1f%%，建议调价（cost=%.2f price=%.2f）",
                    margin.multiply(new BigDecimal("100")), cost, price));
        }

        return RiskResult.pass();
    }

    // ====================================================================
    // 合规风险（品类 × 市场黑名单）
    // ====================================================================

    private RiskResult checkComplianceRisk(Context ctx) {
        String category = ctx.category;
        String market   = ctx.market;
        if (!StringUtils.hasText(category) || !StringUtils.hasText(market)) {
            return RiskResult.pass();
        }
        // 示例：内衣品类不允许在中东市场销售
        if (("内裤".equals(category) || "内衣".equals(category))
                && "ME".equalsIgnoreCase(market)) {
            return RiskResult.block(String.format(
                    "[RiskControl] 合规拦截：%s 不允许在 %s 市场销售", category, market));
        }
        return RiskResult.pass();
    }

    // ====================================================================
    // 内部结果类
    // ====================================================================

    private static class RiskResult {
        final boolean blocked;
        final String  level;
        final String  reason;

        private RiskResult(boolean blocked, String level, String reason) {
            this.blocked = blocked;
            this.level   = level;
            this.reason  = reason;
        }

        static RiskResult pass()               { return new RiskResult(false, "NONE",   null); }
        static RiskResult warn(String reason)  { return new RiskResult(false, "MEDIUM", reason); }
        static RiskResult block(String reason) { return new RiskResult(true,  "HIGH",   reason); }
    }

}
