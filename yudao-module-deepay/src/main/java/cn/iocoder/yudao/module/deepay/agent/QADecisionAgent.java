package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * QADecisionAgent — 五问引导（Phase 6-7 选款引导层核心）。
 *
 * <p><b>核心原则：客户进来不用说关键词，系统主动问。</b></p>
 *
 * <p>五问决策树（优先级从高到低）：
 * <ol>
 *   <li><b>category</b>  — 你主要做什么品类？（影响全部后续 Agent）</li>
 *   <li><b>crowd</b>     — 你的客户是谁？（影响款式推荐方向）</li>
 *   <li><b>style</b>     — 什么风格？（影响 TrendAgent 过滤 + DesignAgent）</li>
 *   <li><b>market</b>    — 主要卖哪里？（影响 StyleEngine Prompt + 品牌参考）</li>
 *   <li><b>priceLevel</b>— 什么价位？（影响定价 + 选款方向）</li>
 * </ol>
 * </p>
 *
 * <p>流程控制：
 * <ul>
 *   <li>一次只问一个问题 → 设置 {@link Context#pendingQuestion} + {@link Context#pendingField} 后立即返回</li>
 *   <li>Orchestrator 检测到 pendingQuestion != null 则将问题返回给调用方，暂停流程</li>
 *   <li>调用方把答案填入对应字段（pendingField），再次调用 Orchestrator 继续</li>
 *   <li>五个字段全部填齐后 pendingQuestion=null，confidenceScore 升至 0.6</li>
 * </ul>
 * </p>
 *
 * <p>验收：
 * <ul>
 *   <li>✔ 客户进来不用说关键词，系统问</li>
 *   <li>✔ 第二次不再问（画像已有，confidence >= 0.6）</li>
 *   <li>✔ 系统记住"做外套 + 欧美 + 工装"，以后只推外套不推内裤</li>
 * </ul>
 * </p>
 */
@Component
public class QADecisionAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(QADecisionAgent.class);

    /** 置信度门槛：低于此值需触发问卷 */
    public static final BigDecimal CONFIDENCE_THRESHOLD = new BigDecimal("0.6");

    // ---- 有效值枚举（防止无效输入进入后续流程）----
    private static final List<String> VALID_MARKETS      = Arrays.asList("CN", "EU", "US", "ME");
    private static final List<String> VALID_PRICE_LEVELS = Arrays.asList("LOW", "MID", "HIGH");
    private static final List<String> VALID_CROWDS       =
            Arrays.asList("男装", "少女", "中老年", "运动");
    private static final List<String> VALID_PURPOSES     = Arrays.asList("WHOLESALE", "RETAIL");

    // ====================================================================
    // Agent.run
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        log.info("[QADecisionAgent] 执行五问决策树 customerId={} confidence={}",
                ctx.customerId, ctx.confidenceScore);

        // ---- Q1: 做什么品类？ ----
        if (!StringUtils.hasText(ctx.category)) {
            return pause(ctx, "category",
                    "你主要做什么品类？（上衣 / 外套 / 裤子 / 内裤 / 连衣裙）");
        }
        // keyword 与 category 同步（供 TrendAgent / DesignAgent 使用）
        if (!StringUtils.hasText(ctx.keyword)) {
            ctx.keyword = ctx.category;
        }

        // ---- Q2: 客户是谁？ ----
        if (!StringUtils.hasText(ctx.crowd) || !VALID_CROWDS.contains(ctx.crowd)) {
            return pause(ctx, "crowd",
                    "你的客户是谁？（男装 / 少女 / 中老年 / 运动）");
        }

        // ---- Q3: 什么风格？ ----
        if (!StringUtils.hasText(ctx.style)) {
            return pause(ctx, "style",
                    "什么风格？（工装 / 极简 / 性感 / 休闲 / 轻奢 / 运动）");
        }

        // ---- Q4: 卖哪里？ ----
        if (!StringUtils.hasText(ctx.market) || !VALID_MARKETS.contains(ctx.market.toUpperCase())) {
            return pause(ctx, "market",
                    "主要卖哪里的市场？（CN=国内 / EU=欧洲 / US=北美 / ME=中东）");
        }
        // 统一大写
        ctx.market = ctx.market.toUpperCase();

        // ---- Q5: 什么价位？ ----
        if (!StringUtils.hasText(ctx.priceLevel)
                || !VALID_PRICE_LEVELS.contains(ctx.priceLevel.toUpperCase())) {
            return pause(ctx, "priceLevel",
                    "大概什么价位？（LOW=低端 / MID=中端 / HIGH=高端）");
        }
        ctx.priceLevel = ctx.priceLevel.toUpperCase();

        // ---- Q6: 用途？（批发 vs 零售）----
        if (!StringUtils.hasText(ctx.purpose)
                || !VALID_PURPOSES.contains(ctx.purpose.toUpperCase())) {
            return pause(ctx, "purpose",
                    "你的用途是？（WHOLESALE=批发 / RETAIL=零售）");
        }
        ctx.purpose = ctx.purpose.toUpperCase();

        // ---- 六问全部填齐 ----
        ctx.pendingQuestion = null;
        ctx.pendingField    = null;
        if (ctx.confidenceScore == null || ctx.confidenceScore.compareTo(CONFIDENCE_THRESHOLD) < 0) {
            ctx.confidenceScore = CONFIDENCE_THRESHOLD;
        }

        log.info("[QADecisionAgent] ✅ 六问完成 category={} crowd={} style={} market={} priceLevel={} purpose={}",
                ctx.category, ctx.crowd, ctx.style, ctx.market, ctx.priceLevel, ctx.purpose);
        return ctx;
    }

    // ====================================================================
    // 静态工具
    // ====================================================================

    /**
     * 判断是否需要触发五问（SelectionFlowService / Orchestrator 调用）。
     * 任一核心字段缺失 或 置信度不足 → 需要问。
     */
    public static boolean needsQA(Context ctx) {
        return ctx.confidenceScore == null
                || ctx.confidenceScore.compareTo(CONFIDENCE_THRESHOLD) < 0
                || !StringUtils.hasText(ctx.category)
                || !StringUtils.hasText(ctx.crowd)
                || !StringUtils.hasText(ctx.style)
                || !StringUtils.hasText(ctx.market)
                || !StringUtils.hasText(ctx.priceLevel)
                || !StringUtils.hasText(ctx.purpose);
    }

    // ====================================================================
    // 内部工具
    // ====================================================================

    /**
     * 设置 pendingQuestion + pendingField 并立即返回（暂停流程）。
     */
    private Context pause(Context ctx, String field, String question) {
        ctx.pendingQuestion = question;
        ctx.pendingField    = field;
        log.info("[QADecisionAgent] ⏸ 等待回答 field={} question={}", field, question);
        return ctx;
    }

}
