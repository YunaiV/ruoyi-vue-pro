package cn.iocoder.yudao.module.ai.framework.ai.core.webserch.parallel;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.netty.channel.ChannelOption;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.WebClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parallel {@link AiWebSearchClient} 实现类
 */
public class AiParallelWebSearchClient implements AiWebSearchClient, AutoCloseable {

    private static final String USER_AGENT = "ruoyi-vue-pro";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration SEARCH_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(2);

    private final McpAsyncClient client;

    public AiParallelWebSearchClient() {
        this("https://mcp.parallel.ai");
    }

    AiParallelWebSearchClient(String baseUrl) {
        HttpClient httpClient = HttpClient.newConnection()
                .headers(headers -> headers.set(HttpHeaders.USER_AGENT, USER_AGENT))
                .followRedirect(false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) CONNECT_TIMEOUT.toMillis())
                .responseTimeout(SEARCH_TIMEOUT);
        WebClient.Builder webClient = WebClient.builder().baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(1024 * 1024))
                .filter((request, next) -> {
                    if (request.method() == HttpMethod.DELETE) {
                        return next.exchange(request).flatMap(response -> response.releaseBody()
                                        .thenReturn(response.mutate().body(Flux.empty()).build()))
                                .timeout(CLOSE_TIMEOUT.minusMillis(250))
                                .onErrorResume(error -> Mono.empty());
                    }
                    return next.exchange(request);
                });
        WebClientStreamableHttpTransport transport = WebClientStreamableHttpTransport.builder(webClient)
                .endpoint("/mcp")
                .resumableStreams(false)
                .build();
        this.client = McpClient.async(transport)
                .clientInfo(new McpSchema.Implementation(USER_AGENT, "1.0"))
                .initializationTimeout(CONNECT_TIMEOUT)
                .requestTimeout(SEARCH_TIMEOUT)
                .build();
    }

    @Override
    public AiWebSearchResponse search(AiWebSearchRequest request) {
        String query = request.getQuery();
        McpSchema.CallToolRequest toolRequest = McpSchema.CallToolRequest.builder().name("web_search")
                .arguments(Map.of("objective", query, "search_queries", List.of(query)))
                .build();
        McpSchema.CallToolResult result = client.callTool(toolRequest).block(CONNECT_TIMEOUT.plus(SEARCH_TIMEOUT));
        if (result == null || Boolean.TRUE.equals(result.isError())) {
            throw new IllegalStateException("Parallel 联网搜索失败");
        }

        SearchResponse response = parseResponse(result);
        if (response == null || response.results() == null) {
            throw new IllegalStateException("Parallel 联网搜索返回格式无效");
        }
        List<AiWebSearchResponse.WebPage> pages = new ArrayList<>();
        for (SearchResult source : response.results()) {
            String host = getWebHost(source.url());
            if (host == null || source.excerpts() == null || source.excerpts().isEmpty()) {
                continue;
            }
            String snippet = source.excerpts().get(0);
            if (StrUtil.isBlank(snippet)) {
                continue;
            }
            AiWebSearchResponse.WebPage page = new AiWebSearchResponse.WebPage()
                    .setName(host).setTitle(StrUtil.blankToDefault(source.title(), host))
                    .setUrl(source.url()).setSnippet(snippet);
            if (Boolean.TRUE.equals(request.getSummary())) {
                page.setSummary(String.join("\n\n", source.excerpts()));
            }
            pages.add(page);
        }
        int count = request.getCount() == null ? pages.size() : Math.max(0, Math.min(request.getCount(), pages.size()));
        return new AiWebSearchResponse().setTotal((long) pages.size()).setLists(pages.subList(0, count));
    }

    private static SearchResponse parseResponse(McpSchema.CallToolResult result) {
        if (result.structuredContent() != null) {
            return JsonUtils.convertObject(result.structuredContent(), SearchResponse.class);
        }
        return result.content().stream().filter(McpSchema.TextContent.class::isInstance)
                .map(McpSchema.TextContent.class::cast).findFirst()
                .map(content -> JsonUtils.parseObject(content.text(), SearchResponse.class)).orElse(null);
    }

    private static String getWebHost(String url) {
        if (url == null) {
            return null;
        }
        try {
            URI uri = URI.create(url);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    ? uri.getHost() : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public void close() {
        // 回收 MCP 会话。关闭请求超时时，终止本地连接。
        client.closeGracefully().timeout(CLOSE_TIMEOUT)
                .onErrorResume(error -> {
                    client.close();
                    return Mono.empty();
                }).block();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SearchResponse(List<SearchResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SearchResult(String url, String title, List<String> excerpts) {
    }
}
