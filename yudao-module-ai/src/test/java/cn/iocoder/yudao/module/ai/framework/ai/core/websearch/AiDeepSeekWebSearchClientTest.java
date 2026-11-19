package cn.iocoder.yudao.module.ai.framework.ai.core.websearch;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Request;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Response;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebFetchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.deepseek.AiDeepSeekWebSearchClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiDeepSeekWebSearchClient} 的单元测试：通过本地 HTTP 桩服务验证真实发出的请求和对响应的处理
 *
 * @author 芋道源码
 */
public class AiDeepSeekWebSearchClientTest {

    private static final String API_KEY = "sk-deepseek-search-test";

    private OcrStubServer server;
    private AiDeepSeekWebSearchClient client;

    @BeforeEach
    public void setUp() throws Exception {
        server = new OcrStubServer();
        client = new AiDeepSeekWebSearchClient(server.baseUrl(), API_KEY);
    }

    @AfterEach
    public void tearDown() {
        server.close();
    }

    @Test
    public void testSearch_success() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":[{\"title\":\"DeepSeek V4\",\"url\":\"https://example.com/a\","
                + "\"snippet\":\"短摘要\",\"summary\":\"长摘要\",\"content\":\"\",\"site_name\":\"Example\","
                + "\"site_icon\":\"https://example.com/favicon.ico\",\"date\":\"2026-01-01\",\"rerank_score\":0.9}]}"));
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            query.append('字');
        }

        // 调用
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery(query.toString())
                .setSummary(true).setCount(10));

        // 断言：结果
        assertEquals(1L, response.getTotal());
        AiWebSearchResponse.WebPage page = response.getLists().get(0);
        assertEquals("Example", page.getName());
        assertEquals("https://example.com/favicon.ico", page.getIcon());
        assertEquals("DeepSeek V4", page.getTitle());
        assertEquals("https://example.com/a", page.getUrl());
        assertEquals("短摘要", page.getSnippet());
        assertEquals("长摘要", page.getSummary());
        // 断言：请求，query 截断到 128 个字符，不传 summary
        Request request = server.lastRequest();
        assertEquals("/v1/tools/search", request.path());
        assertEquals("Bearer " + API_KEY, request.header("Authorization"));
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals(128, body.get("query").asText().length());
        assertEquals(10, body.get("count").asInt());
        assertFalse(body.has("summary"));
    }

    @Test
    public void testSearch_empty() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"results\":[]}"));

        // 调用
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery("无结果").setCount(5));

        // 断言
        assertEquals(0L, response.getTotal());
        assertTrue(response.getLists().isEmpty());
    }

    @Test
    public void testSearch_error() {
        // 准备参数
        server.enqueue(Response.json(400, "{\"error\":{\"message\":\"'count' must be between 1 and 50\","
                + "\"type\":\"invalid_request_error\",\"code\":\"invalid_request_error\"}}"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("x").setCount(100)));
        assertTrue(ex.getMessage().contains("'count' must be between 1 and 50"), ex.getMessage());
    }

    @Test
    public void testFetch_success() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"title\":\"Example article\",\"content\":\"# Example article\\n\\nArticle content.\"}"));

        // 调用
        AiWebFetchResponse response = client.fetch("https://example.com/article");

        // 断言
        assertEquals("Example article", response.getTitle());
        assertEquals("https://example.com/article", response.getUrl());
        assertEquals("# Example article\n\nArticle content.", response.getContent());
        Request request = server.lastRequest();
        assertEquals("/v1/tools/fetch", request.path());
        assertEquals("https://example.com/article", JsonUtils.parseTree(request.body()).get("url").asText());
    }

    @Test
    public void testFetch_forbidden() {
        // 准备参数：被安全策略拒绝
        server.enqueue(Response.json(403, "{\"error\":{\"message\":\"Fetch request rejected\",\"type\":\"permission_error\"}}"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.fetch("https://example.com/private"));
        assertTrue(ex.getMessage().contains("403"), ex.getMessage());
    }

}
