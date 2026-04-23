package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignFeaturesDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignFeaturesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DesignSplitAgent — 把设计图拆解为"可生产要素"（Phase 8 → Phase 9 桥接）。
 *
 * <h3>输出（写入 ctx + 落库 deepay_design_features）</h3>
 * <pre>
 * {
 *   "category":   "外套",
 *   "fabric":     "棉/牛仔",
 *   "colors":     ["黑","灰"],
 *   "style":      "工装",
 *   "structure":  "宽松",
 *   "complexity": 35
 * }
 * </pre>
 *
 * <p>当前版本为规则驱动（后续可接 AI 视觉识别替换）。
 * 规则依据 ctx.category + ctx.style + ctx.priceLevel 推断面料和版型；
 * 复杂度由风格 × 品类矩阵得出（0~100，>80 触发 RiskControl 拦截）。</p>
 */
@Component
public class DesignSplitAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DesignSplitAgent.class);

    @Resource
    private DeepayDesignFeaturesMapper featuresMapper;

    // ====================================================================
    // 规则表
    // ====================================================================

    /** category → 推荐面料 */
    private static final Map<String, String> FABRIC_MAP;
    /** category → 基础复杂度（结构难度） */
    private static final Map<String, Integer> BASE_COMPLEXITY;
    /** style → 复杂度修正量 */
    private static final Map<String, Integer> STYLE_COMPLEXITY_DELTA;
    /** style → 版型结构 */
    private static final Map<String, String> STRUCTURE_MAP;
    /** category → 默认色系 */
    private static final Map<String, List<String>> COLOR_MAP;

    static {
        Map<String, String> fab = new LinkedHashMap<>();
        fab.put("外套",   "棉/涤纶混纺");
        fab.put("大衣",   "羊毛/羊绒");
        fab.put("羽绒服", "尼龙/鸭绒");
        fab.put("西装",   "精纺羊毛/涤纶");
        fab.put("裤子",   "棉/牛仔布");
        fab.put("裙子",   "雪纺/棉");
        fab.put("连衣裙", "雪纺/棉");
        fab.put("上衣",   "棉/莫代尔");
        fab.put("T恤",    "纯棉");
        fab.put("毛衣",   "针织棉/腈纶");
        fab.put("内裤",   "棉/莱卡");
        fab.put("内衣",   "棉/莱卡");
        fab.put("运动服", "涤纶/弹力纤维");
        FABRIC_MAP = Collections.unmodifiableMap(fab);

        Map<String, Integer> comp = new LinkedHashMap<>();
        comp.put("外套",   40);  comp.put("大衣",   55);  comp.put("羽绒服", 60);
        comp.put("西装",   70);  comp.put("裤子",   30);  comp.put("裙子",   25);
        comp.put("连衣裙", 35);  comp.put("上衣",   20);  comp.put("T恤",    15);
        comp.put("毛衣",   40);  comp.put("内裤",   15);  comp.put("内衣",   25);
        comp.put("运动服", 30);
        BASE_COMPLEXITY = Collections.unmodifiableMap(comp);

        Map<String, Integer> sd = new LinkedHashMap<>();
        sd.put("极简",   -10);  sd.put("minimal", -10);
        sd.put("休闲",   -5);   sd.put("基础款",  -10);
        sd.put("工装",   +5);   sd.put("街头",    +5);
        sd.put("性感",   +5);   sd.put("轻奢",    +10);
        sd.put("运动",   0);    sd.put("luxury",  +15);
        sd.put("走秀",   +30);  sd.put("高定",    +35);
        STYLE_COMPLEXITY_DELTA = Collections.unmodifiableMap(sd);

        Map<String, String> st = new LinkedHashMap<>();
        st.put("工装",  "宽松廓形");  st.put("极简",  "修身直筒");
        st.put("性感",  "贴体");      st.put("休闲",  "宽松");
        st.put("运动",  "弹力修身");  st.put("轻奢",  "修身");
        st.put("街头",  "oversize");  st.put("基础款","直筒");
        STRUCTURE_MAP = Collections.unmodifiableMap(st);

        Map<String, List<String>> col = new LinkedHashMap<>();
        col.put("外套",   Arrays.asList("黑", "卡其", "军绿"));
        col.put("裤子",   Arrays.asList("黑", "深蓝", "卡其"));
        col.put("连衣裙", Arrays.asList("白", "米色", "浅粉"));
        col.put("上衣",   Arrays.asList("白", "黑", "灰"));
        col.put("T恤",    Arrays.asList("白", "黑", "灰"));
        col.put("内裤",   Arrays.asList("黑", "白", "灰"));
        col.put("运动服", Arrays.asList("黑", "灰", "深蓝"));
        COLOR_MAP = Collections.unmodifiableMap(col);
    }

    // ====================================================================
    // Agent.run
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        String category = StringUtils.hasText(ctx.category) ? ctx.category : "上衣";
        String style    = StringUtils.hasText(ctx.style)    ? ctx.style    : "休闲";

        String fabric    = FABRIC_MAP.getOrDefault(category, "棉/涤纶混纺");
        String structure = STRUCTURE_MAP.getOrDefault(style, "宽松");
        int    complexity = calcComplexity(category, style);
        List<String> colors = COLOR_MAP.getOrDefault(category, Arrays.asList("黑", "白", "灰"));

        // 写回 ctx
        ctx.complexity     = complexity;
        ctx.designFeatures = buildJson(category, fabric, colors, style, structure, complexity);

        // 落库
        persist(ctx, category, fabric, style, colors, structure, complexity);

        log.info("[DesignSplitAgent] category={} fabric={} style={} structure={} complexity={}",
                category, fabric, style, structure, complexity);
        return ctx;
    }

    // ====================================================================
    // 工具
    // ====================================================================

    private int calcComplexity(String category, String style) {
        int base  = BASE_COMPLEXITY.getOrDefault(category, 35);
        int delta = STYLE_COMPLEXITY_DELTA.getOrDefault(style, 0);
        return ScoreUtil.clamp(base + delta);
    }

    private String buildJson(String category, String fabric, List<String> colors,
                              String style, String structure, int complexity) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"category\":\"").append(category).append("\",");
        sb.append("\"fabric\":\"").append(fabric).append("\",");
        sb.append("\"colors\":[");
        for (int i = 0; i < colors.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(colors.get(i)).append("\"");
        }
        sb.append("],");
        sb.append("\"style\":\"").append(style).append("\",");
        sb.append("\"structure\":\"").append(structure).append("\",");
        sb.append("\"complexity\":").append(complexity);
        sb.append("}");
        return sb.toString();
    }

    private void persist(Context ctx, String category, String fabric, String style,
                          List<String> colors, String structure, int complexity) {
        try {
            DeepayDesignFeaturesDO feat = new DeepayDesignFeaturesDO();
            feat.setChainCode(ctx.chainCode);
            feat.setCategory(category);
            feat.setFabric(fabric);
            feat.setStyle(style);
            feat.setColor(String.join(",", colors));
            feat.setStructure(structure);
            feat.setComplexity(complexity);
            feat.setCreatedAt(LocalDateTime.now());
            featuresMapper.insert(feat);
        } catch (Exception e) {
            log.warn("[DesignSplitAgent] 落库失败（不影响流程）chainCode={}", ctx.chainCode, e);
        }
    }
}
