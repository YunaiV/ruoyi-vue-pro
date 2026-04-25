package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.service.CircuitBreakerService;
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
import java.util.List;
import java.util.Map;

/**
 * FLUX AI 出图服务实现。
 *
 * <p>职责：
 * <ol>
 *   <li>将用户输入拼接固定的时尚风格模板 prompt。</li>
 *   <li>通过 HTTP POST 调用外部 FLUX（或兼容）文生图 API，返回 2～3 张图片 URL。</li>
 *   <li>任何异常（网络超时、API 报错等）均捕获并返回保底默认图片，保证上游接口不报错。</li>
 * </ol>
 * </p>
 *
 * <p>配置项（application.yml）：
 * <pre>
 * deepay:
 *   flux:
 *     api-url:  https://your-flux-api-host/generate   # 留空则直接使用默认图片
 *     api-key:  your-api-key                           # Bearer Token，留空时不带鉴权头
 * </pre>
 * </p>
 */
@Slf4j
@Service
public class FluxServiceImpl implements FluxService {

    /** 固定 prompt 模板前缀（高时尚风格描述词） */
    private static final String PROMPT_TEMPLATE =
            "Minimalist coat, luxury fabric, studio lighting, high fashion, ";

    /** AI 服务不可用时的保底图片列表 */
    private static final List<String> DEFAULT_IMAGES = Collections.unmodifiableList(Arrays.asList(
            "https://deepay-assets.example.com/defaults/design_default_v1.jpg",
            "https://deepay-assets.example.com/defaults/design_default_v2.jpg",
            "https://deepay-assets.example.com/defaults/design_default_v3.jpg"
    ));

    @Value("${deepay.flux.api-url:}")
    private String apiUrl;

    @Value("${deepay.flux.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @Resource
    private CircuitBreakerService circuitBreakerService;

    public FluxServiceImpl(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public List<String> generateImages(String userPrompt, int n) {
        int clamped = Math.max(1, Math.min(8, n));
        String fullPrompt = buildPrompt(userPrompt);

        if (!StringUtils.hasText(apiUrl)) {
            log.info("[FluxService] API URL 未配置，使用默认保底图片（n={}）。prompt={}", clamped, fullPrompt);
            return buildFallbackList(clamped);
        }

        final int finalClamped = clamped;
        return circuitBreakerService.execute(
                "FluxService",
                () -> callFluxApiN(fullPrompt, finalClamped),
                () -> {
                    log.warn("[FluxService] 熔断器已断开，降级为保底图片 n={} prompt={}", finalClamped, fullPrompt);
                    return buildFallbackList(finalClamped);
                }
        );
    }

    /** 构建 n 个保底图片 URL 列表（循环取 DEFAULT_IMAGES）。 */
    private List<String> buildFallbackList(int count) {
        List<String> result = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(DEFAULT_IMAGES.get(i % DEFAULT_IMAGES.size()));
        }
        return result;
    }

    @Override
    public List<String> generateImages(String userPrompt) {
        String fullPrompt = buildPrompt(userPrompt);

        if (!StringUtils.hasText(apiUrl)) {
            log.info("[FluxService] API URL 未配置，使用默认保底图片。prompt={}", fullPrompt);
            return DEFAULT_IMAGES;
        }

        // 通过熔断器调用 FLUX API：失败率 > 50% 自动熔断，30s 后自动恢复探测
        return circuitBreakerService.execute(
                "FluxService",
                () -> callFluxApi(fullPrompt),
                () -> {
                    log.warn("[FluxService] 熔断器已断开，降级为保底图片 prompt={}", fullPrompt);
                    return DEFAULT_IMAGES;
                }
        );
    }

    /**
     * 拼接固定模板前缀与用户输入，生成最终发送给 AI 的 prompt。
     *
     * @param userPrompt 用户输入（可为 null 或空）
     * @return 完整 prompt 字符串
     */
    private String buildPrompt(String userPrompt) {
        if (!StringUtils.hasText(userPrompt)) {
            return PROMPT_TEMPLATE.trim();
        }
        return PROMPT_TEMPLATE + userPrompt.trim();
    }

    /**
     * 调用 FLUX API 获取图片 URL 列表。
     *
     * <p>请求格式（JSON Body）：
     * <pre>{ "prompt": "...", "num_images": 3 }</pre>
     * </p>
     * <p>期望响应格式（JSON）：
     * <pre>{ "images": ["url1", "url2", "url3"] }</pre>
     * </p>
     *
     * @param prompt 完整 prompt
     * @return 图片 URL 列表；若响应解析失败则返回保底图片
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<String> callFluxApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("prompt", prompt);
        body.put("num_images", 3);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

        if (response == null || !response.containsKey("images")) {
            log.warn("[FluxService] API 响应格式异常，缺少 'images' 字段，降级为保底图片。");
            return DEFAULT_IMAGES;
        }

        Object raw = response.get("images");
        if (!(raw instanceof List)) {
            log.warn("[FluxService] API 响应中 'images' 字段类型异常（期望 List），降级为保底图片。");
            return DEFAULT_IMAGES;
        }

        List<?> rawList = (List<?>) raw;
        if (rawList.isEmpty()) {
            log.warn("[FluxService] API 返回图片列表为空，降级为保底图片。");
            return DEFAULT_IMAGES;
        }

        // 过滤并收集有效的字符串 URL
        List<String> images = rawList.stream()
                .filter(item -> item instanceof String)
                .map(item -> (String) item)
                .collect(java.util.stream.Collectors.toList());

        if (images.isEmpty()) {
            log.warn("[FluxService] API 返回图片列表中无有效 URL，降级为保底图片。");
            return DEFAULT_IMAGES;
        }

        return images;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<String> callFluxApiN(String prompt, int n) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            headers.setBearerAuth(apiKey);
        }

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("prompt", prompt);
        body.put("num_images", n);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

        if (response == null || !response.containsKey("images")) {
            log.warn("[FluxService] API 响应格式异常，缺少 'images' 字段，降级为保底图片。n={}", n);
            return buildFallbackList(n);
        }

        Object raw = response.get("images");
        if (!(raw instanceof List)) {
            log.warn("[FluxService] API 响应中 'images' 字段类型异常（期望 List），降级为保底图片。n={}", n);
            return buildFallbackList(n);
        }

        List<?> rawList = (List<?>) raw;
        List<String> images = rawList.stream()
                .filter(item -> item instanceof String)
                .map(item -> (String) item)
                .collect(java.util.stream.Collectors.toList());

        if (images.isEmpty()) {
            log.warn("[FluxService] API 返回图片列表中无有效 URL，降级为保底图片。n={}", n);
            return buildFallbackList(n);
        }

        return images;
    }

}
