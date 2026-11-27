package cn.iocoder.yudao.module.ai.framework.ai.core.webserch.keenable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebFetchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Keenable {@link AiWebSearchClient} 实现类
 *
 * 1. 未配置 API Key 时，匿名访问 public 接口：按 IP 限流，必须带 X-Keenable-Title 请求头
 * 2. 配置 API Key 时，通过 X-API-Key 访问正式接口；Key 无效时直接失败，不降级为匿名访问
 * 3. 失败时只返回接口路径、HTTP 状态码，不返回、不记录请求和响应内容：其中可能带有搜索词、网页地址
 *
 * @see <a href="https://docs.keenable.ai/api-reference">Keenable API</a>
 *
 * @author 芋道源码
 */
@Slf4j
public class AiKeenableWebSearchClient implements AiWebSearchClient {

    public static final String BASE_URL = "https://api.keenable.ai";

    private static final String SEARCH_PUBLIC_URI = "/v1/search/public";
    private static final String SEARCH_URI = "/v1/search";
    private static final String FETCH_PUBLIC_URI = "/v1/fetch/public";
    private static final String FETCH_URI = "/v1/fetch";

    /**
     * 应用名：匿名访问时必填，否则返回 400
     */
    private static final String TITLE_HEADER = "X-Keenable-Title";
    private static final String TITLE = "yudao";
    private static final String API_KEY_HEADER = "X-API-Key";
    /**
     * 示例 API Key：application.yaml 中的默认值，视为未配置
     */
    private static final String SAMPLE_API_KEY = "sk-xxxx";

    /**
     * 返回结果的条数范围：官方限制 1～50
     */
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 50;
    /**
     * 简短描述的最大长度：snippet 是 Markdown 正文摘录，可能有数千字
     */
    private static final int MAX_SNIPPET_LENGTH = 200;

    /**
     * 单个请求的默认超时时间，包括建立连接、等待响应头、读取结果
     */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    /**
     * 单个响应的最大字节数：fetch 返回整页正文
     */
    private static final int MAX_RESPONSE_SIZE = 4 * 1024 * 1024;

    private final WebClient webClient;
    private final String searchUri;
    private final String fetchUri;
    private final Duration timeout;

    public AiKeenableWebSearchClient(String apiKey) {
        this(BASE_URL, apiKey, DEFAULT_TIMEOUT);
    }

