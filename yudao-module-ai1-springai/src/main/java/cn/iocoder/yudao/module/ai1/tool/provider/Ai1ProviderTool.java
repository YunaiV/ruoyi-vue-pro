package cn.iocoder.yudao.module.ai1.tool.provider;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai1.framework.ai.core.llm.Ai1LlmModelFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO @AI：感觉整合到 Ai1ProviderServiceImpl 里更好，本质它不是 tool 呀。
/**
 * AI1 Provider API 工具：面向 OpenAI 兼容接口的原始 HTTP 探测
 *
 * 对外提供 {@link #testConnect}（GET /models 优先、回退 POST /chat/completions 的连通探测）
 * 与 {@link #listModels}（拉取 GET /models 返回的模型标识）两个操作，不感知租户、知识库等业务归属
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1ProviderTool {

    /**
     * 附属 Header 的会话占位符：请求时按当前会话编号替换
     */
    public static final String SESSION_PLACEHOLDER = Ai1LlmModelFactory.SESSION_PLACEHOLDER;

    // TODO @AI：使用 hutool httputil 替代；（对包的版本依赖小）
    /**
     * 连通探测 HTTP 客户端（连接超时 5s）
     */
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * 连通探测：GET {baseUrl}/models 优先；失败（认证错误除外）时，回退 POST {baseUrl}/chat/completions
     *
     * @param baseUrl 接口地址
     * @param apiKey  API 密钥
     * @param headers 请求附属 Header（JSON 数组）；包含 {session} 占位符的 Header 会被跳过
     * @return 探测结果
     */
    public ConnectResult testConnect(String baseUrl, String apiKey, String headers) {
        long startTime = System.currentTimeMillis();
        String base = Ai1LlmModelFactory.normalizeBaseUrl(baseUrl);
        List<Map<String, String>> headerList = parseHeaders(headers);

        // 1. 主校验：GET /models（标准 OpenAI 兼容接口）
        HttpOutcome models = request(base + "/models", apiKey, headerList, "GET", null);
        if (models.getHttpCode() == 200) {
            return new ConnectResult(true, 200, "连通正常：GET /models 返回 HTTP 200", startTime);
        }
        if (isAuthFail(models.getHttpCode())) {
            return new ConnectResult(false, models.getHttpCode(),
                    "认证失败：GET /models 返回 HTTP " + models.getHttpCode() + "，请检查 API 密钥", startTime);
        }

        // 2. 回退：POST /chat/completions 最小请求（仅探测服务可达与鉴权，不做真实对话）
        HttpOutcome chat = request(base + "/chat/completions", apiKey, headerList, "POST",
                "{\"model\":\"test\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}");
        if (chat.getHttpCode() == 200) {
            return new ConnectResult(true, 200, "GET /models 不可用（HTTP " + models.getHttpCode()
                    + "），回退 POST /chat/completions 连通正常：HTTP 200", startTime);
        }
        if (isAuthFail(chat.getHttpCode())) {
            return new ConnectResult(false, chat.getHttpCode(),
                    "认证失败：POST /chat/completions 返回 HTTP " + chat.getHttpCode() + "，请检查 API 密钥", startTime);
        }
        if (chat.getHttpCode() > 0) {
            return new ConnectResult(false, chat.getHttpCode(), "服务可达但接口异常：GET /models HTTP " + models.getHttpCode()
                    + "，POST /chat/completions HTTP " + chat.getHttpCode(), startTime);
        }

        // 3. 两次均网络异常：合并报告
        String modelsMessage = StrUtil.isNotBlank(models.getMessage()) ? "GET /models 失败：" + models.getMessage() : "GET /models 无响应";
        String chatMessage = StrUtil.isNotBlank(chat.getMessage())
                ? "POST /chat/completions 失败：" + chat.getMessage() : "POST /chat/completions 无响应";
        return new ConnectResult(false, 0, "连接失败：" + modelsMessage + "；" + chatMessage, startTime);
    }

    // TODO @AI：按照项目的习惯，应该是 getModelList
    /**
     * 拉取远程模型标识列表：GET {baseUrl}/models，解析 data[].id
     *
     * @param baseUrl 接口地址
     * @param apiKey  API 密钥
     * @param headers 请求附属 Header（JSON 数组）
     * @return 模型标识列表；请求或解析失败时返回 null
     */
    public List<String> listModels(String baseUrl, String apiKey, String headers) {
        HttpOutcome outcome = request(Ai1LlmModelFactory.normalizeBaseUrl(baseUrl) + "/models", apiKey, parseHeaders(headers), "GET", null);
        // TODO @AI：200 使用错误码；
        if (outcome.getHttpCode() != 200) {
            log.warn("[listModels][Provider 模型拉取失败，httpCode({}) message({})]", outcome.getHttpCode(), outcome.getMessage());
            return null;
        }
        Map<String, Object> root = JsonUtils.parseMap(outcome.getBody());
        if (root == null) {
            log.warn("[listModels][Provider 模型数据解析失败，body({})]", StrUtil.maxLength(outcome.getBody(), 200));
            return null;
        }
        List<String> modelList = new ArrayList<>();
        // TODO @AI：jdk8 的兼容性，不要使用过高的语法噢；
        // TODO @AI：使用hutool 的 maputil 简化解析；
        if (root.get("data") instanceof List<?> data) {
            for (Object item : data) {
                if (item instanceof Map<?, ?> map && isScalar(map.get("id")) && StrUtil.isNotBlank(String.valueOf(map.get("id")))) {
                    modelList.add(String.valueOf(map.get("id")));
                }
            }
        }
        return modelList;
    }

    // TODO @AI：jdk8 的兼容性，不要使用过高的语法噢；
    // TODO @AI：使用hutool 的 maputil 简化解析；
    /**
     * 解析请求附属 Header 配置：JSON 数组，格式为 [{"key":"...","value":"..."}]
     *
     * @param headersJson JSON 字符串
     * @return Header 列表，每项包含 key、value；为空或格式错误时返回 null
     */
    public static List<Map<String, String>> parseHeaders(String headersJson) {
        List<?> array = JsonUtils.parseObjectQuietly(headersJson, List.class);
        if (array == null) {
            return null;
        }
        List<Map<String, String>> headers = new ArrayList<>();
        for (Object item : array) {
            // 每项必须是对象，且 key 为标量
            if (!(item instanceof Map<?, ?> map) || !isScalar(map.get("key"))) {
                return null;
            }
            Map<String, String> header = new HashMap<>();
            header.put("key", String.valueOf(map.get("key")));
            header.put("value", isScalar(map.get("value")) ? String.valueOf(map.get("value")) : "");
            headers.add(header);
        }
        return headers;
    }


    // TODO @AI：hutool 有类似的？简化下？
    private static boolean isScalar(Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean;
    }

    /**
     * 发起 HTTP 探测请求，网络异常时记录原因；携带静态附属 Header，包含 {session} 占位符的 Header 跳过
     */
    private HttpOutcome request(String url, String apiKey, List<Map<String, String>> headers, String method, String body) {
        HttpOutcome outcome = new HttpOutcome();
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    // TODO @AI：一些 header，看看有没静态枚举可以替代噢；
                    .header("Accept", "application/json");
            if (StrUtil.isNotBlank(apiKey)) {
                builder.header("Authorization", "Bearer " + apiKey);
            }
            if (CollUtil.isNotEmpty(headers)) {
                for (Map<String, String> header : headers) {
                    String key = header.get("key");
                    String value = header.get("value");
                    if (StrUtil.isBlank(key) || StrUtil.contains(value, SESSION_PLACEHOLDER)) {
                        continue;
                    }
                    builder.header(key.trim(), StrUtil.nullToEmpty(value));
                }
            }
            if ("POST".equals(method)) {
                builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
            } else {
                builder.GET();
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            outcome.setHttpCode(response.statusCode());
            outcome.setBody(response.body());
        } catch (Exception e) {
            outcome.setMessage(e.getMessage());
        }
        return outcome;
    }

    /**
     * 认证失败状态码判断
     */
    private static boolean isAuthFail(int httpCode) {
        // TODO @AI：401、403 使用枚举；
        return httpCode == 401 || httpCode == 403;
    }

    /**
     * HTTP 探测结果
     */
    @Data
    private static class HttpOutcome {

        /**
         * HTTP 状态码，0 表示网络异常未收到响应
         */
        private int httpCode;
        /**
         * 异常信息
         */
        private String message;
        /**
         * 响应体
         */
        private String body;

    }

    /**
     * 连通探测结果
     */
    @Data
    @SuppressWarnings("ClassCanBeRecord")
    public static class ConnectResult {

        /**
         * 是否连通
         */
        private final boolean connectable;
        /**
         * 命中的 HTTP 状态码，0 表示网络异常
         */
        private final int httpCode;
        /**
         * 探测过程描述
         */
        private final String message;
        /**
         * 探测耗时，单位：毫秒
         */
        private final long elapsedMs;

        public ConnectResult(boolean connectable, int httpCode, String message, long startTime) {
            this.connectable = connectable;
            this.httpCode = httpCode;
            this.message = message;
            this.elapsedMs = System.currentTimeMillis() - startTime;
        }

    }

}
