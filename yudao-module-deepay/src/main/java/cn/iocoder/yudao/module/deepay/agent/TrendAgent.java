package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.service.BrandFilterService;
import cn.iocoder.yudao.module.deepay.service.SheinService;
import cn.iocoder.yudao.module.deepay.service.Spider1688Service;
import cn.iocoder.yudao.module.deepay.service.TikTokTrendService;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TrendAgent — 多源趋势款聚合（Phase 9 STEP 9 升级版）。
 *
 * <p>数据源优先级：
 * <ol>
 *   <li>A类（真实爆款）：1688 / TikTok / Shein</li>
 *   <li>S类（内部热销）：deepay_metrics + deepay_product</li>
 *   <li>B类（兜底）：品类专属默认图</li>
 * </ol>
 * </p>
 *
 * <p>评分规则（STEP 9）：
 * <ul>
 *   <li>TikTok 来源 +40</li>
 *   <li>Shein 来源 +35</li>
 *   <li>1688 来源 +30</li>
 *   <li>风格与 ctx.stylePreference 匹配 +30</li>
 * </ul>
 * </p>
 */
@Component
public class TrendAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(TrendAgent.class);

    @Resource private Spider1688Service    spider1688Service;
    @Resource private TikTokTrendService   tiktokService;
    @Resource private SheinService         sheinService;
    @Resource private BrandFilterService   brandFilterService;
    @Resource private DeepayProductMapper  productMapper;

    @Override
    public Context run(Context ctx) {
        log.info("[TrendAgent] START category={} stylePreference={}", ctx.category, ctx.stylePreference);

        List<TrendItem> all = new ArrayList<>();

        // ---- A类：外部真实爆款 ----
        try { all.addAll(spider1688Service.fetch(ctx.category)); } catch (Exception e) { log.warn("[TrendAgent] 1688 fetch 异常", e); }
        try { all.addAll(tiktokService.fetch(ctx.category));     } catch (Exception e) { log.warn("[TrendAgent] TikTok fetch 异常", e); }
        try { all.addAll(sheinService.fetch(ctx.category));      } catch (Exception e) { log.warn("[TrendAgent] Shein fetch 异常", e); }

        // ---- S类：内部热销兜底 ----
        if (all.isEmpty()) {
            log.info("[TrendAgent] 外部数据为空，查内部热销 category={}", ctx.category);
            all.addAll(fetchInternalHot(ctx.category));
        }

        // ---- 品牌过滤 ----
        all = brandFilterService.filter(all);

        // ---- 评分 ----
        for (TrendItem item : all) {
            double score = baseScoreBySource(item.getSource());
            // 风格匹配加分
            if (ctx.stylePreference != null && ctx.stylePreference.equals(item.getStyle())) {
                score += 30;
            }
            if (ctx.style != null && ctx.style.equalsIgnoreCase(item.getStyle())) {
                score += 15;
            }
            item.setScore(score);
        }

        // ---- 排序 + 取 Top 20 ----
        all.sort(Comparator.comparingDouble(TrendItem::getScore).reversed());
        ctx.trendItems = all.stream().limit(20).collect(Collectors.toList());

        // ---- 写 referenceImages（供 DesignAgent 消费）----
        ctx.referenceImages = ctx.trendItems.stream()
                .map(TrendItem::getImageUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        // ---- B类兜底：referenceImages 仍为空 ----
        if (ctx.referenceImages.isEmpty()) {
            log.info("[TrendAgent] referenceImages 仍为空，使用品类默认图 category={}", ctx.category);
            ctx.referenceImages = defaultImages(ctx.category);
        }

        ctx.trendImages = ctx.referenceImages;

        // keyword 兜底
        if (!StringUtils.hasText(ctx.keyword) && StringUtils.hasText(ctx.category)) {
            ctx.keyword = ctx.category;
        }

        log.info("[TrendAgent] DONE trendItems={} referenceImages={} category={}",
                ctx.trendItems.size(), ctx.referenceImages.size(), ctx.category);
        return ctx;
    }

    private double baseScoreBySource(String source) {
        if ("TikTok".equals(source)) return 40;
        if ("Shein".equals(source))  return 35;
        if ("1688".equals(source))   return 30;
        return 20;
    }

    private List<TrendItem> fetchInternalHot(String category) {
        try {
            List<DeepayProductDO> list = StringUtils.hasText(category)
                    ? productMapper.selectHotByCategory(category)
                    : Collections.emptyList();
            return list.stream().map(p -> {
                TrendItem item = new TrendItem();
                item.setImageUrl(p.getMainImage());
                item.setSource("internal");
                item.setCategory(p.getCategory());
                item.setStyle(p.getStyle() != null ? p.getStyle() : "");
                item.setSoldCount(p.getSoldCount() != null ? p.getSoldCount() : 0);
                item.setChainCode(p.getChainCode());
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[TrendAgent] 内部热销查询异常", e);
            return Collections.emptyList();
        }
    }

    private List<String> defaultImages(String category) {
        if ("内裤".equals(category) || "内衣".equals(category))
            return Arrays.asList("img/neiku1.jpg", "img/neiku2.jpg", "img/neiku3.jpg");
        if ("外套".equals(category) || "大衣".equals(category))
            return Arrays.asList("img/coat1.jpg", "img/coat2.jpg", "img/coat3.jpg");
        if ("裤子".equals(category))
            return Arrays.asList("img/pants1.jpg", "img/pants2.jpg");
        if ("上衣".equals(category) || "T恤".equals(category))
            return Arrays.asList("img/top1.jpg", "img/top2.jpg");
        if ("连衣裙".equals(category) || "裙子".equals(category))
            return Arrays.asList("img/dress1.jpg", "img/dress2.jpg");
        return Collections.singletonList("img/default1.jpg");
    }
}
