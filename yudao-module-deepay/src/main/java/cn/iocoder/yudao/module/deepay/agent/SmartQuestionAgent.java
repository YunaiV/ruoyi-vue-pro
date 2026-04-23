package cn.iocoder.yudao.module.deepay.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * SmartQuestionAgent — 问答决策树（Phase 6 升级版）。
 *
 * <p><b>核心原则：一次只问一个问题，有数据就不问。</b></p>
 *
 * <p>决策树顺序（优先级从高到低）：
 * <ol>
 *   <li>category（做什么类目？）— 最重要，影响所有后续 Agent</li>
 *   <li>market（卖哪里？）</li>
 *   <li>style（什么风格？）</li>
 *   <li>priceLevel（什么价位？）</li>
 * </ol>
 * </p>
 *
 * <p>当某个字段缺失时：
 * <ul>
 *   <li>将问题文本写入 {@link Context#pendingQuestion}</li>
 *   <li><b>立即返回</b>（不继续检查后续字段）</li>
 *   <li>Orchestrator 检测到 {@code ctx.pendingQuestion != null} 则停止流程，把问题返回给调用方</li>
 *   <li>调用方在下次请求中把答案填入对应字段，再次调用 Orchestrator 即可继续</li>
 * </ul>
 * </p>
 *
 * <p>所有 4 个字段都填齐后，confidenceScore 升至 0.6（初始门槛），
 * 下次运行 {@link #needsQuestionnaire} 返回 false，跳过问卷。</p>
 *
 * <p>❌ 不对接前端 / 不阻塞线程 — pendingQuestion 是纯数据信号。</p>
 */
@Component
public class SmartQuestionAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(SmartQuestionAgent.class);

    /** 置信度门槛：低于此值需触发问卷 */
    public static final BigDecimal CONFIDENCE_THRESHOLD = new BigDecimal("0.6");

    private static final List<String> VALID_MARKETS      = Arrays.asList("CN", "EU", "US", "ME");
    private static final List<String> VALID_PRICE_LEVELS = Arrays.asList("LOW", "MID", "HIGH");
    private static final List<String> VALID_GENDERS      = Arrays.asList("MALE", "FEMALE", "UNISEX");
    private static final List<String> VALID_AGES         = Arrays.asList("YOUNG", "MIDDLE", "ELDER");

    // ====================================================================
    // Agent.run
    // ====================================================================

    @Override
    public Context run(Context ctx) {
        log.info("[SmartQuestionAgent] 执行决策树 userId={} confidence={}",
                ctx.userId, ctx.confidenceScore);

        // ---- Q1: 做什么类目？ ----
        if (!StringUtils.hasText(ctx.category)) {
            return pause(ctx, "category",
                    "你主要做什么类目？（上衣 / 外套 / 裤子 / 内裤 / 连衣裙）",
                    "你主要做什么类目？（上衣/外套/裤子/内衣）");
        }

        // keyword 与 category 同步
        if (!StringUtils.hasText(ctx.keyword)) {
            ctx.keyword = ctx.category;
        }

        // ---- Q2: 卖哪里？ ----
        if (!StringUtils.hasText(ctx.market) || !VALID_MARKETS.contains(ctx.market)) {
            return pause(ctx, "market",
                    "你主要卖哪里的市场？（CN=国内 / EU=欧洲 / US=北美 / ME=中东）",
                    "主要卖哪里市场？（欧美/中东/国内）");
        }

        // ---- Q3: 什么风格？ ----
        if (!StringUtils.hasText(ctx.style)) {
            return pause(ctx, "style",
                    "你偏什么风格？（SEXY=性感 / CASUAL=休闲 / SPORT=运动 / MINIMAL=极简 / LUXURY=轻奢）",
                    "偏什么风格？（性感/工装/极简/运动）");
        }

        // ---- Q4: 什么价位？ ----
        if (!StringUtils.hasText(ctx.priceLevel) || !VALID_PRICE_LEVELS.contains(ctx.priceLevel)) {
            return pause(ctx, "priceLevel",
                    "大概什么价位？（LOW=低端 / MID=中端 / HIGH=高端）",
                    "大概什么价位？（低/中/高）");
        }

        // ---- 全部填齐 ----
        // 补充可选维度默认值（不打断流程）
        if (!StringUtils.hasText(ctx.gender) || !VALID_GENDERS.contains(ctx.gender)) {
            ctx.gender = "UNISEX";
        }
        if (!StringUtils.hasText(ctx.targetAge) || !VALID_AGES.contains(ctx.targetAge)) {
            ctx.targetAge = "YOUNG";
        }

        // 置信度升至初始门槛
        if (ctx.confidenceScore == null || ctx.confidenceScore.compareTo(CONFIDENCE_THRESHOLD) < 0) {
            ctx.confidenceScore = CONFIDENCE_THRESHOLD;
        }

        log.info("[SmartQuestionAgent] 所有字段就绪 category={} market={} style={} priceLevel={}",
                ctx.category, ctx.market, ctx.style, ctx.priceLevel);
        return ctx;
    }

    // ====================================================================
    // 静态工具
    // ====================================================================

    /**
     * 判断是否需要触发问卷（Orchestrator 调用）。
     *
     * @return true = 需要触发 SmartQuestionAgent
     */
    public static boolean needsQuestionnaire(Context ctx) {
        return ctx.confidenceScore == null
                || ctx.confidenceScore.compareTo(CONFIDENCE_THRESHOLD) < 0
                || !StringUtils.hasText(ctx.category)
                || !StringUtils.hasText(ctx.market)
                || !StringUtils.hasText(ctx.style)
                || !StringUtils.hasText(ctx.priceLevel);
    }

    // ====================================================================
    // 内部工具
    // ====================================================================

    /**
     * 设置 pendingQuestion 并立即返回（暂停流程）。
     * 下次调用时该字段已由调用方填入，决策树继续向后推进。
     */
    private Context pause(Context ctx, String fieldName, String questionFull, String questionShort) {
        ctx.pendingQuestion = questionFull;
        log.info("[SmartQuestionAgent] ⏸ 等待回答 field={} question={}",
                fieldName, questionShort);
        return ctx;
    }

}
