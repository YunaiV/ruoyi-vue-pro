package cn.iocoder.yudao.module.ai.framework.ai.core.webserch.deepseek;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebFetchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * DeepSeek {@link AiWebSearchClient} 实现类
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/search">DeepSeek Search / Fetch</a>
 *
 * @author 芋道源码
 */
@Slf4j
public class AiDeepSeekWebSearchClient implements AiWebSearchClient {

    public static final String BASE_URL = "https://api.deepseek.com";

    /**
     * 搜索词最大长度：官方限制 1～128 个字符
     */
    private static final int MAX_QUERY_LENGTH = 128;

    private final WebClient webClient;

    public AiDeepSeekWebSearchClient(String apiKey) {
        this(BASE_URL, apiKey);
    }

    public AiDeepSeekWebSearchClient(String baseUrl, String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(apiKey);
                })
                .build();
    }

    @Override
    public AiWebSearchResponse search(AiWebSearchRequest request) {
        Assert.notBlank(request.getQuery(), "query 不能为空");
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(StrUtil.sub(StrUtil.trim(request.getQuery()), 0, MAX_QUERY_LENGTH));
        searchRequest.setCount(request.getCount());
        SearchResponse response = webClient.post()
                .uri("/v1/tools/search")
                .bodyValue(searchRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::buildException)
                .bodyToMono(SearchResponse.class)
                .block();
        if (response == null) {
            throw new IllegalStateException("[search][搜索结果为空]");
        }

        // 转换结果：官方不返回总匹配数，使用本次返回条数
        AiWebSearchResponse result = new AiWebSearchResponse();
        if (CollUtil.isEmpty(response.getResults())) {
            return result.setTotal(0L).setLists(Collections.emptyList());
        }
        return result.setTotal((long) response.getResults().size())
                .setLists(convertList(response.getResults(), page -> new AiWebSearchResponse.WebPage()
                        .setName(page.getSiteName()).setIcon(page.getSiteIcon())
                        .setTitle(page.getTitle()).setUrl(page.getUrl())
                        .setSnippet(page.getSnippet()).setSummary(page.getSummary())));
    }

    @Override
    public AiWebFetchResponse fetch(String url) {
        Assert.notBlank(url, "url 不能为空");
        FetchRequest fetchRequest = new FetchRequest();
        fetchRequest.setUrl(url);
        FetchResponse response = webClient.post()
                .uri("/v1/tools/fetch")
                .bodyValue(fetchRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::buildException)
                .bodyToMono(FetchResponse.class)
                .block();
        if (response == null) {
            throw new IllegalStateException("[fetch][读取结果为空]");
        }
        return new AiWebFetchResponse().setTitle(response.getTitle()).setUrl(url).setContent(response.getContent());
    }

    /**
     * 将 OpenAI 兼容的错误响应（error.message）转换为异常
     */
    private Mono<? extends Throwable> buildException(ClientResponse response) {
        return response.bodyToMono(String.class).defaultIfEmpty("").map(body -> {
            log.error("[AiDeepSeekWebSearchClient] 调用失败！状态码: [{}]，响应数据: [{}]", response.statusCode(), body);
            ErrorResponse error = JsonUtils.parseObjectQuietly(body, ErrorResponse.class);
            String message = error != null && error.getError() != null ? error.getError().getMessage() : null;
            return new IllegalStateException(String.format("[AiDeepSeekWebSearchClient] 调用失败！status = %s, message = %s",
                    response.statusCode().value(), StrUtil.blankToDefault(message, body)));
        });
    }

    /**
     * 错误响应
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorResponse {

        private ErrorDetail error;

    }

    /**
     * 错误详情
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetail {

        private String message;
        private String type;
        private String code;

    }

    /**
     * 搜索请求参数
     */
    @Data
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public static class SearchRequest {

        private String query;
        private Integer count;

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
        private String snippet;
        private String summary;
        @JsonProperty("site_name")
        private String siteName;
        @JsonProperty("site_icon")
        private String siteIcon;

    }

    /**
     * 读取请求参数
     */
    @Data
    public static class FetchRequest {

        private String url;

    }

    /**
     * 读取响应
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FetchResponse {

        private String title;
        private String content;

    }

}
