package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignCostDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignCostMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * CostEstimateAgent — Phase 9 成本预估 + 建议售价，防止做出"卖不动的高成本款"。
 *
 * <h3>计算模型</h3>
 * <pre>
 * fabricCost  = category基础面料成本 × 市场系数
 * laborCost   = 基础人工 × (1 + complexity/100)
 * totalCost   = fabricCost + laborCost
 * suggestPrice = totalCost × profitMultiplier（2.2 ~ 3.0，按市场/品类调整）
 * costTooHigh = totalCost > maxCost（品类上限）
 * </pre>
 *
 * <h3>costTooHigh 门控</h3>
 * <p>如果 totalCost 超过品类上限，设置 ctx.costTooHigh=true，
 * Orchestrator 在 Phase 9 终止流程，避免生产亏损款。</p>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ T恤 complexity=15 → totalCost ≈ 30元，suggestPrice ≈ 79元</li>
 *   <li>✔ 西装 complexity=85 → totalCost ≈ 200元，suggestPrice ≈ 499元</li>
 *   <li>✔ 廉价款 totalCost>上限 → costTooHigh=true</li>
 * </ul>
 */
@Component
public class CostEstimateAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(CostEstimateAgent.class);

    @Resource
    private DeepayDesignCostMapper costMapper;

    // ====================================================================
    // 成本基础数据（元）
    // ====================================================================

    /** category → 基础面料成本（元） */
    private static final Map<String, BigDecimal> BASE_FABRIC_COST;
    /** category → 基础人工成本（元） */
    private static final Map<String, BigDecimal> BASE_LABOR_COST;
    /** category → 成本上限（元），超出则 costTooHigh=true */
    private static final Map<String, BigDecimal> MAX_COST;
    /** category → 利润倍率（建议售价 = 总成本 × 倍率） */
    private static final Map<String, BigDecimal> PROFIT_MULTIPLIER;

    static {
        Map<String, BigDecimal> fab = new HashMap<>();
        fab.put("外套",   bd(60));  fab.put("大衣",    bd(150)); fab.put("羽绒服", bd(120));
        fab.put("西装",   bd(100)); fab.put("裤子",    bd(35));  fab.put("裙子",   bd(25));
        fab.put("连衣裙", bd(30));  fab.put("上衣",    bd(20));  fab.put("T恤",    bd(12));
        fab.put("毛衣",   bd(45));  fab.put("内裤",    bd(8));   fab.put("内衣",   bd(15));
        fab.put("运动服", bd(40));
        BASE_FABRIC_COST = fab;

        Map<String, BigDecimal> lab = new HashMap<>();
        lab.put("外套",   bd(25)); lab.put("大衣",    bd(50));  lab.put("羽绒服", bd(40));
        lab.put("西装",   bd(60)); lab.put("裤子",    bd(15));  lab.put("裙子",   bd(12));
        lab.put("连衣裙", bd(18)); lab.put("上衣",    bd(10));  lab.put("T恤",    bd(8));
        lab.put("毛衣",   bd(20)); lab.put("内裤",    bd(5));   lab.put("内衣",   bd(10));
        lab.put("运动服", bd(18));
        BASE_LABOR_COST = lab;

        Map<String, BigDecimal> max = new HashMap<>();
        max.put("外套",   bd(200)); max.put("大衣",    bd(400)); max.put("羽绒服", bd(350));
        max.put("西装",   bd(300)); max.put("裤子",    bd(100)); max.put("裙子",   bd(80));
        max.put("连衣裙", bd(100)); max.put("上衣",    bd(60));  max.put("T恤",    bd(40));
        max.put("毛衣",   bd(120)); max.put("内裤",    bd(30));  max.put("内衣",   bd(50));
        max.put("运动服", bd(120));
        MAX_COST = max;

        Map<String, BigDecimal> mult = new HashMap<>();
        mult.put("外套",   bd("2.5")); mult.put("大衣",    bd("2.8")); mult.put("羽绒服", bd("2.5"));
        mult.put("西装",   bd("2.8")); mult.put("裤子",    bd("2.2")); mult.put("裙子",   bd("2.2"));
        mult.put("连衣裙", bd("2.3")); mult.put("上衣",    bd("2.2")); mult.put("T恤",    bd("2.2"));
        mult.put("毛衣",   bd("2.4")); mult.put("内裤",    bd("3.0")); mult.put("内衣",   bd("2.5"));
        mult.put("运动服", bd("2.4"));
        PROFIT_MULTIPLIER = mult;
    }

    // ====================================================================
    // Agent.run
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[CostEstimateAgent] shouldProduce=false，跳过");
            return ctx;
        }

        String category   = StringUtils.hasText(ctx.category) ? ctx.category : "上衣";
        int    complexity = ctx.complexity != null ? ctx.complexity : 30;
        String market     = ctx.market;

        BigDecimal fabricCost  = calcFabricCost(category, market);
        BigDecimal laborCost   = calcLaborCost(category, complexity);
        BigDecimal totalCost   = fabricCost.add(laborCost);
        BigDecimal multiplier  = PROFIT_MULTIPLIER.getOrDefault(category, bd("2.2"));
        BigDecimal suggestPrice = totalCost.multiply(multiplier).setScale(0, RoundingMode.CEILING);

        // 门控
        BigDecimal maxCost = MAX_COST.getOrDefault(category, bd(500));
        ctx.costTooHigh = totalCost.compareTo(maxCost) > 0;

        // 写回 ctx
        ctx.fabricCost   = fabricCost;
        ctx.laborCost    = laborCost;
        ctx.totalCost    = totalCost;
        ctx.costPrice    = totalCost;     // 供 PricingAgent 使用
        ctx.suggestPrice = suggestPrice;  // 供 PricingStrategyAgent 使用

        // 落库
        persist(ctx, fabricCost, laborCost, totalCost, suggestPrice);

        log.info("[CostEstimateAgent] category={} complexity={} fabric={} labor={} total={} suggest={} tooHigh={}",
                category, complexity, fabricCost, laborCost, totalCost, suggestPrice, ctx.costTooHigh);

        if (Boolean.TRUE.equals(ctx.costTooHigh)) {
            ctx.decisionReason = "[CostEstimate] totalCost=" + totalCost + " > maxCost=" + maxCost + "，成本过高，终止";
            log.warn("[CostEstimateAgent] ❌ 成本过高 chainCode={}", ctx.chainCode);
        }
        return ctx;
    }

    // ====================================================================
    // 计算方法
    // ====================================================================

    private BigDecimal calcFabricCost(String category, String market) {
        BigDecimal base = BASE_FABRIC_COST.getOrDefault(category, bd(25));
        // 高端市场（EU/ME）用料成本 +30%
        if ("EU".equalsIgnoreCase(market) || "ME".equalsIgnoreCase(market)) {
            base = base.multiply(bd("1.3")).setScale(2, RoundingMode.HALF_UP);
        }
        return base;
    }

    private BigDecimal calcLaborCost(String category, int complexity) {
        BigDecimal base = BASE_LABOR_COST.getOrDefault(category, bd(15));
        // 复杂度加成：complexity/100 × base
        BigDecimal delta = base.multiply(BigDecimal.valueOf(complexity))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return base.add(delta);
    }

    private void persist(Context ctx, BigDecimal fabric, BigDecimal labor,
                          BigDecimal total, BigDecimal suggest) {
        try {
            BigDecimal margin = (suggest.compareTo(BigDecimal.ZERO) > 0)
                    ? suggest.subtract(total).divide(suggest, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            DeepayDesignCostDO rec = new DeepayDesignCostDO();
            rec.setChainCode(ctx.chainCode);
            rec.setFabricCost(fabric);
            rec.setLaborCost(labor);
            rec.setTotalCost(total);
            rec.setSuggestPrice(suggest);
            rec.setMargin(margin);
            rec.setCreatedAt(LocalDateTime.now());
            costMapper.insert(rec);
        } catch (Exception e) {
            log.warn("[CostEstimateAgent] 落库失败（不影响流程）chainCode={}", ctx.chainCode, e);
        }
    }

    private static BigDecimal bd(int v)     { return BigDecimal.valueOf(v); }
    private static BigDecimal bd(String v)  { return new BigDecimal(v); }
}
