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
     * 当前待回答问题（SmartQuestionAgent 决策树输出）。
     * 非 null 表示流程"暂停等待用户输入"，Orchestrator 收到后立即返回，不继续执行后续 Agent。
     * 调用方在下次请求中把答案填入对应字段，再次调用 Orchestrator 即可继续。
     */
    public String pendingQuestion;
    /** TrendAgent 输出：结构化趋势商品列表（含 imageUrl / category / style / soldCount） */
    public java.util.List<TrendItem> trendItems;
    /** TrendSourceAgent 输出：趋势图 URL 列表（来自内部近7天热销） */
    public java.util.List<String> trendImages;
    /** TrendSourceAgent 输出：趋势关键词 */
    public java.util.List<String> trendKeywords;

    // ===== Phase 9 StyleEngineAgent 输出 =====
    /**
     * 客户风格偏好（中文标签，如 工装 / 极简 / 性感 / 运动）。
     * 由 SmartQuestionAgent / StyleEngineAgent 填充。
     * 与 {@link #style}（英文枚举）并存，StyleEngineAgent 优先读此字段。
     */
    public String stylePreference;

    /**
     * 目标市场（中文标签，如 欧美 / 中东 / 国内）。
     * StyleEngineAgent 读取后映射为英文 Prompt 描述。
     * 与 {@link #market}（英文枚举 CN/EU/US/ME）并存。
     */
    public String targetMarket;

    /**
     * StyleEngineAgent 输出：可直接发给 FLUX API 的完整英文 Prompt。
     * DesignGenAgent 读取此字段生成图片。
     */
    public String finalPrompt;

    // ===== Phase 8 图片评分 =====
    /** ImageScoringAgent 输出：带评分的设计图列表 */
    public List<DesignImage> scoredImages;
    /** ImageScoringAgent 输出：综合分 Top 2 图片 */
    public List<DesignImage> topImages;

    // ===== Phase 8 设计拆分 =====
    /** 版型（oversize / fitted / straight），由 DesignSplitAgent 填充 */
    public String patternType;
    /** 面料（cotton / denim / knit / wool），由 DesignSplitAgent 填充 */
    public String fabric;
    /** 设计细节（多口袋 / 刺绣 / 拉链等），由 DesignSplitAgent 填充 */
    public String designDetails;

    // ===== Phase 8 风险 =====
    /** 版权风险评分（0~100），>80 视为高风险，由 RiskControlAgent 填充 */
    public Integer riskScore;

    // ===== Phase 8 成本 =====
    /** 估算生产成本，由 CostEstimateAgent 填充 */
    public java.math.BigDecimal cost;

    // ===== Phase 9 变体与安全图 =====
    /** DesignVariantAgent 输出：所有变体图片 URL 列表（5种颜色×面料，共10张） */
    public List<String> variantImages;
    /** RiskControlAgent 输出：过滤高风险图后的安全图片列表 */
    public List<String> safeImages;

    // ===== 向后兼容（ChainOrchestrator）=====
    /** @deprecated 使用 keyword */
    @Deprecated public String prompt;
    /** @deprecated 使用 designImages */
    @Deprecated public List<String> images;
    /** @deprecated */
    @Deprecated public String imaKbId;
    /** @deprecated */
    @Deprecated public String iban;

}
