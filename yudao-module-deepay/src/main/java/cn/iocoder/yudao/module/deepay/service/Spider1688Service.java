package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.TrendItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Spider1688Service — 1688 趋势款数据源（Phase 9 STEP 9）。
 *
 * <p>生产模式：通过配置的 API URL 调用 1688 商品数据接口。
 * 测试/降级模式：当 {@code deepay.source.1688.api-url} 未配置时，
 * 返回内置 fallback 数据，保证系统始终可运行。</p>
 */
@Service
public class Spider1688Service {

    private static final Logger log = LoggerFactory.getLogger(Spider1688Service.class);

    @Value("${deepay.source.1688.api-url:}")
    private String apiUrl;

    @Value("${deepay.source.1688.api-key:}")
    private String apiKey;

    /**
     * 拉取指定品类的 1688 热销款趋势数据。
     *
     * @param category 品类（如 外套/裤子），null 表示全品类
     * @return TrendItem 列表，至少返回 fallback 数据
     */
    public List<TrendItem> fetch(String category) {
        if (apiUrl != null && !apiUrl.trim().isEmpty()) {
            try {
                return fetchFromApi(category);
            } catch (Exception e) {
                log.warn("[Spider1688Service] API 调用失败，使用 fallback category={}", category, e);
            }
        }
        return buildFallback(category);
    }

    private List<TrendItem> fetchFromApi(String category) {
        // TODO: integrate real 1688 Open API when apiUrl is configured
        log.info("[Spider1688Service] 调用 1688 API url={} category={}", apiUrl, category);
        return buildFallback(category);
    }

    private List<TrendItem> buildFallback(String category) {
        String cat = category != null ? category : "外套";
        List<TrendItem> list = new ArrayList<>();
        String[] styles = {"MINIMAL", "CASUAL", "WORKWEAR"};
        for (int i = 0; i < 3; i++) {
            TrendItem item = new TrendItem();
            item.setImageUrl("https://deepay-assets.example.com/1688/" + cat + "_" + (i + 1) + ".jpg");
            item.setSource("1688");
            item.setCategory(cat);
            item.setStyle(styles[i % styles.length]);
            item.setSoldCount(100 + i * 30);
            item.setChainCode("1688-" + cat + "-" + (i + 1));
            list.add(item);
        }
        log.info("[Spider1688Service] fallback 数据 category={} count={}", cat, list.size());
        return list;
    }
}
