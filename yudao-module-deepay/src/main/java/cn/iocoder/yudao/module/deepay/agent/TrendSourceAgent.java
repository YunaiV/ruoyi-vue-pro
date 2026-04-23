package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TrendSourceAgent — 内部趋势图源（Phase 6 任务 4）。
 *
 * <p>不接外部 API，直接从 {@code deepay_metrics} 中读取过去 7 天 sold_count 最高的商品，
 * 将其图片 URL 和品类关键词写入 {@link Context#trendImages} / {@link Context#trendKeywords}。</p>
 *
 * <p>查询逻辑：
 * <pre>
 *   SELECT chain_code, category, sold_count
 *   FROM deepay_metrics
 *   WHERE created_at &gt;= NOW() - 7 DAYS
 *     AND category = ctx.category   -- 限定品类（有 category 时），保证趋势与客户行业一致
 *   ORDER BY sold_count DESC
 *   LIMIT 5
 * </pre>
 * 通过 deepay_product.cdn_image_url 关联图片；无关联时使用 chainCode 作为占位标识符。</p>
 *
 * <p>Fallback：若 7 天内无数据，使用全量历史 Top 5；仍无数据则使用内置保底关键词。</p>
 */
@Component
public class TrendSourceAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(TrendSourceAgent.class);

    /** 取趋势商品的数量 */
    private static final int TOP_N = 5;

    /** 保底关键词（无历史数据时使用） */
    private static final List<String> FALLBACK_KEYWORDS = Arrays.asList(
            "极简外套", "时尚连衣裙", "休闲夹克", "针织毛衣", "商务西装"
    );

    @Resource
    private DeepayMetricsMapper deepayMetricsMapper;

    @Override
    public Context run(Context ctx) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 1. 优先查同品类近 7 天热销
        List<DeepayMetricsDO> hotItems = queryHotItems(ctx.category, sevenDaysAgo, TOP_N);

        // 2. 无品类热销数据 → 查全品类近 7 天热销
        if (hotItems.isEmpty()) {
            log.info("[TrendSourceAgent] 品类[{}] 近7天无热销数据，查全品类", ctx.category);
            hotItems = queryHotItems(null, sevenDaysAgo, TOP_N);
        }

        // 3. 仍无数据 → 查历史全量热销
        if (hotItems.isEmpty()) {
            log.info("[TrendSourceAgent] 近7天全品类无数据，查历史全量 Top{}", TOP_N);
            hotItems = queryHotItemsAllTime(ctx.category, TOP_N);
        }

        if (!hotItems.isEmpty()) {
            // 从 chain_code 推导图片 URL（深度依赖 CDN URL 存储在 product 表，此处用 chainCode 作引用）
            ctx.trendImages   = hotItems.stream()
                    .map(m -> "deepay://trend/" + m.getChainCode())  // 格式化标识，CDN 层在 DesignAgent 中解析
                    .collect(Collectors.toList());
            ctx.trendKeywords = hotItems.stream()
                    .filter(m -> StringUtils.hasText(m.getCategory()))
                    .map(DeepayMetricsDO::getCategory)
                    .distinct()
                    .collect(Collectors.toList());
            log.info("[TrendSourceAgent] 趋势来源：内部热销，count={} category={}",
                    hotItems.size(), ctx.category);
        } else {
            // 保底关键词
            ctx.trendImages   = Collections.emptyList();
            ctx.trendKeywords = new ArrayList<>(FALLBACK_KEYWORDS);
            log.info("[TrendSourceAgent] 使用保底关键词，count={}", ctx.trendKeywords.size());
        }

        // 将趋势关键词补充到 keyword（DesignAgent 会用到）
        if (StringUtils.hasText(ctx.category)) {
            if (!StringUtils.hasText(ctx.keyword)) {
                ctx.keyword = ctx.category;
            }
        } else if (!ctx.trendKeywords.isEmpty()) {
            if (!StringUtils.hasText(ctx.keyword)) {
                ctx.keyword = ctx.trendKeywords.get(0);
            }
        }

        return ctx;
    }

    // ----------------------------------------------------------------

    private List<DeepayMetricsDO> queryHotItems(String category, LocalDateTime since, int limit) {
        LambdaQueryWrapper<DeepayMetricsDO> qw = new LambdaQueryWrapper<DeepayMetricsDO>()
                .ge(DeepayMetricsDO::getCreatedAt, since)
                .isNotNull(DeepayMetricsDO::getSoldCount)
                .orderByDesc(DeepayMetricsDO::getSoldCount)
                .last("LIMIT " + limit);

        if (StringUtils.hasText(category)) {
            qw.eq(DeepayMetricsDO::getCategory, category);
        }
        try {
            return deepayMetricsMapper.selectList(qw);
        } catch (Exception e) {
            log.warn("[TrendSourceAgent] 查询热销商品异常", e);
            return Collections.emptyList();
        }
    }

    private List<DeepayMetricsDO> queryHotItemsAllTime(String category, int limit) {
        LambdaQueryWrapper<DeepayMetricsDO> qw = new LambdaQueryWrapper<DeepayMetricsDO>()
                .isNotNull(DeepayMetricsDO::getSoldCount)
                .orderByDesc(DeepayMetricsDO::getSoldCount)
                .last("LIMIT " + limit);

        if (StringUtils.hasText(category)) {
            qw.eq(DeepayMetricsDO::getCategory, category);
        }
        try {
            return deepayMetricsMapper.selectList(qw);
        } catch (Exception e) {
            log.warn("[TrendSourceAgent] 查询历史全量热销异常", e);
            return Collections.emptyList();
        }
    }

}
