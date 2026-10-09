package cn.iocoder.yudao.module.ai1.harness.mcp;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai1.dal.dataobject.mcp.Ai1McpDO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tools.jackson.databind.JsonNode;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Ai1McpClientTool} 的运行时测试
 */
public class Ai1McpRuntimeCompatibilityTest {

    @Test
    @Timeout(15)
    public void testStreamableHttp_initializeListAndCall() throws Exception {
        // mock HTTP 服务
        List<String> methods = new CopyOnWriteArrayList<>();
        List<String> paths = new CopyOnWriteArrayList<>();
        List<String> headers = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/proxy/mcp", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }
            paths.add(exchange.getRequestURI().toString());
            headers.add(exchange.getRequestHeaders().getFirst("X-Probe"));
            JsonNode request = JsonUtils.parseTree(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String method = request.get("method").asText();
            methods.add(method);
            if (!request.has("id")) {
                exchange.sendResponseHeaders(202, -1);
                exchange.close();
                return;
            }
            Object result = switch (method) {
                case "initialize" -> Map.of("protocolVersion", request.path("params").path("protocolVersion").asText(),
                        "capabilities", Map.of("tools", Map.of("listChanged", false)),
                        "serverInfo", Map.of("name", "ai1-probe", "version", "1.0"));
                case "tools/list" -> Map.of("tools", List.of(Map.of("name", "echo", "description", "Echo tool",
                        "inputSchema", Map.of("type", "object", "properties", Map.of("value", Map.of("type", "string"))))));
                case "tools/call" -> Map.of("content", List.of(Map.of("type", "text",
                        "text", request.path("params").path("arguments").path("value").asText())), "isError", false);
                default -> Map.of();
            };
            byte[] response = JsonUtils.toJsonByte(Map.of("jsonrpc", "2.0", "id", request.get("id"), "result", result));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        Ai1McpClientTool client = new Ai1McpClientTool();
        try {
            // 准备参数
            Ai1McpDO mcp = new Ai1McpDO().setId(1L).setName("probe").setTransport("http")
                    .setUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/proxy/mcp?key=test")
                    .setHeaders(Map.of("X-Probe", "compatibility"));

            // 调用，并断言工具发现和调用
            assertEquals(1, client.listTools(mcp).size());
            assertEquals("echo", client.listTools(mcp).get(0).name());
            assertEquals("hello", client.callTool(mcp, "echo", Map.of("value", "hello")));
            // 断言 HTTP 请求
            assertTrue(methods.containsAll(List.of("initialize", "notifications/initialized", "tools/list", "tools/call")));
            assertTrue(paths.stream().allMatch("/proxy/mcp?key=test"::equals));
            assertTrue(headers.stream().allMatch("compatibility"::equals));
        } finally {
            client.destroy();
            server.stop(0);
        }
    }
}
