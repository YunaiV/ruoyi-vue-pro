package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * AnalyticsAgent — 记录初始指标快照（Phase 4 升级版：含 cost/profit/roi）。
 */
@Component
public class AnalyticsAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsAgent.class);

    @Resource
    private DeepayMetricsMapper deepayMetricsMapper;

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @Override
    public Context run(Context ctx) {
        // 落库指标快照（含 Phase 4 利润字段）
        DeepayMetricsDO metrics = new DeepayMetricsDO();
        metrics.setChainCode(ctx.chainCode);
        metrics.setSoldCount(0);
        metrics.setPrice(ctx.price);
        metrics.setCategory(ctx.keyword);
        metrics.setCostPrice(ctx.costPrice);
        metrics.setProfit(computeProfit(ctx));
        metrics.setRoi(computeRoi(ctx));
        metrics.setViewCount(0);
        metrics.setPayCount(0);
        metrics.setConversionRate(BigDecimal.ZERO);
        metrics.setCreatedAt(LocalDateTime.now());
        deepayMetricsMapper.insert(metrics);

        ctx.soldCount = 0;
        ctx.profit    = metrics.getProfit();
        ctx.roi       = metrics.getRoi();

        // 生成可解释报告
        ctx.analyticsReport = String.format(
                "[初始化] chainCode=%s | keyword=%s | price=%s | cost=%s | profit=%s | roi=%s | stock=%d | action=%s | decision=%s",
                ctx.chainCode, ctx.keyword, ctx.price, ctx.costPrice,
                ctx.profit, ctx.roi, ctx.stock, ctx.action, ctx.decisionReason);

        // 回写 deepay_style_chain（action 可被复盘调度器读取）
        if (ctx.chainCode != null) {
            deepayStyleChainMapper.update(null, new LambdaUpdateWrapper<DeepayStyleChainDO>()
                    .eq(DeepayStyleChainDO::getChainCode, ctx.chainCode)
                    .set(DeepayStyleChainDO::getStatus, "PUBLISHED"));
        }

        log.info("AnalyticsAgent: 初始快照完成 chainCode={} profit={} roi={}",
                ctx.chainCode, ctx.profit, ctx.roi);
        return ctx;
    }

    // ----------------------------------------------------------------

    private BigDecimal computeProfit(Context ctx) {
        if (ctx.price == null || ctx.costPrice == null) {
            return null;
        }
        return ctx.price.subtract(ctx.costPrice).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeRoi(Context ctx) {
        BigDecimal profit = computeProfit(ctx);
        if (profit == null || ctx.costPrice == null || ctx.costPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return profit.divide(ctx.costPrice, 4, RoundingMode.HALF_UP);
    }

}

