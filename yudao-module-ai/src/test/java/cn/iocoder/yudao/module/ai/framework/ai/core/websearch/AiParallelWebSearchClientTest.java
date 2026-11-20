package cn.iocoder.yudao.module.ai.framework.ai.core.websearch;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebFetchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchRequest;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.parallel.AiParallelWebSearchClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiParallelWebSearchClient} 的单元测试：通过本地 MCP 桩服务，验证真实发出的请求、对结果的处理、错误信息和会话、连接的关闭
 *
 * @author 芋道源码
 */
public class AiParallelWebSearchClientTest {

    /**
     * 服务端错误信息中的私有内容，不能出现在异常中
     */
    private static final String PRIVATE_MARKER = "CLIENT_PRIVATE_MARKER_215";
    private static final String SEARCH_RESULT = "{\"content\":[],\"structuredContent\":{\"results\":["
            + "{\"url\":\"https://example.com/a\",\"title\":\"A\",\"excerpts\":[\"摘录 A\"]}]}}";

    private HttpServer server;
    private ExecutorService executor;
    private AiParallelWebSearchClient client;

    /**
     * 收到的请求
     */
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private final AtomicInteger sessionCount = new AtomicInteger();
    /**
     * initialize 的 JSON-RPC result；null 表示同意请求的协议版本，并声明 tools 能力
     */
    private volatile String initializeResult;
    /**
     * initialize 的 SSE 事件数据，用于返回无效的响应；null 表示使用 initializeResult
     */
    private volatile String initializeData;
    /**
     * tools/call 的 HTTP 状态码、JSON-RPC result、JSON-RPC error
     */
    private volatile int toolStatus = 200;
    private volatile String toolResult;
    private volatile String toolError;
    /**
     * tools/call 返回结果（HTTP 失败时为错误内容）后，是否保持响应体不结束
     */
    private volatile boolean toolStreamKeptOpen;
    /**
     * DELETE 返回响应头前的等待毫秒数，之后保持响应体不结束；-1 表示正常返回
     */
    private volatile long deleteStallMillis = -1;
    /**
     * 服务端发现客户端关闭连接
     */
    private final CountDownLatch toolStreamClosed = new CountDownLatch(1);
    private final CountDownLatch deleteStreamClosed = new CountDownLatch(1);

