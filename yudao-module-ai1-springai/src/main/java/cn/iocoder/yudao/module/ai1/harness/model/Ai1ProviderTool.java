package cn.iocoder.yudao.module.ai1.harness.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.Method;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderConnectRespVO;
import cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmModelFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.PROVIDER_REMOTE_MODEL_EMPTY;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.PROVIDER_REMOTE_MODEL_LOAD_FAIL;
import static cn.iocoder.yudao.module.ai1.harness.llm.Ai1LlmModelFactory.normalizeBaseUrl;

/**
 * AI1 供应商工具：面向 OpenAI 兼容接口的原始 HTTP 探测
 *
 * 1. {@link #testConnect}：GET /models 优先、回退 POST /chat/completions 的连通测试
 * 2. {@link #listModels}：拉取 GET /models 返回的模型标识
 *
 * 入参为已解析 ${ENV} 占位符的地址、密钥、Header，不感知供应商的存储与租户归属
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class Ai1ProviderTool {

    /**
     * 请求的连接超时，单位：毫秒
     */
    private static final int CONNECT_TIMEOUT = 5000;
    /**
     * 请求的读取超时，单位：毫秒
     */
    private static final int READ_TIMEOUT = 8000;
    /**
     * 连通测试回退时的最小对话请求体：仅探测服务可达与鉴权，不做真实对话
     */
    private static final String CONNECT_CHAT_BODY = "{\"model\":\"test\",\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}";

    /**
     * 连通测试：GET {baseUrl}/models 优先；失败（认证错误除外）时，回退 POST {baseUrl}/chat/completions
     *
     * @param baseUrl 接口地址
     * @param apiKey  API 密钥
     * @param headers 请求附属 Header
     * @return 测试结果
     */
    public Ai1ProviderConnectRespVO testConnect(String baseUrl, String apiKey, List<Map<String, String>> headers) {
        long startTime = System.currentTimeMillis();
        String url = normalizeBaseUrl(baseUrl);

        // 1. 主校验：GET /models（标准 OpenAI 兼容接口）
        HttpOutcome models = executeRequest(Method.GET, url + "/models", apiKey, headers, null);
        if (models.getHttpCode() == HttpStatus.HTTP_OK) {
            return buildConnectResult(true, HttpStatus.HTTP_OK, "连通正常：GET /models 返回 HTTP 200", startTime);
        }
        if (isAuthFail(models.getHttpCode())) {
            return buildConnectResult(false, models.getHttpCode(),
                    "认证失败：GET /models 返回 HTTP " + models.getHttpCode() + "，请检查 API 密钥", startTime);
        }

        // 2. 回退：POST /chat/completions 最小请求（仅探测服务可达与鉴权，不做真实对话）
        HttpOutcome chat = executeRequest(Method.POST, url + "/chat/completions", apiKey, headers, CONNECT_CHAT_BODY);
        if (chat.getHttpCode() == HttpStatus.HTTP_OK) {
            return buildConnectResult(true, HttpStatus.HTTP_OK, "GET /models 不可用（HTTP " + models.getHttpCode()
                    + "），回退 POST /chat/completions 连通正常：HTTP 200", startTime);
        }
        if (isAuthFail(chat.getHttpCode())) {
            return buildConnectResult(false, chat.getHttpCode(),
                    "认证失败：POST /chat/completions 返回 HTTP " + chat.getHttpCode() + "，请检查 API 密钥", startTime);
        }
        if (chat.getHttpCode() > 0) {
            return buildConnectResult(false, chat.getHttpCode(), "服务可达但接口异常：GET /models HTTP " + models.getHttpCode()
                    + "，POST /chat/completions HTTP " + chat.getHttpCode(), startTime);
        }

        // 3. 两次均网络异常：合并报告
        String modelsMessage = StrUtil.isNotBlank(models.getMessage()) ? "GET /models 失败：" + models.getMessage() : "GET /models 无响应";
        String chatMessage = StrUtil.isNotBlank(chat.getMessage())
                ? "POST /chat/completions 失败：" + chat.getMessage() : "POST /chat/completions 无响应";
        return buildConnectResult(false, 0, "连接失败：" + modelsMessage + "；" + chatMessage, startTime);
    }

    /**
     * 拉取远程模型标识列表：GET {baseUrl}/models，解析 data[].id
     *
     * @param baseUrl 接口地址
     * @param apiKey  API 密钥
     * @param headers 请求附属 Header
     * @return 模型标识列表
     */
    public List<String> listModels(String baseUrl, String apiKey, List<Map<String, String>> headers) {
        // 1. 请求 GET /models：非 HTTP 200 时，提示检查配置
        String url = normalizeBaseUrl(baseUrl) + "/models";
        HttpOutcome outcome = executeRequest(Method.GET, url, apiKey, headers, null);
        if (outcome.getHttpCode() != HttpStatus.HTTP_OK) {
            log.warn("[listModels][地址({}) 模型拉取失败，httpCode({}) message({})]", url, outcome.getHttpCode(), outcome.getMessage());
            throw exception(PROVIDER_REMOTE_MODEL_LOAD_FAIL);
        }

        // 2. 解析 data[].id：解析失败时，提示检查配置；解析结果为空时，提示无可用模型
        Map<String, Object> root = JsonUtils.parseMap(outcome.getBody());
        if (root == null) {
            log.warn("[listModels][地址({}) 模型数据解析失败，body({})]", url, StrUtil.maxLength(outcome.getBody(), 200));
            throw exception(PROVIDER_REMOTE_MODEL_LOAD_FAIL);
        }
        List<?> data = MapUtil.get(root, "data", List.class);
        List<String> models = convertList(data, item -> item instanceof Map
                ? StrUtil.blankToDefault(MapUtil.getStr((Map<?, ?>) item, "id"), null) : null);
        if (CollUtil.isEmpty(models)) {
            throw exception(PROVIDER_REMOTE_MODEL_EMPTY);
        }
        return models;
    }

    /**
     * 执行 OpenAI 兼容接口请求，地址非法、网络异常时记录原因
     *
     * @param method  请求方法
     * @param url     请求地址
     * @param apiKey  API 密钥
     * @param headers 请求附属 Header
     * @param body    JSON 请求体，可为空
     * @return 请求结果
     */
    private static HttpOutcome executeRequest(Method method, String url, String apiKey,
                                              List<Map<String, String>> headers, String body) {
        try {
            HttpRequest request = buildOpenAiRequest(method, url, apiKey, headers);
            if (body != null) {
                request.body(body, ContentType.JSON.getValue());
            }
            try (HttpResponse response = request.execute()) {
                return new HttpOutcome(response.getStatus(), null, response.body());
            }
        } catch (Exception e) {
            return new HttpOutcome(0, e.getMessage(), null);
        }
    }

    /**
     * 构建 OpenAI 兼容接口的 HTTP 请求：携带 API 密钥与静态附属 Header，包含 {session} 占位符的 Header 跳过
     *
     * @param method  请求方法
     * @param url     请求地址
     * @param apiKey  API 密钥
     * @param headers 请求附属 Header
     * @return HTTP 请求
     */
    private static HttpRequest buildOpenAiRequest(Method method, String url, String apiKey, List<Map<String, String>> headers) {
        HttpRequest request = HttpRequest.of(url).method(method)
                .setConnectionTimeout(CONNECT_TIMEOUT).setReadTimeout(READ_TIMEOUT)
                .header(Header.ACCEPT, ContentType.JSON.getValue());
        if (StrUtil.isNotBlank(apiKey)) {
            request.bearerAuth(apiKey);
        }
        if (CollUtil.isNotEmpty(headers)) {
            for (Map<String, String> header : headers) {
                String key = header.get("key");
                String value = header.get("value");
                if (StrUtil.isBlank(key) || StrUtil.contains(value, Ai1LlmModelFactory.SESSION_PLACEHOLDER)) {
                    continue;
                }
                request.header(key.trim(), StrUtil.nullToEmpty(value));
            }
        }
        return request;
    }

    /**
     * 认证失败：HTTP 401 未授权、403 禁止访问
     */
    private static boolean isAuthFail(int httpCode) {
        return httpCode == HttpStatus.HTTP_UNAUTHORIZED || httpCode == HttpStatus.HTTP_FORBIDDEN;
    }

    private static Ai1ProviderConnectRespVO buildConnectResult(boolean connectable, int httpCode, String message, long startTime) {
        return new Ai1ProviderConnectRespVO().setConnectable(connectable).setHttpCode(httpCode).setMessage(message)
                .setElapsedMs(System.currentTimeMillis() - startTime);
    }

    /**
     * HTTP 请求结果
     */
    @Data
    @AllArgsConstructor
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

}
