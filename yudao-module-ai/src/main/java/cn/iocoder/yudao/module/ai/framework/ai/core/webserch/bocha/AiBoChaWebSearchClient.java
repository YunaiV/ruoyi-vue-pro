package cn.iocoder.yudao.module.ai.framework.ai.core.webserch.bocha;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebFetchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 博查 {@link AiWebSearchClient} 实现类
 *
 * @see <a href="https://open.bochaai.com/overview">博查 AI 开放平台</a>
 * @see <a href="https://bocha-ai.feishu.cn/wiki/LvYdwenEbizr5hkp8UqcUHqgnwe">博查 DeepSeek Fetch API</a>
 *
 * @author 芋道源码
 */
@Slf4j
public class AiBoChaWebSearchClient implements AiWebSearchClient {

    public static final String BASE_URL = "https://api.bochaai.com";
    /**
     * 网页读取地址：与搜索不是同一个域名
     */
    public static final String FETCH_URL = "https://api.bocha.cn/v1/deepseek-fetch";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final WebClient webClient;

    private final Predicate<HttpStatus> STATUS_PREDICATE = status -> !status.is2xxSuccessful();

    private final Function<Object, Function<ClientResponse, Mono<? extends Throwable>>> EXCEPTION_FUNCTION =
            reqParam -> response -> response.bodyToMono(String.class).handle((responseBody, sink) -> {
                log.error("[AiBoChaWebSearchClient] 调用失败！请求参数:[{}]，响应数据: [{}]", reqParam, responseBody);
                sink.error(new IllegalStateException("[AiBoChaWebSearchClient] 调用失败！"));
            });

    public AiBoChaWebSearchClient(String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeaders((headers) -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.add(AUTHORIZATION_HEADER, BEARER_PREFIX + apiKey);
                })
                .build();
    }

    @Override
    public AiWebSearchResponse search(AiWebSearchRequest request) {
        // 转换请求参数
        WebSearchRequest webSearchRequest = new WebSearchRequest(
                request.getQuery(),
                request.getSummary(),
                request.getCount()
        );
        // 调用博查 API
        CommonResult<WebSearchResponse> response = this.webClient.post()
                .uri("/v1/web-search")
                .bodyValue(webSearchRequest)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(webSearchRequest))
                .bodyToMono(new ParameterizedTypeReference<CommonResult<WebSearchResponse>>() {})
                .block();
        if (response == null) {
            throw new IllegalStateException("[search][搜索结果为空]");
        }
        if (response.getData() == null) {
            throw new IllegalStateException(String.format("[search][搜索失败，code = %s, msg = %s]",
                    response.getCode(), response.getMsg()));
        }
        WebSearchResponse data = response.getData();

        // 转换结果
        AiWebSearchResponse result = new AiWebSearchResponse();
        if (data.webPages() == null || CollUtil.isEmpty(data.webPages().value())) {
            return result.setTotal(0L).setLists(Collections.emptyList());
        }
        return result.setTotal(data.webPages().totalEstimatedMatches())
                .setLists(convertList(data.webPages().value(), page -> new AiWebSearchResponse.WebPage()
                        .setName(page.siteName()).setIcon(page.siteIcon())
                        .setTitle(page.name()).setUrl(page.url())
                        .setSnippet(page.snippet()).setSummary(page.summary())));
    }

    @Override
    public AiWebFetchResponse fetch(String url) {
        // 调用博查 API：返回体没有 CommonResult 包装
        WebFetchRequest webFetchRequest = new WebFetchRequest(url);
        WebFetchResponse response = this.webClient.post()
                .uri(FETCH_URL)
                .bodyValue(webFetchRequest)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(webFetchRequest))
                .bodyToMono(WebFetchResponse.class)
                .block();
        if (response == null) {
            throw new IllegalStateException("[fetch][读取结果为空]");
        }
        return new AiWebFetchResponse().setTitle(response.title()).setUrl(url).setContent(response.content());
    }

    /**
     * 网页搜索请求参数
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @Value
    @Accessors(fluent = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class WebSearchRequest {

        String query;
        Boolean summary;
        Integer count;

        @JsonCreator
        public WebSearchRequest(@JsonProperty("query") String query,
                                @JsonProperty("summary") Boolean summary,
                                @JsonProperty("count") Integer count) {
            Assert.notBlank(query, "query 不能为空");
            this.query = query;
            this.summary = summary;
            this.count = count;
        }

    }

    /**
     * 网页读取请求参数
     */
    @Value
    @Accessors(fluent = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class WebFetchRequest {

        String url;

        @JsonCreator
        public WebFetchRequest(@JsonProperty("url") String url) {
            Assert.notBlank(url, "url 不能为空");
            this.url = url;
        }

    }

    /**
     * 网页读取响应
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Value
    @Accessors(fluent = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    @AllArgsConstructor
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class WebFetchResponse {

        String title;
        String content;

    }

    /**
     * 网页搜索响应
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @Value
    @Accessors(fluent = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    @AllArgsConstructor
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class WebSearchResponse {

        WebSearchWebPages webPages;

    }

    /**
     * 网页搜索结果
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @Value
    @Accessors(fluent = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    @AllArgsConstructor
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class WebSearchWebPages {

        String webSearchUrl;
        Long totalEstimatedMatches;
        List<WebPageValue> value;
        Boolean someResultsRemoved;

        /**
         * 网页结果值
         */
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        @Value
        @Accessors(fluent = true)
        @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
        @AllArgsConstructor
        @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
        public static class WebPageValue {

            String id;
            String name;
            String url;
            String displayUrl;
            String snippet;
            String summary;
            String siteName;
            String siteIcon;
            String datePublished;
            String dateLastCrawled;
            String cachedPageUrl;
            String language;
            Boolean isFamilyFriendly;
            Boolean isNavigational;

        }

    }

}
