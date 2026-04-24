package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.agent.QADecisionAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * AI 对话核心服务 — 将自然语言消息路由到对应板块，
 * 运行 Agent 流程，返回下一条 AI 回复。
 *
 * <h3>支持的板块（module）</h3>
 * <ul>
 *   <li>{@code selection} — 选款引导（QADecisionAgent + SelectionFlowService）</li>
 *   <li>{@code design}    — 设计出图（DesignAgent + ImageScoringAgent）</li>
 *   <li>{@code product}   — 商品管理（TrendAgent + PricingStrategyAgent）</li>
 *   <li>{@code inventory} — 库存查询 / 补货建议（DemandAgent）</li>
 *   <li>{@code finance}   — 财务分析（FinanceAgent）</li>
 *   <li>{@code trend}     — 趋势分析（TrendAgent）</li>
 *   <li>{@code order}     — 订单查询（OrderFlowAgent）</li>
 * </ul>
 *
 * <h3>返回结构（{@link ChatReply}）</h3>
 * <ul>
 *   <li>{@code aiMessage}    — 下一条 AI 回复文本</li>
 *   <li>{@code pendingField} — 还需要回答的字段名</li>
 *   <li>{@code quickReplies} — 候选快速回复按钮</li>
 *   <li>{@code images}       — 图片列表（有图时）</li>
 *   <li>{@code done}         — 本轮是否完成</li>
 *   <li>{@code sessionId}    — 会话 ID（可继续对话）</li>
 * </ul>
 */
@Slf4j
@Service
public class AiChatService {

    // ---- 快速回复候选值 ---------------------------------------------------
    private static final Map<String, List<String>> QUICK_REPLY_MAP = new LinkedHashMap<>();
    static {
        QUICK_REPLY_MAP.put("category",   Arrays.asList("外套", "裤子", "上衣", "连衣裙", "内裤", "T恤"));
        QUICK_REPLY_MAP.put("crowd",      Arrays.asList("男装", "少女", "中老年", "运动"));
        QUICK_REPLY_MAP.put("style",      Arrays.asList("工装", "极简", "性感", "休闲", "轻奢", "运动"));
        QUICK_REPLY_MAP.put("market",     Arrays.asList("国内(CN)", "欧洲(EU)", "北美(US)", "中东(ME)"));
        QUICK_REPLY_MAP.put("priceLevel", Arrays.asList("低端(LOW)", "中端(MID)", "高端(HIGH)"));
        QUICK_REPLY_MAP.put("purpose",    Arrays.asList("批发(WHOLESALE)", "零售(RETAIL)"));
    }

    // ---- 自动补全候选词 ---------------------------------------------------
    private static final List<String> AUTOCOMPLETE_HINTS = Arrays.asList(
            "外套", "裤子", "上衣", "连衣裙", "内裤", "T恤",
            "工装风格", "极简风格", "性感风格", "休闲风格", "轻奢风格",
            "男装", "少女", "中老年", "运动",
            "欧美市场", "国内市场", "中东市场",
            "低端价位", "中端价位", "高端价位",
            "批发", "零售",
            "我想做", "帮我推荐", "最近流行", "库存状态", "趋势分析"
    );

    @Resource private ChatSessionService    chatSessionService;
    @Resource private NlpParserService      nlpParserService;
    @Resource private SelectionFlowService  selectionFlowService;

    // ====================================================================
    // 主入口：处理一条对话消息
    // ====================================================================

    /**
     * 处理用户消息，返回 AI 回复。
     *
     * @param module      板块名（selection/design/product/inventory/finance/trend/order）
     * @param sessionId   会话 ID（首次传 null，系统自动创建）
     * @param customerId  用户/客户 ID
     * @param userMessage 用户输入的自然语言
     * @return AI 回复结构
     */
    public ChatReply chat(String module, String sessionId, Long customerId, String userMessage) {
        log.info("[AiChatService] module={} sessionId={} customerId={} message='{}'",
                module, sessionId, customerId, userMessage);

        // 1. 加载或创建 Context
        boolean isNewSession = !StringUtils.hasText(sessionId);
        if (isNewSession) {
            sessionId = chatSessionService.newSessionId();
        }
        Context ctx = chatSessionService.load(sessionId);
        if (ctx == null) {
            ctx = new Context();
        }
        if (customerId != null) {
            ctx.customerId = customerId;
        }

        // 2. NLP 解析：将自然语言映射到结构化字段
        nlpParserService.parse(userMessage, ctx);

        // 3. 根据 module 路由执行
        ChatReply reply;
        switch (module == null ? "selection" : module.toLowerCase()) {
            case "selection":
                reply = handleSelection(ctx, userMessage);
                break;
            case "design":
                reply = handleDesign(ctx, userMessage);
                break;
            case "trend":
                reply = handleTrend(ctx, userMessage);
                break;
            case "inventory":
                reply = handleInventory(ctx, userMessage);
                break;
            case "finance":
                reply = handleFinance(ctx, userMessage);
                break;
            case "product":
                reply = handleProduct(ctx, userMessage);
                break;
            case "order":
                reply = handleOrder(ctx, userMessage);
                break;
            default:
                reply = handleSelection(ctx, userMessage);
        }

        // 4. 保存 Context（覆盖写）
        chatSessionService.save(sessionId, ctx);
        reply.setSessionId(sessionId);
        return reply;
    }

