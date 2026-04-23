package cn.iocoder.yudao.module.deepay.orchestrator;

import cn.iocoder.yudao.module.deepay.agent.*;
import cn.iocoder.yudao.module.deepay.scheduler.DeepayRetryScheduler;
import cn.iocoder.yudao.module.deepay.service.CdnService;
import cn.iocoder.yudao.module.deepay.service.ContextSnapshotService;
import cn.iocoder.yudao.module.deepay.service.DeepayAuditService;
import cn.iocoder.yudao.module.deepay.service.StyleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * ProductionOrchestrator — 全链路唯一调度器（Phase 6 最终版）。
 *
 * <pre>
 * 标准执行顺序（Phase 6）：
 *
 *   1.  chainAgent            → chainCode
 *
 *   2.  customerProfileAgent  → 加载用户画像 (category/style/market/styleWeights)
 *   3.  questionAgent         → 缺字段则设 pendingQuestion 并中断 ⏸
 *
 *   4.  trendAgent            → referenceImages（WHERE p.category=? 强品类过滤）
 *
 *   5.  StyleEngine.build     → ctx.stylePrompt（风格组合 Prompt）
 *   6.  designAgent           → designImages（个性化出图）
 *   7.  judgeAgent            → imageScores（历史销量加权打分）
 *   8.  aiDecisionAgent       → selectedImage / shouldProduce / action
 *       selectionAgent        → 记录用户选择 deepay_user_selection（越用越准）
 *       [snapshot]
 *       if needRedesign → 重跑 5~8
 *       if !shouldProduce → 终止
 *
 *   9.  patternAgent          → patternFile / techPackUrl
 *   10. productAgent          → title / description
 *   11. pricingAgent          → price
 *   12. publishAgent          → productUrl
 *
 *   13. financeAgent          → orderId / paymentId
 *   14. inventoryAgent        → stock / lockedStock
 *   15. analyticsAgent        → profit / roi
 *       [snapshot]
 *
 *   16. Phase5: demandAgent / productionPlanner / clientAgent（B2B供应链，可选）
 *
 * 验收：
 *   ✔ 不会问错行业
 *   ✔ 不会出错类目（做内裤不出外套）
 *   ✔ 能出图
 *   ✔ 用户选过之后下次更准
 * </pre>
 */
