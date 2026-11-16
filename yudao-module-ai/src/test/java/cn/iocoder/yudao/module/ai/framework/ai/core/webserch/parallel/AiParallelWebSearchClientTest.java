package cn.iocoder.yudao.module.ai.framework.ai.core.webserch.parallel;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AiParallelWebSearchClientTest {

    @Test
    void testSearchTimeoutClosesStreamAndSession() throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var executor = Executors.newCachedThreadPool();
        var sessionCount = new AtomicInteger();
        var closedSessions = new CopyOnWriteArrayList<String>();
        var streamClosed = new CountDownLatch(1);
        server.setExecutor(executor);
        server.createContext("/mcp", exchange -> {
            try (exchange) {
                switch (exchange.getRequestMethod()) {
                    case "POST" -> {
                        JsonNode request = JsonUtils.parseTree(exchange.getRequestBody().readAllBytes());
                        switch (request.path("method").asText()) {
                            case "initialize" -> {
                                exchange.getResponseHeaders().set("Mcp-Session-Id", "test-" + sessionCount.incrementAndGet());
                                sendResult(exchange, request, Map.of(
                                        "protocolVersion", "2025-11-25",
                                        "capabilities", Map.of("tools", Map.of()),
                                        "serverInfo", Map.of("name", "test", "version", "1")));
                            }
                            case "notifications/initialized" -> exchange.sendResponseHeaders(202, -1);
                            case "tools/call" -> {
                                if ("slow query".equals(request.path("params").path("arguments").path("objective").asText())) {
                                    holdStreamOpen(exchange, streamClosed);
                                } else {
                                    sendResult(exchange, request, Map.of("content", List.of(), "structuredContent", Map.of(
                                            "results", List.of(Map.of("title", "Example", "url", "https://example.org",
                                                    "excerpts", List.of("Example excerpt"))))));
                                }
                            }
                            default -> exchange.sendResponseHeaders(400, -1);
                        }
                    }
                    case "GET" -> exchange.sendResponseHeaders(405, -1);
                    case "DELETE" -> {
                        closedSessions.add(exchange.getRequestHeaders().getFirst("Mcp-Session-Id"));
                        exchange.sendResponseHeaders(204, -1);
                    }
                    default -> exchange.sendResponseHeaders(400, -1);
                }
            }
        });
        server.start();
        try {
            var client = new AiParallelWebSearchClient("http://127.0.0.1:" + server.getAddress().getPort());
            RuntimeException failure = assertTimeoutPreemptively(Duration.ofSeconds(30), () ->
                    assertThrows(RuntimeException.class, () -> client.search(new AiWebSearchRequest().setQuery("slow query"))));
            assertTrue(failure.toString().contains("Timeout"), failure.toString());
            assertEquals(List.of("test-1"), closedSessions);
            assertTrue(streamClosed.await(3, TimeUnit.SECONDS));

            var response = client.search(new AiWebSearchRequest().setQuery("next query"));
            assertEquals("https://example.org", response.getLists().get(0).getUrl());
            assertEquals("Example excerpt", response.getLists().get(0).getSnippet());
            assertEquals(List.of("test-1", "test-2"), closedSessions);
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    private static void sendResult(HttpExchange exchange, JsonNode request, Object result) throws IOException {
        byte[] body = JsonUtils.toJsonString(Map.of("jsonrpc", "2.0", "id", request.get("id"), "result", result))
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
    }

    private static void holdStreamOpen(HttpExchange exchange, CountDownLatch streamClosed) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
        exchange.sendResponseHeaders(200, 0);
        byte[] heartbeat = ": heartbeat\n\n".getBytes(StandardCharsets.UTF_8);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                exchange.getResponseBody().write(heartbeat);
                exchange.getResponseBody().flush();
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (IOException disconnected) {
            streamClosed.countDown();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

}
