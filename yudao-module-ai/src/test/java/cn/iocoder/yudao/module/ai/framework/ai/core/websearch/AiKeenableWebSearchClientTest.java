package cn.iocoder.yudao.module.ai.framework.ai.core.websearch;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.config.AiAutoConfiguration;
import cn.iocoder.yudao.module.ai.framework.ai.config.YudaoAiProperties;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Request;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Response;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebFetchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.deepseek.AiDeepSeekWebSearchClient;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.keenable.AiKeenableWebSearchClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiKeenableWebSearchClient} 的单元测试：通过本地 HTTP 桩服务验证真实发出的请求和对响应的处理
 *
 * @author 芋道源码
 */
public class AiKeenableWebSearchClientTest {

    private static final String QUERY = "芋道 Keenable 搜索词";
    private static final String URL = "https://example.com/page";

    private OcrStubServer server;

    @BeforeEach
    public void setUp() throws Exception {
        server = new OcrStubServer();
    }

    @AfterEach
    public void tearDown() {
        server.close();
    }

    private AiKeenableWebSearchClient buildClient(String apiKey) {
        return new AiKeenableWebSearchClient(server.baseUrl(), apiKey, Duration.ofSeconds(5));
    }

    @Test
    public void testSearch_success() {
        // 准备参数：第 1 条 snippet 超长，第 2 条非 http(s) 网页，第 3 条没有标题
        StringBuilder snippet = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            snippet.append('字');
        }
        server.enqueue(Response.json(200, "{\"query\":\"q\",\"mode\":\"pro\",\"results\":["
                + "{\"title\":\"标题 A\",\"url\":\"https://www.example.com/a\",\"description\":\"\",\"snippet\":\"" + snippet
                + "\",\"published_at\":\"2026-01-01T00:00:00Z\",\"acquired_at\":\"2026-11-01T00:00:00Z\"},"
                + "{\"title\":\"FTP\",\"url\":\"ftp://example.com/b\",\"description\":\"\",\"snippet\":\"忽略\"},"
                + "{\"title\":\"\",\"url\":\"http://example.org/c\",\"description\":\"\",\"snippet\":\"短摘录\"}]}"));

        // 调用
        AiWebSearchResponse response = buildClient(null).search(new AiWebSearchRequest()
                .setQuery("  " + QUERY + "  ").setSummary(true).setCount(100));

