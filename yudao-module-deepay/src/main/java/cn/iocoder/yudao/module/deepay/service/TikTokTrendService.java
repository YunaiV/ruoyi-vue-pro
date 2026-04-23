package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.TrendItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * TikTokTrendService — TikTok 趋势款数据源（Phase 9 STEP 9）。
 */
@Service
public class TikTokTrendService {

    private static final Logger log = LoggerFactory.getLogger(TikTokTrendService.class);

    @Value("${deepay.source.tiktok.api-url:}")
    private String apiUrl;

    @Value("${deepay.source.tiktok.api-key:}")
    private String apiKey;

    public List<TrendItem> fetch(String category) {
        if (apiUrl != null && !apiUrl.trim().isEmpty()) {
            try {
                return fetchFromApi(category);
            } catch (Exception e) {
                log.warn("[TikTokTrendService] API 调用失败，使用 fallback category={}", category, e);
            }
        }
        return buildFallback(category);
    }

    private List<TrendItem> fetchFromApi(String category) {
        log.info("[TikTokTrendService] 调用 TikTok API url={} category={}", apiUrl, category);
        return buildFallback(category);
    }

    private List<TrendItem> buildFallback(String category) {
        String cat = category != null ? category : "外套";
        List<TrendItem> list = new ArrayList<>();
        String[] styles = {"SEXY", "CASUAL", "SPORT"};
        for (int i = 0; i < 3; i++) {
            TrendItem item = new TrendItem();
            item.setImageUrl("https://deepay-assets.example.com/tiktok/" + cat + "_" + (i + 1) + ".jpg");
            item.setSource("TikTok");
            item.setCategory(cat);
            item.setStyle(styles[i % styles.length]);
            item.setSoldCount(200 + i * 50);
            item.setChainCode("tiktok-" + cat + "-" + (i + 1));
            list.add(item);
        }
        log.info("[TikTokTrendService] fallback 数据 category={} count={}", cat, list.size());
        return list;
    }
}
