package cn.iocoder.yudao.module.ai.framework.ai.core.webserch.parallel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebFetchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Parallel {@link AiWebSearchClient} 实现类：通过 Search MCP 调用 web_search、web_fetch
 *
 * 1. 匿名访问，无需 API Key；限流比登录用户更低
 * 2. 每次调用使用独立的 MCP 会话：initialize、tools/call 后，DELETE 关闭会话
 * 3. 只实现上述 Streamable HTTP 请求，每个请求都有超时；超时或收到结果后取消请求，由 WebClient 关闭连接，
 *    不使用 MCP SDK 的客户端：其关闭会话超时后，不会关闭仍在读取的连接
 *
 * @see <a href="https://docs.parallel.ai/integrations/mcp/quickstart">Parallel MCP</a>
 *
 * @author 芋道源码
 */
@Slf4j
public class AiParallelWebSearchClient implements AiWebSearchClient {

    private static final String BASE_URL = "https://mcp.parallel.ai";
    private static final String ENDPOINT = "/mcp";

    /**
     * User-Agent 及 MCP 客户端名
     */
    private static final String USER_AGENT = "yudao";
    private static final String CLIENT_VERSION = "1.0.0";
    private static final String PROTOCOL_VERSION = "2025-06-18";
    private static final String SESSION_ID_HEADER = "Mcp-Session-Id";
    private static final String PROTOCOL_VERSION_HEADER = "MCP-Protocol-Version";

    private static final String TOOL_WEB_SEARCH = "web_search";
    private static final String TOOL_WEB_FETCH = "web_fetch";

    /**
     * 搜索目标：web_search 必填；与搜索词区分，搜索词只发送一次
     */
    private static final String SEARCH_OBJECTIVE = "Find web pages relevant to the search query.";

    /**
     * 单个请求的超时时间，包括建立连接、等待响应头、读取结果
     */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    /**
     * 关闭会话的超时时间：尽力关闭，超时后放弃
     */
    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(3);
    /**
     * 单个响应（SSE 事件）的最大字节数：web_fetch 返回整页正文
     */
    private static final int MAX_RESPONSE_SIZE = 4 * 1024 * 1024;

    private static final ParameterizedTypeReference<ServerSentEvent<String>> SSE_TYPE =
            new ParameterizedTypeReference<ServerSentEvent<String>>() {};

    private final WebClient webClient;

    public AiParallelWebSearchClient() {
        this(BASE_URL);
    }

