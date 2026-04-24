package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

/**
 * 自然语言解析服务 — 调用 LLM（DeepSeek / OpenAI 兼容接口），
 * 将用户一句话映射到 {@link Context} 的结构化字段。
 *
 * <p>配置项（application.yml）：
 * <pre>
 * deepay:
 *   nlp:
 *     api-url:   https://api.deepseek.com/v1/chat/completions  # 留空则不调用LLM
 *     api-key:   sk-xxxx
 *     model:     deepseek-chat
 *     timeout-seconds: 15
 * </pre>
 * </p>
 *
 * <p>当 api-url 未配置时退化为关键词规则匹配，确保不依赖外部服务也能运行。</p>
 */
@Slf4j
@Service
public class NlpParserService {

    /** LLM 系统 Prompt —— 严格指定输出格式 */
    private static final String SYSTEM_PROMPT =
            "你是一个服装电商助手，请从用户输入中提取结构化信息，以 JSON 格式返回。\n" +
            "字段说明：\n" +
            "  category   - 品类，如 外套/裤子/上衣/连衣裙/内裤（若提到则填写，否则 null）\n" +
            "  style      - 风格，如 工装/极简/性感/休闲/轻奢/运动（若提到则填写，否则 null）\n" +
            "  crowd      - 客群，如 男装/少女/中老年/运动（若提到则填写，否则 null）\n" +
            "  market     - 市场，CN/EU/US/ME（若提到则填写，否则 null）\n" +
            "  priceLevel - 价位，LOW/MID/HIGH（若提到则填写，否则 null）\n" +
            "  purpose    - 用途，WHOLESALE/RETAIL（若提到则填写，否则 null）\n" +
            "  color      - 颜色关键词（若提到则填写，否则 null）\n" +
            "  keyword    - 整句中最核心的商品词（必填）\n" +
            "只返回 JSON，不要任何解释。示例：\n" +
            "{\"category\":\"外套\",\"style\":\"极简\",\"crowd\":null,\"market\":\"EU\"," +
            "\"priceLevel\":\"MID\",\"purpose\":null,\"color\":\"蓝色\",\"keyword\":\"外套\"}";

    @Value("${deepay.nlp.api-url:}")
    private String apiUrl;

    @Value("${deepay.nlp.api-key:}")
    private String apiKey;

    @Value("${deepay.nlp.model:deepseek-chat}")
    private String model;

    @Value("${deepay.nlp.timeout-seconds:15}")
    private int timeoutSeconds;

    private RestTemplate restTemplate;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @PostConstruct
    private void init() {
        restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    // ====================================================================
    // 主入口
    // ====================================================================

    /**
     * 解析用户自然语言，将提取到的字段合并到 ctx（只覆盖原本为空的字段）。
     *
     * @param userMessage 用户输入的一句话
     * @param ctx         当前 Context（仅填充空字段，不覆盖已有值）
     * @return 填充后的 ctx（同一对象，便于链式调用）
     */
    public Context parse(String userMessage, Context ctx) {
        if (!StringUtils.hasText(userMessage)) {
            return ctx;
        }

        Map<String, String> parsed;
        if (StringUtils.hasText(apiUrl) && StringUtils.hasText(apiKey)) {
            parsed = callLlm(userMessage);
        } else {
            log.debug("[NlpParser] apiUrl 未配置，使用规则匹配");
            parsed = ruleBased(userMessage);
        }

        // 只覆盖原本为空的字段
        if (parsed == null) {
            return ctx;
        }

        if (!StringUtils.hasText(ctx.category)    && has(parsed, "category"))    ctx.category    = parsed.get("category");
        if (!StringUtils.hasText(ctx.style)        && has(parsed, "style"))        ctx.style        = parsed.get("style");
        if (!StringUtils.hasText(ctx.crowd)        && has(parsed, "crowd"))        ctx.crowd        = parsed.get("crowd");
        if (!StringUtils.hasText(ctx.market)       && has(parsed, "market"))       ctx.market       = parsed.get("market");
        if (!StringUtils.hasText(ctx.priceLevel)   && has(parsed, "priceLevel"))   ctx.priceLevel   = parsed.get("priceLevel");
        if (!StringUtils.hasText(ctx.purpose)      && has(parsed, "purpose"))      ctx.purpose      = parsed.get("purpose");
        if (!StringUtils.hasText(ctx.keyword)      && has(parsed, "keyword"))      ctx.keyword      = parsed.get("keyword");

        // keyword 与 category 同步（保底）
        if (!StringUtils.hasText(ctx.keyword) && StringUtils.hasText(ctx.category)) {
            ctx.keyword = ctx.category;
        }

        log.info("[NlpParser] 解析完成 message='{}' → category={} style={} crowd={} market={}",
                userMessage, ctx.category, ctx.style, ctx.crowd, ctx.market);
        return ctx;
    }

    // ====================================================================
    // LLM 调用
    // ====================================================================

    private Map<String, String> callLlm(String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(messageOf("system", SYSTEM_PROMPT));
            messages.add(messageOf("user", userMessage));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("max_tokens", 200);
            body.put("temperature", 0.0);

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(apiUrl, req, String.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                log.warn("[NlpParser] LLM 返回非 2xx: {}", resp.getStatusCode());
                return ruleBased(userMessage);
            }

            JsonNode root    = MAPPER.readTree(resp.getBody());
            String   content = root.path("choices").path(0)
                                   .path("message").path("content").asText("");

            // 提取 JSON（LLM 可能在 JSON 外包裹 markdown ```json ... ```）
            content = extractJson(content);
            if (!StringUtils.hasText(content)) {
                return ruleBased(userMessage);
            }

            JsonNode json = MAPPER.readTree(content);
            Map<String, String> result = new LinkedHashMap<>();
            json.fields().forEachRemaining(e -> {
                if (!e.getValue().isNull()) {
                    result.put(e.getKey(), e.getValue().asText());
                }
            });
            return result;

        } catch (Exception e) {
            log.warn("[NlpParser] LLM 调用失败，降级为规则匹配", e);
            return ruleBased(userMessage);
        }
    }

