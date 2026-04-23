package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * RiskControlAgent — 过滤 IP / 版权风险关键词（Phase 8/9 STEP 14）。
 *
 * <p>两项职责：
 * <ol>
 *   <li>检测 finalPrompt / designDetails 中的品牌关键词，设置 riskScore；
 *       riskScore &gt; 80 时重置 finalPrompt 为安全描述。</li>
 *   <li>过滤 variantImages URL 列表（URL 中含品牌词的丢弃），输出 safeImages。</li>
 * </ol>
 * </p>
 */
@Component
public class RiskControlAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(RiskControlAgent.class);

    private static final List<String> BLOCKED_BRANDS = Arrays.asList(
            "nike", "adidas", "gucci", "lv", "louis vuitton", "chanel",
            "prada", "dior", "hermes", "zara", "h&m", "logo", "trademark"
    );

    private static final int RISK_SCORE_PER_MATCH = 30;
    private static final int HIGH_RISK_THRESHOLD  = 80;
    private static final String SAFE_PROMPT =
            "minimalist fashion clothing, original design, no brand logo, no trademark";

    @Override
    public Context run(Context ctx) {
        try {
            // 1. Prompt-level risk check
            int matches = countBlockedMatches(ctx.finalPrompt) + countBlockedMatches(ctx.designDetails);
            int riskScore = Math.min(100, matches * RISK_SCORE_PER_MATCH);
            ctx.riskScore = riskScore;

            if (riskScore > HIGH_RISK_THRESHOLD) {
                log.warn("[RiskControlAgent] 高风险 riskScore={} finalPrompt 已重置为安全描述", riskScore);
                ctx.finalPrompt = SAFE_PROMPT;
            } else {
                if (StringUtils.hasText(ctx.finalPrompt)) {
                    ctx.finalPrompt = ctx.finalPrompt + ", no brand logo, no trademark";
                } else {
                    ctx.finalPrompt = SAFE_PROMPT;
                }
            }

            // 2. STEP 14: filter variantImages → safeImages
            List<String> source = ctx.variantImages != null ? ctx.variantImages : ctx.designImages;
            List<String> safe = new ArrayList<>();
            if (source != null) {
                for (String url : source) {
                    if (!containsBlockedBrand(url)) {
                        safe.add(url);
                    } else {
                        log.info("[RiskControlAgent] 过滤高风险图片 url={}", url);
                    }
                }
            }
            ctx.safeImages = safe;

            log.info("[RiskControlAgent] riskScore={} safeImages={}/{} finalPrompt={}",
                    riskScore, safe.size(), source != null ? source.size() : 0, ctx.finalPrompt);
        } catch (Exception e) {
            log.warn("[RiskControlAgent] 风险检测异常，跳过", e);
        }
        return ctx;
    }

    private boolean containsBlockedBrand(String url) {
        if (!StringUtils.hasText(url)) return false;
        String lower = url.toLowerCase(Locale.ROOT);
        for (String brand : BLOCKED_BRANDS) {
            if (lower.contains(brand)) return true;
        }
        return false;
    }

    private int countBlockedMatches(String text) {
        if (!StringUtils.hasText(text)) return 0;
        String lower = text.toLowerCase(Locale.ROOT);
        int count = 0;
        for (String brand : BLOCKED_BRANDS) {
            if (lower.contains(brand)) count++;
        }
        return count;
    }
}
