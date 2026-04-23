package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.service.FluxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * DesignGenerateAgent — Phase 8 专用 AI 出图 Agent。
 *
 * <h3>与 DesignAgent 的区别</h3>
 * <ul>
 *   <li>DesignAgent：从 styleWeights/stylePrompt 生成通用设计图（Phase 6 个性化改款）</li>
 *   <li>DesignGenerateAgent：Phase 8 选款落地，明确以 {@code trendImages} 为参考爆款元素，
 *       生成 4 张可量产、适合批发的设计图</li>
 * </ul>
 *
 * <h3>Prompt 模板</h3>
 * <pre>
 * 设计一款{category}，
 * 风格：{style}，
 * 参考这些爆款元素：{trendImages}，
 * 要求：可量产、适合{market}市场
 * </pre>
 *
 * <h3>输入</h3>
 * <ul>
 *   <li>{@link Context#category} — 品类（外套/裤子…）</li>
 *   <li>{@link Context#style}    — 风格（工装/极简…）</li>
 *   <li>{@link Context#trendImages} — 参考爆款图列表（来自 TrendAgent）</li>
 *   <li>{@link Context#market}   — 目标市场（EU/US/CN/ME）</li>
 * </ul>
 *
 * <h3>输出</h3>
 * <ul>
 *   <li>{@link Context#designImages} — 3~6 张 AI 生成设计图 URL</li>
 * </ul>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ Prompt 包含品类 + 风格 + 市场 + 爆款参考元素</li>
 *   <li>✔ 生成 4 张图（FluxService 失败时至少保底 1 张）</li>
 *   <li>✔ trendImages 为空时退化为 keyword 兜底，不报错</li>
 * </ul>
 */
@Component
public class DesignGenerateAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignGenerateAgent.class);

    /** Phase 8 默认生成张数 */
    private static final int DEFAULT_COUNT = 4;

    @Resource
    private FluxService fluxService;

    @Override
    public Context run(Context ctx) {
        String prompt = buildPrompt(ctx);
        log.info("[DesignGenerateAgent] 生成设计图 prompt={}", prompt);

        List<String> images = fluxService.generateImages(prompt, DEFAULT_COUNT);
        ctx.designImages = images;

        log.info("[DesignGenerateAgent] 生成完成 count={} category={} style={}",
                images.size(), ctx.category, ctx.style);
        return ctx;
    }

    // ====================================================================
    // Prompt 构建
    // ====================================================================

    private String buildPrompt(Context ctx) {
        String category = StringUtils.hasText(ctx.category) ? ctx.category : "服装";
        String style    = StringUtils.hasText(ctx.style)    ? ctx.style    : "休闲";
        String market   = StringUtils.hasText(ctx.market)   ? ctx.market   : "CN";

        // 趋势元素摘要（取前 3 张 URL 的末段文件名，避免 prompt 过长）
        String trendRef = buildTrendRef(ctx.trendImages);

        StringBuilder sb = new StringBuilder();
        sb.append("设计一款").append(category);
        sb.append("，风格：").append(style);
        if (StringUtils.hasText(trendRef)) {
            sb.append("，参考这些爆款元素：").append(trendRef);
        }
        sb.append("，要求：可量产、适合").append(marketLabel(market)).append("（").append(market).append("）市场");

        // 补充客群信息
        if (StringUtils.hasText(ctx.crowd)) {
            sb.append("，目标客群：").append(ctx.crowd);
        }
        // 价位信息
        if (StringUtils.hasText(ctx.priceLevel)) {
            sb.append("，").append(priceLevelLabel(ctx.priceLevel)).append("价位");
        }

        return sb.toString();
    }

    private String buildTrendRef(java.util.List<String> trendImages) {
        if (trendImages == null || trendImages.isEmpty()) {
            return "";
        }
        // 取前 3 张，每张取 URL 最后一段（文件名）作为简短引用
        return trendImages.stream()
                .limit(3)
                .map(url -> {
                    if (!StringUtils.hasText(url)) return "";
                    int slash = url.lastIndexOf('/');
                    return slash >= 0 ? url.substring(slash + 1) : url;
                })
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.joining("、"));
    }

    private String marketLabel(String market) {
        switch (market.toUpperCase()) {
            case "EU": return "欧洲";
            case "US": return "北美";
            case "ME": return "中东";
            default:   return "国内";
        }
    }

    private String priceLevelLabel(String level) {
        switch (level.toUpperCase()) {
            case "HIGH": return "高端";
            case "LOW":  return "低端";
            default:     return "中端";
        }
    }

}