    @BeforeEach
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.createContext("/mcp", this::handle);
        server.start();
        client = new AiParallelWebSearchClient("http://" + server.getAddress().getHostString()
                + ":" + server.getAddress().getPort());
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
        executor.shutdownNow();
    }

    @Test
    public void testSearch_success() {
        // 准备参数：非 http(s)、没有摘录的结果会被过滤
        toolResult = "{\"content\":[{\"type\":\"text\",\"text\":\"{}\"}],\"isError\":false,\"structuredContent\":"
                + "{\"search_id\":\"search_1\",\"results\":["
                + "{\"url\":\"https://example.com/a\",\"title\":\"Example A\",\"publish_date\":null,\"excerpts\":[\"摘录 1\",\"摘录 2\"]},"
                + "{\"url\":\"javascript:alert(1)\",\"title\":\"Script\",\"excerpts\":[\"摘录\"]},"
                + "{\"url\":\"https://example.net/c\",\"title\":\"Example C\",\"excerpts\":[]},"
                + "{\"url\":\"https://example.org/b\",\"title\":null,\"excerpts\":[\"摘录 B\"]}],"
                + "\"warnings\":null,\"session_id\":\"session_1\"}}";

        // 调用
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery("  Maven 构建生命周期  ")
                .setSummary(true).setCount(10));

        // 断言：结果
        assertEquals(2L, response.getTotal());
        AiWebSearchResponse.WebPage page = response.getLists().get(0);
        assertEquals("example.com", page.getName());
        assertNull(page.getIcon());
        assertEquals("Example A", page.getTitle());
        assertEquals("https://example.com/a", page.getUrl());
        assertEquals("摘录 1", page.getSnippet());
        assertEquals("摘录 1\n\n摘录 2", page.getSummary());
        AiWebSearchResponse.WebPage page2 = response.getLists().get(1);
        assertEquals("example.org", page2.getTitle());
        assertEquals("摘录 B", page2.getSummary());
        // 断言：请求，objective 为固定的搜索目标，搜索词去掉首尾空白后只发送一次
        JsonNode params = findRequest("tools/call").body.get("params");
        assertEquals("web_search", params.get("name").asText());
        JsonNode arguments = params.get("arguments");
        assertEquals(2, arguments.size());
        assertEquals("Find web pages relevant to the search query.", arguments.get("objective").asText());
        assertEquals(1, arguments.get("search_queries").size());
        assertEquals("Maven 构建生命周期", arguments.get("search_queries").get(0).asText());
        assertEquals("yudao", findRequest("initialize").body.get("params").get("clientInfo").get("name").asText());
        assertSessionClosed();
    }

    @Test
    public void testSearch_countWithoutSummary() {
        // 准备参数
        toolResult = "{\"content\":[],\"structuredContent\":{\"results\":["
                + "{\"url\":\"https://example.com/a\",\"title\":\"A\",\"excerpts\":[\"摘录 A\"]},"
                + "{\"url\":\"https://example.com/b\",\"title\":\"B\",\"excerpts\":[\"摘录 B\"]}]}}";

        // 调用
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery("yudao").setCount(1));

        // 断言：按 count 截取，未要求摘要时不返回
        assertEquals(1L, response.getTotal());
        assertEquals("https://example.com/a", response.getLists().get(0).getUrl());
        assertNull(response.getLists().get(0).getSummary());
        assertSessionClosed();
    }

    @Test
    public void testSearch_toolError() {
        // 准备参数：工具返回错误，错误文本带有私有内容
        toolResult = "{\"content\":[{\"type\":\"text\",\"text\":\"Rate limit exceeded for " + PRIVATE_MARKER
                + "\"}],\"isError\":true}";

        // 调用，并断言异常：只返回工具名，不返回错误文本
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5)));
        assertTrue(ex.getMessage().contains("web_search"), ex.getMessage());
        assertFalse(ex.getMessage().contains("Rate limit exceeded"), ex.getMessage());
        assertNoPrivateMarker(ex);
        assertSessionClosed();
    }

    @Test
    public void testSearch_malformed() {
        // 准备参数：既没有 structuredContent，文本也不是 JSON
        toolResult = "{\"content\":[{\"type\":\"text\",\"text\":\"not json\"}],\"isError\":false}";

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5)));
        assertTrue(ex.getMessage().contains("格式无效"), ex.getMessage());
        assertSessionClosed();
    }

    @Test
    public void testSearch_httpError() {
        // 准备参数：服务端异常，响应体带有私有内容
        toolStatus = 500;

        // 调用，并断言异常：只返回状态码，失败时仍关闭会话
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5)));
        assertTrue(ex.getMessage().contains("status = 500"), ex.getMessage());
        assertNoPrivateMarker(ex);
        assertSessionClosed();
    }

    @Test
    public void testSearch_httpErrorBodyStalled() throws InterruptedException {
        // 准备参数：HTTP 500，响应体不结束
        toolStatus = 500;
        toolStreamKeptOpen = true;

        // 调用，并断言异常：只返回状态码
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5)));
        assertTrue(ex.getMessage().contains("status = 500"), ex.getMessage());
        assertNoPrivateMarker(ex);
        // 断言：不等响应体结束，已关闭 tools/call 的连接
        assertTrue(toolStreamClosed.await(3, TimeUnit.SECONDS));
        assertSessionClosed();
    }

    @Test
    public void testSearch_rpcError() {
        // 准备参数：JSON-RPC 错误，message 带有私有内容
        toolError = "{\"code\":-32000,\"message\":\"" + PRIVATE_MARKER + "\"}";

        // 调用，并断言异常：只返回错误码，失败时仍关闭会话
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5)));
        assertTrue(ex.getMessage().contains("code = -32000"), ex.getMessage());
        assertNoPrivateMarker(ex);
        assertSessionClosed();
    }

    @Test
    public void testSearch_rpcErrorMalformedCode() {
        // 准备参数：JSON-RPC 错误，code 不是整数，而是私有内容
        toolError = "{\"code\":\"" + PRIVATE_MARKER + "\",\"message\":\"" + PRIVATE_MARKER + "\"}";

        // 调用，并断言异常：不返回非整数的错误码
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5)));
        assertTrue(ex.getMessage().contains("rpc error"), ex.getMessage());
        assertNoPrivateMarker(ex);
        assertSessionClosed();
    }

    @Test
    public void testSearch_streamKeptOpen() throws InterruptedException {
        // 准备参数：返回结果后，SSE 流不结束
        toolResult = SEARCH_RESULT;
        toolStreamKeptOpen = true;

        // 调用
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5));

        // 断言：收到结果即返回，并关闭 SSE 连接
        assertEquals(1L, response.getTotal());
        assertTrue(toolStreamClosed.await(3, TimeUnit.SECONDS));
        assertSessionClosed();
    }

    @Test
    public void testSearch_deleteBodyStalled() throws InterruptedException {
        // 准备参数：DELETE 返回响应头后，响应体不结束
        toolResult = SEARCH_RESULT;
        deleteStallMillis = 0;

        // 调用
        long start = System.currentTimeMillis();
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5));

        // 断言：关闭会话超时（3 秒）后返回结果，并关闭 DELETE 连接
        assertEquals(1L, response.getTotal());
        assertTrue(System.currentTimeMillis() - start < 5000);
        assertTrue(deleteStreamClosed.await(3, TimeUnit.SECONDS));
        assertSessionClosed();
    }

    @Test
    public void testSearch_deleteHeaderDelayed() throws InterruptedException {
        // 准备参数：DELETE 6 秒后才返回响应头
        toolResult = SEARCH_RESULT;
        deleteStallMillis = 6000;

        // 调用
        long start = System.currentTimeMillis();
        AiWebSearchResponse response = client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5));

        // 断言：不等响应头，关闭会话超时（3 秒）后返回结果，并关闭 DELETE 连接
        assertEquals(1L, response.getTotal());
        assertTrue(System.currentTimeMillis() - start < 5000);
        assertTrue(deleteStreamClosed.await(6, TimeUnit.SECONDS));
        assertSessionClosed();
    }

    @Test
    public void testFetch_success() {
        // 准备参数：只有 JSON 文本，没有 structuredContent
        String text = "{\"extract_id\":\"extract_1\",\"results\":[{\"url\":\"https://example.com/article\","
                + "\"title\":\"Example article\",\"publish_date\":null,\"excerpts\":[\"摘录\"],"
                + "\"full_content\":\"# Example article\\n\\nArticle content.\"}],\"errors\":[],\"session_id\":\"session_1\"}";
        toolResult = "{\"content\":[{\"type\":\"text\",\"text\":" + JsonUtils.toJsonString(text) + "}],\"isError\":false}";

        // 调用
        AiWebFetchResponse response = client.fetch("https://example.com/article");

        // 断言：结果
        assertEquals("Example article", response.getTitle());
        assertEquals("https://example.com/article", response.getUrl());
        assertEquals("# Example article\n\nArticle content.", response.getContent());
        // 断言：请求，只发送网页地址
        JsonNode params = findRequest("tools/call").body.get("params");
        assertEquals("web_fetch", params.get("name").asText());
        JsonNode arguments = params.get("arguments");
        assertEquals(2, arguments.size());
        assertEquals(Collections.singletonList("https://example.com/article"),
                JsonUtils.parseArray(arguments.get("urls").toString(), String.class));
        assertTrue(arguments.get("full_content").asBoolean());
        assertSessionClosed();
    }

    @Test
    public void testFetch_error() {
        // 准备参数：读取失败时，失败原因在 errors 中；error_type、content 是服务端的任意字符串
        toolResult = "{\"content\":[],\"isError\":false,\"structuredContent\":{\"extract_id\":\"extract_1\",\"results\":[],"
                + "\"errors\":[{\"url\":\"https://example.invalid/\",\"error_type\":\"" + PRIVATE_MARKER + "\","
                + "\"http_status_code\":404,\"content\":\"" + PRIVATE_MARKER + "\"}],\"session_id\":\"session_1\"}}";

        // 调用，并断言异常：只返回 HTTP 状态码
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.fetch("https://example.invalid/"));
        assertTrue(ex.getMessage().contains("httpStatusCode = 404"), ex.getMessage());
        assertFalse(ex.getMessage().contains("example.invalid"), ex.getMessage());
        assertNoPrivateMarker(ex);
        assertSessionClosed();
    }

    @Test
    public void testFetch_errorMalformedStatus() {
        // 准备参数：http_status_code 不是数字，而是私有内容
        toolResult = "{\"content\":[],\"isError\":false,\"structuredContent\":{\"results\":[],"
                + "\"errors\":[{\"url\":\"https://example.invalid/\",\"error_type\":\"" + PRIVATE_MARKER + "\","
                + "\"http_status_code\":\"" + PRIVATE_MARKER + "\"}]}}";

        // 调用，并断言异常：结果格式无效，不返回私有内容
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.fetch("https://example.invalid/"));
        assertTrue(ex.getMessage().contains("读取结果格式无效"), ex.getMessage());
        assertNoPrivateMarker(ex);
        assertSessionClosed();
    }

    @Test
    public void testSearch_initializeMismatchedId() {
        // 准备参数：initialize 返回了会话编号，但响应的 id 不对应
        initializeData = "{\"jsonrpc\":\"2.0\",\"id\":\"" + PRIVATE_MARKER + "\",\"result\":"
                + "{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{\"tools\":{}}}}";

        // 调用，并断言：不继续调用工具，但仍关闭会话
        assertInitializeRejected("empty response");
    }

    @Test
    public void testSearch_initializeMalformed() {
        // 准备参数：initialize 返回了会话编号，但响应不是 JSON
        initializeData = PRIVATE_MARKER;

        // 调用，并断言：不继续调用工具，但仍关闭会话
        assertInitializeRejected("empty response");
    }

    @Test
    public void testSearch_initializeUnsupportedVersion() {
        // 准备参数：服务端返回不支持的协议版本
        initializeResult = "{\"protocolVersion\":\"" + PRIVATE_MARKER + "\",\"capabilities\":{\"tools\":{}}}";

        // 调用，并断言：不继续调用工具，不返回服务端的协议版本，但仍关闭会话
        assertInitializeRejected("unsupported protocol version");
    }

    @Test
    public void testSearch_initializeMissingVersion() {
        // 准备参数：服务端没有返回协议版本
        initializeResult = "{\"capabilities\":{\"tools\":{}}}";

        // 调用，并断言：不继续调用工具，但仍关闭会话
        assertInitializeRejected("unsupported protocol version");
    }

    @Test
    public void testSearch_initializeNoTools() {
        // 准备参数：服务端没有声明 tools 能力
        initializeResult = "{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{\"prompts\":{}}}";

        // 调用，并断言：不继续调用工具，但仍关闭会话
        assertInitializeRejected("tools not supported");
    }

    @Test
    public void testSearch_initializeInvalidTools() {
        // 准备参数：服务端声明的 tools 能力不是对象
        initializeResult = "{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{\"tools\":\"" + PRIVATE_MARKER + "\"}}";

        // 调用，并断言：不继续调用工具，不返回服务端的能力，但仍关闭会话
        assertInitializeRejected("tools not supported");
    }

    /**
     * 断言：initialize 失败时，抛出不含私有内容的异常；不发送通知、搜索词，但仍关闭会话
     */
    private void assertInitializeRejected(String error) {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> client.search(new AiWebSearchRequest().setQuery("yudao").setCount(5)));
        assertTrue(ex.getMessage().contains(error), ex.getMessage());
        assertNoPrivateMarker(ex);
        assertEquals(2, requests.size());
        assertEquals("initialize", requests.get(0).body.path("method").asText());
        assertEquals("DELETE", requests.get(1).method);
        assertSessionClosed();
    }

    /**
     * 断言：本次使用的会话已通过 DELETE 关闭，且所有请求都带有 User-Agent；initialize 之后的请求带有协议版本
     */
    private void assertSessionClosed() {
        assertEquals(1, sessionCount.get());
        List<String> methods = new ArrayList<>();
        for (Request request : requests) {
            methods.add(request.method);
            assertEquals("yudao", request.userAgent, request.method);
            boolean initialize = request.body != null && "initialize".equals(request.body.path("method").asText());
            assertEquals(initialize ? null : "2025-06-18", request.protocolVersion, request.method);
        }
        assertTrue(methods.contains("DELETE"), methods.toString());
        for (Request request : requests) {
            if ("DELETE".equals(request.method)) {
                assertEquals("session-1", request.sessionId);
            }
        }
    }

    /**
     * 断言：异常及其 cause 中，没有服务端返回的私有内容
     */
    private static void assertNoPrivateMarker(Throwable ex) {
        for (Throwable item = ex; item != null; item = item.getCause()) {
            assertFalse(String.valueOf(item.getMessage()).contains(PRIVATE_MARKER), item.toString());
        }
    }

    private Request findRequest(String rpcMethod) {
        for (Request request : requests) {
            if (request.body != null && rpcMethod.equals(request.body.path("method").asText())) {
                return request;
            }
        }
        throw new AssertionError("未收到请求：" + rpcMethod);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            byte[] bytes = IoUtil.readBytes(exchange.getRequestBody());
            JsonNode body = "POST".equals(method) ? JsonUtils.parseTree(bytes) : null;
            requests.add(new Request(method, exchange.getRequestHeaders().getFirst("User-Agent"),
                    exchange.getRequestHeaders().getFirst("Mcp-Session-Id"),
                    exchange.getRequestHeaders().getFirst("MCP-Protocol-Version"), body));
            if ("DELETE".equals(method) && deleteStallMillis < 0) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("DELETE".equals(method)) {
                sleep(deleteStallMillis);
                exchange.sendResponseHeaders(200, 0);
                streamUntilClosed(exchange, deleteStreamClosed);
                return;
            }
            if (body == null) { // 不提供 GET 的 SSE 流
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String rpcMethod = body.path("method").asText();
            if ("initialize".equals(rpcMethod)) {
                exchange.getResponseHeaders().set("Mcp-Session-Id", "session-" + sessionCount.incrementAndGet());
                if (initializeData != null) {
                    sendData(exchange, initializeData);
                    return;
                }
                sendMessage(exchange, body, "result", initializeResult != null ? initializeResult
                        : "{\"protocolVersion\":" + body.get("params").get("protocolVersion")
                        + ",\"capabilities\":{\"tools\":{\"listChanged\":false}},"
                        + "\"serverInfo\":{\"name\":\"Parallel MCP\",\"version\":\"\"}}");
                return;
            }
            // 之后的响应带有其它会话编号：客户端只使用 initialize 返回的会话编号
            exchange.getResponseHeaders().set("Mcp-Session-Id", "unrelated-session");
            if ("tools/call".equals(rpcMethod) && toolStatus != 200) {
                byte[] error = ("{\"error\":\"" + PRIVATE_MARKER + "\"}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(toolStatus, toolStreamKeptOpen ? 0 : error.length);
                exchange.getResponseBody().write(error);
                if (toolStreamKeptOpen) {
                    streamUntilClosed(exchange, toolStreamClosed);
                }
            } else if ("tools/call".equals(rpcMethod) && toolError != null) {
                sendMessage(exchange, body, "error", toolError);
            } else if ("tools/call".equals(rpcMethod) && toolStreamKeptOpen) {
                exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().write(buildEvent(buildMessage(body, "result", toolResult)));
                streamUntilClosed(exchange, toolStreamClosed);
            } else if ("tools/call".equals(rpcMethod)) {
                sendMessage(exchange, body, "result", toolResult);
            } else { // notifications/initialized 等通知
                exchange.sendResponseHeaders(202, -1);
            }
        } finally {
            exchange.close();
        }
    }

    /**
     * 与 Parallel 一致，使用 SSE 返回 JSON-RPC 响应
     *
     * @param member result 或 error
     */
    private static void sendMessage(HttpExchange exchange, JsonNode request, String member, String value) throws IOException {
        sendData(exchange, buildMessage(request, member, value));
    }

    /**
     * 使用 SSE 返回一个事件
     *
     * @param data 事件数据，可以不是 JSON-RPC 消息
     */
    private static void sendData(HttpExchange exchange, String data) throws IOException {
        byte[] bytes = buildEvent(data);
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String buildMessage(JsonNode request, String member, String value) {
        return "{\"jsonrpc\":\"2.0\",\"id\":" + request.get("id") + ",\"" + member + "\":" + value + "}";
    }

    private static byte[] buildEvent(String data) {
        return ("event: message\ndata: " + data + "\n\n").getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 持续发送 SSE 注释，直到客户端关闭连接；最多 15 秒
     */
    private static void streamUntilClosed(HttpExchange exchange, CountDownLatch closed) {
        try {
            for (int i = 0; i < 150; i++) {
                exchange.getResponseBody().write(": heartbeat\n\n".getBytes(StandardCharsets.UTF_8));
                exchange.getResponseBody().flush();
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (IOException ex) {
            closed.countDown();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static class Request {

        private final String method;
        private final String userAgent;
        private final String sessionId;
        private final String protocolVersion;
        private final JsonNode body;

        private Request(String method, String userAgent, String sessionId, String protocolVersion, JsonNode body) {
            this.method = method;
            this.userAgent = userAgent;
            this.sessionId = sessionId;
            this.protocolVersion = protocolVersion;
            this.body = body;
        }

    }

}
