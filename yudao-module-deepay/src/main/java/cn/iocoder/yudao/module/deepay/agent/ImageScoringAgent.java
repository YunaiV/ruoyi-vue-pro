package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignImageDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignImageMapper;
import cn.iocoder.yudao.module.deepay.service.TrendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ImageScoringAgent — 对设计图进行综合评分并筛选 Top 图片（Phase 8 Step 4）。
 *
 * <p>对 {@link Context#designImages} 中每张图进行评分：
 * <ul>
 *   <li>trendScore = TrendService.getTrendScore(category, stylePreference)</li>
 *   <li>matchScore = category 非空 +50；stylePreference 非空 +50</li>
 *   <li>score = trendScore * 0.5 + matchScore * 0.5</li>
 * </ul>
 * 结果写入 {@link Context#scoredImages}，Top 2 写入 {@link Context#topImages}，
 * 并持久化到 deepay_design_image。
 * </p>
 */
@Component
public class ImageScoringAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(ImageScoringAgent.class);

    private static final double TREND_WEIGHT = 0.5;
    private static final double MATCH_WEIGHT = 0.5;

    @Resource
    private TrendService trendService;

    @Resource
    private DeepayDesignImageMapper designImageMapper;

    @Override
    public Context run(Context ctx) {
        try {
            if (ctx.designImages == null || ctx.designImages.isEmpty()) {
                log.info("[ImageScoringAgent] 无设计图，跳过评分");
                return ctx;
            }

            double trendScore = trendService.getTrendScore(ctx.category, ctx.stylePreference);

            List<DesignImage> scored = new ArrayList<>();
            for (String url : ctx.designImages) {
                DesignImage img = new DesignImage();
                img.setUrl(url);
                img.setCategory(ctx.category);
                img.setStyle(ctx.stylePreference != null ? ctx.stylePreference : ctx.style);
                img.setTrendScore(trendScore);

                double matchScore = 0;
                if (StringUtils.hasText(ctx.category)) {
                    matchScore += 50;
                }
                if (StringUtils.hasText(ctx.stylePreference)) {
                    matchScore += 50;
                }
                img.setMatchScore(matchScore);
                img.setScore(trendScore * TREND_WEIGHT + matchScore * MATCH_WEIGHT);

                scored.add(img);

                // 持久化到 deepay_design_image
                persistDesignImage(img);
            }

            scored.sort(Comparator.comparingDouble(DesignImage::getScore).reversed());

            ctx.scoredImages = scored;
            ctx.topImages = scored.stream().limit(2).collect(Collectors.toList());

            log.info("[ImageScoringAgent] 评分完成 total={} topImages={}", scored.size(), ctx.topImages.size());
        } catch (Exception e) {
            log.warn("[ImageScoringAgent] 评分异常，跳过", e);
        }
        return ctx;
    }

    private void persistDesignImage(DesignImage img) {
        try {
            DeepayDesignImageDO record = new DeepayDesignImageDO();
            record.setUrl(img.getUrl());
            record.setCategory(img.getCategory());
            record.setStyle(img.getStyle());
            record.setScore(img.getScore());
            record.setTrendScore(img.getTrendScore());
            record.setMatchScore(img.getMatchScore());
            record.setCreatedAt(LocalDateTime.now());
            designImageMapper.insert(record);
        } catch (Exception e) {
            log.warn("[ImageScoringAgent] 持久化设计图失败 url={}", img.getUrl(), e);
        }
    }

}
