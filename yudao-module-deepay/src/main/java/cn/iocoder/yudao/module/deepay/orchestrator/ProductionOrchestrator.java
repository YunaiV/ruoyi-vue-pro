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
    @Resource private CustomerProfileAgent    customerProfileAgent;
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
                customerProfileAgent.updateProfile(ctx.customerId, ctx);
                return ctx;
            }
            customerProfileAgent.updateProfile(ctx.customerId, ctx);
        } else if (SmartQuestionAgent.needsQuestionnaire(ctx)) {
            ctx = questionAgent.run(ctx);
            if (StringUtils.hasText(ctx.pendingQuestion)) {
                log.info("[Orchestrator] ⏸ 等待用户回答 q={}", ctx.pendingQuestion);
                customerProfileAgent.updateProfile(ctx.customerId, ctx);
                return ctx;
            }
            customerProfileAgent.updateProfile(ctx.customerId, ctx);
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