    private static Map<String, String> messageOf(String role, String content) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    /** 从 LLM 返回文本中提取第一个 JSON 对象（去除 markdown 包裹）。 */
    private static String extractJson(String text) {
        if (!StringUtils.hasText(text)) return null;
        int start = text.indexOf('{');
        int end   = text.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        return text.substring(start, end + 1);
    }

    // ====================================================================
    // 规则匹配（LLM 不可用时的降级策略）
    // ====================================================================

    private static Map<String, String> ruleBased(String msg) {
        Map<String, String> result = new LinkedHashMap<>();
        String lower = msg.toLowerCase();

        // 品类
        for (String cat : new String[]{"外套", "大衣", "裤子", "上衣", "T恤", "连衣裙", "裙子", "内裤", "内衣"}) {
            if (msg.contains(cat)) { result.put("category", cat); break; }
        }
        // 风格
        for (String s : new String[]{"工装", "极简", "性感", "休闲", "轻奢", "运动", "街头"}) {
            if (msg.contains(s)) { result.put("style", s); break; }
        }
        // 客群
        if (msg.contains("男") || lower.contains("man"))    result.put("crowd", "男装");
        else if (msg.contains("少女") || msg.contains("女"))   result.put("crowd", "少女");
        else if (msg.contains("老年") || msg.contains("中老")) result.put("crowd", "中老年");
        else if (msg.contains("运动"))                         result.put("crowd", "运动");
        // 市场
        if      (msg.contains("欧") || lower.contains("eu"))       result.put("market", "EU");
        else if (msg.contains("美") || lower.contains("us"))       result.put("market", "US");
        else if (msg.contains("中东") || lower.contains("me"))     result.put("market", "ME");
        else if (msg.contains("国内") || msg.contains("中国") || lower.contains("cn")) result.put("market", "CN");
        // 价位
        if      (msg.contains("高端") || msg.contains("轻奢") || msg.contains("奢")) result.put("priceLevel", "HIGH");
        else if (msg.contains("低端") || msg.contains("便宜"))                       result.put("priceLevel", "LOW");
        else if (msg.contains("中端") || msg.contains("中档"))                       result.put("priceLevel", "MID");
        // 用途
        if      (msg.contains("批发"))  result.put("purpose", "WHOLESALE");
        else if (msg.contains("零售"))  result.put("purpose", "RETAIL");
        // keyword（优先 category，其次整句前 10 字）
        String kw = result.containsKey("category")
                ? result.get("category")
                : (msg.length() > 10 ? msg.substring(0, 10) : msg);
        result.put("keyword", kw);

        return result;
    }

    private static boolean has(Map<String, String> m, String key) {
        return m != null && StringUtils.hasText(m.get(key));
    }

}
