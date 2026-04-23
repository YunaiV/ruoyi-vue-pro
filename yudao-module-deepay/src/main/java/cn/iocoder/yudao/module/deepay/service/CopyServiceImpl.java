package cn.iocoder.yudao.module.deepay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品文案生成服务实现（OpenAI-compatible chat completions）。
 *
 * <p>配置项（application.yml）：
 * <pre>
 * deepay:
 *   copy:
 *     api-url:  https://api.openai.com/v1/chat/completions   # 留空则使用模板字符串
 *     api-key:  your-api-key
 *     model:    gpt-4o-mini                                   # 默认 gpt-4o-mini
 * </pre>
 * </p>
 */
@Slf4j
@Service
public class CopyServiceImpl implements CopyService {

    @Value("${deepay.copy.api-url:}")
    private String apiUrl;

    @Value("${deepay.copy.api-key:}")
    private String apiKey;

    @Value("${deepay.copy.model:gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate;

    public CopyServiceImpl(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public String generateTitle(String keyword) {
        String prompt = "为一件「" + keyword + "」风格的服装写一个电商商品标题，不超过 20 字，只输出标题本身，不要加引号和解释。";
        String fallback = keyword + " 限量款";
        return callChatApi(prompt, fallback);
    }

    @Override
    public String generateDescription(String keyword) {
        String prompt = "为一件「" + keyword + "」风格的服装写 2～3 句电商卖点描述，突出设计感和品质感，不超过 80 字，只输出描述本身，不要加引号和解释。";
        String fallback = "由 AI 设计，独家限量发售。关键词：" + keyword;
        return callChatApi(prompt, fallback);
    }

    /**
     * 调用 OpenAI-compatible chat completions API。
     * 任何异常均捕获并返回 fallback 文本。
     *
     * @param userPrompt 发送给模型的用户消息
     * @param fallback   API 不可用时的降级文本
     * @return 模型生成的文本，或降级文本
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String callChatApi(String userPrompt, String fallback) {
        if (!StringUtils.hasText(apiUrl)) {
            log.info("[CopyService] API URL 未配置，使用模板文案。prompt={}", userPrompt);
            return fallback;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(apiKey)) {
                headers.setBearerAuth(apiKey);
            }

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", userPrompt);
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", Collections.singletonList(message));
            body.put("max_tokens", 100);
            body.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

            String content = extractContent(response);
            if (content != null) {
                log.info("[CopyService] AI 文案生成成功，长度={}", content.length());
                return content.trim();
            }
        } catch (Exception e) {
            log.warn("[CopyService] AI 文案生成失败，降级为模板文案。prompt={}", userPrompt, e);
        }

        return fallback;
    }

    /** 从 OpenAI-compatible 响应中提取 choices[0].message.content。 */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String extractContent(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        Object choicesRaw = response.get("choices");
        if (!(choicesRaw instanceof List)) {
            return null;
        }
        List<?> choices = (List<?>) choicesRaw;
        if (choices.isEmpty()) {
            return null;
        }
        Object first = choices.get(0);
        if (!(first instanceof Map)) {
            return null;
        }
        Object messageRaw = ((Map<?, ?>) first).get("message");
        if (!(messageRaw instanceof Map)) {
            return null;
        }
        Object content = ((Map<?, ?>) messageRaw).get("content");
        return content instanceof String ? (String) content : null;
    }

}
