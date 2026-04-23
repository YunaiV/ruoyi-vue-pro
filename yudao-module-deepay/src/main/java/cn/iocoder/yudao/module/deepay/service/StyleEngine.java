package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.agent.TrendItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StyleEngine — 风格组合引擎（Phase 6 任务 2）。
 *
 * <p>将客户的 styleWeights Map 转为可直接拼入 Prompt 的风格组合描述，
 * 并组装包含品类、市场、趋势的完整设计 Prompt。</p>
 *
 * <p>风格映射（大写枚举 → 中文）：
 * <pre>
 *   SEXY    → 性感
 *   CASUAL  → 休闲
 *   SPORT   → 运动
 *   MINIMAL → 极简
 *   LUXURY  → 轻奢
 * </pre>
 * </p>
 *
 * <p>示例输出：
 * <pre>
 *   styleWeights = {SEXY:0.8, MINIMAL:0.5, CASUAL:0.1}
 *   → stylePrompt = "性感 + 极简"
 *
 *   全 prompt = "设计一款 外套，性感 + 极简 风格，欧洲（EU）市场，参考热销：外套、裤子"
 * </pre>
 * </p>
 *
 * <p>验收：
 * <ul>
 *   <li>✔ 至少组合 2 个风格</li>
 *   <li>✔ 权重影响排序（高权重在前）</li>
 *   <li>✔ 输出可直接用于 Prompt</li>
 * </ul>
 * </p>
 */
@Service
public class StyleEngine {

    private static final Logger log = LoggerFactory.getLogger(StyleEngine.class);

    /** 风格枚举 → 中文映射（支持大小写混用） */
    private static final Map<String, String> STYLE_MAP;

    static {
        STYLE_MAP = new LinkedHashMap<>();
        STYLE_MAP.put("SEXY",    "性感");
        STYLE_MAP.put("CASUAL",  "休闲");
        STYLE_MAP.put("SPORT",   "运动");
        STYLE_MAP.put("MINIMAL", "极简");
        STYLE_MAP.put("LUXURY",  "轻奢");
        // 小写 / 其他系统历史值
        STYLE_MAP.put("sexy",       "性感");
        STYLE_MAP.put("casual",     "休闲");
        STYLE_MAP.put("sport",      "运动");
        STYLE_MAP.put("minimal",    "极简");
        STYLE_MAP.put("minimalist", "极简");
        STYLE_MAP.put("luxury",     "轻奢");
        STYLE_MAP.put("streetwear", "街头");
        STYLE_MAP.put("workwear",   "工装");
        STYLE_MAP.put("elegant",    "优雅");
        STYLE_MAP.put("cute",       "少女");
    }

    // ====================================================================
    // 核心方法
    // ====================================================================

    /**
     * 根据 styleWeights 生成"风格 A + 风格 B"组合字符串。
     *
     * <p>规则：取权重最高的前 2 个风格，映射为中文后用" + "连接。
     * 只有 1 个风格时直接返回单个风格名。styleWeights 为空时返回空字符串。</p>
     *
     * @param weights 风格权重 Map（key=风格名, value=0~1 权重）
     * @return 风格组合字符串，例如"性感 + 极简"
     */
    public String buildStylePrompt(Map<String, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            return "";
        }
        List<String> top2 = weights.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(2)
                .map(e -> mapToChinese(e.getKey()))
                .collect(Collectors.toList());

        String result = String.join(" + ", top2);
        log.debug("[StyleEngine] styleWeights={} → stylePrompt={}", weights, result);
        return result;
    }

    /**
     * 组装完整的设计 Prompt（供 DesignAgent / FluxService 使用）。
     *
     * <p>格式：
     * <pre>
     *   设计一款 {category}，{stylePrompt} 风格，{market}市场，参考热销：{trendKeywords}
     * </pre>
     * </p>
     *
     * @param ctx        当前 Context（读取 category / stylePrompt / market / trendKeywords）
     * @return 完整 Prompt 字符串；若无任何信息则返回空字符串
     */
    public String buildFullPrompt(Context ctx) {
        StringBuilder sb = new StringBuilder();

        // 品类
        String cat = StringUtils.hasText(ctx.category) ? ctx.category : ctx.keyword;
        if (StringUtils.hasText(cat)) {
            sb.append("设计一款 ").append(cat.trim());
        }

        // 风格组合
        String stylePrompt = ctx.stylePrompt;
        if (!StringUtils.hasText(stylePrompt) && ctx.styleWeights != null) {
            stylePrompt = buildStylePrompt(ctx.styleWeights);
        }
        if (StringUtils.hasText(stylePrompt)) {
            sb.append("，").append(stylePrompt).append(" 风格");
        }

        // 市场
        if (StringUtils.hasText(ctx.market)) {
            sb.append("，").append(marketLabel(ctx.market)).append("市场");
        }

        // 价格带
        if (StringUtils.hasText(ctx.priceLevel)) {
            sb.append("，").append(priceLevelLabel(ctx.priceLevel)).append("价位");
        }

        // 趋势关键词
        List<String> keywords = ctx.trendKeywords;
        if (keywords != null && !keywords.isEmpty()) {
            String kw = keywords.stream().filter(StringUtils::hasText).limit(3)
                    .collect(Collectors.joining("、"));
            if (StringUtils.hasText(kw)) {
                sb.append("，参考热销：").append(kw);
            }
        }

        // 趋势商品风格补充
        if (ctx.trendItems != null && !ctx.trendItems.isEmpty()) {
            String trendStyles = ctx.trendItems.stream()
                    .map(TrendItem::getStyle)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .limit(2)
                    .map(this::mapToChinese)
                    .collect(Collectors.joining("、"));
            if (StringUtils.hasText(trendStyles)) {
                sb.append("，流行元素：").append(trendStyles);
            }
        }

        String prompt = sb.toString().trim();
        log.info("[StyleEngine] buildFullPrompt → {}", prompt);
        return prompt;
    }

    // ====================================================================
    // 工具方法（可复用）
    // ====================================================================

    public String mapToChinese(String style) {
        if (!StringUtils.hasText(style)) return "";
        String mapped = STYLE_MAP.get(style);
        return mapped != null ? mapped : style;
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
