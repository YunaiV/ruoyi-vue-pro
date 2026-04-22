package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.agent.TrendItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cn.iocoder.yudao.module.deepay.agent.StyleCombo;

import java.util.Arrays;
import java.util.ArrayList;
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

    // ====================================================================
    // Phase 6-7：风格组合方向生成（10~20 个，供设计师选方向）
    // ====================================================================

    /**
     * 根据客户画像生成 10~20 个风格组合方向。
     *
     * <p>规则：
     * <ul>
     *   <li>🟢 HOT 层（爆款）：主风格 × 4 个兼容风格 × 大众品牌 → 4~5 个方向</li>
     *   <li>🔴 DESIGN 层（灵感）：主风格 × 4 个互补风格 × 设计师品牌 → 4~5 个方向</li>
     *   <li>每种二级风格至少出一个，保证多样性</li>
     * </ul>
     * </p>
     *
     * @param ctx 包含 category / style / crowd / market / priceLevel
     * @return 10~20 个 StyleCombo，id 从 1 起连续编号
     */
    public List<StyleCombo> buildCombinations(Context ctx) {
        String category   = StringUtils.hasText(ctx.category)   ? ctx.category   : "服装";
        String primary    = StringUtils.hasText(ctx.style)      ? ctx.style      : "极简";
        String crowd      = StringUtils.hasText(ctx.crowd)      ? ctx.crowd      : "";
        String market     = StringUtils.hasText(ctx.market)     ? ctx.market     : "CN";
        String priceLevel = StringUtils.hasText(ctx.priceLevel) ? ctx.priceLevel : "MID";

        List<String> hotCompat    = getCompatibleStyles(primary, "HOT");
        List<String> designCompat = getCompatibleStyles(primary, "DESIGN");

        List<StyleCombo> result = new ArrayList<>();
        int seq = 1;

        // 🟢 HOT 层：爆款方向
        List<String> hotBrands = getMarketBrands(market, "HOT", priceLevel);
        for (String secondary : hotCompat) {
            StyleCombo combo = buildCombo(seq++, category, primary, secondary,
                    crowd, market, "HOT", hotBrands, ctx);
            result.add(combo);
        }

        // 🔴 DESIGN 层：灵感方向
        List<String> designBrands = getMarketBrands(market, "DESIGN", priceLevel);
        for (String secondary : designCompat) {
            StyleCombo combo = buildCombo(seq++, category, primary, secondary,
                    crowd, market, "DESIGN", designBrands, ctx);
            result.add(combo);
        }

        // 用主风格自身（单风格方向）填充至少 2 条保底
        if (result.size() < 10) {
            result.add(buildCombo(seq++, category, primary, null,
                    crowd, market, "HOT", hotBrands, ctx));
            result.add(buildCombo(seq++, category, primary, null,
                    crowd, market, "DESIGN", designBrands, ctx));
        }

        log.info("[StyleEngine] buildCombinations category={} primary={} count={}",
                category, primary, result.size());
        return result;
    }

    // ====================================================================
    // 风格兼容矩阵
    // ====================================================================

    /**
     * 返回与主风格兼容的副风格列表。
     * HOT 层偏大众，DESIGN 层偏高端 / 前卫。
     */
    private List<String> getCompatibleStyles(String primary, String tier) {
        Map<String, List<String>> hotMap = new LinkedHashMap<>();
        hotMap.put("工装",    Arrays.asList("极简", "街头", "休闲", "运动"));
        hotMap.put("极简",    Arrays.asList("工装", "休闲", "轻奢", "优雅"));
        hotMap.put("性感",    Arrays.asList("轻奢", "极简", "少女", "优雅"));
        hotMap.put("休闲",    Arrays.asList("极简", "运动", "街头", "工装"));
        hotMap.put("轻奢",    Arrays.asList("极简", "性感", "优雅", "工装"));
        hotMap.put("运动",    Arrays.asList("街头", "休闲", "极简", "工装"));
        hotMap.put("街头",    Arrays.asList("运动", "工装", "休闲", "极简"));
        hotMap.put("少女",    Arrays.asList("休闲", "性感", "极简", "优雅"));
        hotMap.put("优雅",    Arrays.asList("轻奢", "极简", "少女", "休闲"));
        hotMap.put("minimalist", Arrays.asList("工装", "休闲", "轻奢", "优雅"));
        hotMap.put("MINIMAL",    Arrays.asList("工装", "休闲", "轻奢", "优雅"));
        hotMap.put("SEXY",       Arrays.asList("轻奢", "极简", "少女", "优雅"));
        hotMap.put("CASUAL",     Arrays.asList("极简", "运动", "街头", "工装"));
        hotMap.put("SPORT",      Arrays.asList("街头", "休闲", "极简", "工装"));
        hotMap.put("LUXURY",     Arrays.asList("极简", "性感", "优雅", "工装"));

        Map<String, List<String>> designMap = new LinkedHashMap<>();
        designMap.put("工装",    Arrays.asList("轻奢", "极简", "前卫", "艺术"));
        designMap.put("极简",    Arrays.asList("轻奢", "前卫", "艺术", "工装"));
        designMap.put("性感",    Arrays.asList("轻奢", "前卫", "艺术", "优雅"));
        designMap.put("休闲",    Arrays.asList("轻奢", "前卫", "艺术", "极简"));
        designMap.put("轻奢",    Arrays.asList("前卫", "艺术", "优雅", "极简"));
        designMap.put("运动",    Arrays.asList("前卫", "轻奢", "艺术", "极简"));
        designMap.put("街头",    Arrays.asList("前卫", "艺术", "轻奢", "极简"));
        designMap.put("少女",    Arrays.asList("轻奢", "前卫", "艺术", "优雅"));
        designMap.put("优雅",    Arrays.asList("轻奢", "前卫", "艺术", "极简"));
        designMap.put("minimalist", Arrays.asList("轻奢", "前卫", "艺术", "工装"));
        designMap.put("MINIMAL",    Arrays.asList("轻奢", "前卫", "艺术", "工装"));
        designMap.put("SEXY",       Arrays.asList("轻奢", "前卫", "艺术", "优雅"));
        designMap.put("CASUAL",     Arrays.asList("轻奢", "前卫", "艺术", "极简"));
        designMap.put("SPORT",      Arrays.asList("前卫", "轻奢", "艺术", "极简"));
        designMap.put("LUXURY",     Arrays.asList("前卫", "艺术", "优雅", "极简"));

        Map<String, List<String>> map = "HOT".equals(tier) ? hotMap : designMap;
        List<String> styles = map.get(primary);
        if (styles == null) {
            // 兜底
            styles = "HOT".equals(tier)
                    ? Arrays.asList("极简", "休闲", "运动", "工装")
                    : Arrays.asList("轻奢", "前卫", "艺术", "优雅");
        }
        return styles;
    }

    // ====================================================================
    // 品牌参考（按市场 + 层级）
    // ====================================================================

    private List<String> getMarketBrands(String market, String tier, String priceLevel) {
        if ("HOT".equals(tier)) {
            switch (market.toUpperCase()) {
                case "EU": return Arrays.asList("ZARA", "H&M", "Mango", "Pull&Bear");
                case "US": return Arrays.asList("Nike", "Adidas", "Levi's", "Tommy");
                case "ME": return Arrays.asList("ZARA", "H&M", "Mango", "Aldo");
                default:   return Arrays.asList("SHEIN", "1688", "优衣库", "H&M");
            }
        } else {
            // DESIGN 层
            switch (market.toUpperCase()) {
                case "EU": return Arrays.asList("Balenciaga", "Gucci", "Prada", "COS");
                case "US": return Arrays.asList("Off-White", "Supreme", "Ralph Lauren", "Coach");
                case "ME": return Arrays.asList("Gucci", "Prada", "Chanel", "Versace");
                default:   return Arrays.asList("Calvin Klein", "COS", "& Other Stories", "Acne Studios");
            }
        }
    }

    // ====================================================================
    // 构建单个 StyleCombo
    // ====================================================================

    private StyleCombo buildCombo(int id, String category, String primary, String secondary,
                                  String crowd, String market, String tier,
                                  List<String> brands, Context ctx) {
        StyleCombo combo = new StyleCombo();
        combo.setId(id);
        combo.setCategory(category);
        combo.setPrimaryStyle(primary);
        combo.setSecondaryStyle(secondary);
        combo.setCrowd(crowd);
        combo.setMarket(market);
        combo.setTier(tier);
        combo.setSourceRefs(brands.subList(0, Math.min(2, brands.size())));

        // 构建 Prompt
        StringBuilder sb = new StringBuilder();
        sb.append("设计一款").append(category);
        sb.append("，").append(mapToChinese(primary));
        if (StringUtils.hasText(secondary)) {
            sb.append("+").append(mapToChinese(secondary));
        }
        sb.append(" 风格");
        if (StringUtils.hasText(crowd)) {
            sb.append("，").append(crowd);
        }
        sb.append("，").append(marketLabel(market)).append("（").append(market).append("）市场");
        sb.append("，参考：").append(String.join("、", combo.getSourceRefs()));
        combo.setPrompt(sb.toString());

        return combo;
    }

}
