package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * RiskControlAgent — Phase 8 风险拦截（logo + complexity + cost/margin）。
 *
 * <h3>三类拦截规则</h3>
 * <ol>
 *   <li><b>品牌Logo检测</b>：selectedImage/designImages URL含知名品牌关键词 → 拒绝</li>
 *   <li><b>结构复杂度</b>：ctx.complexity &gt; 80 → 拒绝（无法量产）</li>
 *   <li><b>成本/利润</b>：毛利率 &lt; 20% 或绝对利润 &lt; 10元 → 拒绝</li>
 * </ol>
 *
 * <h3>品类×市场合规</h3>
 * <p>内裤/内衣 在 中东（ME）市场 → 合规拦截。</p>
 */
@Component
public class RiskControlAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(RiskControlAgent.class);

    private static final BigDecimal MIN_MARGIN = new BigDecimal("0.20");
    private static final BigDecimal MIN_PROFIT = new BigDecimal("10.00");

    /** 禁止出现在图片URL中的品牌关键词（大小写不敏感） */
    private static final List<String> LOGO_BRANDS = Arrays.asList(
            "gucci","prada","balenciaga","chanel","hermes","louis vuitton","lv",
            "nike","adidas","supreme","off-white","offwhite","dior","versace",
            "burberry","fendi","givenchy","valentino","moncler","armani"
    );

    @Override
    public Context run(Context ctx) {
        ctx.riskLevel = "NONE";

        // ── 1. Logo 检测 ──────────────────────────────────────────
        if (detectLogo(ctx)) {
            ctx.logoDetected = true;
            return block(ctx, "HIGH", "[RiskControl] 检测到品牌Logo，拒绝生产");
        }
        ctx.logoDetected = false;

        // ── 2. 复杂度门控 ─────────────────────────────────────────
        if (ctx.complexity != null && ctx.complexity > 80) {
            return block(ctx, "HIGH",
                    "[RiskControl] 结构复杂度=" + ctx.complexity + ">80，无法量产");
        }

        // ── 3. 无设计图 ───────────────────────────────────────────
        if (ctx.designImages == null || ctx.designImages.isEmpty()) {
            return block(ctx, "HIGH", "[RiskControl] 无设计图，拒绝进入生产");
        }

        // ── 4. 合规校验 ───────────────────────────────────────────
        RiskResult compliance = checkCompliance(ctx);
        if (compliance.blocked) return block(ctx, "HIGH", compliance.reason);

        // ── 5. 成本/利润校验 ─────────────────────────────────────
        RiskResult price = checkPriceRisk(ctx);
        if (price.blocked) return block(ctx, "HIGH", price.reason);
        if ("MEDIUM".equals(price.level)) {
            ctx.riskLevel      = "MEDIUM";
            ctx.decisionReason = price.reason;
            log.warn("[RiskControl] ⚠️ MEDIUM风险 {} chainCode={}", price.reason, ctx.chainCode);
        }

        if (ctx.shouldProduce == null) ctx.shouldProduce = true;
        log.info("[RiskControl] ✅ 通过 complexity={} riskLevel={} chainCode={}",
                ctx.complexity, ctx.riskLevel, ctx.chainCode);
        return ctx;
    }

    // ----------------------------------------------------------------

    private boolean detectLogo(Context ctx) {
        List<String> urls = new java.util.ArrayList<>();
        if (StringUtils.hasText(ctx.selectedImage)) urls.add(ctx.selectedImage);
        if (ctx.designImages != null) urls.addAll(ctx.designImages);
        if (StringUtils.hasText(ctx.designFeatures)) urls.add(ctx.designFeatures);

        for (String url : urls) {
            if (!StringUtils.hasText(url)) continue;
            String lower = url.toLowerCase();
            for (String brand : LOGO_BRANDS) {
                if (lower.contains(brand)) {
                    log.warn("[RiskControl] Logo检测命中 brand={} url={}", brand, shorten(url));
                    return true;
                }
            }
        }
        return false;
    }

    private RiskResult checkCompliance(Context ctx) {
        if (!StringUtils.hasText(ctx.category) || !StringUtils.hasText(ctx.market)) {
            return RiskResult.pass();
        }
        if (("内裤".equals(ctx.category) || "内衣".equals(ctx.category))
                && "ME".equalsIgnoreCase(ctx.market)) {
            return RiskResult.block("[RiskControl] 合规拦截：" + ctx.category + " 不允许在ME市场销售");
        }
        return RiskResult.pass();
    }

    private RiskResult checkPriceRisk(Context ctx) {
        BigDecimal cost  = ctx.costPrice != null ? ctx.costPrice :
                           (ctx.totalCost != null ? ctx.totalCost : null);
        BigDecimal price = ctx.suggestPrice != null ? ctx.suggestPrice : ctx.price;
        if (cost == null || price == null || cost.compareTo(BigDecimal.ZERO) <= 0) {
            return RiskResult.pass();
        }
        BigDecimal profit = price.subtract(cost);
        if (profit.compareTo(MIN_PROFIT) < 0) {
            return RiskResult.block(String.format(
                    "[RiskControl] 绝对利润%.2f<%.2f（cost=%.2f price=%.2f）",
                    profit, MIN_PROFIT, cost, price));
        }
        BigDecimal margin = profit.divide(cost, 4, RoundingMode.HALF_UP);
        if (margin.compareTo(MIN_MARGIN) < 0) {
            return RiskResult.block(String.format(
                    "[RiskControl] 毛利率%.1f%%<20%%（cost=%.2f price=%.2f）",
                    margin.multiply(new BigDecimal("100")), cost, price));
        }
        if (margin.compareTo(new BigDecimal("0.30")) < 0) {
            return RiskResult.warn(String.format(
                    "[RiskControl] 毛利率偏低%.1f%%，建议调价", margin.multiply(new BigDecimal("100"))));
        }
        return RiskResult.pass();
    }

    private Context block(Context ctx, String level, String reason) {
        ctx.shouldProduce  = false;
        ctx.riskLevel      = level;
        ctx.decisionReason = reason;
        log.warn("[RiskControl] ❌ {} chainCode={}", reason, ctx.chainCode);
        return ctx;
    }

    private static String shorten(String s) {
        if (s == null) return "null";
        return s.length() > 80 ? s.substring(0, 80) + "…" : s;
    }

    private static class RiskResult {
        final boolean blocked; final String level; final String reason;
        RiskResult(boolean b, String l, String r) { blocked=b; level=l; reason=r; }
        static RiskResult pass()               { return new RiskResult(false,"NONE",  null); }
        static RiskResult warn(String r)       { return new RiskResult(false,"MEDIUM",r); }
        static RiskResult block(String r)      { return new RiskResult(true, "HIGH",  r); }
    }
}
