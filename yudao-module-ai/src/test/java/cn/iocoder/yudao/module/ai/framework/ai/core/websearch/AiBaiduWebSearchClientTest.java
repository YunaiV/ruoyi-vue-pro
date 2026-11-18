package cn.iocoder.yudao.module.ai.framework.ai.core.websearch;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Request;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Response;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.baidu.AiBaiduWebSearchClient;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiBaiduWebSearchClient} 的单元测试：通过本地 HTTP 桩服务验证真实发出的请求和对响应的处理
 *
 * @author 芋道源码
 */
public class AiBaiduWebSearchClientTest {

    private static final String API_KEY = "bce-v3/ALTAK-baidu-search-test";

    private OcrStubServer server;
    private AiBaiduWebSearchClient client;

    @BeforeEach
    public void setUp() throws Exception {
        server = new OcrStubServer();
        client = new AiBaiduWebSearchClient(server.baseUrl(), API_KEY);
    }

    @AfterEach
    public void tearDown() {
        server.close();
    }

    @Test
    public void testSearch_success() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"request_id\":\"r-1\",\"references\":[{\"id\":1,\"title\":\"芋道源码\","
                + "\"url\":\"https://www.iocoder.cn\",\"website\":\"芋道\",\"icon\":\"https://www.iocoder.cn/favicon.ico\","
                + "\"snippet\":\"短摘要\",\"content\":\"长内容\",\"type\":\"web\",\"date\":\"2026-01-01 00:00:00\"}]}"));

        // 调用
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery("芋道源码").setCount(5));

        // 断言：结果
        assertEquals(1L, response.getTotal());
        AiWebSearchResponse.WebPage page = response.getLists().get(0);
        assertEquals("芋道", page.getName());
        assertEquals("https://www.iocoder.cn/favicon.ico", page.getIcon());
        assertEquals("芋道源码", page.getTitle());
        assertEquals("https://www.iocoder.cn", page.getUrl());
        assertEquals("短摘要", page.getSnippet());
        assertEquals("长内容", page.getSummary());
        // 断言：请求
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/v2/ai_search/web_search", request.path());
        assertEquals("Bearer " + API_KEY, request.header("Authorization"));
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals("user", body.path("messages").path(0).path("role").asText());
        assertEquals("芋道源码", body.path("messages").path(0).path("content").asText());
        assertEquals("baidu_search_v2", body.path("search_source").asText());
        assertEquals("web", body.path("resource_type_filter").path(0).path("type").asText());
        assertEquals(5, body.path("resource_type_filter").path(0).path("top_k").asInt());
    }

    @Test
    public void testSearch_countDefaultAndMax() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"references\":[]}"));
        server.enqueue(Response.json(200, "{\"references\":[]}"));

        // 调用：不传条数、超过上限
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery("a"));
        client.search(new AiWebSearchRequest().setQuery("a").setCount(100));

        // 断言
        assertEquals(0L, response.getTotal());
        assertEquals(10, JsonUtils.parseTree(server.requests().get(0).body())
                .path("resource_type_filter").path(0).path("top_k").asInt());
        assertEquals(50, JsonUtils.parseTree(server.requests().get(1).body())
                .path("resource_type_filter").path(0).path("top_k").asInt());
    }

    @Test
    public void testSearch_errorWithHttp200() {
        // 准备参数：失败也可能返回 HTTP 200
        server.enqueue(Response.json(200, "{\"requestId\":\"r-2\",\"code\":\"InvalidArgument\",\"message\":\"query is empty\"}"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("a").setCount(5)));
        assertTrue(ex.getMessage().contains("InvalidArgument"), ex.getMessage());
        assertTrue(ex.getMessage().contains("query is empty"), ex.getMessage());
    }

    @Test
    public void testSearch_httpError() {
        // 准备参数
        server.enqueue(Response.json(429, "{\"requestId\":\"r-3\",\"code\":\"RateLimit\",\"message\":\"too many requests\"}"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("a").setCount(5)));
        assertTrue(ex.getMessage().contains("429"), ex.getMessage());
        assertTrue(ex.getMessage().contains("RateLimit"), ex.getMessage());
        assertFalse(ex.getMessage().contains(API_KEY));
    }

    @Test
    public void testFetch_notSupported() {
        assertThrows(UnsupportedOperationException.class, () -> client.fetch("https://www.iocoder.cn"));
        assertTrue(server.requests().isEmpty());
    }

}
