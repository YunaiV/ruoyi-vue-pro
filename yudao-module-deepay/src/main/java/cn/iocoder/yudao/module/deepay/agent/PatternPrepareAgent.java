package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * PatternPrepareAgent — Phase 9 打版准备，把设计要素转换为可打版参数。
 *
 * <h3>输出（写入 ctx）</h3>
 * <pre>
 * ctx.patternType        — 基础版型 / 贴体版型 / 宽松版型
 * ctx.sizeRange          — ["S","M","L","XL"] （按市场适配）
 * ctx.fabricSuggestion   — "棉+弹力" 等
 * ctx.difficulty         — low / medium / high
 * </pre>
 *
 * <p>规则驱动（后续可接 CAD 系统替换）。数据来源：ctx.category / ctx.style /
 * ctx.designFeatures（DesignSplitAgent 输出）/ ctx.market。</p>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ 外套+宽松 → 宽松版型，difficulty=medium</li>
 *   <li>✔ 内裤+贴体 → 贴体版型，difficulty=low</li>
 *   <li>✔ 西装+修身 → 修身版型，difficulty=high</li>
 *   <li>✔ 欧美市场 → 尺码含 XL/XXL</li>
 * </ul>
 */
@Component
public class PatternPrepareAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(PatternPrepareAgent.class);

    // ====================================================================
    // 规则表
    // ====================================================================

    /** category → patternType */
    private static final Map<String, String> PATTERN_TYPE_MAP;
    /** complexity区间 → difficulty */
    /** category → difficulty */
    private static final Map<String, String> DIFFICULTY_MAP;
    /** category → fabricSuggestion */
    private static final Map<String, String> FABRIC_SUGGEST_MAP;

    static {
        Map<String, String> pt = new LinkedHashMap<>();
        pt.put("外套",   "宽松版型");  pt.put("大衣",   "宽松版型");
        pt.put("羽绒服", "宽松版型");  pt.put("西装",   "修身版型");
        pt.put("裤子",   "基础版型");  pt.put("裙子",   "基础版型");
        pt.put("连衣裙", "基础版型");  pt.put("上衣",   "基础版型");
        pt.put("T恤",    "基础版型");  pt.put("毛衣",   "宽松版型");
        pt.put("内裤",   "贴体版型");  pt.put("内衣",   "贴体版型");
        pt.put("运动服", "弹力版型");
        PATTERN_TYPE_MAP = Collections.unmodifiableMap(pt);

        Map<String, String> diff = new LinkedHashMap<>();
        diff.put("外套",  "medium"); diff.put("大衣",   "medium"); diff.put("羽绒服","medium");
        diff.put("西装",  "high");   diff.put("裤子",   "low");    diff.put("裙子",  "low");
        diff.put("连衣裙","medium"); diff.put("上衣",   "low");    diff.put("T恤",   "low");
        diff.put("毛衣",  "medium"); diff.put("内裤",   "low");    diff.put("内衣",  "low");
        diff.put("运动服","medium");
        DIFFICULTY_MAP = Collections.unmodifiableMap(diff);

        Map<String, String> fab = new LinkedHashMap<>();
        fab.put("外套",   "棉+涤纶（3:2）");  fab.put("大衣",   "羊毛+涤纶（7:3）");
        fab.put("羽绒服", "尼龙+鸭绒");        fab.put("西装",   "精纺羊毛+衬里");
        fab.put("裤子",   "棉+弹力（9:1）");  fab.put("裙子",   "雪纺或棉");
        fab.put("连衣裙", "雪纺或棉");         fab.put("上衣",   "棉+莫代尔");
        fab.put("T恤",    "纯棉（200g）");     fab.put("毛衣",   "腈纶+棉");
        fab.put("内裤",   "棉+莱卡（9:1）");  fab.put("内衣",   "棉+莱卡");
        fab.put("运动服", "涤纶+弹力纤维（8:2）");
        FABRIC_SUGGEST_MAP = Collections.unmodifiableMap(fab);
    }

    @Override
    public Context run(Context ctx) {
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.debug("[PatternPrepareAgent] shouldProduce=false，跳过");
            return ctx;
        }

        String category = StringUtils.hasText(ctx.category) ? ctx.category : "上衣";

        ctx.patternType      = PATTERN_TYPE_MAP.getOrDefault(category, "基础版型");
        ctx.sizeRange        = buildSizeRange(ctx.market, ctx.crowd);
        ctx.fabricSuggestion = FABRIC_SUGGEST_MAP.getOrDefault(category, "棉/涤纶混纺");
        ctx.difficulty       = calcDifficulty(category, ctx.complexity);

        log.info("[PatternPrepareAgent] patternType={} difficulty={} sizeRange={} fabric={} chainCode={}",
                ctx.patternType, ctx.difficulty, ctx.sizeRange, ctx.fabricSuggestion, ctx.chainCode);
        return ctx;
    }

    // ----------------------------------------------------------------

    private List<String> buildSizeRange(String market, String crowd) {
        // 欧美 / 中东 → 加大码；国内 → 标准码
        boolean bigMarket = "EU".equalsIgnoreCase(market) || "US".equalsIgnoreCase(market);
        boolean plus      = "中老年".equals(crowd);

        if (bigMarket) return Arrays.asList("XS","S","M","L","XL","XXL");
        if (plus)      return Arrays.asList("L","XL","XXL","XXXL");
        return Arrays.asList("S","M","L","XL");
    }

    private String calcDifficulty(String category, Integer complexity) {
        if (complexity != null) {
            if (complexity > 60) return "high";
            if (complexity > 35) return "medium";
            return "low";
        }
        return DIFFICULTY_MAP.getOrDefault(category, "medium");
    }
}
