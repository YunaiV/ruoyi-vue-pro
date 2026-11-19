package cn.iocoder.yudao.module.ai.framework.ai.core.webserch.baidu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebFetchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 百度（千帆）{@link AiWebSearchClient} 实现类：百度搜索
 *
 * 1. 使用千帆的 API Key（Bearer）；免费额度用完后，需要在千帆控制台开通按量后付费
 * 2. 百度没有公开的网页读取接口，{@link #fetch(String)} 不支持
 *
 * @see <a href="https://cloud.baidu.com/doc/qianfan-api/s/Wmbq4z7e5">百度搜索 API</a>
 *
 * @author 芋道源码
 */
@Slf4j
public class AiBaiduWebSearchClient implements AiWebSearchClient {

    public static final String BASE_URL = "https://qianfan.baidubce.com";

    /**
     * 网页的默认、最大返回条数
     */
    private static final int DEFAULT_COUNT = 10;
    private static final int MAX_COUNT = 50;

    private final WebClient webClient;

    public AiBaiduWebSearchClient(String apiKey) {
        this(BASE_URL, apiKey);
    }

    public AiBaiduWebSearchClient(String baseUrl, String apiKey) {
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
        // 1. 请求：只搜网页；query 超长时由百度截断
        Map<String, Object> body = MapUtil.<String, Object>builder()
                .put("messages", Collections.singletonList(MapUtil.builder("role", "user")
                        .put("content", request.getQuery()).build()))
                .put("search_source", "baidu_search_v2")
                .put("resource_type_filter", Collections.singletonList(MapUtil.<String, Object>builder("type", "web")
                        .put("top_k", Math.min(ObjUtil.defaultIfNull(request.getCount(), DEFAULT_COUNT), MAX_COUNT))
                        .build()))
                .build();
        SearchResponse response = webClient.post()
                .uri("/v2/ai_search/web_search")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatus::isError, this::buildException)
                .bodyToMono(SearchResponse.class)
                .block();
        if (response == null) {
            throw new IllegalStateException("[search][搜索结果为空]");
        }
        // 2. 失败时也可能是 HTTP 200：{"code","message"}
        if (StrUtil.isNotEmpty(response.getCode()) && CollUtil.isEmpty(response.getReferences())) {
            throw new IllegalStateException(String.format("[search][搜索失败，code = %s, message = %s]",
                    response.getCode(), response.getMessage()));
        }

        // 3. 转换结果：官方不返回总匹配数，使用本次返回条数
        AiWebSearchResponse result = new AiWebSearchResponse();
        if (CollUtil.isEmpty(response.getReferences())) {
            return result.setTotal(0L).setLists(Collections.emptyList());
        }
        return result.setTotal((long) response.getReferences().size())
                .setLists(convertList(response.getReferences(), page -> new AiWebSearchResponse.WebPage()
                        .setName(page.getWebsite()).setIcon(page.getIcon())
                        .setTitle(page.getTitle()).setUrl(page.getUrl())
                        .setSnippet(page.getSnippet()).setSummary(page.getContent())));
    }

    @Override
    public AiWebFetchResponse fetch(String url) {
        throw new UnsupportedOperationException("百度搜索没有网页读取接口，请使用 DeepSeek 或博查");
    }

    private Mono<? extends Throwable> buildException(ClientResponse response) {
        return response.bodyToMono(String.class).defaultIfEmpty("").map(body -> {
            log.error("[AiBaiduWebSearchClient] 调用失败！状态码: [{}]，响应数据: [{}]", response.rawStatusCode(), body);
            SearchResponse error = JsonUtils.parseObjectQuietly(body, SearchResponse.class);
            return new IllegalStateException(String.format("[AiBaiduWebSearchClient] 调用失败！status = %s, code = %s, message = %s",
                    response.rawStatusCode(), error != null ? error.getCode() : null,
                    error != null ? error.getMessage() : body));
        });
    }

    /**
     * 搜索响应
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResponse {

        private String code;
        private String message;
        private List<Reference> references;

    }

    /**
     * 搜索结果
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reference {

        private String title;
        private String url;
        private String website;
        private String icon;
        private String snippet;
        private String content;

    }

}
