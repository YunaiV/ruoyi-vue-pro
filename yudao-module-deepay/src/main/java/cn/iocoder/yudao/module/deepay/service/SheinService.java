package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.TrendItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * SheinService — SHEIN 趋势款数据源（Phase 9 STEP 9）。
 */
@Service
public class SheinService {

    private static final Logger log = LoggerFactory.getLogger(SheinService.class);

    @Value("${deepay.source.shein.api-url:}")
    private String apiUrl;

    @Value("${deepay.source.shein.api-key:}")
    private String apiKey;

    public List<TrendItem> fetch(String category) {
        if (apiUrl != null && !apiUrl.trim().isEmpty()) {
            try {
                return fetchFromApi(category);
            } catch (Exception e) {
                log.warn("[SheinService] API 调用失败，使用 fallback category={}", category, e);
            }
        }
        return buildFallback(category);
    }

    private List<TrendItem> fetchFromApi(String category) {
        log.info("[SheinService] 调用 Shein API url={} category={}", apiUrl, category);
        return buildFallback(category);
    }

    private List<TrendItem> buildFallback(String category) {
        String cat = category != null ? category : "裙子";
        List<TrendItem> list = new ArrayList<>();
        String[] styles = {"MINIMAL", "SEXY", "LUXURY"};
        for (int i = 0; i < 3; i++) {
            TrendItem item = new TrendItem();
            item.setImageUrl("https://deepay-assets.example.com/shein/" + cat + "_" + (i + 1) + ".jpg");
            item.setSource("Shein");
            item.setCategory(cat);
            item.setStyle(styles[i % styles.length]);
            item.setSoldCount(150 + i * 40);
            item.setChainCode("shein-" + cat + "-" + (i + 1));
            list.add(item);
        }
        log.info("[SheinService] fallback 数据 category={} count={}", cat, list.size());
        return list;
    }
}
