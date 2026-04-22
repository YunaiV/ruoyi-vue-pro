package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayVariantDO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 全流程唯一数据载体 —— 最终生产版，一次定死，不再变更。
 *
 * <pre>
 * keyword         → TrendAgent       → referenceImages
 * referenceImages → DesignAgent      → designImages
 * designImages    → JudgeAgent       → imageScores
 * imageScores     → AIDecisionAgent  → selectedImage / needRedesign / shouldProduce / suggestPrice / action
 * selectedImage   → ChainAgent       → chainCode
 * chainCode       → PatternAgent     → patternFile
 * keyword+image   → ProductAgent     → title / description
 * suggestPrice    → PricingAgent     → price
 * productId+price → PublishAgent     → published / productId
 * productId       → PaymentAgent     → paymentId / paid
 * price           → InventoryAgent   → stock / lockedStock
 * *               → AnalyticsAgent   → soldCount / analyticsReport
 * </pre>
 */
public class Context {

    // ===== 输入 =====
    public String keyword;

    // ===== 趋势 =====
    public List<String> referenceImages;

    // ===== 设计 =====
    public List<String> designImages;

    // ===== 评分 =====
    public Map<String, Integer> imageScores;

    // ===== AI决策 =====
    public String selectedImage;
    public Boolean needRedesign;
    public Boolean shouldProduce;
    public BigDecimal suggestPrice;
    public String decisionReason;

    // ===== 链路 =====
    public String chainCode;

    // ===== 打版 =====
    public String patternFile;
    public String techPackUrl;

    // ===== 商品 =====
    public String title;
    public String description;

    // ===== 定价 =====
    public BigDecimal price;

    // ===== 发布 =====
    public Boolean published;
    public String productId;
    public String productUrl;

    // ===== 支付 =====
    public String paymentId;
    public Boolean paid;
    public String orderId;
    public Long userId;

    // ===== 库存 =====
    public Integer stock;
    public Integer lockedStock;

    // ===== 销售 =====
    public Integer soldCount;
    /** HotCloneAgent 生成的款式变体列表 */
    public List<DeepayVariantDO> variants;

    // ===== 成本与利润（Phase 4 利润驱动核心） =====
    /** 生产成本（元）；ProductAgent 落库时写入，PricingAgent 据此定价 */
    public java.math.BigDecimal costPrice;
    /** 单笔利润 = price - costPrice，Payment 回调时计算 */
    public java.math.BigDecimal profit;
    /** 投资回报率 = profit / costPrice，Payment 回调 & Scheduler 决策依据 */
    public java.math.BigDecimal roi;

    // ===== Phase 5 B2B 批发 =====
    /** 客户 ID（批发场景） */
    public Long clientId;
    /** 客户等级 A/B/C，由 ClientAgent 注入，影响定价与供货优先级 */
    public String clientLevel;
    /** 批发数量（下单件数，影响阶梯定价） */
    public Integer wholesaleQty;
    /** 批发折扣后的单价 */
    public java.math.BigDecimal wholesalePrice;
    /** 本单批发总利润 = (wholesalePrice - costPrice) × wholesaleQty */
    public java.math.BigDecimal wholesaleProfit;
    /** DemandAgent 预测销量（未来 7 天） */
    public Integer predictedDemand;
    /** DemandAgent 预测置信度（0~1） */
    public java.math.BigDecimal demandConfidence;
    /** ProductionPlanner 建议生产量 */
    public Integer suggestedProductionQty;

    // ===== 分析 =====
    public String action;           // BOOST / STOP / REDESIGN
    public String analyticsReport;

