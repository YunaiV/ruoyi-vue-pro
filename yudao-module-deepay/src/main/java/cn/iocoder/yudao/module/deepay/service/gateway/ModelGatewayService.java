package cn.iocoder.yudao.module.deepay.service.gateway;

import cn.iocoder.yudao.module.deepay.dal.mongodb.AiModelUsageDocument;
import cn.iocoder.yudao.module.deepay.dal.mongodb.AiModelUsageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * AI 模型网关 — 统一模型调用入口。
 *
 * <h3>功能</h3>
 * <ul>
 *   <li>统一接口：选择模型、温度、超时、重试</li>
 *   <li>多 Provider 支持（OpenAI 兼容格式：deepseek / moonshot / zhipu / openai）</li>
 *   <li>Fallback：首选模型失败时自动切换备用模型</li>
 *   <li>用量统计：token in/out / 耗时 / 成本估算（USD），落库到 {@code ai_model_usage}</li>
 *   <li>与限流/配额体系打通（预留接口）</li>
 * </ul>
 *
 * <h3>配置示例（application.yml）</h3>
 * <pre>
 * deepay:
 *   gateway:
 *     primary-model:   deepseek-chat
 *     primary-api-url: https://api.deepseek.com/v1/chat/completions
 *     primary-api-key: sk-xxx
 *     fallback-model:   moonshot-v1-8k
 *     fallback-api-url: https://api.moonshot.cn/v1/chat/completions
 *     fallback-api-key: sk-yyy
 *     timeout-seconds: 30
 *     max-retries:     2
 * </pre>
 */
@Slf4j
@Service
public class ModelGatewayService {

    // -------------------------------------------------------------------------
    // 成本参考（每 1M token，单位 USD，粗估）
    // -------------------------------------------------------------------------
    private static final Map<String, double[]> COST_TABLE = new LinkedHashMap<>();
    static {
        // [inputPer1M, outputPer1M]
        COST_TABLE.put("deepseek-chat",       new double[]{0.14, 0.28});
        COST_TABLE.put("moonshot-v1-8k",      new double[]{1.60, 1.60});
        COST_TABLE.put("moonshot-v1-32k",     new double[]{3.20, 3.20});
        COST_TABLE.put("gpt-4o-mini",         new double[]{0.15, 0.60});
        COST_TABLE.put("gpt-4o",              new double[]{5.00, 15.00});
        COST_TABLE.put("glm-4",               new double[]{0.10, 0.10});
    }

    @Value("${deepay.gateway.primary-model:deepseek-chat}")
    private String primaryModel;

    @Value("${deepay.gateway.primary-api-url:}")
    private String primaryApiUrl;

    @Value("${deepay.gateway.primary-api-key:}")
    private String primaryApiKey;

    @Value("${deepay.gateway.fallback-model:moonshot-v1-8k}")
    private String fallbackModel;

    @Value("${deepay.gateway.fallback-api-url:}")
    private String fallbackApiUrl;

    @Value("${deepay.gateway.fallback-api-key:}")
    private String fallbackApiKey;

    @Value("${deepay.gateway.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${deepay.gateway.max-retries:2}")
    private int maxRetries;

    @Resource
    private AiModelUsageRepository usageRepo;

