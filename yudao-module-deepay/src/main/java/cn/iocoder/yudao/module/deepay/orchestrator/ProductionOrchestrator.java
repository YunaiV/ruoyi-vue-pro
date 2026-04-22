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
 * ProductionOrchestrator — 全链路调度器（Phase 10 最终版）。
 *
 * <pre>
 * Phase 6   画像 + 问答（QADecision / SmartQuestion）
 * Phase 7   趋势（TrendAgent → referenceImages）
 * Phase 8   选款（StyleConsistency → DesignConfirm → DesignSplit → RiskControl）
 * Phase 9   生成（DesignGen → DesignVariant → PatternPrepare → CostEstimate）
 * Phase 10  商品化（ProductFinalize → PricingStrategy → PublishChannel → OrderFlow）
 *           → 等待支付回调 → PaymentCallbackService（原子事务）
 *           → 数据回流（AIDecision + Analytics）
 * </pre>
 */
@Service
public class ProductionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ProductionOrchestrator.class);

    // ---- Phase 6 — 记忆 + 问答 + 选择 ----
    @Resource private MemoryAgent             customerProfileAgent;
    @Resource private SmartQuestionAgent      questionAgent;
    @Resource private QADecisionAgent         qaDecisionAgent;
    @Resource private SelectionAgent          selectionAgent;
    @Resource private CategoryFilterAgent     categoryFilterAgent;
    @Resource private DesignFilterAgent       designFilterAgent;
    @Resource private DesignSelectAgent       designSelectAgent;

    // ---- Phase 8 选款 ----
    @Resource private StyleConsistencyAgent   styleConsistencyAgent;
    @Resource private DesignConfirmAgent      designConfirmAgent;
    @Resource private DesignSplitAgent        designSplitAgent;
    @Resource private RiskControlAgent        riskControlAgent;

    // ---- Phase 9 生成 ----
    @Resource private DesignGenAgent          designGenAgent;
    @Resource private DesignVariantAgent      designVariantAgent;
    @Resource private PatternPrepareAgent     patternPrepareAgent;
    @Resource private CostEstimateAgent       costEstimateAgent;

    // ---- Phase 10 商品化 ----
    @Resource private ProductFinalizeAgent    productFinalizeAgent;
    @Resource private PricingStrategyAgent    pricingStrategyAgent;
    @Resource private PublishChannelAgent     publishChannelAgent;
    @Resource private OrderFlowAgent          orderFlowAgent;

    // ---- 核心 Agent（原有）----
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

    // ---- Phase 5 B2B 供应链 ----
    @Resource private DemandAgent             demandAgent;
    @Resource private ClientAgent             clientAgent;
    @Resource private ProductionPlanner       productionPlanner;

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

        // ──────────────────────────────────────────────────────────────
        // Phase 6: 链路初始化 + 画像 + 问答
        // ──────────────────────────────────────────────────────────────
        ctx = chainAgent.run(ctx);
        ctx = customerProfileAgent.run(ctx);

        boolean useNewQA = ctx.customerId != null && QADecisionAgent.needsQA(ctx);
        if (useNewQA) {
            ctx = qaDecisionAgent.run(ctx);
            if (StringUtils.hasText(ctx.pendingQuestion)) {
                log.info("[Orchestrator] ⏸ 等待用户回答(QA) q={}", ctx.pendingQuestion);
                customerProfileAgent.save(ctx);
                return ctx;
            }
            customerProfileAgent.save(ctx);
        } else if (SmartQuestionAgent.needsQuestionnaire(ctx)) {
            ctx = questionAgent.run(ctx);
            if (StringUtils.hasText(ctx.pendingQuestion)) {
                log.info("[Orchestrator] ⏸ 等待用户回答 q={}", ctx.pendingQuestion);
                customerProfileAgent.save(ctx);
                return ctx;
            }
            customerProfileAgent.save(ctx);
        }

        // ──────────────────────────────────────────────────────────────
        // Phase 7: 趋势参考图
        // ──────────────────────────────────────────────────────────────
        ctx = trendAgent.run(ctx);
        ctx = syncReferenceImagesToCdn(ctx);
        ctx.stylePrompt = styleEngine.buildFullPrompt(ctx);

        // ──────────────────────────────────────────────────────────────
        // 设计循环（Phase 6-7 选款 + Phase 8 风格锁定）
        // ──────────────────────────────────────────────────────────────
        ctx = runDesignLoop(ctx);
        if (StringUtils.hasText(ctx.pendingQuestion)) {
            return ctx;
        }

        selectionAgent.run(ctx);
        snapshotService.save(ctx, "AIDecisionAgent");
        auditService.log(ctx.chainCode, "CREATE",
                "category=" + ctx.category + " style=" + ctx.style,
                "action=" + ctx.action + " shouldProduce=" + ctx.shouldProduce);

        // ──────────────────────────────────────────────────────────────
        // Phase 8: 风格校验 → 确认 → 设计拆解 → 风控
        // ──────────────────────────────────────────────────────────────
        ctx = styleConsistencyAgent.run(ctx);
        ctx = designConfirmAgent.run(ctx);

        // 未确认（需人工/重设计）→ 终止，等待下次调用
        if (!Boolean.TRUE.equals(ctx.designConfirmed)) {
            log.info("[Orchestrator] ⏸ designConfirmed=false reason={}", ctx.decisionReason);
            snapshotService.save(ctx, "DesignConfirmAgent");
            return ctx;
        }

        ctx = designSplitAgent.run(ctx);
        ctx = riskControlAgent.run(ctx);

        if (!Boolean.TRUE.equals(ctx.shouldProduce)) {
            log.info("[Orchestrator] 🚫 风控拦截 reason={}", ctx.decisionReason);
            return ctx;
        }

        // ──────────────────────────────────────────────────────────────
        // Phase 9: 原创生成 → 多变体 → 打版准备 → 成本估算
        // ──────────────────────────────────────────────────────────────
        ctx = designGenAgent.run(ctx);
        ctx = syncDesignImagesToCdn(ctx);
        ctx = designVariantAgent.run(ctx);
        snapshotService.save(ctx, "DesignVariantAgent");

        ctx = patternPrepareAgent.run(ctx);
        ctx = costEstimateAgent.run(ctx);

        if (Boolean.TRUE.equals(ctx.costTooHigh)) {
            log.info("[Orchestrator] 🚫 成本过高 reason={}", ctx.decisionReason);
            return ctx;
        }

        // 原有打版（tech pack 生成）
        try {
            ctx = patternAgent.run(ctx);
        } catch (Exception e) {
            log.error("[Orchestrator] PatternAgent 失败 chainCode={}", ctx.chainCode, e);
            retryScheduler.register(ctx.chainCode, "PATTERN", e.getMessage());
            return ctx;
        }

        // ──────────────────────────────────────────────────────────────
        // Phase 10: 商品化 → 定价 → 上架 → 订单
        // ──────────────────────────────────────────────────────────────

        // 先用 ProductAgent 创建基础商品记录（ctx.productId）
        ctx = productAgent.run(ctx);

        // Phase 10: 定稿标题+描述（去AI味）
        ctx = productFinalizeAgent.run(ctx);
        snapshotService.save(ctx, "ProductFinalizeAgent");

        // Phase 10: 动态定价（cost×2.2 + trendBoost + marketAdjust）
        ctx = pricingStrategyAgent.run(ctx);
        auditService.log(ctx.chainCode, "REPRICE",
                "cost=" + ctx.totalCost,
                "price=" + ctx.price + " trendBoost=" + ctx.trendBoost + " mktAdj=" + ctx.marketAdjust);

        // 同时也跑旧版 PricingAgent（维护 wholesalePrice 阶梯价）
        ctx = pricingAgent.run(ctx);

        // Phase 10: 多渠道发布（H5 必须 + 1688/Shopify 配置开关）
        ctx = publishChannelAgent.run(ctx);
        // 原 PublishAgent 更新 deepay_style_chain.status → PUBLISHED
        ctx = publishAgent.run(ctx);
        auditService.log(ctx.chainCode, "PUBLISH",
                "status=DRAFT",
                "status=SELLING channels=" + ctx.channels + " url=" + ctx.productUrl);

        // Phase 10: 创建订单（通过 PaymentPluginManager → JeepayPlugin）
        ctx = orderFlowAgent.run(ctx);
        // 向后兼容：原 FinanceAgent 中的 IBAN 逻辑由 orderFlowAgent 覆盖
        ctx = inventoryAgent.run(ctx);

        ctx = analyticsAgent.run(ctx);
        snapshotService.save(ctx, "AnalyticsAgent");

        // ──────────────────────────────────────────────────────────────
        // Phase 5 B2B 供应链（可选）
        // ──────────────────────────────────────────────────────────────
        ctx = demandAgent.run(ctx);
        ctx = productionPlanner.run(ctx);
        if (ctx.clientId != null) {
            ctx = clientAgent.run(ctx);
        }
        snapshotService.save(ctx, "Phase5[B2B]");

        log.info("=== ProductionOrchestrator DONE chainCode={} url={} price={} roi={} " +
                        "channels={} paymentId={} ===",
                ctx.chainCode, ctx.productUrl, ctx.price, ctx.roi,
                ctx.channels, ctx.paymentId);
        return ctx;
    }

    // ====================================================================
    // 设计循环（含重设计门控）
    // ====================================================================

    private Context runDesignLoop(Context ctx) {
        ctx = executeDesignIteration(ctx);
        if (ctx == null) return new Context();

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
            ctx = designFilterAgent.run(ctx);
            ctx = categoryFilterAgent.run(ctx);
            ctx = designSelectAgent.run(ctx);
            if (StringUtils.hasText(ctx.pendingQuestion)
                    && "selectedImage".equals(ctx.pendingField)) {
                return ctx;
            }
        } catch (Exception e) {
            log.error("[Orchestrator] DesignAgent 失败，登记重试 chainCode={}", ctx.chainCode, e);
            retryScheduler.register(ctx.chainCode, "AI_DESIGN", e.getMessage());
            ctx.pendingQuestion = "_DESIGN_FAILED_";
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
    /** 旧版问答决策树（向后兼容） */
    @Resource private SmartQuestionAgent      questionAgent;
    /** Phase 6-7 五问引导（新版，category + crowd + style + market + priceLevel） */
    @Resource private QADecisionAgent         qaDecisionAgent;
    /** 用户选图记录（越用越准） */
    @Resource private SelectionAgent          selectionAgent;
    /** 二次品类防错过滤 */
    @Resource private CategoryFilterAgent     categoryFilterAgent;
    /** Phase 8：AI 生成图质量过滤（清晰度 + 品类一致性） */
    @Resource private DesignFilterAgent       designFilterAgent;
    /** Phase 8：设计图选择（HUMAN=人工 / AI=自动）*/
    @Resource private DesignSelectAgent       designSelectAgent;

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
        boolean useNewQA = ctx.customerId != null && QADecisionAgent.needsQA(ctx);
        if (useNewQA) {
            // Phase 6-7：五问引导（customerId 存在时优先）
            ctx = qaDecisionAgent.run(ctx);
            if (StringUtils.hasText(ctx.pendingQuestion)) {
                log.info("[Orchestrator] ⏸ 等待用户回答(QA) pendingQuestion={}", ctx.pendingQuestion);
                customerProfileAgent.save(ctx);
                return ctx;
            }
            customerProfileAgent.save(ctx);
        } else if (SmartQuestionAgent.needsQuestionnaire(ctx)) {
            // 向后兼容旧版问卷（无 customerId 时）
            ctx = questionAgent.run(ctx);
            if (StringUtils.hasText(ctx.pendingQuestion)) {
                log.info("[Orchestrator] ⏸ 等待用户回答 pendingQuestion={}", ctx.pendingQuestion);
                customerProfileAgent.save(ctx);
                return ctx;
            }
            customerProfileAgent.save(ctx);
        }

        // ---- 4. 趋势参考图（强品类过滤 WHERE p.category=?）----
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
            ctx = designAgent.run(ctx);                // Phase 8 Step 1：生成 3~6 张图
            ctx = syncDesignImagesToCdn(ctx);
            ctx = designFilterAgent.run(ctx);          // Phase 8 Step 2：过滤垃圾图
            ctx = categoryFilterAgent.run(ctx);        // 二次品类防错
            ctx = designSelectAgent.run(ctx);          // Phase 8 Step 3：选图（AI 默认/HUMAN 可选）
            // 若人工选图模式，pendingQuestion 已设置，外层感知后返回
            if (StringUtils.hasText(ctx.pendingQuestion)
                    && "selectedImage".equals(ctx.pendingField)) {
                return ctx;
            }
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
