package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.TrendItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * BrandFilterService — 过滤大牌 / 侵权款式（Phase 9 STEP 10）。
 *
 * <p>从趋势款列表中移除图片 URL 或来源含有受限品牌词的条目，
 * 防止系统直接使用大牌版型作为参考输出到客户。</p>
 */
@Component
public class BrandFilterService {

    private static final Logger log = LoggerFactory.getLogger(BrandFilterService.class);

    private static final List<String> BLOCK_LIST = Arrays.asList(
            "nike", "adidas", "gucci", "balenciaga", "lv", "louis vuitton",
            "chanel", "prada", "dior", "hermes", "versace", "burberry",
            "supreme", "off-white", "logo", "trademark", "copyright"
    );

    /**
     * 过滤趋势款列表，移除含受限品牌词的条目。
     *
     * @param list 原始趋势款列表
     * @return 安全款式列表
     */
    public List<TrendItem> filter(List<TrendItem> list) {
        if (list == null) return Collections.emptyList();
        List<TrendItem> filtered = list.stream()
                .filter(item -> !isBlocked(item))
                .collect(Collectors.toList());
        int removed = list.size() - filtered.size();
        if (removed > 0) {
            log.info("[BrandFilterService] 过滤侵权款 {} 条，剩余 {} 条", removed, filtered.size());
        }
        return filtered;
    }

    private boolean isBlocked(TrendItem item) {
        String imageUrl = item.getImageUrl();
        String chainCode = item.getChainCode();
        String combined = ((imageUrl != null ? imageUrl : "") + " " + (chainCode != null ? chainCode : "")).toLowerCase(Locale.ROOT);
        for (String brand : BLOCK_LIST) {
            if (combined.contains(brand)) {
                log.debug("[BrandFilterService] 命中受限词 '{}' item={}", brand, imageUrl);
                return true;
            }
        }
        return false;
    }
}