    public AiParallelWebSearchClient(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM));
                    headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
                })
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(MAX_RESPONSE_SIZE))
                .build();
    }

    @Override
    public AiWebSearchResponse search(AiWebSearchRequest request) {
        Assert.notBlank(request.getQuery(), "query 不能为空");
        // 1. 请求：objective 使用固定的搜索目标，search_queries 为搜索词
        Map<String, Object> arguments = MapUtil.<String, Object>builder("objective", SEARCH_OBJECTIVE)
                .put("search_queries", Collections.singletonList(StrUtil.trim(request.getQuery()))).build();
        ToolResult response = callTool(TOOL_WEB_SEARCH, arguments);
        if (response == null || response.getResults() == null) {
            throw new IllegalStateException("[search][搜索结果格式无效]");
        }

        // 2. 转换结果：只保留 http(s) 且有摘录的网页；官方不支持指定条数，按 count 截取，总数使用本次返回条数
        List<AiWebSearchResponse.WebPage> pages = new ArrayList<>();
        for (Page result : response.getResults()) {
            if (request.getCount() != null && pages.size() >= request.getCount()) {
                break;
            }
            String host = getHttpHost(result.getUrl());
            String snippet = CollUtil.getFirst(result.getExcerpts());
            if (host == null || StrUtil.isBlank(snippet)) {
                continue;
            }
            pages.add(new AiWebSearchResponse.WebPage().setName(host)
                    .setTitle(StrUtil.blankToDefault(result.getTitle(), host)).setUrl(result.getUrl())
                    .setSnippet(snippet).setSummary(Boolean.TRUE.equals(request.getSummary())
                            ? CollUtil.join(result.getExcerpts(), "\n\n") : null));
        }
        return new AiWebSearchResponse().setTotal((long) pages.size()).setLists(pages);
    }

    @Override
    public AiWebFetchResponse fetch(String url) {
        Assert.notBlank(url, "url 不能为空");
        // 1. 请求：只发送网页地址，读取整页正文（Markdown）
        Map<String, Object> arguments = MapUtil.<String, Object>builder("urls", Collections.singletonList(url))
                .put("full_content", true).build();
        ToolResult response = callTool(TOOL_WEB_FETCH, arguments);
        if (response == null || response.getResults() == null) {
            throw new IllegalStateException("[fetch][读取结果格式无效]");
        }
        // 2. 读取失败时，结果在 errors 中，不是工具错误：只返回 HTTP 状态码，error_type 等是服务端的任意字符串，不返回
        if (response.getResults().isEmpty()) {
            FetchError error = CollUtil.getFirst(response.getErrors());
            throw new IllegalStateException(String.format("[fetch][读取失败，httpStatusCode = %s]",
                    error != null ? error.getHttpStatusCode() : null));
        }

        // 3. 转换结果：没有整页正文时，使用摘录
        Page result = response.getResults().get(0);
        return new AiWebFetchResponse().setTitle(result.getTitle()).setUrl(url)
                .setContent(StrUtil.blankToDefault(result.getFullContent(), CollUtil.join(result.getExcerpts(), "\n\n")));
    }

    /**
     * 调用 MCP 工具，并解析结果
     *
     * 失败时只返回工具名、HTTP 状态码、JSON-RPC 错误码等，不返回、不记录响应内容：其中可能带有搜索词、网页地址
     *
     * @param name 工具名
     * @param arguments 工具参数
     * @return 结果；格式无效时返回 null
     */
    private ToolResult callTool(String name, Map<String, Object> arguments) {
        // 会话编号：收到 initialize 的响应头时保存，之后响应体无效、超时，也能关闭会话
        AtomicReference<String> sessionId = new AtomicReference<>();
        try {
            // 1.1 初始化会话
            Map<String, Object> initializeParams = MapUtil.<String, Object>builder("protocolVersion", PROTOCOL_VERSION)
                    .put("capabilities", Collections.emptyMap())
                    .put("clientInfo", MapUtil.<String, Object>builder("name", USER_AGENT).put("version", CLIENT_VERSION).build())
                    .build();
            JsonNode initializeResult = getResult(name, send(name, sessionId, buildMessage(1, "initialize", initializeParams)));
            // 1.2 校验服务端同意的协议版本、声明了 tools 能力；否则不发送搜索词、网页地址
            if (!PROTOCOL_VERSION.equals(initializeResult.path("protocolVersion").textValue())) {
                throw buildException(name, "unsupported protocol version");
            }
            if (!initializeResult.path("capabilities").path("tools").isObject()) {
                throw buildException(name, "tools not supported");
            }
            // 2. 通知初始化完成
            checkStatus(name, send(name, sessionId, buildMessage(null, "notifications/initialized", null)));
            // 3. 调用工具
            JsonNode result = getResult(name, send(name, sessionId, buildMessage(2, "tools/call",
                    MapUtil.<String, Object>builder("name", name).put("arguments", arguments).build())));
            if (result.path("isError").asBoolean(false)) {
                throw buildException(name, "tool error");
            }
            return parseResult(result);
        } finally {
            closeSession(sessionId.get());
        }
    }

    /**
     * 发送 JSON-RPC 消息
     *
     * @param sessionId 会话编号：initialize 时，从响应头保存；其它请求时，放到请求头
     * @return 请求时，返回对应的响应消息；通知时，只返回状态码
     */
    private McpResponse send(String tool, AtomicReference<String> sessionId, Map<String, Object> message) {
        String id = message.containsKey("id") ? String.valueOf(message.get("id")) : null;
        boolean initialize = "initialize".equals(message.get("method"));
        try {
            return webClient.post()
                    .uri(ENDPOINT)
                    .headers(headers -> {
                        if (!initialize) {
                            setSessionHeaders(headers, sessionId.get());
                        }
                    })
                    .bodyValue(message)
                    .exchangeToFlux(response -> {
                        // 在读取响应体前保存会话编号：服务端返回响应头时，已经创建会话
                        String responseSessionId = response.headers().asHttpHeaders().getFirst(SESSION_ID_HEADER);
                        if (initialize && StrUtil.isNotEmpty(responseSessionId)) {
                            sessionId.set(responseSessionId);
                        }
                        int status = response.rawStatusCode();
                        if (id == null || status < 200 || status >= 300) {
                            // 不使用响应体：读到第一段（或结束）后取消读取，由 WebClient 关闭连接，避免响应体不结束时占用连接
                            return response.bodyToFlux(DataBuffer.class).take(1).doOnNext(DataBufferUtils::release)
                                    .then().thenReturn(new McpResponse(status, null)).flux();
                        }
                        // 响应可能是 JSON，也可能是 SSE：SSE 中可能有其它消息，只取 id 对应的响应
                        return readMessages(response).<McpResponse>handle((text, sink) -> {
                            JsonNode node = JsonUtils.parseTreeQuietly(text);
                            if (node != null && id.equals(node.path("id").asText())) {
                                sink.next(new McpResponse(status, node));
                            }
                        });
                    })
                    // 取到响应后取消读取，不等服务端关闭 SSE 流
                    .next()
                    .timeout(REQUEST_TIMEOUT)
                    .block();
        } catch (RuntimeException ex) {
            // 网络、超时、响应过大等异常的信息里，可能带有响应内容
            throw buildException(tool, Exceptions.unwrap(ex) instanceof TimeoutException ? "timeout" : "request failed");
        }
    }

    private static Flux<String> readMessages(ClientResponse response) {
        boolean eventStream = response.headers().contentType()
                .filter(MediaType.TEXT_EVENT_STREAM::isCompatibleWith).isPresent();
        if (!eventStream) {
            return response.bodyToMono(String.class).flux();
        }
        return response.bodyToFlux(SSE_TYPE).filter(event -> event.data() != null).map(ServerSentEvent::data);
    }

    /**
     * 关闭会话：尽力而为，超时或失败时取消请求，由 WebClient 关闭连接
     */
    private void closeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        try {
            webClient.delete()
                    .uri(ENDPOINT)
                    .headers(headers -> setSessionHeaders(headers, sessionId))
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(CLOSE_TIMEOUT)
                    .block();
        } catch (RuntimeException ex) {
            log.warn("[closeSession][关闭 Parallel MCP 会话失败，error = {}]",
                    Exceptions.unwrap(ex) instanceof TimeoutException ? "timeout" : "request failed");
        }
    }

    /**
     * 设置 initialize 之后请求的请求头：协议版本只能是 {@link #PROTOCOL_VERSION}，initialize 已校验
     */
    private static void setSessionHeaders(HttpHeaders headers, String sessionId) {
        if (sessionId != null) {
            headers.set(SESSION_ID_HEADER, sessionId);
        }
        headers.set(PROTOCOL_VERSION_HEADER, PROTOCOL_VERSION);
    }

    private static Map<String, Object> buildMessage(Integer id, String method, Map<String, Object> params) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("jsonrpc", "2.0");
        if (id != null) {
            message.put("id", id);
        }
        message.put("method", method);
        if (params != null) {
            message.put("params", params);
        }
        return message;
    }

    /**
     * 获得 JSON-RPC 响应的 result：HTTP 失败、JSON-RPC 错误时，只返回状态码、错误码
     */
    private static JsonNode getResult(String tool, McpResponse response) {
        checkStatus(tool, response);
        if (response.getMessage() == null) {
            throw buildException(tool, "empty response");
        }
        JsonNode error = response.getMessage().get("error");
        if (error != null) {
            // 只返回整数错误码：其它类型的 code 可能带有响应内容
            JsonNode code = error.get("code");
            throw buildException(tool, code != null && code.isIntegralNumber() ? "code = " + code.asText() : "rpc error");
        }
        JsonNode result = response.getMessage().get("result");
        if (result == null || !result.isObject()) {
            throw buildException(tool, "invalid response");
        }
        return result;
    }

    private static void checkStatus(String tool, McpResponse response) {
        if (response == null) {
            throw buildException(tool, "empty response");
        }
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw buildException(tool, "status = " + response.getStatus());
        }
    }

    private static IllegalStateException buildException(String tool, String error) {
        log.error("[AiParallelWebSearchClient] 调用失败！工具: [{}]，错误: [{}]", tool, error);
        return new IllegalStateException(String.format("[AiParallelWebSearchClient] 调用失败！tool = %s, error = %s",
                tool, error));
    }

    /**
     * 解析工具结果：优先使用 structuredContent，没有时解析等价的 JSON 文本
     */
    private static ToolResult parseResult(JsonNode result) {
        JsonNode structuredContent = result.get("structuredContent");
        if (structuredContent != null && structuredContent.isObject()) {
            try {
                return JsonUtils.convertObject(structuredContent, ToolResult.class);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        for (JsonNode content : result.path("content")) {
            if ("text".equals(content.path("type").asText())) {
                return JsonUtils.parseObjectQuietly(content.path("text").asText(), ToolResult.class);
            }
        }
        return null;
    }

    /**
     * 获得 http(s) 网页的域名；其它协议或无效地址返回 null
     */
    private static String getHttpHost(String url) {
        if (StrUtil.isBlank(url)) {
            return null;
        }
        try {
            URI uri = URI.create(url);
            return StrUtil.equalsAnyIgnoreCase(uri.getScheme(), "http", "https") ? uri.getHost() : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * MCP 的 HTTP 响应
     */
    @Data
    @AllArgsConstructor
    private static class McpResponse {

        /**
         * HTTP 状态码
         */
        private int status;
        /**
         * JSON-RPC 响应消息；通知、HTTP 失败时为 null
         */
        private JsonNode message;

    }

    /**
     * web_search、web_fetch 的结果：两者的网页结构相同，web_fetch 额外返回 errors
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolResult {

        private List<Page> results;
        private List<FetchError> errors;

    }

    /**
     * 网页
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Page {

        private String url;
        private String title;
        private List<String> excerpts;
        /**
         * 整页正文，只有 web_fetch 设置 full_content 时返回
         */
        @JsonProperty("full_content")
        private String fullContent;

    }

    /**
     * web_fetch 的读取失败
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FetchError {

        @JsonProperty("http_status_code")
        private Integer httpStatusCode;

    }

}
