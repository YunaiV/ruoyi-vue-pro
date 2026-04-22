package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * SelectionFlowService — 选款引导层主编排（Phase 6-7）。
 *
 * <h3>主流程（选款链）</h3>
 * <pre>
 * ctx = CustomerProfileAgent.run(ctx)       // 加载历史画像
 *
 * if (QADecisionAgent.needsQA(ctx)) {
 *     ctx = qaDecisionAgent.run(ctx)         // 五问：category/crowd/style/market/price
 *     if (pendingQuestion != null) return    // ⏸ 等待用户回答
 *     customerProfileAgent.save(ctx)         // 保存画像
 * }
 *
 * ctx = trendAgent.run(ctx)                  // 从 trend_pool 取趋势款
 * ctx.styleCombos = styleEngine.buildCombinations(ctx)  // 生成 10~20 个方向
 * ctx = selectionFeedAgent.run(ctx)          // 过滤 + 推图
 *
 * return ctx                                 // 含 selectionImages + styleCombos
 * </pre>
 *
 * <h3>用户选图后</h3>
 * <pre>
 * feedbackAgent.learn(ctx, imageUrl, isSelected)   // 记录 + 权重更新
 * customerProfileAgent.save(ctx)                   // 画像进化
 * </pre>
 *
 * <h3>验收</h3>
 * <ul>
 *   <li>✔ 第一次：系统自动问 5 个问题</li>
 *   <li>✔ 第二次：直接出图（画像已有，无需再问）</li>
 *   <li>✔ 做外套 → 永远只推外套，不推内裤</li>
 *   <li>✔ 选图后系统越来越准（selection_log + 权重进化）</li>
 * </ul>
 */
@Service
public class SelectionFlowService {

    private static final Logger log = LoggerFactory.getLogger(SelectionFlowService.class);

    @Resource private CustomerProfileAgent customerProfileAgent;
    @Resource private QADecisionAgent      qaDecisionAgent;
    @Resource private TrendAgent           trendAgent;
    @Resource private StyleEngine          styleEngine;
    @Resource private SelectionFeedAgent   selectionFeedAgent;
    @Resource private BrandFilterAgent     brandFilterAgent;
    @Resource private FeedbackAgent        feedbackAgent;

    // ====================================================================
    // 主入口：开始 / 继续选款流程
    // ====================================================================

    /**
     * 开始或继续选款流程（幂等）。
     *
     * <p>第一次调用：返回第一个 pendingQuestion，等待回答。<br>
     * 之后每次把答案填入对应字段，继续调用此方法，直到 pendingQuestion=null，
     * 返回 selectionImages + styleCombos。</p>
     *
     * @param ctx 至少包含 customerId（其余字段可逐步填入）
     * @return 含 pendingQuestion 或 selectionImages 的 Context
     */
    public Context startOrContinue(Context ctx) {
        log.info("[SelectionFlowService] START customerId={} category={} crowd={}",
                ctx.customerId, ctx.category, ctx.crowd);

        // 1. 加载历史画像（有则直接跳过问答）
        ctx = customerProfileAgent.run(ctx);

        // 2. 五问引导（缺字段 → 暂停返回）
        if (QADecisionAgent.needsQA(ctx)) {
            ctx = qaDecisionAgent.run(ctx);
            if (StringUtils.hasText(ctx.pendingQuestion)) {
                log.info("[SelectionFlowService] ⏸ 等待回答 field={}", ctx.pendingField);
                return ctx;
            }
            // 五问全部完成 → 持久化画像
            customerProfileAgent.updateProfile(ctx.customerId, ctx);
        }

        // 3. 趋势款池（按 category + crowd + style 精准过滤）
        ctx = trendAgent.run(ctx);

        // 4. 风格组合方向（10~20 个）
        ctx.styleCombos = styleEngine.buildCombinations(ctx);

        // 5. 参考图推送（给设计师 / 客户选）
        ctx = selectionFeedAgent.run(ctx);

        log.info("[SelectionFlowService] DONE selectionImages={} styleCombos={}",
                ctx.selectionImages != null ? ctx.selectionImages.size() : 0,
                ctx.styleCombos    != null ? ctx.styleCombos.size()    : 0);
        return ctx;
    }

    // ====================================================================
    // 用户选图反馈（触发学习 + 画像进化）
    // ====================================================================

    /**
     * 用户对参考图执行"选中"或"跳过"后调用。
     *
     * @param ctx        含 customerId / category / style / crowd 的 Context
     * @param imageUrl   用户操作的图片 URL
     * @param isSelected true=选中 / false=跳过
     */
    public void submitFeedback(Context ctx, String imageUrl, boolean isSelected) {
        log.info("[SelectionFlowService] submitFeedback customerId={} isSelected={} image={}",
                ctx.customerId, isSelected, shorten(imageUrl));

        // 1. 记录选款行为 + 趋势库权重更新 + 画像风格进化
        feedbackAgent.learn(ctx, imageUrl, isSelected);

        // 2. 持久化更新后的画像
        if (ctx.customerId != null) {
            customerProfileAgent.updateProfile(ctx.customerId, ctx);
        }
    }

    // ====================================================================
    // 工具
    // ====================================================================

    private static String shorten(String s) {
        if (s == null) return "null";
        return s.length() > 60 ? s.substring(0, 60) + "..." : s;
    }

}
