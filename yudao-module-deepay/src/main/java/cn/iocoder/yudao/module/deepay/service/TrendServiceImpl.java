package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 趋势参考图服务实现。
 *
 * <p>配置项（application.yml）：
 * <pre>
 * deepay:
 *   trend:
 *     api-url:  https://your-trend-api-host/trending   # 留空则跳过外部 API
 *     api-key:  your-api-key
 * </pre>
 * </p>
 *
 * <p>调用优先级：外部 API → 系统内近 7 天热销图 → 内置 fallback。</p>
 */
@Slf4j
@Service
public class TrendServiceImpl implements TrendService {

    /** 内置 fallback 图片（当外部 API 和内部数据均不可用时使用） */
    private static final List<String> FALLBACK_IMAGES = Collections.unmodifiableList(Arrays.asList(
            "https://deepay-assets.example.com/trends/trend_default_v1.jpg",
            "https://deepay-assets.example.com/trends/trend_default_v2.jpg",
            "https://deepay-assets.example.com/trends/trend_default_v3.jpg"
    ));

    /** 内部热销查询返回图片数量上限 */
    private static final int TOP_SELLING_LIMIT = 5;

    @Value("${deepay.trend.api-url:}")
    private String apiUrl;

    @Value("${deepay.trend.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @Resource
    private DeepayMetricsMapper deepayMetricsMapper;

    public TrendServiceImpl(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public List<String> getReferenceImages(String keyword) {
        // 优先级 1：外部趋势 API
        if (StringUtils.hasText(apiUrl)) {
            try {
                List<String> external = callTrendApi(keyword);
                if (external != null && !external.isEmpty()) {
                    log.info("[TrendService] 外部 API 返回 {} 张趋势图，keyword={}", external.size(), keyword);
                    return external;
                }
            } catch (Exception e) {
                log.warn("[TrendService] 外部趋势 API 调用失败，降级为内部热销数据。keyword={}", keyword, e);
            }
        }

        // 优先级 2：系统内近 7 天销量最高的款式图片
        try {
            List<String> hotImages = deepayStyleChainMapper.selectTopSellingImages(TOP_SELLING_LIMIT);
            if (hotImages != null && !hotImages.isEmpty()) {
                log.info("[TrendService] 使用系统内热销图 {} 张，keyword={}", hotImages.size(), keyword);
                return hotImages;
            }
        } catch (Exception e) {
            log.warn("[TrendService] 查询内部热销图失败，降级为内置 fallback。keyword={}", keyword, e);
        }

        // 优先级 3：内置 fallback
        log.info("[TrendService] 无历史热销数据，使用内置 fallback 图片。keyword={}", keyword);
        return FALLBACK_IMAGES;
    }

    @Override
    public double getTrendScore(String category, String style) {
        try {
            Double avg = deepayMetricsMapper.selectAvgSoldCountByCategory(category);
            if (avg == null) {
                return 50.0;
            }
            return Math.min(100.0, avg / 10.0);
        } catch (Exception e) {
            log.warn("[TrendService] getTrendScore 查询失败，返回中性分 50 category={} style={}", category, style, e);
            return 50.0;
        }
    }

    /**
     * 调用外部趋势 API。
     *
     * <p>请求格式（JSON Body）：
     * <pre>{ "keyword": "...", "limit": 5 }</pre>
     * </p>
     * <p>期望响应格式（JSON）：
     * <pre>{ "images": ["url1", "url2", ...] }</pre>
     * </p>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<String> callTrendApi(String keyword) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("keyword", keyword != null ? keyword : "");
        body.put("limit", TOP_SELLING_LIMIT);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

        if (response == null || !response.containsKey("images")) {
            log.warn("[TrendService] 外部 API 响应格式异常，缺少 'images' 字段。");
            return null;
        }

        Object raw = response.get("images");
        if (!(raw instanceof List)) {
            return null;
        }

        List<String> images = ((List<?>) raw).stream()
                .filter(item -> item instanceof String)
                .map(item -> (String) item)
                .collect(java.util.stream.Collectors.toList());

        return images.isEmpty() ? null : images;
    }

}
