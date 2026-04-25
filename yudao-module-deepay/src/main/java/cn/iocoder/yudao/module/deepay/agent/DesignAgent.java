package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.service.FluxService;
import cn.iocoder.yudao.module.deepay.service.StyleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * DesignAgent — 根据客户画像生成个性化改款图（Phase 6 最终版）。
 *
 * <p>Prompt 优先级：
 * <ol>
 *   <li>{@link Context#stylePrompt}（已由 StyleEngine 组装好，最高优先级）</li>
 *   <li>StyleEngine 实时组装（有画像字段但 stylePrompt 未填时）</li>
 *   <li>{@link Context#keyword}（纯关键词兜底，与原版行为完全相同）</li>
 * </ol>
 * </p>
 *
 * <p>Prompt 示例（有完整画像时）：
 * <pre>
 *   设计一款 外套，性感 + 极简 风格，欧洲市场，参考热销：外套、连衣裙，流行元素：MINIMAL
 * </pre>
 * </p>
 */
@Component
public class DesignAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignAgent.class);

    @Resource private FluxService  fluxService;
    @Resource private StyleEngine  styleEngine;

    @Override
    public Context run(Context ctx) {
        String prompt = resolvePrompt(ctx);
        log.info("[DesignAgent] 生成设计图 prompt={}", prompt);
        ctx.designImages = fluxService.generateImages(prompt);
        return ctx;
    }

    // ====================================================================
    // Prompt 解析
    // ====================================================================

    private String resolvePrompt(Context ctx) {
        // 优先使用已组装好的 stylePrompt（由 Orchestrator 调用 StyleEngine 预先填充）
        if (StringUtils.hasText(ctx.stylePrompt)) {
            return appendTrendHint(ctx.stylePrompt, ctx);
        }

        // 有画像字段 → 实时组装完整 prompt
        boolean hasProfile = StringUtils.hasText(ctx.category)
                || (ctx.styleWeights != null && !ctx.styleWeights.isEmpty())
                || StringUtils.hasText(ctx.style)
                || StringUtils.hasText(ctx.market);

        if (hasProfile) {
            // 先把主风格折算进 styleWeights（若只有 style 字符串）
            if (ctx.styleWeights == null && StringUtils.hasText(ctx.style)) {
                ctx.styleWeights = new java.util.LinkedHashMap<>();
                ctx.styleWeights.put(ctx.style, 0.8);
            }
            String full = styleEngine.buildFullPrompt(ctx);
            if (StringUtils.hasText(full)) {
                return appendTrendHint(full, ctx);
            }
        }

        // 最终兜底：只用 keyword（与原版行为一致）
        String kw = StringUtils.hasText(ctx.keyword) ? ctx.keyword : "";
        log.debug("[DesignAgent] 无画像，退化为 keyword={}", kw);
        return appendTrendHint(kw, ctx);
    }

    // STEP 12: append trend reference hint if trendItems available
    private String appendTrendHint(String prompt, Context ctx) {
        if (ctx.trendItems == null || ctx.trendItems.isEmpty()) return prompt;
        String topTrendUrls = ctx.trendItems.stream()
                .limit(3)
                .map(TrendItem::getImageUrl)
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.joining(", "));
        if (StringUtils.hasText(topTrendUrls)) {
            return prompt + ", trend reference: " + topTrendUrls;
        }
        return prompt;
    }

}
