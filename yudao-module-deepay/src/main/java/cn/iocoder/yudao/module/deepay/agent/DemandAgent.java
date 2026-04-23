package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDemandForecastDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDemandForecastMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;

/**
 * DemandAgent — Phase 5 需求预测大脑。
 *
 * <p>在"卖之前"预测未来 7 天销量，为 ProductionPlanner 提供输入，
 * 实现"提前备货"而非"卖了再补"。</p>
 *
 * <p>预测模型（可替换为真实 ML 模型）：
 * <pre>
 *   predictedDemand = historicalAvg × seasonFactor × confidenceWeight
 *   suggestedQty    = predictedDemand × safetyFactor（默认 1.2）
 * </pre>
 * </p>
 *
 * <p>配置项（application.yml）：
 * <pre>
 * deepay:
 *   demand:
 *     forecast-days: 7        # 预测周期（天）
 *     safety-factor: 1.2      # 安全系数（建议生产量 = 预测量 × 安全系数）
 *     default-demand: 20      # 无历史数据时的默认预测值
 * </pre>
 * </p>
 */
@Component
public class DemandAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(DemandAgent.class);

    @Value("${deepay.demand.forecast-days:7}")
    private int forecastDays;

    @Value("${deepay.demand.safety-factor:1.2}")
    private double safetyFactor;

    @Value("${deepay.demand.default-demand:20}")
    private int defaultDemand;

    private static final double DAYS_PER_MONTH = 30.0;

    @Resource
    private DeepayDemandForecastMapper demandForecastMapper;

    @Override
    public Context run(Context ctx) {
        String category = ctx.keyword != null ? ctx.keyword : "通用";

        // 1. 查历史均销量
        Double avgSales = null;
        try {
            avgSales = demandForecastMapper.selectAvgSalesByCategory(category);
        } catch (Exception e) {
            log.warn("[DemandAgent] 查询历史销量失败，使用默认值 category={}", category, e);
        }

        double base = (avgSales != null && avgSales > 0) ? avgSales : defaultDemand;

        // 2. 季节因子（简单月份规则，可替换为真实模型）
        BigDecimal seasonFactor = computeSeasonFactor();

        // 3. 预测销量 = base × seasonFactor × (forecastDays / 30)
        double predicted = base * seasonFactor.doubleValue() * ((double) forecastDays / DAYS_PER_MONTH);
        int predictedSales = Math.max(1, (int) Math.round(predicted));

        // 4. 置信度（有历史数据时较高，无时较低）
        BigDecimal confidence = avgSales != null && avgSales > 0
                ? new BigDecimal("0.78") : new BigDecimal("0.40");

        // 5. 建议生产量
        int suggestedQty = (int) Math.ceil(predictedSales * safetyFactor);

        ctx.predictedDemand        = predictedSales;
        ctx.demandConfidence       = confidence;
        ctx.suggestedProductionQty = suggestedQty;

        // 6. 落库
        DeepayDemandForecastDO forecast = new DeepayDemandForecastDO();
        forecast.setChainCode(ctx.chainCode);
        forecast.setCategory(category);
        forecast.setPredictedSales(predictedSales);
        forecast.setConfidence(confidence);
        forecast.setForecastDays(forecastDays);
        forecast.setSeasonFactor(seasonFactor);
        forecast.setSuggestedProductionQty(suggestedQty);
        forecast.setCreatedAt(LocalDateTime.now());
        demandForecastMapper.insert(forecast);

        log.info("[DemandAgent] 需求预测完成 category={} predicted={} confidence={} suggestedQty={}",
                category, predictedSales, confidence, suggestedQty);
        return ctx;
    }

    /**
     * 简单季节因子：旺季（10-12月、1-2月）× 1.5，淡季（6-8月）× 0.7，其他 × 1.0。
     * 生产中可替换为基于真实数据训练的模型。
     */
    private BigDecimal computeSeasonFactor() {
        Month month = LocalDateTime.now().getMonth();
        switch (month) {
            case OCTOBER:
            case NOVEMBER:
            case DECEMBER:
            case JANUARY:
            case FEBRUARY:
                return new BigDecimal("1.5");
            case JUNE:
            case JULY:
            case AUGUST:
                return new BigDecimal("0.7");
            default:
                return BigDecimal.ONE;
        }
    }

}