@Service
public class ProductionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ProductionOrchestrator.class);

    // ---- Phase 6 — 记忆 + 问答 + 选择 ----
    /** 用户画像加载（对应 spec 的 customerProfileAgent） */
    @Resource private MemoryAgent             customerProfileAgent;
    /** 问答决策树（对应 spec 的 questionAgent） */
    @Resource private SmartQuestionAgent      questionAgent;
    /** 用户选图记录（越用越准） */
    @Resource private SelectionAgent          selectionAgent;
    /** 二次品类防错过滤 */
    @Resource private CategoryFilterAgent     categoryFilterAgent;

    // ---- 核心 Agent ----
    @Resource private ChainAgent              chainAgent;
    @Resource private TrendAgent              trendAgent;
    @Resource private DesignAgent             designAgent;
    @Resource private JudgeAgent              judgeAgent;
    @Resource private AIDecisionAgent         aiDecisionAgent;
    @Resource private PatternAgent            patternAgent;
    @Resource private ProductAgent            productAgent;
    @Resource private PricingAgent            pricingAgent;
    @Resource private PublishAgent            publishAgent;
    @Resource private FinanceAgent            financeAgent;
    @Resource private InventoryAgent          inventoryAgent;
    @Resource private AnalyticsAgent          analyticsAgent;

    // ---- Phase 5 B2B 供应链（可选）----
    @Resource private DemandAgent             demandAgent;
    @Resource private ClientAgent             clientAgent;
    @Resource private ProductionPlanner       productionPlanner;

    // ---- Phase 8 设计深化流水线 ----
    @Resource private StyleConsistencyAgent   styleConsistencyAgent;
    @Resource private DesignConfirmAgent      designConfirmAgent;
    @Resource private DesignSplitAgent        designSplitAgent;
    @Resource private RiskControlAgent        riskControlAgent;
    @Resource private DesignGenAgent          designGenAgent;
    @Resource private DesignVariantAgent      designVariantAgent;
    @Resource private PatternPrepareAgent     patternPrepareAgent;
    @Resource private CostEstimateAgent       costEstimateAgent;
    @Resource private ImageScoringAgent       imageScoringAgent;
    @Resource private FeedbackAgent           feedbackAgent;
    @Resource private SelectionFeedAgent      selectionFeedAgent;

    // ---- 基础服务 ----
    @Resource private ContextSnapshotService  snapshotService;
    @Resource private DeepayAuditService      auditService;
    @Resource private CdnService              cdnService;
    @Resource private DeepayRetryScheduler    retryScheduler;
    @Resource private StyleEngine             styleEngine;

    // ====================================================================
    // 主入口
    // ====================================================================

    public Context run(Context ctx) {
        log.info("=== ProductionOrchestrator START keyword={} userId={} category={} ===",
                ctx.keyword, ctx.userId, ctx.category);

        // ---- 1. 链路初始化 ----
        ctx = chainAgent.run(ctx);

        // ---- 2. 加载用户画像 ----
        ctx = customerProfileAgent.run(ctx);

        // ---- 3. 问答决策树（缺字段 → 中断，等待用户回答）⏸ ----
        if (SmartQuestionAgent.needsQuestionnaire(ctx)) {
            ctx = questionAgent.run(ctx);
            if (StringUtils.hasText(ctx.pendingQuestion)) {
                log.info("[Orchestrator] ⏸ 等待用户回答 pendingQuestion={}", ctx.pendingQuestion);
                // 保存已填充的部分答案，下次跳过已回答字段
                customerProfileAgent.save(ctx);
                return ctx;
            }
            // 全部字段填齐，回写画像
            customerProfileAgent.save(ctx);
        }

        // ---- 4. 趋势参考图（强品类过滤 WHERE p.category=?）----
        ctx = selectionFeedAgent.run(ctx);
        ctx = trendAgent.run(ctx);
        ctx = syncReferenceImagesToCdn(ctx);

        // ---- 5. StyleEngine 组装 Prompt ----
        ctx.stylePrompt = styleEngine.buildFullPrompt(ctx);

        // ---- 6~8. 设计 → 评分 → 决策（含重设计门控）----
        ctx = runDesignLoop(ctx);
        if (StringUtils.hasText(ctx.pendingQuestion)) {
            // 设计循环内部异常已登记重试，直接返回
            return ctx;
        }

        // 记录用户选择（越用越准）
        selectionAgent.run(ctx);
        snapshotService.save(ctx, "AIDecisionAgent");
        auditService.log(ctx.chainCode, "CREATE",
                "category=" + ctx.category + " style=" + ctx.style,
                "action=" + ctx.action + " shouldProduce=" + ctx.shouldProduce);

        // 门控：shouldProduce=false → 流程终止
        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.info("[门控] shouldProduce=false reason={}", ctx.decisionReason);
            return ctx;
        }

        // ---- Phase 8: 设计深化流水线 ----
        ctx = designConfirmAgent.run(ctx);
        if (StringUtils.hasText(ctx.pendingQuestion)) {
            log.info("[Orchestrator] ⏸ Phase8 等待品类确认 pendingQuestion={}", ctx.pendingQuestion);
            return ctx;
        }
        ctx = styleConsistencyAgent.run(ctx);
        ctx = designSplitAgent.run(ctx);
        ctx = designGenAgent.run(ctx);
        ctx = imageScoringAgent.run(ctx);
        ctx = feedbackAgent.run(ctx);
        ctx = designVariantAgent.run(ctx);
        ctx = riskControlAgent.run(ctx);
        ctx = patternPrepareAgent.run(ctx);
        ctx = costEstimateAgent.run(ctx);
        snapshotService.save(ctx, "Phase8[Design]");

        // ---- 9. 打版 ----
        try {
            ctx = patternAgent.run(ctx);
        } catch (Exception e) {
            log.error("[Orchestrator] PatternAgent 失败 chainCode={}", ctx.chainCode, e);
            retryScheduler.register(ctx.chainCode, "PATTERN", e.getMessage());
            return ctx;
        }

        // ---- 10~12. 商品 → 定价 → 上架 ----
        ctx = productAgent.run(ctx);
        // CostEstimateAgent may have set costPrice and price; PricingAgent can refine
        ctx = pricingAgent.run(ctx);
        snapshotService.save(ctx, "PricingAgent");
        auditService.log(ctx.chainCode, "REPRICE",
                "cost=" + ctx.costPrice, "price=" + ctx.price);

        ctx = publishAgent.run(ctx);
        auditService.log(ctx.chainCode, "PUBLISH",
                "status=DRAFT", "status=SELLING url=" + ctx.productUrl);

        // ---- 13~15. 支付 → 库存 → 分析 ----
        ctx = financeAgent.run(ctx);
        ctx = inventoryAgent.run(ctx);
        ctx = analyticsAgent.run(ctx);
        snapshotService.save(ctx, "AnalyticsAgent");

        // ---- 16. Phase 5 B2B 供应链（可选）----
        ctx = demandAgent.run(ctx);
        ctx = productionPlanner.run(ctx);
        if (ctx.clientId != null) {
            ctx = clientAgent.run(ctx);
        }
        snapshotService.save(ctx, "Phase5[B2B]");

        log.info("=== ProductionOrchestrator DONE chainCode={} url={} price={} roi={} " +
                        "category={} style={} ===",
                ctx.chainCode, ctx.productUrl, ctx.price, ctx.roi,
                ctx.category, ctx.style);
        return ctx;
    }

    // ====================================================================
    // 设计循环（含重设计门控）
    // ====================================================================

    private Context runDesignLoop(Context ctx) {
        ctx = executeDesignIteration(ctx);
        if (ctx == null) return new Context();  // should not happen

        // needRedesign → 重跑一次
        if (Boolean.TRUE.equals(ctx.needRedesign)) {
            log.info("[门控] needRedesign=true，重新执行设计循环 chainCode={}", ctx.chainCode);
            ctx = executeDesignIteration(ctx);
        }
        return ctx;
    }

    private Context executeDesignIteration(Context ctx) {
        try {
            ctx = designAgent.run(ctx);
            ctx = syncDesignImagesToCdn(ctx);
            ctx = categoryFilterAgent.run(ctx);   // 二次品类防错
        } catch (Exception e) {
            log.error("[Orchestrator] DesignAgent 失败，登记重试 chainCode={}", ctx.chainCode, e);
            retryScheduler.register(ctx.chainCode, "AI_DESIGN", e.getMessage());
            ctx.pendingQuestion = "_DESIGN_FAILED_";  // 信号位，让外层感知异常
            return ctx;
        }
        ctx = judgeAgent.run(ctx);
        ctx = aiDecisionAgent.run(ctx);
        return ctx;
    }

    // ====================================================================
    // CDN helpers
    // ====================================================================

    private Context syncReferenceImagesToCdn(Context ctx) {
        if (ctx.referenceImages != null && !ctx.referenceImages.isEmpty()) {
            ctx.referenceImages = cdnService.syncAllToCdn(ctx.referenceImages);
        }
        return ctx;
    }

    private Context syncDesignImagesToCdn(Context ctx) {
        if (ctx.designImages != null && !ctx.designImages.isEmpty()) {
            ctx.designImages = cdnService.syncAllToCdn(ctx.designImages);
        }
        if (ctx.selectedImage != null) {
            ctx.selectedImage = cdnService.syncToCdn(ctx.selectedImage);
        }
        return ctx;
    }

}
