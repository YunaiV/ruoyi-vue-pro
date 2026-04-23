package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayCustomerProfileDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayCustomerProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * StyleConsistencyAgent — 风格一致性校验（Phase 8）。
 *
 * <h3>校验逻辑</h3>
 * <ol>
 *   <li>从 deepay_customer_profile 读取 categoryLevel1（主营品类）</li>
 *   <li>若 ctx.category ≠ historyMainCategory → penalty -30</li>
 *   <li>高端市场（EU/ME）出现廉价信号词 → penalty -20</li>
 *   <li>其余 URL 品类交叉检测（已有实现） → 每张违规 -20</li>
 * </ol>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>ctx.styleConsistencyScore — 0~100，&lt;60 触发 DesignConfirmAgent 重设计</li>
 *   <li>ctx.needRedesign — true 当 score&lt;60</li>
 * </ul>
 */
@Component
public class StyleConsistencyAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(StyleConsistencyAgent.class);

    private static final int PASS_THRESHOLD = 60;

    @Resource
    private DeepayCustomerProfileMapper profileMapper;

    /** 品类关键词（用于跨品类检测） */
    private static final Map<String, List<String>> CATEGORY_KEYWORDS;
    /** 高端市场廉价信号词 */
    private static final List<String> CHEAP_SIGNALS =
            Arrays.asList("cheap","bargain","shein","taobao","1688","aliexpress","low-cost");

    static {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("外套",   Arrays.asList("coat","jacket","outerwear"));
        m.put("内裤",   Arrays.asList("underwear","briefs","boxer","panty"));
        m.put("内衣",   Arrays.asList("bra","lingerie","intimate"));
        m.put("裤子",   Arrays.asList("pants","trousers","jeans"));
        m.put("连衣裙", Arrays.asList("dress","gown"));
        m.put("裙子",   Arrays.asList("skirt","dress"));
        m.put("上衣",   Arrays.asList("tshirt","t-shirt","top","blouse"));
        m.put("西装",   Arrays.asList("suit","blazer","formal"));
        CATEGORY_KEYWORDS = Collections.unmodifiableMap(m);
    }

    @Override
    public Context run(Context ctx) {
        if (ctx.designImages == null || ctx.designImages.isEmpty()) {
            ctx.styleConsistencyScore = 80;
            return ctx;
        }

        int score = 100;

        // ── 1. 主营品类校验（读 customer_profile）─────────────────
        score += checkCategoryConsistency(ctx);

        // ── 2. 高端市场廉价信号检测 ───────────────────────────────
        if (isLuxuryMarket(ctx.market)) {
            for (String url : ctx.designImages) {
                if (containsCheapSignal(url)) { score -= 20; break; }
            }
        }

        // ── 3. URL 品类交叉检测 ───────────────────────────────────
        if (StringUtils.hasText(ctx.category)) {
            int violations = 0;
            for (String url : ctx.designImages) {
                if (hasCategoryConflict(url.toLowerCase(), ctx.category)) violations++;
            }
            score -= violations * 20;
        }

        score = ScoreUtil.clamp(score);
        ctx.styleConsistencyScore = score;

        if (score < PASS_THRESHOLD) {
            ctx.needRedesign   = true;
            ctx.decisionReason = "[StyleConsistency] score=" + score + "<" + PASS_THRESHOLD + "，风格偏离，触发重设计";
            log.info("[StyleConsistencyAgent] ❌ score={} category={} → needRedesign", score, ctx.category);
        } else {
            log.info("[StyleConsistencyAgent] ✅ score={} category={} style={}", score, ctx.category, ctx.style);
        }
        return ctx;
    }

    // ----------------------------------------------------------------

    private int checkCategoryConsistency(Context ctx) {
        if (ctx.customerId == null || !StringUtils.hasText(ctx.category)) return 0;
        try {
            DeepayCustomerProfileDO profile = profileMapper.selectByCustomerId(ctx.customerId);
            if (profile == null) return 0;
            String historyMain = profile.getCategoryLevel1();
            if (!StringUtils.hasText(historyMain)) return 0;
            if (!historyMain.equals(ctx.category)) {
                log.info("[StyleConsistencyAgent] 品类偏离 history={} current={} penalty=-30",
                        historyMain, ctx.category);
                return -30;
            }
        } catch (Exception e) {
            log.debug("[StyleConsistencyAgent] profile查询失败（不影响评分）", e);
        }
        return 0;
    }

    private boolean hasCategoryConflict(String urlLower, String targetCat) {
        List<String> targetKws = CATEGORY_KEYWORDS.get(targetCat);
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (entry.getKey().equals(targetCat)) continue;
            for (String kw : entry.getValue()) {
                if (urlLower.contains(kw)) {
                    if (targetKws != null) {
                        for (String tkw : targetKws) {
                            if (urlLower.contains(tkw)) return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsCheapSignal(String url) {
        if (!StringUtils.hasText(url)) return false;
        String lower = url.toLowerCase();
        for (String s : CHEAP_SIGNALS) { if (lower.contains(s)) return true; }
        return false;
    }

    private boolean isLuxuryMarket(String market) {
        return "EU".equalsIgnoreCase(market) || "ME".equalsIgnoreCase(market);
    }
}