        // 断言：结果
        assertEquals(2L, response.getTotal());
        AiWebSearchResponse.WebPage page = response.getLists().get(0);
        assertEquals("www.example.com", page.getName());
        assertEquals("标题 A", page.getTitle());
        assertEquals("https://www.example.com/a", page.getUrl());
        assertEquals(snippet.substring(0, 200) + "...", page.getSnippet());
        assertEquals(snippet.toString(), page.getSummary());
        AiWebSearchResponse.WebPage page2 = response.getLists().get(1);
        assertEquals("example.org", page2.getTitle());
        assertEquals("短摘录", page2.getSnippet());
        // 断言：请求，匿名访问 public 接口，query 去除首尾空格，max_results 不超过 50
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/v1/search/public", request.path());
        assertEquals("yudao", request.header("X-Keenable-Title"));
        assertNull(request.header("X-API-Key"));
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals(QUERY, body.get("query").asText());
        assertEquals(50, body.get("max_results").asInt());
    }

    @Test
    public void testSearch_withoutSummary() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":[{\"title\":\"A\",\"url\":\"https://example.com/a\","
                + "\"snippet\":\"摘录\"}]}"));

        // 调用
        AiWebSearchResponse response = buildClient(null).search(new AiWebSearchRequest().setQuery(QUERY));

        // 断言：不返回 summary，不传 max_results
        assertNull(response.getLists().get(0).getSummary());
        assertFalse(JsonUtils.parseTree(server.lastRequest().body()).has("max_results"));
    }

    @Test
    public void testSearch_countMin() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":[]}"));

        // 调用
        buildClient(null).search(new AiWebSearchRequest().setQuery(QUERY).setCount(0));

        // 断言：max_results 不小于 1
        assertEquals(1, JsonUtils.parseTree(server.lastRequest().body()).get("max_results").asInt());
    }

    @Test
    public void testSearch_sampleApiKey() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":[]}"));

        // 调用：application.yaml 中的示例 API Key，视为未配置
        AiWebSearchResponse response = buildClient(" sk-xxxx ").search(new AiWebSearchRequest().setQuery(QUERY));

        // 断言
        assertEquals(0L, response.getTotal());
        assertEquals("/v1/search/public", server.lastRequest().path());
        assertNull(server.lastRequest().header("X-API-Key"));
    }

    @Test
    public void testSearch_apiKey() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":[]}"));

        // 调用
        buildClient("keen_test").search(new AiWebSearchRequest().setQuery(QUERY));

        // 断言：使用正式接口
        Request request = server.lastRequest();
        assertEquals("/v1/search", request.path());
        assertEquals("keen_test", request.header("X-API-Key"));
        assertEquals("yudao", request.header("X-Keenable-Title"));
    }

    @Test
    public void testSearch_invalidApiKey() {
        // 准备参数
        server.enqueue(Response.json(401, "{\"error\":\"Unauthorized\",\"message\":\"Invalid API key keen_bad\"}"));

        // 调用，并断言异常：不降级为匿名访问，不返回响应内容
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildClient("keen_bad").search(new AiWebSearchRequest().setQuery(QUERY)));
        assertTrue(ex.getMessage().contains("status = 401"));
        assertFalse(ex.getMessage().contains("keen_bad"));
        assertEquals(1, server.requests().size());
    }

    @Test
    public void testSearch_rateLimited() {
        // 准备参数
        server.enqueue(Response.json(429, "{\"error\":\"Rate limit exceeded\",\"message\":\"" + QUERY
                + "\",\"retryAfter\":30}"));

        // 调用，并断言异常：不返回请求、响应内容
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildClient(null).search(new AiWebSearchRequest().setQuery(QUERY)));
        assertTrue(ex.getMessage().contains("status = 429, rate limited"));
        assertFalse(ex.getMessage().contains(QUERY));
        assertFalse(ex.getMessage().contains("Rate limit exceeded"));
        assertEquals(1, server.requests().size());
    }

    @Test
    public void testSearch_timeout() {
        // 准备参数
        server.enqueue(Response.delayed(1500));
        AiKeenableWebSearchClient client = new AiKeenableWebSearchClient(server.baseUrl(), null, Duration.ofMillis(300));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery(QUERY)));
        assertTrue(ex.getMessage().contains("timeout"));
        assertFalse(ex.getMessage().contains(QUERY));
    }

    @Test
    public void testSearch_malformed() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildClient(null).search(new AiWebSearchRequest().setQuery(QUERY)));
        assertTrue(ex.getMessage().contains("request failed"));
    }

    @Test
    public void testFetch_success() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":[{\"url\":\"https://example.com/final\",\"title\":\"页面标题\","
                + "\"content\":\"# 正文\",\"description\":\"\",\"truncated\":false,\"image\":null,\"favicon\":null}]}"));

        // 调用
        AiWebFetchResponse response = buildClient(null).fetch(URL);

        // 断言：结果
        assertEquals("页面标题", response.getTitle());
        assertEquals(URL, response.getUrl());
        assertEquals("# 正文", response.getContent());
        // 断言：请求，POST 只传一个 URL
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/v1/fetch/public", request.path());
        assertEquals("yudao", request.header("X-Keenable-Title"));
        assertEquals("{\"urls\":[\"" + URL + "\"]}", request.body());
    }

    @Test
    public void testFetch_pageError() {
        // 准备参数：外层 HTTP 200，单条结果失败
        server.enqueue(Response.json(200, "{\"results\":[{\"url\":\"" + URL + "\",\"error\":{\"error\":\"Not found\","
                + "\"message\":\"Page " + URL + " returned 404\",\"status\":404}}]}"));

        // 调用，并断言异常：只返回该网页的 HTTP 状态码
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> buildClient(null).fetch(URL));
        assertTrue(ex.getMessage().contains("page status = 404"));
        assertFalse(ex.getMessage().contains(URL));
    }

    @Test
    public void testFetch_emptyResults() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":[]}"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> buildClient(null).fetch(URL));
        assertTrue(ex.getMessage().contains("invalid response"));
    }

    @Test
    public void testWebSearchClient_default() {
        // 调用：未指定平台
        YudaoAiProperties properties = new YudaoAiProperties().setWebSearch(new YudaoAiProperties.WebSearch()
                .setEnable(true).setApiKey("sk-xxxx"));

        // 断言：仍为 DeepSeek
        assertInstanceOf(AiDeepSeekWebSearchClient.class, new AiAutoConfiguration().webSearchClient(properties));
    }

    @Test
    public void testWebSearchClient_keenable() {
        // 调用：显式指定 keenable
        YudaoAiProperties properties = new YudaoAiProperties().setWebSearch(new YudaoAiProperties.WebSearch()
                .setEnable(true).setPlatform(AiWebSearchPlatformEnum.KEENABLE).setApiKey("sk-xxxx"));

        // 断言
        assertInstanceOf(AiKeenableWebSearchClient.class, new AiAutoConfiguration().webSearchClient(properties));
    }

}