    // ====================================================================
    // 自动补全
    // ====================================================================

    /**
     * 根据用户已输入的前缀返回候选补全词。
     *
     * @param prefix 用户已输入的前缀（可为空，返回全部热门词）
     * @return 最多 8 个候选词
     */
    public List<String> autocomplete(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return AUTOCOMPLETE_HINTS.subList(0, Math.min(8, AUTOCOMPLETE_HINTS.size()));
        }
        List<String> matched = new ArrayList<>();
        for (String hint : AUTOCOMPLETE_HINTS) {
            if (hint.contains(prefix)) {
                matched.add(hint);
                if (matched.size() >= 8) break;
            }
        }
        return matched;
    }

    // ====================================================================
    // 板块处理器
    // ====================================================================

    private ChatReply handleSelection(Context ctx, String userMessage) {
        ctx = selectionFlowService.startOrContinue(ctx);

        if (StringUtils.hasText(ctx.pendingQuestion)) {
            return ChatReply.question(
                    ctx.pendingQuestion,
                    ctx.pendingField,
                    QUICK_REPLY_MAP.get(ctx.pendingField),
                    null
            );
        }

        // 所有问答完成，返回图片
        String msg = buildSelectionDoneMessage(ctx);
        List<String> images = ctx.selectionImages != null ? ctx.selectionImages :
                (ctx.trendImages != null ? ctx.trendImages : Collections.emptyList());
        return ChatReply.done(msg, images);
    }

    private ChatReply handleDesign(Context ctx, String userMessage) {
        // 若 category 缺失先问
        if (!StringUtils.hasText(ctx.category)) {
            return ChatReply.question(
                    "你想设计什么品类？（外套 / 连衣裙 / 裤子 / 上衣 / 内裤）",
                    "category",
                    QUICK_REPLY_MAP.get("category"),
                    null
            );
        }
        // 有 category → 给出设计建议（图片需要真实 FluxService，此处返回引导信息）
        String msg = String.format("好的！正在为你生成「%s」设计方向，风格：%s，市场：%s。\n" +
                "AI 设计图生成通常需要 10~30 秒，请稍候…",
                ctx.category,
                StringUtils.hasText(ctx.style) ? ctx.style : "极简（默认）",
                StringUtils.hasText(ctx.market) ? ctx.market : "EU（默认）");
        return ChatReply.info(msg, null);
    }

    private ChatReply handleTrend(Context ctx, String userMessage) {
        String cat = StringUtils.hasText(ctx.category) ? ctx.category : "全品类";
        List<String> images = ctx.trendImages != null ? ctx.trendImages : Collections.emptyList();
        String msg = String.format("为你找到「%s」最新趋势款 %d 张：", cat, images.size());
        if (images.isEmpty()) {
            msg = "暂时没有找到对应趋势款，建议换个品类或风格试试。";
        }
        return ChatReply.done(msg, images);
    }

    private ChatReply handleInventory(Context ctx, String userMessage) {
        String msg;
        if (StringUtils.hasText(ctx.keyword)) {
            int demand = ctx.predictedDemand != null ? ctx.predictedDemand : 0;
            int qty    = ctx.suggestedProductionQty != null ? ctx.suggestedProductionQty : 0;
            msg = String.format("「%s」未来 7 天预测销量：%d 件，建议备货：%d 件。\n" +
                    "如需手动补货，请前往库存管理页面操作。", ctx.keyword, demand, qty);
        } else {
            msg = "请告诉我你想查询哪个品类的库存？（如：外套库存还有多少？）";
        }
        return ChatReply.info(msg, null);
    }

    private ChatReply handleFinance(Context ctx, String userMessage) {
        String msg;
        if (ctx.roi != null) {
            msg = String.format("当前链码 ROI：%.1f%%，利润：%s 元，成本：%s 元。",
                    ctx.roi.doubleValue() * 100,
                    ctx.profit != null ? ctx.profit.toPlainString() : "—",
                    ctx.costPrice != null ? ctx.costPrice.toPlainString() : "—");
        } else {
            msg = "请告诉我你想查询哪个商品的财务数据？（如：这款外套的 ROI 是多少？）";
        }
        return ChatReply.info(msg, null);
    }

    private ChatReply handleProduct(Context ctx, String userMessage) {
        if (!StringUtils.hasText(ctx.category)) {
            return ChatReply.question(
                    "你想管理哪个品类的商品？",
                    "category",
                    QUICK_REPLY_MAP.get("category"),
                    null
            );
        }
        String msg = String.format("「%s」商品已加载。你可以问我：\n" +
                "• 这款定什么价？\n• 上架哪个渠道？\n• 库存还剩多少？", ctx.category);
        return ChatReply.info(msg, null);
    }

    private ChatReply handleOrder(Context ctx, String userMessage) {
        if (StringUtils.hasText(ctx.orderId)) {
            String status = ctx.paid != null && ctx.paid ? "已支付" : "待支付";
            String msg = String.format("订单 %s 状态：%s，金额：%s %s。",
                    ctx.orderId, status,
                    ctx.displayPrice != null ? ctx.displayPrice.toPlainString() :
                            (ctx.price != null ? ctx.price.toPlainString() : "—"),
                    StringUtils.hasText(ctx.userCurrency) ? ctx.userCurrency : "");
            return ChatReply.info(msg, null);
        }
        return ChatReply.info("请告诉我订单 ID 或链码，我来帮你查询订单状态。", null);
    }

    // ====================================================================
    // 工具
    // ====================================================================

    private String buildSelectionDoneMessage(Context ctx) {
        StringBuilder sb = new StringBuilder("✅ 选款完成！根据你的偏好：\n");
        if (StringUtils.hasText(ctx.category))   sb.append("• 品类：").append(ctx.category).append("\n");
        if (StringUtils.hasText(ctx.style))       sb.append("• 风格：").append(ctx.style).append("\n");
        if (StringUtils.hasText(ctx.crowd))       sb.append("• 客群：").append(ctx.crowd).append("\n");
        if (StringUtils.hasText(ctx.market))      sb.append("• 市场：").append(ctx.market).append("\n");
        if (StringUtils.hasText(ctx.priceLevel))  sb.append("• 价位：").append(ctx.priceLevel).append("\n");
        int cnt = ctx.selectionImages != null ? ctx.selectionImages.size() : 0;
        sb.append("\n为你推荐了 ").append(cnt).append(" 款参考图，点击图片可进入设计改款。");
        return sb.toString();
    }

    // ====================================================================
    // 返回结构
    // ====================================================================

    /**
     * AI 对话回复结构。
     */
    public static class ChatReply {
        /** AI 回复文本 */
        private String       aiMessage;
        /** 当前等待回答的字段名（可为 null） */
        private String       pendingField;
        /** 快速回复候选按钮（可为 null） */
        private List<String> quickReplies;
        /** 图片列表（可为 null） */
        private List<String> images;
        /** 是否本轮流程完成 */
        private boolean      done;
        /** 会话 ID（服务层回填） */
        private String       sessionId;

        private ChatReply() {}

        static ChatReply question(String msg, String field, List<String> qr, List<String> imgs) {
            ChatReply r = new ChatReply();
            r.aiMessage    = msg;
            r.pendingField = field;
            r.quickReplies = qr;
            r.images       = imgs;
            r.done         = false;
            return r;
        }

        static ChatReply done(String msg, List<String> imgs) {
            ChatReply r = new ChatReply();
            r.aiMessage    = msg;
            r.images       = imgs;
            r.done         = true;
            return r;
        }

        static ChatReply info(String msg, List<String> imgs) {
            ChatReply r = new ChatReply();
            r.aiMessage = msg;
            r.images    = imgs;
            r.done      = false;
            return r;
        }

        // ---- getters / setters ----
        public String       getAiMessage()    { return aiMessage; }
        public String       getPendingField() { return pendingField; }
        public List<String> getQuickReplies() { return quickReplies; }
        public List<String> getImages()       { return images; }
        public boolean      isDone()          { return done; }
        public String       getSessionId()    { return sessionId; }
        public void         setSessionId(String s) { this.sessionId = s; }
    }

}
