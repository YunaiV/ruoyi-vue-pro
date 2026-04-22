package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StyleConsistencyAgent — 层2 风格一致性校验（Phase 8）。
 *
 * <h3>功能</h3>
 * <p>在设计图生成后（DesignGenerateAgent / DesignAgent 之后），校验
 * {@link Context#designImages} 中的图片风格是否与客户画像一致。
 * 不一致时设置 {@link Context#needRedesign}=true，触发重新设计。</p>
 *
 * <h3>校验维度（规则驱动，后期接 AI 视觉检测）</h3>
 * <ol>
 *   <li><b>品类一致</b>：设计图 URL/标签不含其他品类关键词</li>
 *   <li><b>风格相符</b>：设计图 URL 中包含目标风格关键词（有则优先）</li>
 *   <li><b>市场适配</b>：高端市场（EU/ME）不出现"廉价"信号词</li>
 * </ol>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>{@link Context#styleConsistencyScore} — 0~100 分，&lt;60 触发重设计</li>
 *   <li>{@link Context#needRedesign} — true 时 Orchestrator 重跑设计循环</li>
 * </ul>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ 外套客户拿到内裤图 → score&lt;60 → needRedesign=true</li>
 *   <li>✔ 外套客户拿到外套图 → score≥60 → 通过</li>
 *   <li>✔ 无画像信息 → 默认通过（score=80）</li>
 * </ul>
 */
@Component
public class StyleConsistencyAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(StyleConsistencyAgent.class);

    private static final int PASS_THRESHOLD = 60;

    /** 品类关键词组（用于检测图片是否跨品类） */
    private static final Map<String, List<String>> CATEGORY_KEYWORDS;

    static {
        Map<String, List<String>> m = new HashMap<>();
        m.put("外套",  Arrays.asList("coat", "jacket", "outerwear", "outer"));
        m.put("内裤",  Arrays.asList("underwear", "briefs", "boxer", "panty", "lingerie"));
        m.put("内衣",  Arrays.asList("bra", "lingerie", "underwear", "intimate"));
        m.put("裤子",  Arrays.asList("pants", "trousers", "jeans", "leggings"));
        m.put("连衣裙", Arrays.asList("dress", "gown", "skirt"));
        m.put("裙子",  Arrays.asList("skirt", "dress"));
        m.put("上衣",  Arrays.asList("tshirt", "t-shirt", "top", "blouse", "shirt"));
        m.put("西装",  Arrays.asList("suit", "blazer", "formal"));
        m.put("毛衣",  Arrays.asList("sweater", "knitwear", "pullover"));
        CATEGORY_KEYWORDS = Collections.unmodifiableMap(m);
    }

    /** 高端市场廉价信号词 */
    private static final List<String> CHEAP_SIGNALS =
            Arrays.asList("cheap", "low-cost", "bargain", "shein", "taobao", "1688", "aliexpress");

    @Override
    public Context run(Context ctx) {
        if (ctx.designImages == null || ctx.designImages.isEmpty()) {
            log.debug("[StyleConsistencyAgent] 无设计图，跳过");
            ctx.styleConsistencyScore = 80;
            return ctx;
        }

        // 无画像信息 → 默认通过
        if (!StringUtils.hasText(ctx.category) && !StringUtils.hasText(ctx.style)) {
            ctx.styleConsistencyScore = 80;
            log.debug("[StyleConsistencyAgent] 无画像，默认通过 score=80");
            return ctx;
        }

        int score = evaluate(ctx);
        ctx.styleConsistencyScore = score;

        if (score < PASS_THRESHOLD) {
            ctx.needRedesign   = true;
            ctx.decisionReason = "[StyleConsistency] score=" + score + " < " + PASS_THRESHOLD
                    + "，风格与画像不一致，触发重设计";
            log.info("[StyleConsistencyAgent] ❌ 风格不一致 score={} category={} style={} → needRedesign",
                    score, ctx.category, ctx.style);
        } else {
            ctx.needRedesign = false;
            log.info("[StyleConsistencyAgent] ✅ 风格一致 score={} category={} style={}",
                    score, ctx.category, ctx.style);
        }
        return ctx;
    }

    // ====================================================================
    // 评分逻辑
    // ====================================================================

    private int evaluate(Context ctx) {
        int score       = 100;
        int imageCount  = ctx.designImages.size();
        int violations  = 0;

        String targetCat = ctx.category != null ? ctx.category.toLowerCase() : null;
        boolean isLuxury = isLuxuryMarket(ctx.market);

        for (String url : ctx.designImages) {
            if (!StringUtils.hasText(url)) continue;
            String lower = url.toLowerCase();

            // 检查品类冲突
            if (targetCat != null && hasCategoryConflict(lower, targetCat)) {
                violations++;
            }

            // 高端市场廉价信号
            if (isLuxury && containsCheapSignal(lower)) {
                violations++;
            }
        }

        if (imageCount > 0 && violations > 0) {
            // 每张违规图扣 20 分
            score = Math.max(0, 100 - (violations * 20));
        }

        return score;
    }

    private boolean hasCategoryConflict(String urlLower, String targetCat) {
        // 找目标品类的关键词
        List<String> targetKws = CATEGORY_KEYWORDS.get(targetCat);

        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (entry.getKey().equals(targetCat)) continue;
            for (String kw : entry.getValue()) {
                if (urlLower.contains(kw)) {
                    // URL 含有其他品类关键词
                    // 如果同时含有目标品类关键词则不算冲突
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

    private boolean containsCheapSignal(String urlLower) {
        for (String s : CHEAP_SIGNALS) {
            if (urlLower.contains(s)) return true;
        }
        return false;
    }

    private boolean isLuxuryMarket(String market) {
        return "EU".equalsIgnoreCase(market) || "ME".equalsIgnoreCase(market);
    }

}
