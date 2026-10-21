package cn.iocoder.yudao.module.ai1.harness.model;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderConnectRespVO;
import cn.iocoder.yudao.module.ai1.util.Ai1Utils;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.PROVIDER_REMOTE_MODEL_LOAD_FAIL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

/**
 * {@link Ai1ProviderTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1ProviderToolTest {

    // 本机不可达的接口地址
    private static final String UNREACHABLE_BASE_URL = "http://127.0.0.1:1/v1";

    private final Ai1ProviderTool providerTool = new Ai1ProviderTool();

    @Test
    public void testTestConnect_unreachable() {
        // 调用
        Ai1ProviderConnectRespVO result = providerTool.testConnect(UNREACHABLE_BASE_URL, null, null);
        // 断言
        assertFalse(result.getConnectable());
        assertEquals(0, result.getHttpCode());
        assertTrue(result.getMessage().startsWith("连接失败："));
        assertNotNull(result.getElapsedMs());
    }

    @Test
    public void testListModels_unreachable() {
        // 调用，并断言异常
        assertServiceException(() -> providerTool.listModels(UNREACHABLE_BASE_URL, null, null), PROVIDER_REMOTE_MODEL_LOAD_FAIL);
    }

    @Test
    public void testTestConnectAndListModels_staticHeaders() throws Exception {
        // mock HTTP 服务
        List<Map<String, String>> received = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/proxy/v1/models", exchange -> {
            received.add(Map.of("auth", exchange.getRequestHeaders().getFirst("Authorization"),
                    "test", exchange.getRequestHeaders().getFirst("X-Test"),
                    "session", String.valueOf(exchange.getRequestHeaders().getFirst("X-Session"))));
            byte[] body = "{\"data\":[{\"id\":\"test-model\"}]}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        try {
            // 准备参数
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/proxy/v1/";
            Map<String, String> headers = Map.of("X-Test", "a\"b\\c", "X-Session", "{session}");
            // 调用
            Ai1ProviderConnectRespVO result = providerTool.testConnect(url, "synthetic-key", headers);
            List<String> models = providerTool.listModels(url, "synthetic-key", headers);
            // 断言
            assertTrue(result.getConnectable());
            assertEquals(List.of("test-model"), models);
            assertEquals(2, received.size());
            received.forEach(request -> assertEquals(Map.of("auth", "Bearer synthetic-key", "test", "a\"b\\c", "session", "null"), request));
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testTestConnect_invalidConfiguration() throws Exception {
        // 准备参数
        String secret = "synthetic-private-token\nprivate-suffix";
        // mock HTTP 服务
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
            // mock 环境变量
            spring.when(() -> SpringUtil.getBean(Environment.class))
                    .thenReturn(new MockEnvironment().withProperty("HTTP_HEADER_TOKEN", secret));
            // 准备参数
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
            Map<String, String> headers = Ai1Utils.resolveSpringPlaceholders(Map.of("X-Token", "${HTTP_HEADER_TOKEN}"));
            // 调用
            List<Ai1ProviderConnectRespVO> results = List.of(
                    providerTool.testConnect(url, null, headers), providerTool.testConnect(url, secret, null));
            // 断言
            for (Ai1ProviderConnectRespVO result : results) {
                assertFalse(result.getConnectable());
                assertEquals(0, result.getHttpCode());
                assertFalse(result.getMessage().contains("synthetic-private-token"));
                assertFalse(result.getMessage().contains("private-suffix"));
            }
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testParseModels() {
        // 调用，并断言过滤无效模型
        assertEquals(List.of("gpt-4o", "qwen3"), Ai1ProviderTool.parseModels(
                "{\"data\":[{\"id\":\"gpt-4o\"},{\"id\":\"\"},\"x\",{\"id\":\"qwen3\"}]}"));
        // 调用，并断言空列表
        assertTrue(Ai1ProviderTool.parseModels("{\"object\":\"list\"}").isEmpty());
        assertTrue(Ai1ProviderTool.parseModels("{\"data\":[]}").isEmpty());
        // 调用，并断言解析失败
        assertNull(Ai1ProviderTool.parseModels("{\"data\":{\"id\":\"gpt-4o\"}}"));
        assertNull(Ai1ProviderTool.parseModels("not json"));
    }

}