    public AiKeenableWebSearchClient(String baseUrl, String apiKey, Duration timeout) {
        String key = StrUtil.trim(apiKey);
        boolean anonymous = StrUtil.isEmpty(key) || SAMPLE_API_KEY.equals(key);
        this.searchUri = anonymous ? SEARCH_PUBLIC_URI : SEARCH_URI;
        this.fetchUri = anonymous ? FETCH_PUBLIC_URI : FETCH_URI;
        this.timeout = timeout;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set(TITLE_HEADER, TITLE);
                    if (!anonymous) {
                        headers.set(API_KEY_HEADER, key);
                    }
                })
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(MAX_RESPONSE_SIZE))
                .build();
    }

    @Override
    public AiWebSearchResponse search(AiWebSearchRequest request) {
        Assert.notBlank(request.getQuery(), "query 不能为空");
        // 1. 请求
        Integer count = request.getCount() != null ? Math.max(MIN_COUNT, Math.min(request.getCount(), MAX_COUNT)) : null;
        SearchRequest searchRequest = new SearchRequest().setQuery(StrUtil.trim(request.getQuery())).setMaxResults(count);
        SearchResponse response = post(searchUri, searchRequest, SearchResponse.class);
        if (response == null || response.getResults() == null) {
            throw buildException(searchUri, "invalid response");
        }

        // 2. 转换结果：description 恒为空，使用 snippet；只保留 http(s) 网页；官方不返回总匹配数，使用本次返回条数
        List<AiWebSearchResponse.WebPage> pages = new ArrayList<>();
        for (SearchResult result : response.getResults()) {
            String host = getHttpHost(result.getUrl());
            if (host == null) {
                continue;
            }
            pages.add(new AiWebSearchResponse.WebPage().setName(host)
                    .setTitle(StrUtil.blankToDefault(result.getTitle(), host)).setUrl(result.getUrl())
                    .setSnippet(StrUtil.maxLength(result.getSnippet(), MAX_SNIPPET_LENGTH))
                    .setSummary(Boolean.TRUE.equals(request.getSummary()) ? result.getSnippet() : null));
        }
        return new AiWebSearchResponse().setTotal((long) pages.size()).setLists(pages);
    }

    @Override
    public AiWebFetchResponse fetch(String url) {
        Assert.notBlank(url, "url 不能为空");
        // 1. 请求：匿名访问时，每次只能读取一个网页
        FetchResponse response = post(fetchUri, Collections.singletonMap("urls", Collections.singletonList(url)),
                FetchResponse.class);
        FetchPage page = response != null ? CollUtil.getFirst(response.getResults()) : null;
        if (page == null) {
            throw buildException(fetchUri, "invalid response");
        }
        // 2. 读取失败时，HTTP 状态码仍为 200，错误在单条结果的 error 中：只返回其 HTTP 状态码，message 等可能带有网页地址
        if (page.getError() != null) {
            throw buildException(fetchUri, "page status = " + page.getError().getStatus());
        }

        // 3. 转换结果
        return new AiWebFetchResponse().setTitle(page.getTitle()).setUrl(url).setContent(page.getContent());
    }

    /**
     * 发送 POST 请求，并解析结果
     *
     * @param uri 接口路径
     * @param body 请求体
     * @param responseType 结果类型
     * @return 结果；响应体为空时返回 null
     */
    private <T> T post(String uri, Object body, Class<T> responseType) {
        ResponseEntity<T> entity;
        try {
            entity = webClient.post()
                    .uri(uri)
                    .bodyValue(body)
                    .exchangeToMono(response -> {
                        int status = response.rawStatusCode();
                        if (status >= 200 && status < 300) {
                            return response.toEntity(responseType);
                        }
                        // 不使用响应体：读到第一段（或结束）后取消读取，由 WebClient 关闭连接，避免响应体不结束时占用连接
                        return response.bodyToFlux(DataBuffer.class).take(1).doOnNext(DataBufferUtils::release)
                                .then().thenReturn(ResponseEntity.status(status).<T>build());
                    })
                    .timeout(timeout)
                    .block();
        } catch (RuntimeException ex) {
            // 网络、超时、响应格式无效等异常的信息里，可能带有响应内容
            throw buildException(uri, Exceptions.unwrap(ex) instanceof TimeoutException ? "timeout" : "request failed");
        }
        if (entity == null) {
            throw buildException(uri, "empty response");
        }
        int status = entity.getStatusCodeValue();
        if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
            throw buildException(uri, "status = 429, rate limited");
        }
        if (status < 200 || status >= 300) {
            throw buildException(uri, "status = " + status);
        }
        return entity.getBody();
    }

    private static IllegalStateException buildException(String uri, String error) {
        log.error("[AiKeenableWebSearchClient] 调用失败！接口: [{}]，错误: [{}]", uri, error);
        return new IllegalStateException(String.format("[AiKeenableWebSearchClient] 调用失败！uri = %s, error = %s",
                uri, error));
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
     * 搜索请求参数
     */
    @Data
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class SearchRequest {

        private String query;
        @JsonProperty("max_results")
        private Integer maxResults;

    }

    /**
     * 搜索响应
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResponse {

        private List<SearchResult> results;

    }

    /**
     * 搜索结果
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResult {

        private String title;
        private String url;
        /**
         * 网页正文摘录，Markdown 格式
         */
        private String snippet;

    }

    /**
     * 读取响应：每个 URL 一条结果，顺序与请求相同
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FetchResponse {

        private List<FetchPage> results;

    }

    /**
     * 读取结果：成功时有 title、content；失败时只有 error
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FetchPage {

        private String title;
        /**
         * 网页正文，Markdown 格式
         */
        private String content;
        private FetchError error;

    }

    /**
     * 读取失败
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FetchError {

        /**
         * 该网页的 HTTP 状态码
         */
        private Integer status;

    }

}