    // ===== Phase 6 客户画像（记忆 + 个性化）=====
    /** 客户 ID（B2B 客户或用户 ID，关联 deepay_customer_profile） */
    public Long customerId;
    /** 品类（如 外套 / 内裤 / 连衣裙），SmartQuestionAgent 填充或从画像加载 */
    public String category;
    /** 主风格标签（SEXY / CASUAL / SPORT / MINIMAL / LUXURY / minimalist 等） */
    public String style;
    /**
     * 目标客群：男装 / 少女 / 中老年 / 运动。
     * QADecisionAgent 填充，CustomerProfileAgent 持久化。
     */
    public String crowd;
    /** 风格权重 Map（key=风格名, value=0~1 权重），由 MemoryAgent / PreferenceLearningAgent 维护 */
    public java.util.Map<String, Double> styleWeights;
    /**
     * 组合风格 Prompt（由 StyleEngine 生成，例如"性感 + 极简"）。
     * DesignAgent 直接消费此字段，不需要自己拼接。
     */
    public String stylePrompt;
    /** 目标市场：CN / EU / US / ME */
    public String market;
    /** 价格带：LOW / MID / HIGH（别名 priceLevel，统一用此字段） */
    public String priceLevel;
    /** 目标年龄：YOUNG / MIDDLE / ELDER */
    public String targetAge;
    /** 目标性别：MALE / FEMALE / UNISEX */
    public String gender;
    /** 客户画像置信度（0~1），低于 0.6 时触发 SmartQuestionAgent */
    public java.math.BigDecimal confidenceScore;
    /**
     * 当前待回答问题（SmartQuestionAgent / QADecisionAgent 决策树输出）。
     * 非 null 表示流程"暂停等待用户输入"，Orchestrator 收到后立即返回，不继续执行后续 Agent。
     * 调用方在下次请求中把答案填入对应字段，再次调用 Orchestrator 即可继续。
     */
    public String pendingQuestion;
    /**
     * 当前待填写的字段名（对应 pendingQuestion 的字段）。
     * 例如 "category"、"crowd"、"style"、"market"、"priceLevel"。
     * 前端/客户端据此知道把用户答案填入哪个字段。
     */
    public String pendingField;
    /** TrendAgent 输出：结构化趋势商品列表（含 imageUrl / category / style / soldCount） */
    public java.util.List<TrendItem> trendItems;
    /** TrendAgent 输出：趋势图 URL 列表（来自 deepay_trend_pool 或内部近7天热销） */
    public java.util.List<String> trendImages;
    /** TrendSourceAgent 输出：趋势关键词 */
    public java.util.List<String> trendKeywords;
    /**
     * SelectionFeedAgent 输出：推给设计师的参考图列表（按品类 + 风格 + 客群过滤排序）。
     * ❗ 这是参考款图片，不是商品。设计师从这里选方向。
     */
    public java.util.List<String> selectionImages;
    /**
     * StyleEngine.buildCombinations() 输出：10~20 个风格组合方向。
     * 每个 StyleCombo 包含主风格、副风格、参考品牌和完整 Prompt。
     */
    public java.util.List<StyleCombo> styleCombos;

    // ===== 向后兼容（ChainOrchestrator）=====
    /** @deprecated 使用 keyword */
    @Deprecated public String prompt;
    /** @deprecated 使用 designImages */
    @Deprecated public List<String> images;
    /** @deprecated */
    @Deprecated public String imaKbId;
    /** @deprecated */
    @Deprecated public String iban;

    // ===== Phase 8 设计落地 =====
    /** DesignSelectAgent 模式：HUMAN=人工选图 / AI=自动选图（默认 AI）。 */
    public String  designSelectMode;
    /** DesignConfirmAgent 是否已确认（true = 可进入生产）。 */
    public Boolean designConfirmed;
    /** StyleConsistencyAgent 评分（0~100，<60 触发重设计）。 */
    public Integer styleConsistencyScore;
    /** RiskControlAgent 风险等级：NONE / LOW / MEDIUM / HIGH。 */
    public String  riskLevel;
    /** RiskControlAgent：是否检测到品牌 Logo（true → 拒绝生产）。 */
    public Boolean logoDetected;
    /** DesignSplitAgent 输出：结构复杂度评分（0~100，>80 拒绝生产）。 */
    public Integer complexity;
    /** DesignSplitAgent 输出：生产要素 JSON
     *  {"category":"外套","fabric":"棉/牛仔","colors":["黑","灰"],"style":"工装","structure":"宽松","complexity":40} */
    public String  designFeatures;
    /** 统一设计评分（ScoreUtil.computeDesignScore），用于 DesignVariantAgent 选最优。 */
    public Integer designScore;

    // ===== Phase 9 原创生成 =====
    /** DesignGenAgent 生成设计图时使用的 Prompt（记录备查）。 */
    public String  designPrompt;
    /** DesignVariantAgent 输出：3+ 个风格变体。 */
    public java.util.List<DesignVariant> designVariants;
    /** DesignVariantAgent 选出的最佳变体图 URL（高分者）。 */
    public String  finalDesign;
    /** PatternPrepareAgent 输出：版型类型（基础版型 / 贴体 / 宽松）。 */
    public String  patternType;
    /** PatternPrepareAgent 输出：尺码范围。 */
    public java.util.List<String> sizeRange;
    /** PatternPrepareAgent 输出：面料建议（如"棉+弹力"）。 */
    public String  fabricSuggestion;
    /** PatternPrepareAgent 输出：打版难度（low / medium / high）。 */
    public String  difficulty;
    /** CostEstimateAgent 输出：面料成本（元）。 */
    public BigDecimal fabricCost;
    /** CostEstimateAgent 输出：人工成本（元）。 */
    public BigDecimal laborCost;
    /** CostEstimateAgent 输出：总成本（元）= fabricCost + laborCost。 */
    public BigDecimal totalCost;
    /** CostEstimateAgent 输出：true 表示成本过高，流程在 Phase 9 终止。 */
    public Boolean costTooHigh;

}


