package cn.iocoder.yudao.module.ai.framework.ai.core.webserch.bocha;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 博查 {@link AiWebSearchClient} 实现类
 *
 * @see <a href="https://open.bochaai.com/overview">博查 AI 开放平台</a>
 *
 * @author 芋道源码
 */
@Slf4j
public class AiBoChaWebSearchClient implements AiWebSearchClient {

    public static final String BASE_URL = "https://api.bochaai.com";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final WebClient webClient;

    private final Predicate<HttpStatusCode> STATUS_PREDICATE = status -> !status.is2xxSuccessful();

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
            return result.setTotal(0L).setLists(List.of());
        }
        return result.setTotal(data.webPages().totalEstimatedMatches())
                .setLists(convertList(data.webPages().value(), page -> new AiWebSearchResponse.WebPage()
                        .setName(page.siteName()).setIcon(page.siteIcon())
                        .setTitle(page.name()).setUrl(page.url())
                        .setSnippet(page.snippet()).setSummary(page.summary())));
    }

    /**
     * 网页搜索请求参数
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record WebSearchRequest(
            String query,
            Boolean summary,
            Integer count
    ) {
        public WebSearchRequest {
            Assert.notBlank(query, "query 不能为空");
        }
    }

    /**
     * 网页搜索响应
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record WebSearchResponse(
            WebSearchWebPages webPages
    ) {
    }

    /**
     * 网页搜索结果
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record WebSearchWebPages(
            String webSearchUrl,
            Long totalEstimatedMatches,
            List<WebPageValue> value,
            Boolean someResultsRemoved
    ) {

        /**
         * 网页结果值
         */
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        public record WebPageValue(
                String id,
                String name,
                String url,
                String displayUrl,
                String snippet,
                String summary,
                String siteName,
                String siteIcon,
                String datePublished,
                String dateLastCrawled,
                String cachedPageUrl,
                String language,
                Boolean isFamilyFriendly,
                Boolean isNavigational
        ) {
        }

    }

}
