package cn.iocoder.yudao.module.deepay.agent;

/**
 * ScoreUtil — 全系统统一评分工具（Phase 8-9 核心）。
 *
 * <h3>三套公式</h3>
 * <pre>
 * ① 全局最终评分（SelectionFeed / AIDecision 通用）
 *    finalScore = trendScore×0.3 + brandScore×0.2 + matchScore×0.2
 *                + historyScore×0.2 + riskPenalty×0.1
 *
 * ② 设计确认评分（DesignConfirmAgent 用）
 *    confirmScore = trendScore×0.4 + brandScore×0.3 + matchScore×0.3
 *
 * ③ 设计质量评分（DesignVariantAgent 用）
 *    designScore = originality×0.3 + producibility×0.3
 *                + costScore×0.2    + marketMatch×0.2
 * </pre>
 *
 * <p>所有入参均在 0~100 区间；返回值同样在 0~100 区间（四舍五入取整）。</p>
 */
public final class ScoreUtil {

    private ScoreUtil() {}

    // ====================================================================
    // ① 全局最终评分
    // ====================================================================

    /**
     * 全局最终评分。
     *
     * @param trendScore   趋势匹配度（0~100）：trendImages 越多/越精准越高
     * @param brandScore   品牌合规度（0~100）：无 Logo=90，有 Logo=0
     * @param matchScore   画像匹配度（0~100）：来自 StyleConsistencyAgent
     * @param historyScore 历史偏好匹配（0~100）：来自 CustomerProfile.styleWeights
     * @param riskPenalty  风险惩罚（0~100，值越高扣分越多）：RiskControlAgent 输出
     * @return 综合评分 0~100
     */
    public static int computeFinalScore(int trendScore, int brandScore,
                                        int matchScore, int historyScore,
                                        int riskPenalty) {
        double raw = trendScore   * 0.3
                   + brandScore   * 0.2
                   + matchScore   * 0.2
                   + historyScore * 0.2
                   + (100 - riskPenalty) * 0.1;   // penalty 越高 → 贡献越低
        return clamp((int) Math.round(raw));
    }

    // ====================================================================
    // ② 设计确认评分（DesignConfirmAgent）
    // ====================================================================

    /**
     * 设计确认评分。
     *
     * @param trendScore 趋势评分（0~100）
     * @param brandScore 品牌合规（0~100）
     * @param matchScore 风格匹配（0~100）
     * @return confirmScore 0~100，&lt;60 拒绝进入生产
     */
    public static int computeConfirmScore(int trendScore, int brandScore, int matchScore) {
        double raw = trendScore * 0.4
                   + brandScore * 0.3
                   + matchScore * 0.3;
        return clamp((int) Math.round(raw));
    }

    // ====================================================================
    // ③ 设计质量评分（DesignVariantAgent）
    // ====================================================================

    /**
     * 设计质量评分。
     *
     * @param originality    原创度（0~100）：与参考图相似度越低越高
     * @param producibility  可生产度（0~100）：复杂度越低越高
     * @param costScore      成本得分（0~100）：成本越合理越高
     * @param marketMatch    市场匹配（0~100）：与目标市场风格越符合越高
     * @return designScore 0~100
     */
    public static int computeDesignScore(int originality, int producibility,
                                         int costScore, int marketMatch) {
        double raw = originality   * 0.3
                   + producibility * 0.3
                   + costScore     * 0.2
                   + marketMatch   * 0.2;
        return clamp((int) Math.round(raw));
    }

    // ====================================================================
    // 工具
    // ====================================================================

    /**
     * 根据复杂度（0~100）推算可生产度评分（复杂度越低越易量产）。
     * complexity=0 → producibility=100；complexity=100 → producibility=0
     */
    public static int producibilityFromComplexity(int complexity) {
        return clamp(100 - complexity);
    }

    /**
     * 根据 costTotal / suggestPrice 计算成本得分。
     * 毛利率 &ge;50% → 100分；毛利率 &le;0% → 0分；线性插值。
     */
    public static int costScoreFromMargin(double costTotal, double suggestPrice) {
        if (suggestPrice <= 0) return 0;
        double margin = (suggestPrice - costTotal) / suggestPrice;
        return clamp((int) Math.round(margin * 200));   // 50%毛利率 → 100分
    }

    /** 将值约束在 [0, 100] 区间。 */
    public static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
