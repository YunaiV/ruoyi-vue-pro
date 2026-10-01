package cn.iocoder.yudao.module.ai1.harness.mcp;

import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import com.sun.net.httpserver.HttpServer;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Ai1McpClientTool} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1McpClientToolTest {

    @Test
    public void testParseConfig_argsArray() {
        // 准备参数
        Ai1McpDO mcp = new Ai1McpDO().setTransport("stdio")
                .setConfig("{\"command\":\"npx\",\"args\":[\"-y\",\"server\"]}");

        // 调用
        Object config = ReflectionTestUtils.invokeMethod(Ai1McpClientTool.class, "parseConfig", mcp);
        // 断言
        assertNotNull(config);
        assertEquals(List.of("-y", "server"), ReflectionTestUtils.getField(config, "args"));
    }

    @Test
    public void testParseConfig_argsNotArray() {
        // 准备参数
        List<String> argsValues = List.of("\"-y,server\"", "123", "true", "{\"value\":\"server\"}", "null");

        for (String args : argsValues) {
            Ai1McpDO mcp = new Ai1McpDO().setTransport("stdio")
                    .setConfig("{\"command\":\"npx\",\"args\":" + args + "}");

            // 调用
            Object config = ReflectionTestUtils.invokeMethod(Ai1McpClientTool.class, "parseConfig", mcp);
            // 断言
            assertNotNull(config);
            assertNull(ReflectionTestUtils.getField(config, "args"));
        }
    }

    @Test
    public void testParseConfig_argsEmptyOrMissing() {
        // 准备参数
        List<String> configs = List.of("{\"command\":\"npx\",\"args\":[]}", "{\"command\":\"npx\"}");

        // 调用
        Object emptyConfig = ReflectionTestUtils.invokeMethod(Ai1McpClientTool.class, "parseConfig",
                new Ai1McpDO().setTransport("stdio").setConfig(configs.get(0)));
        Object missingConfig = ReflectionTestUtils.invokeMethod(Ai1McpClientTool.class, "parseConfig",
                new Ai1McpDO().setTransport("stdio").setConfig(configs.get(1)));
        // 断言
        assertNotNull(emptyConfig);
        assertEquals(List.of(), ReflectionTestUtils.getField(emptyConfig, "args"));
        assertNotNull(missingConfig);
        assertNull(ReflectionTestUtils.getField(missingConfig, "args"));
    }

    @Test
    public void testBuildTransport_httpQuery() {
        // 准备参数
        Ai1McpDO mcp = new Ai1McpDO().setTransport("http")
                .setUrl("https://example.com/custom/mcp?token=a%2Bb%3D&tag=x&tag=y#ignored");

        // 调用
        McpClientTransport transport = ReflectionTestUtils.invokeMethod(new Ai1McpClientTool(), "buildTransport", mcp);
        // 断言
        assertNotNull(transport);
        try {
            assertEquals(URI.create("https://example.com"), ReflectionTestUtils.getField(transport, "baseUri"));
            assertEquals("/custom/mcp?token=a%2Bb%3D&tag=x&tag=y", ReflectionTestUtils.getField(transport, "endpoint"));
        } finally {
            transport.closeGracefully().block(Duration.ofSeconds(5));
        }
    }

    @Test
    public void testBuildTransport_httpDefaultEndpoint() {
        // 准备参数
        Ai1McpDO mcp = new Ai1McpDO().setTransport("http").setUrl("https://example.com?api_key=x");

        // 调用
        McpClientTransport transport = ReflectionTestUtils.invokeMethod(new Ai1McpClientTool(), "buildTransport", mcp);
        // 断言
        assertNotNull(transport);
        try {
            assertEquals("/mcp?api_key=x", ReflectionTestUtils.getField(transport, "endpoint"));
        } finally {
            transport.closeGracefully().block(Duration.ofSeconds(5));
        }
    }

    @Test
    public void testBuildTransport_httpRequestPreservesEncoding() throws Exception {
        // mock 数据
        AtomicReference<String> requestUri = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestUri.set(exchange.getRequestURI().toASCIIString());
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(202, -1);
            exchange.close();
        });
        server.start();
        try {
            for (String endpoint : List.of("/custom%2Fmcp?token=a%2Bb%3D&tag=x&tag=y", "?api_key=x")) {
                // 准备参数
                Ai1McpDO mcp = new Ai1McpDO().setTransport("http")
                        .setUrl("http://127.0.0.1:" + server.getAddress().getPort() + endpoint);
                McpClientTransport transport = ReflectionTestUtils.invokeMethod(new Ai1McpClientTool(), "buildTransport", mcp);
                assertNotNull(transport);
                try {
                    // 调用
                    transport.sendMessage(new McpSchema.JSONRPCNotification("notifications/initialized"))
                            .block(Duration.ofSeconds(5));
                    // 断言
                    assertEquals(endpoint.startsWith("?") ? "/mcp" + endpoint : endpoint, requestUri.get());
                } finally {
                    transport.closeGracefully().block(Duration.ofSeconds(5));
                }
            }
        } finally {
            server.stop(0);
        }
    }

}