    private RestTemplate restTemplate;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        log.info("[ModelGateway] 初始化 primaryModel={} fallbackModel={}", primaryModel, fallbackModel);
    }

    // =========================================================================
    // 主调用入口
    // =========================================================================

    /**
     * 调用模型（自动 fallback，自动记录用量）。
     *
     * @param req 调用请求
     * @return 模型回复
     */
    public GatewayResponse chat(GatewayRequest req) {
        // 1. 尝试主模型
        String model   = StringUtils.hasText(req.getModel()) ? req.getModel() : primaryModel;
        String apiUrl  = StringUtils.hasText(req.getApiUrl()) ? req.getApiUrl() : primaryApiUrl;
        String apiKey  = StringUtils.hasText(req.getApiKey()) ? req.getApiKey() : primaryApiKey;

        Exception lastError = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return doCall(req, model, apiUrl, apiKey, false);
            } catch (Exception e) {
                lastError = e;
                log.warn("[ModelGateway] 主模型调用失败 attempt={} model={} error={}", attempt, model, e.getMessage());
            }
        }

        // 2. 主模型失败，尝试 fallback
        if (StringUtils.hasText(fallbackApiUrl) && StringUtils.hasText(fallbackApiKey)) {
            log.warn("[ModelGateway] 切换 fallback model={}", fallbackModel);
            try {
                return doCall(req, fallbackModel, fallbackApiUrl, fallbackApiKey, true);
            } catch (Exception e) {
                log.error("[ModelGateway] fallback 也失败 model={}", fallbackModel, e);
                recordError(req, fallbackModel, fallbackApiUrl, true, e.getMessage());
                throw new ModelGatewayException("所有模型调用均失败: " + e.getMessage(), e);
            }
        }

        recordError(req, model, apiUrl, false, lastError != null ? lastError.getMessage() : "unknown");
        throw new ModelGatewayException("模型调用失败: " + (lastError != null ? lastError.getMessage() : "unknown"),
                lastError);
    }

    // =========================================================================
    // 内部
    // =========================================================================

    private GatewayResponse doCall(GatewayRequest req, String model, String apiUrl,
                                   String apiKey, boolean isFallback) {
        if (!StringUtils.hasText(apiUrl) || !StringUtils.hasText(apiKey)) {
            throw new ModelGatewayException("模型 API 未配置 (url/key 为空): model=" + model);
        }
        long start = System.currentTimeMillis();

        // 构建请求体
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", req.getMessages());
        body.put("temperature", req.getTemperature() > 0 ? req.getTemperature() : 0.7);
        if (req.getMaxTokens() > 0) {
            body.put("max_tokens", req.getMaxTokens());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        long latencyMs = System.currentTimeMillis() - start;

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new ModelGatewayException("模型返回非 2xx: " + resp.getStatusCode());
        }

        // 解析响应
        String content = "";
        int tokensIn = 0, tokensOut = 0;
        try {
            JsonNode root = MAPPER.readTree(resp.getBody());
            content   = root.path("choices").get(0).path("message").path("content").asText("");
            tokensIn  = root.path("usage").path("prompt_tokens").asInt(0);
            tokensOut = root.path("usage").path("completion_tokens").asInt(0);
        } catch (Exception e) {
            log.warn("[ModelGateway] 响应解析失败 model={}", model, e);
        }

        // 记录用量
        double cost = estimateCost(model, tokensIn, tokensOut);
        recordUsage(req, model, tokensIn, tokensOut, latencyMs, cost, isFallback, null);

        log.info("[ModelGateway] 调用成功 model={} fallback={} tokensIn={} tokensOut={} latency={}ms cost=${}",
                model, isFallback, tokensIn, tokensOut, latencyMs, String.format("%.6f", cost));

        return GatewayResponse.builder()
                .content(content)
                .model(model)
                .provider(resolveProvider(model))
                .tokensIn(tokensIn)
                .tokensOut(tokensOut)
                .latencyMs(latencyMs)
                .estimatedCostUsd(cost)
                .fallback(isFallback)
                .build();
    }

    private void recordError(GatewayRequest req, String model, String apiUrl,
                             boolean isFallback, String error) {
        recordUsage(req, model, 0, 0, 0, 0, isFallback, error);
    }

    @Async("deepayAsyncExecutor")
    public void recordUsage(GatewayRequest req, String model, int tokensIn, int tokensOut,
                             long latencyMs, double cost, boolean fallback, String error) {
        try {
            AiModelUsageDocument usage = AiModelUsageDocument.builder()
                    .tenantId(req.getTenantId())
                    .customerId(req.getCustomerId())
                    .sessionId(req.getSessionId())
                    .module(req.getModule())
                    .model(model)
                    .provider(resolveProvider(model))
                    .tokensIn(tokensIn)
                    .tokensOut(tokensOut)
                    .tokensTotal(tokensIn + tokensOut)
                    .latencyMs(latencyMs)
                    .estimatedCostUsd(cost)
                    .fallback(fallback)
                    .error(error)
                    .createdAt(Instant.now())
                    .build();
            usageRepo.save(usage);
        } catch (Exception e) {
            log.warn("[ModelGateway] 用量记录失败 model={}", model, e);
        }
    }

    private double estimateCost(String model, int tokensIn, int tokensOut) {
        double[] prices = COST_TABLE.getOrDefault(model, new double[]{1.0, 1.0});
        return (tokensIn * prices[0] + tokensOut * prices[1]) / 1_000_000.0;
    }

    private String resolveProvider(String model) {
        if (model == null) return "unknown";
        if (model.startsWith("gpt")) return "openai";
        if (model.startsWith("moonshot")) return "moonshot";
        if (model.startsWith("glm")) return "zhipu";
        if (model.startsWith("deepseek")) return "deepseek";
        if (model.startsWith("spark")) return "spark";
        return "custom";
    }

}
