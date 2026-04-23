package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignImageDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayFeedbackDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignImageMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayFeedbackMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * FeedbackAgent — 记录用户选图反馈并更新图片评分（Phase 8 升级版 STEP 28）。
 *
 * <p>升级版评分公式（三因子）：
 * <pre>
 *   score = clickRate * 0.3 + conversionRate * 0.5 + freshness * 0.2
 *
 *   clickRate      = clickCount / max(1, viewCount)         取值 0~1，×100 归一化
 *   conversionRate = orderCount / max(1, clickCount)        取值 0~1，×100 归一化
 *   freshness      = 1 / (1 + daysSinceCreate)              越新越高，取值 0~1，×100 归一化
 * </pre>
 * </p>
 */
@Component
public class FeedbackAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(FeedbackAgent.class);

    // 三因子权重（STEP 28）
    private static final double CLICK_WEIGHT      = 0.3;
    private static final double CONVERSION_WEIGHT = 0.5;
    private static final double FRESHNESS_WEIGHT  = 0.2;

    @Resource
    private DeepayFeedbackMapper feedbackMapper;

    @Resource
    private DeepayDesignImageMapper designImageMapper;

    @Override
    public Context run(Context ctx) {
        try {
            if (ctx.scoredImages == null || ctx.scoredImages.isEmpty()) {
                log.info("[FeedbackAgent] 无评分图，跳过反馈记录");
                return ctx;
            }

            String userId = resolveUserId(ctx);
            String selectedUrl = ctx.selectedImage;

            for (DesignImage img : ctx.scoredImages) {
                boolean isSelected = StringUtils.hasText(selectedUrl) && selectedUrl.equals(img.getUrl());

                // 1. 每次曝光都递增 view_count
                designImageMapper.incrementViewCount(img.getUrl());

                // 2. 选中时递增 click_count
                if (isSelected) {
                    designImageMapper.incrementClickCount(img.getUrl());
                }

                // 3. 写入反馈记录
                saveFeedback(img.getUrl(), userId, isSelected ? 1 : 0);

                // 4. 按三因子公式重算综合分
                recalculateScore(img.getUrl());
            }

            log.info("[FeedbackAgent] 反馈记录完成 total={} selectedUrl={}",
                    ctx.scoredImages.size(), selectedUrl);
        } catch (Exception e) {
            log.warn("[FeedbackAgent] 反馈记录异常，跳过", e);
        }
        return ctx;
    }

    private void saveFeedback(String imageUrl, String userId, int selected) {
        try {
            DeepayFeedbackDO record = new DeepayFeedbackDO();
            record.setImageUrl(imageUrl);
            record.setUserId(userId);
            record.setSelected(selected);
            record.setCreatedAt(LocalDateTime.now());
            feedbackMapper.insert(record);
        } catch (Exception e) {
            log.warn("[FeedbackAgent] 写入 deepay_feedback 失败 url={}", imageUrl, e);
        }
    }

    /**
     * 按三因子公式重算并更新综合分（STEP 28）。
     *
     * <pre>
     *   score = clickRate * 0.3 + conversionRate * 0.5 + freshness * 0.2
     * </pre>
     */
    private void recalculateScore(String imageUrl) {
        try {
            List<DeepayDesignImageDO> records = designImageMapper.selectList(
                    new LambdaQueryWrapper<DeepayDesignImageDO>()
                            .eq(DeepayDesignImageDO::getUrl, imageUrl)
                            .orderByDesc(DeepayDesignImageDO::getCreatedAt)
                            .last("LIMIT 1"));
            if (records.isEmpty()) return;
            DeepayDesignImageDO rec = records.get(0);

            int viewCount  = rec.getViewCount()  != null ? rec.getViewCount()  : 0;
            int clickCount = rec.getClickCount()  != null ? rec.getClickCount() : 0;
            int orderCount = rec.getOrderCount()  != null ? rec.getOrderCount() : 0;

            double clickRate      = (double) clickCount  / Math.max(1, viewCount);
            double conversionRate = (double) orderCount  / Math.max(1, clickCount);

            long daysSince = rec.getCreatedAt() != null
                    ? ChronoUnit.DAYS.between(rec.getCreatedAt().toLocalDate(), LocalDate.now()) : 0;
            double freshness = 1.0 / (1 + daysSince);

            double newScore = (clickRate * CLICK_WEIGHT
                             + conversionRate * CONVERSION_WEIGHT
                             + freshness * FRESHNESS_WEIGHT) * 100.0;

            designImageMapper.update(null, new LambdaUpdateWrapper<DeepayDesignImageDO>()
                    .eq(DeepayDesignImageDO::getId, rec.getId())
                    .set(DeepayDesignImageDO::getScore, newScore));
        } catch (Exception e) {
            log.warn("[FeedbackAgent] 重算评分失败 url={}", imageUrl, e);
        }
    }

    private String resolveUserId(Context ctx) {
        if (ctx.userId != null)     return String.valueOf(ctx.userId);
        if (ctx.customerId != null) return String.valueOf(ctx.customerId);
        return null;
    }

}
