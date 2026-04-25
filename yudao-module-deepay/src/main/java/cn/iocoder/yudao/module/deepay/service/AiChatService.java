package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.*;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayInventoryDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayInventoryMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.vo.AiChatContextVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    @Resource private TrendAgent            trendAgent;
    @Resource private DemandAgent           demandAgent;
    @Resource private DeepayInventoryMapper inventoryMapper;
    @Resource private DeepayProductMapper   productMapper;
    @Resource private DeepayOrderMapper     orderMapper;
    @Resource private DeepayMetricsMapper   metricsMapper;
    @Resource private AiPersonaService      aiPersonaService;

    // ====================================================================
    // 角色人设 system prompt（与前端 aiRoles.ts 对应）
    // ====================================================================

    private static final Map<String, String> ROLE_SYSTEM_PROMPTS = new LinkedHashMap<>();
    static {
        ROLE_SYSTEM_PROMPTS.put("selection",
                "你是一位专业的服装购物顾问，精通服装选款、市场趋势和消费者心理。" +
                "用热情、亲切的语气与用户对话，善于推荐爆款单品，语言简洁活泼。");
        ROLE_SYSTEM_PROMPTS.put("design",
                "你是一位顶尖的服装设计师，擅长时尚趋势分析、款式设计和面料搭配。" +
                "用充满创意和专业的语气与用户对话，对设计细节充满热情，善于用生动的语言描述设计方案。");
        ROLE_SYSTEM_PROMPTS.put("product",
                "你是一位经验丰富的电商产品经理，专注于商品管理、定价策略和市场竞争分析。" +
                "用严谨、条理清晰的语气与用户对话，善于数据驱动的决策建议。");
        ROLE_SYSTEM_PROMPTS.put("inventory",
                "你是一位专业的库存管理专员，熟悉供应链管理、库存预测和补货策略。" +
                "用准确、高效的语气回答问题，善于用数据说话，帮助用户优化库存管理。");
        ROLE_SYSTEM_PROMPTS.put("finance",
                "你是一位严谨专业的财务总监，擅长 ROI 分析、成本管控和财务决策。" +
                "用正式、数据驱动的语气与用户对话，给出具体的财务数据和改善建议。");
        ROLE_SYSTEM_PROMPTS.put("trend",
                "你是一位敏锐的时尚趋势分析师，精通全球市场动态、消费者偏好分析和流行趋势预测。" +
                "用充满洞察力和前瞻性的语气对话，善于将数据转化为可操作的市场建议。");
        ROLE_SYSTEM_PROMPTS.put("order",
                "你是一位耐心、专业的客服专员，擅长处理订单查询、售后服务和用户问题。" +
                "用温暖、亲切的语气与用户对话，始终以解决用户问题为首要目标。");
    }

    /**
     * Get the role system prompt prefix for a given module.
     * Queries AiPersonaService (DB → global default → hardcoded fallback).
     *
     * @param module 模块名
     * @return system prompt 字符串（永不为 null）
     */
    public String getRolePrompt(String module) {
        try {
            return aiPersonaService.getSystemPrompt(AiPersonaService.GLOBAL_TENANT_ID, module);
        } catch (Exception e) {
            log.warn("[AiChatService] AiPersonaService 异常，使用硬编码 prompt module={}", module, e);
            return ROLE_SYSTEM_PROMPTS.getOrDefault(
                    module == null ? "selection" : module.toLowerCase(),
                    "你是一个智能 AI 助手，帮助用户解决各类业务问题。用专业、友善的语气对话。"
            );
        }
    }

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
        return chat(module, sessionId, customerId, userMessage, null);
    }

    /**
     * 处理用户消息（带上下文注入），返回 AI 回复。
     *
     * @param module      板块名
     * @param sessionId   会话 ID（首次传 null，系统自动创建）
     * @param customerId  用户/客户 ID
     * @param userMessage 用户输入的自然语言
     * @param context     前端注入的上下文（可为 null）
     * @return AI 回复结构
     */
    public ChatReply chat(String module, String sessionId, Long customerId,
                          String userMessage, AiChatContextVO context) {
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

        // 2. 注入前端上下文（entityId 等实体信息）
        String effectiveModule = module;
        String contextPrefix = "";
        if (context != null) {
            contextPrefix = buildContextPrefix(context);
            // 允许前端 context 中的 module 覆盖 URL param（更精确）
            if (StringUtils.hasText(context.getModule())) {
                effectiveModule = context.getModule();
            }
        }

        // 3. NLP 解析：将自然语言映射到结构化字段（注入角色人设 prompt）
        String rolePrompt = getRolePrompt(effectiveModule);
        // 将上下文前缀拼入 rolePrompt，让 NLP 模型感知页面状态
        String fullPrompt = StringUtils.hasText(contextPrefix)
                ? rolePrompt + "\n\n[当前页面上下文]\n" + contextPrefix
                : rolePrompt;
        nlpParserService.parse(userMessage, ctx, fullPrompt);

        // 4. 根据 module 路由执行
        ChatReply reply;
        switch (effectiveModule == null ? "selection" : effectiveModule.toLowerCase()) {
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

        // 5. 保存 Context（覆盖写）
        chatSessionService.save(sessionId, ctx);
        reply.setSessionId(sessionId);
        return reply;
    }

    /**
     * 将前端上下文 VO 构建为可读的 prompt 前缀字符串。
     */
    private String buildContextPrefix(AiChatContextVO ctx) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(ctx.getRoute()))      sb.append("当前路由：").append(ctx.getRoute()).append("\n");
        if (StringUtils.hasText(ctx.getEntityType())) sb.append("实体类型：").append(ctx.getEntityType()).append("\n");
        if (StringUtils.hasText(ctx.getEntityId()))   sb.append("实体ID：").append(ctx.getEntityId()).append("\n");
        if (StringUtils.hasText(ctx.getSnapshot()))   sb.append("页面数据快照：").append(ctx.getSnapshot()).append("\n");
        return sb.toString().trim();
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
        // Run TrendAgent to fetch real trend images
        try {
            ctx = trendAgent.run(ctx);
        } catch (Exception e) {
            log.warn("[AiChatService] TrendAgent 异常", e);
        }
        String cat = StringUtils.hasText(ctx.category) ? ctx.category : "全品类";
        List<String> images = ctx.trendImages != null ? ctx.trendImages : Collections.emptyList();
        String msg;
        if (!images.isEmpty()) {
            msg = String.format("📈 「%s」近期趋势热款 %d 张来啦！点击图片可进入改款设计。", cat, images.size());
        } else {
            msg = String.format("暂时没有找到「%s」的趋势数据，建议换个品类或风格试试。", cat);
        }
        return images.isEmpty() ? ChatReply.info(msg, null) : ChatReply.done(msg, images);
    }

    private ChatReply handleInventory(Context ctx, String userMessage) {
        // Intent detection: 查询 vs 补货建议
        boolean isRestock = userMessage.contains("补货") || userMessage.contains("不够") || userMessage.contains("缺货");

        // Try to get real inventory data
        String chainCode = ctx.chainCode;
        if (StringUtils.hasText(chainCode)) {
            try {
                DeepayInventoryDO inv = inventoryMapper.selectByChainCode(chainCode);
                if (inv != null) {
                    int stock = inv.getStock() != null ? inv.getStock() : 0;
                    String status = stock <= 3 ? "⚠️ 库存告急" : (stock <= 10 ? "📦 库存偏低" : "✅ 库存充足");
                    String msg = String.format("%s\n\n链码：%s\n当前库存：%d 件\n锁定库存：%d 件",
                            status, chainCode, stock, inv.getLockedStock() != null ? inv.getLockedStock() : 0);
                    if (isRestock || stock <= 3) {
                        // Run DemandAgent for restock suggestion
                        try { ctx = demandAgent.run(ctx); } catch (Exception ignored) {}
                        int suggested = ctx.suggestedProductionQty != null ? ctx.suggestedProductionQty : 20;
                        msg += String.format("\n\n📊 AI 建议补货：%d 件（基于近 7 天销量预测）", suggested);
                    }
                    return ChatReply.info(msg, null);
                }
            } catch (Exception e) {
                log.warn("[AiChatService] 库存查询异常 chainCode={}", chainCode, e);
            }
        }

        // No chainCode - category-based demand forecast
        if (StringUtils.hasText(ctx.category)) {
            try { ctx = demandAgent.run(ctx); } catch (Exception ignored) {}
            int demand = ctx.predictedDemand != null ? ctx.predictedDemand : 0;
            int qty    = ctx.suggestedProductionQty != null ? ctx.suggestedProductionQty : 0;
            String msg = String.format("📊 「%s」未来 7 天预测销量：%d 件\n建议备货：%d 件（含 20%% 安全系数）\n\n如需补货，请前往「库存管理」页面操作。",
                    ctx.category, demand, qty);
            return ChatReply.info(msg, null);
        }

        return ChatReply.question(
                "请告诉我你想查哪个品类的库存或销量预测？",
                "category",
                QUICK_REPLY_MAP.get("category"),
                null
        );
    }

    private ChatReply handleFinance(Context ctx, String userMessage) {
        // Intent detection
        boolean wantsRoi     = userMessage.contains("ROI") || userMessage.contains("roi") || userMessage.contains("回报");
        boolean wantsProfit  = userMessage.contains("利润") || userMessage.contains("赚");
        boolean wantsSales   = userMessage.contains("销售") || userMessage.contains("销量") || userMessage.contains("卖了");

        // Try to fetch real metrics
        if (StringUtils.hasText(ctx.chainCode)) {
            try {
                BigDecimal roi = metricsMapper.selectLatestRoiByChainCode(ctx.chainCode);
                BigDecimal avgPrice = StringUtils.hasText(ctx.category)
                        ? metricsMapper.selectAvgPriceByCategory(ctx.category) : null;

                StringBuilder sb = new StringBuilder("💰 财务分析报告\n\n");
                if (roi != null) {
                    double roiPct = roi.doubleValue() * 100;
                    String roiMark = roiPct >= 40 ? "🟢 优秀" : (roiPct >= 20 ? "🟡 良好" : "🔴 偏低");
                    sb.append(String.format("ROI：%.1f%% %s\n", roiPct, roiMark));
                }
                if (ctx.profit != null) {
                    sb.append(String.format("单件利润：¥%s\n", ctx.profit.toPlainString()));
                }
                if (ctx.costPrice != null) {
                    sb.append(String.format("生产成本：¥%s\n", ctx.costPrice.toPlainString()));
                }
                if (ctx.price != null) {
                    sb.append(String.format("当前售价：¥%s\n", ctx.price.toPlainString()));
                }
                if (avgPrice != null) {
                    sb.append(String.format("同品类均价：¥%s\n", avgPrice.toPlainString()));
                }
                if (sb.length() > 12) {
                    return ChatReply.info(sb.toString().trim(), null);
                }
            } catch (Exception e) {
                log.warn("[AiChatService] 财务数据查询异常", e);
            }
        }

        // Category-based avg price
        if (StringUtils.hasText(ctx.category)) {
            try {
                BigDecimal avgPrice = metricsMapper.selectAvgPriceByCategory(ctx.category);
                if (avgPrice != null) {
                    String msg = String.format("📊 「%s」同品类历史均价：¥%s\n\n建议定价区间：¥%s ~ ¥%s（±10%%）\n\n如需查看某款具体数据，请告诉我链码。",
                            ctx.category, avgPrice.toPlainString(),
                            avgPrice.multiply(new BigDecimal("0.9")).setScale(0, java.math.RoundingMode.FLOOR).toPlainString(),
                            avgPrice.multiply(new BigDecimal("1.1")).setScale(0, java.math.RoundingMode.CEILING).toPlainString());
                    return ChatReply.info(msg, null);
                }
            } catch (Exception e) {
                log.warn("[AiChatService] 均价查询异常", e);
            }
        }

        return ChatReply.info(
                "请告诉我你想分析哪款商品的财务数据？\n" +
                "可以问我：「这款外套的 ROI 是多少？」或「外套品类均价是多少？」",
                null
        );
    }

    private ChatReply handleProduct(Context ctx, String userMessage) {
        // Intent detection
        boolean wantsPrice  = userMessage.contains("价") || userMessage.contains("定价");
        boolean wantsSearch = userMessage.contains("查") || userMessage.contains("找") || userMessage.contains("哪些");
        boolean wantsStatus = userMessage.contains("状态") || userMessage.contains("上架") || userMessage.contains("下架");

        // Try to find product by chainCode
        if (StringUtils.hasText(ctx.chainCode)) {
            try {
                DeepayProductDO p = productMapper.selectByChainCode(ctx.chainCode);
                if (p != null) {
                    String msg = String.format("📦 商品详情\n\n" +
                            "标题：%s\n状态：%s\n库存：%d 件\n" +
                            "售价：¥%s\n销量：%d 件\n渠道：%s",
                            p.getTitle(), p.getStatus(),
                            p.getStock() != null ? p.getStock() : 0,
                            p.getPrice() != null ? p.getPrice().toPlainString() : "—",
                            p.getSoldCount() != null ? p.getSoldCount() : 0,
                            p.getChannel() != null ? p.getChannel() : "未发布");
                    return ChatReply.info(msg, p.getMainImage() != null
                            ? Collections.singletonList(p.getMainImage()) : null);
                }
            } catch (Exception e) {
                log.warn("[AiChatService] 商品查询异常", e);
            }
        }

        // Category search
        if (StringUtils.hasText(ctx.category) && wantsSearch) {
            try {
                List<DeepayProductDO> products = productMapper.selectHotByCategory(ctx.category);
                if (!products.isEmpty()) {
                    List<String> images = new ArrayList<>();
                    StringBuilder sb = new StringBuilder(String.format("🔍 「%s」热销商品 Top %d：\n\n",
                            ctx.category, products.size()));
                    for (int i = 0; i < Math.min(products.size(), 5); i++) {
                        DeepayProductDO p = products.get(i);
                        sb.append(String.format("%d. %s — ¥%s（已售 %d 件）\n",
                                i + 1, p.getTitle(),
                                p.getPrice() != null ? p.getPrice().toPlainString() : "—",
                                p.getSoldCount() != null ? p.getSoldCount() : 0));
                        if (p.getMainImage() != null) images.add(p.getMainImage());
                    }
                    return ChatReply.done(sb.toString().trim(), images.isEmpty() ? null : images);
                }
            } catch (Exception e) {
                log.warn("[AiChatService] 品类商品查询异常", e);
            }
        }

        if (!StringUtils.hasText(ctx.category)) {
            return ChatReply.question(
                    "你想管理哪个品类的商品？",
                    "category",
                    QUICK_REPLY_MAP.get("category"),
                    null
            );
        }

        String msg = String.format("「%s」商品管理已就绪。你可以问我：\n" +
                "• 「查一下外套有哪些在卖」\n" +
                "• 「这款外套应该定多少价？」\n" +
                "• 「帮我查链码 XXXXX 的状态」", ctx.category);
        return ChatReply.info(msg, null);
    }

    private ChatReply handleOrder(Context ctx, String userMessage) {
        // Try to find order by chainCode
        if (StringUtils.hasText(ctx.chainCode)) {
            try {
                DeepayOrderDO order = orderMapper.selectByChainCode(ctx.chainCode);
                if (order != null) {
                    String statusText = "PAID".equals(order.getStatus()) ? "✅ 已支付"
                            : "PENDING".equals(order.getStatus()) ? "⏳ 待支付"
                            : "CANCELLED".equals(order.getStatus()) ? "❌ 已取消" : order.getStatus();
                    String msg = String.format("📋 订单详情\n\n" +
                            "订单 ID：%s\n状态：%s\n链码：%s\n" +
                            "金额：%s %s\n创建时间：%s",
                            order.getId(), statusText, order.getChainCode(),
                            order.getAmount() != null ? "¥" + order.getAmount().toPlainString() : "—",
                            order.getCurrency() != null ? order.getCurrency() : "",
                            order.getCreatedAt() != null ? order.getCreatedAt().toString().replace("T", " ") : "—");
                    return ChatReply.info(msg, null);
                }
            } catch (Exception e) {
                log.warn("[AiChatService] 订单查询异常", e);
            }
        }

        // Try by orderId in ctx
        if (StringUtils.hasText(ctx.orderId)) {
            return ChatReply.info(
                    String.format("订单 %s 状态：%s",
                            ctx.orderId,
                            Boolean.TRUE.equals(ctx.paid) ? "✅ 已支付" : "⏳ 待支付"),
                    null
            );
        }

        return ChatReply.info(
                "请告诉我你想查询的订单信息：\n" +
                "• 「查一下链码 XXXXX 的订单」\n" +
                "• 「我的最新订单状态」",
                null
        );
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
