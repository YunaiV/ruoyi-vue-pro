package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepaySelectionPoolDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTrendItemDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepaySelectionPoolMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayTrendItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SelectionFeedAgent — 外部趋势款选品注入（Phase 9）。
 *
 * <p>从 {@code deepay_trend_item} 和 {@code deepay_selection_pool} 两表读取热门款式，
 * 合并去重后注入 {@link Context#trendItems} 和 {@link Context#referenceImages}。
 * 同时将外部来源的新 TrendItem 持久化到 {@code deepay_selection_pool}，供下次使用。</p>
 */
@Component
public class SelectionFeedAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(SelectionFeedAgent.class);

    private static final int TOP_N = 10;

    @Resource
    private DeepayTrendItemMapper trendItemMapper;

    @Resource
    private DeepaySelectionPoolMapper selectionPoolMapper;

    @Override
    public Context run(Context ctx) {
        log.info("[SelectionFeedAgent] START category={}", ctx.category);

        // 1. Read from deepay_trend_item
        List<TrendItem> fromTrendItem = fetchFromTrendItemTable(ctx.category);

        // 2. Read from deepay_selection_pool
        List<TrendItem> fromSelectionPool = fetchFromSelectionPool(ctx.category);

        // 3. Merge and deduplicate by imageUrl
        Map<String, TrendItem> merged = new LinkedHashMap<>();
        for (TrendItem item : fromSelectionPool) {
            if (StringUtils.hasText(item.getImageUrl())) {
                merged.put(item.getImageUrl(), item);
            }
        }
        for (TrendItem item : fromTrendItem) {
            if (StringUtils.hasText(item.getImageUrl())) {
                merged.putIfAbsent(item.getImageUrl(), item);
            }
        }

        List<TrendItem> combined = new ArrayList<>(merged.values());

        if (combined.isEmpty()) {
            log.info("[SelectionFeedAgent] 无外部趋势款数据，跳过注入 category={}", ctx.category);
            return ctx;
        }

        // 4. Inject into ctx.trendItems
        if (ctx.trendItems == null) {
            ctx.trendItems = new ArrayList<>();
        }
        ctx.trendItems.addAll(combined);

        // 5. Append image URLs to ctx.referenceImages
        List<String> imageUrls = combined.stream()
                .map(TrendItem::getImageUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        if (!imageUrls.isEmpty()) {
            if (ctx.referenceImages == null) {
                ctx.referenceImages = new ArrayList<>();
            }
            ctx.referenceImages.addAll(imageUrls);
        }

        // 6. Persist external TrendItems from ctx back to deepay_selection_pool
        persistExternalItems(combined);

        log.info("[SelectionFeedAgent] DONE 注入趋势款 count={} category={}", combined.size(), ctx.category);
        return ctx;
    }

    private List<TrendItem> fetchFromTrendItemTable(String category) {
        try {
            List<DeepayTrendItemDO> items = trendItemMapper.selectTopByCategory(category, TOP_N);
            return items.stream().map(do_ -> {
                TrendItem item = new TrendItem();
                item.setImageUrl(do_.getImageUrl());
                item.setCategory(do_.getCategory());
                item.setStyle(do_.getStyle());
                item.setSoldCount(do_.getHeatScore() != null ? do_.getHeatScore() : 0);
                item.setChainCode("ext-" + do_.getSource() + "-" + do_.getId());
                item.setSource(do_.getSource());
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[SelectionFeedAgent] 查询 deepay_trend_item 异常，跳过", e);
            return java.util.Collections.emptyList();
        }
    }

    private List<TrendItem> fetchFromSelectionPool(String category) {
        try {
            List<DeepaySelectionPoolDO> items = selectionPoolMapper.selectTopByCategory(category, TOP_N);
            return items.stream().map(do_ -> {
                TrendItem item = new TrendItem();
                item.setImageUrl(do_.getImageUrl());
                item.setCategory(do_.getCategory());
                item.setStyle(do_.getStyle());
                item.setSource(do_.getSource());
                item.setScore(do_.getScore() != null ? do_.getScore() : 0.0);
                item.setChainCode("pool-" + do_.getId());
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[SelectionFeedAgent] 查询 deepay_selection_pool 异常，跳过", e);
            return java.util.Collections.emptyList();
        }
    }

    private void persistExternalItems(List<TrendItem> items) {
        for (TrendItem item : items) {
            if ("internal".equals(item.getSource())) continue;
            try {
                DeepaySelectionPoolDO pool = new DeepaySelectionPoolDO();
                pool.setImageUrl(item.getImageUrl());
                pool.setCategory(item.getCategory());
                pool.setStyle(item.getStyle());
                pool.setSource(item.getSource());
                pool.setScore(resolveScore(item));
                pool.setCreatedAt(LocalDateTime.now());
                selectionPoolMapper.insert(pool);
            } catch (Exception e) {
                log.debug("[SelectionFeedAgent] 持久化 selection_pool 跳过（可能已存在）: {}", item.getImageUrl());
            }
        }
    }

    private double resolveScore(TrendItem item) {
        return item.getScore() > 0 ? item.getScore() : (double) (item.getSoldCount() != null ? item.getSoldCount() : 0);
    }
}
