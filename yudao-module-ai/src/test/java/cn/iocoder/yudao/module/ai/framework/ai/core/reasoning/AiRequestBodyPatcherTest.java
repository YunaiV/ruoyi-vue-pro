package cn.iocoder.yudao.module.ai.framework.ai.core.reasoning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link AiRequestBodyPatcher} 的单元测试类
 *
 * 1. RFC 7396 合并与失败关闭：纯内存
 * 2. 同步、流式、工具续调用：本机回环 HTTP 服务 + Spring AI 1.1.8 真实的 DeepSeekApi / DeepSeekChatModel，不访问厂商
 *
 * @author 芋道源码
 */
public class AiRequestBodyPatcherTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String PATCH_JSON = "{\"thinking\":{\"type\":\"enabled\"},\"reasoning_effort\":\"high\",\"temperature\":1.0}";

    private HttpServer server;
    private String baseUrl;
    /**
     * 收到的请求
     */
    private final List<CapturedRequest> requests = new CopyOnWriteArrayList<>();
    /**
     * 按请求顺序返回的响应；用完后重复最后一个
     */
    private final List<StubResponse> responses = new CopyOnWriteArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger index = new AtomicInteger();
        server.createContext("/", exchange -> {
            byte[] body;
            try (InputStream in = exchange.getRequestBody()) {
                body = in.readAllBytes();
            }
            Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            exchange.getRequestHeaders().forEach((k, v) -> headers.put(k, String.join(",", v)));
            requests.add(new CapturedRequest(exchange.getRequestURI().getPath(), headers, body));
            int i = Math.min(index.getAndIncrement(), responses.size() - 1);
            StubResponse response = responses.get(i);
            byte[] bytes = response.body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", response.contentType);
            exchange.sendResponseHeaders(response.status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
    }

    // ========== RFC 7396 ==========

    @Test
    public void testMergePatch_rfc7396() throws Exception {
        // 准备参数：对象递归、null 删除、数组整体替换、标量覆盖、新增
        JsonNode target = MAPPER.readTree("{\"a\":\"b\",\"c\":{\"d\":\"e\",\"f\":\"g\"},\"arr\":[1,2,3],\"keep\":1}");
        JsonNode patch = MAPPER.readTree("{\"a\":\"z\",\"c\":{\"f\":null,\"h\":1},\"arr\":[9],\"new\":{\"x\":null,\"y\":2}}");

        // 调用
        JsonNode result = AiRequestBodyPatcher.mergePatch(target, patch);
        // 断言
        assertEquals(MAPPER.readTree("{\"a\":\"z\",\"c\":{\"d\":\"e\",\"h\":1},\"arr\":[9],\"keep\":1,\"new\":{\"y\":2}}"),
                result);
    }

    @Test
    public void testMergePatch_nonObjectPatchReplaces() throws Exception {
        // 调用，并断言：补丁或目标不是对象时，整体替换
        assertEquals(MAPPER.readTree("[\"c\"]"),
                AiRequestBodyPatcher.mergePatch(MAPPER.readTree("{\"a\":1}"), MAPPER.readTree("[\"c\"]")));
        assertEquals(MAPPER.readTree("{\"a\":1}"),
                AiRequestBodyPatcher.mergePatch(MAPPER.readTree("[1]"), MAPPER.readTree("{\"a\":1}")));
    }

    @Test
    public void testMergePatch_deepCopy() throws Exception {
        // 准备参数
        ObjectNode patch = (ObjectNode) MAPPER.readTree("{\"thinking\":{\"type\":\"enabled\"},\"arr\":[1]}");
        JsonNode before = patch.deepCopy();

        // 调用
        ObjectNode result = (ObjectNode) AiRequestBodyPatcher.mergePatch(MAPPER.createObjectNode(), patch);
        // 断言：补丁的值写入目标后，修改目标不能影响补丁
        ((ObjectNode) result.get("thinking")).put("type", "disabled");
        ((ArrayNode) result.get("arr")).add(2);
        assertEquals(before, patch);
    }

    @Test
    public void testApply_repeatedCallsIndependent() throws Exception {
        // 准备参数
        AiRequestBodyPatcher patcher = new AiRequestBodyPatcher("{\"thinking\":{\"type\":\"enabled\"}}");

        // 调用
        byte[] first = patcher.apply(jsonHeaders(), "{\"thinking\":{\"budget\":1}}".getBytes(StandardCharsets.UTF_8));
        byte[] second = patcher.apply(jsonHeaders(), "{\"model\":\"m\"}".getBytes(StandardCharsets.UTF_8));
        // 断言：两次调用互不影响
        assertEquals(MAPPER.readTree("{\"thinking\":{\"budget\":1,\"type\":\"enabled\"}}"), MAPPER.readTree(first));
        assertEquals(MAPPER.readTree("{\"model\":\"m\",\"thinking\":{\"type\":\"enabled\"}}"), MAPPER.readTree(second));
    }

    @Test
    public void testApply_utf8() throws Exception {
        // 准备参数
        AiRequestBodyPatcher patcher = new AiRequestBodyPatcher("{\"note\":\"思考\"}");

        // 调用
        byte[] merged = patcher.apply(jsonHeaders(), "{\"content\":\"你好，世界 😀\"}".getBytes(StandardCharsets.UTF_8));
        // 断言
        JsonNode node = MAPPER.readTree(new String(merged, StandardCharsets.UTF_8));
        assertEquals("你好，世界 😀", node.get("content").asText());
        assertEquals("思考", node.get("note").asText());
    }

    // ========== 失败关闭 ==========

    @Test
    public void testConstructor_invalidPatch() {
        // 调用，并断言异常：非法 JSON、非对象、尾部多余内容
        assertThrows(IllegalArgumentException.class, () -> new AiRequestBodyPatcher("not json"));
        assertThrows(IllegalArgumentException.class, () -> new AiRequestBodyPatcher("[1]"));
        assertThrows(IllegalArgumentException.class, () -> new AiRequestBodyPatcher("{\"a\":1} {}"));
    }

    @Test
    public void testApply_failClosed() {
        // 准备参数
        AiRequestBodyPatcher patcher = new AiRequestBodyPatcher(PATCH_JSON);
        byte[] json = "{\"a\":1}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders text = new HttpHeaders();
        text.setContentType(MediaType.TEXT_PLAIN);
        HttpHeaders gbk = new HttpHeaders();
        gbk.setContentType(new MediaType(MediaType.APPLICATION_JSON, Charset.forName("GBK")));

        // 调用，并断言异常：非 JSON Content-Type
        assertThrows(IllegalStateException.class, () -> patcher.apply(text, json));
        // 调用，并断言异常：没有 Content-Type
        assertThrows(IllegalStateException.class, () -> patcher.apply(new HttpHeaders(), json));
        // 调用，并断言异常：非 UTF-8
        assertThrows(IllegalStateException.class, () -> patcher.apply(gbk, json));
        // 调用，并断言异常：空请求体
        assertThrows(IllegalStateException.class, () -> patcher.apply(jsonHeaders(), new byte[0]));
        assertThrows(IllegalStateException.class, () -> patcher.apply(jsonHeaders(), null));
        // 调用，并断言异常：非法 JSON、非对象、尾部多余内容
        assertThrows(IllegalStateException.class, () -> patcher.apply(jsonHeaders(), "{bad".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalStateException.class, () -> patcher.apply(jsonHeaders(), "[1]".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalStateException.class, () -> patcher.apply(jsonHeaders(), "{} {}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testRestClient_failClosed_noRequestSent() {
        // mock 响应
        responses.add(StubResponse.json(200, "{}"));
        // 准备参数
        RestClient client = new AiRequestBodyPatcher(PATCH_JSON).restClientBuilder().baseUrl(baseUrl).build();

        // 调用，并断言异常
        assertThrows(RuntimeException.class, () -> client.post().uri("/chat/completions")
                .contentType(MediaType.TEXT_PLAIN).body("hello").retrieve().toBodilessEntity());
        // 断言
        assertTrue(requests.isEmpty(), "补丁失败时不能发出请求");
    }

    @Test
    public void testWebClient_failClosed_noRequestSent() {
        // mock 响应
        responses.add(StubResponse.json(200, "{}"));
        // 准备参数
        WebClient client = new AiRequestBodyPatcher(PATCH_JSON).webClientBuilder().baseUrl(baseUrl).build();

        // 调用，并断言异常：非 JSON Content-Type
        assertThrows(RuntimeException.class, () -> client.post().uri("/chat/completions")
                .contentType(MediaType.TEXT_PLAIN).bodyValue("hello").retrieve().toBodilessEntity()
                .block(Duration.ofSeconds(10)));
        // 调用，并断言异常：非 JSON 对象
        assertThrows(RuntimeException.class, () -> client.post().uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON).bodyValue("[1]").retrieve().toBodilessEntity()
                .block(Duration.ofSeconds(10)));
        // 断言
        assertTrue(requests.isEmpty(), "补丁失败时不能发出请求");
    }

    // ========== DeepSeekChatModel 同步 / 流式 ==========

    @Test
    public void testConfigure_nullPatcherKeepsBuilder() {
        // 准备参数
        DeepSeekApi.Builder builder = DeepSeekApi.builder();

        // 调用，并断言
        assertSame(builder, AiRequestBodyPatcher.configure(builder, null));
    }

    @Test
    public void testDeepSeekCall_patched() throws Exception {
        // mock 响应
        responses.add(StubResponse.json(200, syncStop("ok")));
        // 准备参数
        DeepSeekChatModel model = buildModel(new AiRequestBodyPatcher(PATCH_JSON));

        // 调用
        ChatResponse response = model.call(new Prompt(new UserMessage("你好，世界")));
        // 断言
        assertEquals("ok", response.getResult().getOutput().getText());
        assertEquals(1, requests.size());
        CapturedRequest request = requests.get(0);
        JsonNode body = request.json();
        assertEquals("enabled", body.at("/thinking/type").asText());
        assertEquals("high", body.get("reasoning_effort").asText());
        assertEquals(1.0, body.get("temperature").asDouble(), "思考参数应覆盖 temperature");
        assertEquals("deepseek-chat", body.get("model").asText());
        assertEquals("你好，世界", body.at("/messages/0/content").asText());
        assertFalse(body.get("stream").asBoolean());
        assertDefaultRequest(request);
        assertContentLength(request);
    }

    @Test
    public void testDeepSeekCall_emptyPatchEqualsUnpatched() throws Exception {
        // mock 响应
        responses.add(StubResponse.json(200, syncStop("ok")));

        // 调用：不装补丁、装空补丁各一次
        buildModel(null).call(new Prompt(new UserMessage("你好")));
        buildModel(new AiRequestBodyPatcher("{}")).call(new Prompt(new UserMessage("你好")));
        // 断言：空补丁时，请求体与不装补丁时一致；请求头、路径也一致
        assertEquals(2, requests.size());
        assertEquals(requests.get(0).json(), requests.get(1).json());
        assertEquals(requests.get(0).path, requests.get(1).path);
        assertEquals(requests.get(0).headers.get("Authorization"), requests.get(1).headers.get("Authorization"));
        assertEquals(requests.get(0).headers.get("Content-Type"), requests.get(1).headers.get("Content-Type"));
        // 断言：不装补丁时，请求体中没有 thinking
        assertFalse(requests.get(0).json().has("thinking"));
        assertTrue(requests.get(0).json().has("temperature"));
    }

    @Test
    public void testDeepSeekStream_patched() throws Exception {
        // mock 响应
        responses.add(StubResponse.sse(streamChunk("你", null), streamChunk("好", "stop")));
        // 准备参数
        DeepSeekChatModel model = buildModel(new AiRequestBodyPatcher(PATCH_JSON));

        // 调用
        List<ChatResponse> chunks = model.stream(new Prompt(new UserMessage("你好，世界 😀")))
                .collectList().block(Duration.ofSeconds(30));
        // 断言
        assertNotNull(chunks);
        StringBuilder text = new StringBuilder();
        chunks.forEach(c -> {
            if (c.getResult() != null && c.getResult().getOutput().getText() != null) {
                text.append(c.getResult().getOutput().getText());
            }
        });
        assertEquals("你好", text.toString());
        assertEquals(1, requests.size());
        CapturedRequest request = requests.get(0);
        JsonNode body = request.json();
        assertEquals("enabled", body.at("/thinking/type").asText());
        assertEquals("high", body.get("reasoning_effort").asText());
        assertEquals(1.0, body.get("temperature").asDouble());
        assertTrue(body.get("stream").asBoolean());
        assertEquals("你好，世界 😀", body.at("/messages/0/content").asText());
        assertDefaultRequest(request);
        assertContentLength(request);
    }

    // ========== 工具续调用 ==========

    @Test
    public void testDeepSeekCall_toolContinuationPatched() throws Exception {
        // mock 响应：先返回工具调用，再返回结果
        responses.add(StubResponse.json(200, syncToolCall()));
        responses.add(StubResponse.json(200, syncStop("done")));
        // 准备参数
        List<String> invoked = new CopyOnWriteArrayList<>();
        DeepSeekChatModel model = buildModel(new AiRequestBodyPatcher(PATCH_JSON));

        // 调用
        ChatResponse response = model.call(new Prompt(new UserMessage("调用工具"), toolOptions(invoked)));
        // 断言
        assertEquals("done", response.getResult().getOutput().getText());
        assertEquals(List.of("hi"), invoked);
        assertToolContinuation();
    }

    @Test
    public void testDeepSeekStream_toolContinuationPatched() throws Exception {
        // mock 响应：先返回工具调用，再返回结果
        responses.add(StubResponse.sse(streamToolCall()));
        responses.add(StubResponse.sse(streamChunk("done", "stop")));
        // 准备参数
        List<String> invoked = new CopyOnWriteArrayList<>();
        DeepSeekChatModel model = buildModel(new AiRequestBodyPatcher(PATCH_JSON));

        // 调用
        List<ChatResponse> chunks = model.stream(new Prompt(new UserMessage("调用工具"), toolOptions(invoked)))
                .collectList().block(Duration.ofSeconds(30));
        // 断言
        assertNotNull(chunks);
        assertEquals(List.of("hi"), invoked);
        assertToolContinuation();
        assertTrue(requests.get(1).json().get("stream").asBoolean());
    }

    @Test
    public void testDeepSeekCall_errorResponsePropagates() throws Exception {
        // mock 响应
        responses.add(StubResponse.json(400, "{\"error\":{\"message\":\"bad\"}}"));
        // 准备参数
        DeepSeekChatModel model = buildModel(new AiRequestBodyPatcher(PATCH_JSON));

        // 调用，并断言异常
        assertThrows(RuntimeException.class, () -> model.call(new Prompt(new UserMessage("你好"))));
        // 断言：补丁已打上，厂商的错误原样抛出
        assertEquals("high", requests.get(0).json().get("reasoning_effort").asText());
    }

    // ========== 辅助方法 ==========

    private void assertToolContinuation() throws Exception {
        assertEquals(2, requests.size());
        for (CapturedRequest request : requests) {
            JsonNode body = request.json();
            assertEquals("enabled", body.at("/thinking/type").asText());
            assertEquals("high", body.get("reasoning_effort").asText());
            assertEquals(1.0, body.get("temperature").asDouble());
            assertEquals("echo", body.at("/tools/0/function/name").asText(), "工具定义不能被补丁破坏");
            assertContentLength(request);
        }
        // 续调用带上了工具结果
        JsonNode messages = requests.get(1).json().get("messages");
        boolean hasToolMessage = false;
        for (JsonNode message : messages) {
            if ("tool".equals(message.path("role").asText())) {
                hasToolMessage = true;
                assertEquals("call_1", message.get("tool_call_id").asText());
            }
        }
        assertTrue(hasToolMessage);
    }

    private DeepSeekChatModel buildModel(AiRequestBodyPatcher patcher) {
        DeepSeekApi api = AiRequestBodyPatcher.configure(DeepSeekApi.builder()
                .baseUrl(baseUrl).apiKey("test-key"), patcher).build();
        return DeepSeekChatModel.builder()
                .deepSeekApi(api)
                .defaultOptions(DeepSeekChatOptions.builder().model("deepseek-chat").temperature(0.7).build())
                .toolCallingManager(DefaultToolCallingManager.builder().build())
                .retryTemplate(RetryTemplate.builder().maxAttempts(1).build())
                .build();
    }

    private static DeepSeekChatOptions toolOptions(List<String> invoked) {
        Function<EchoRequest, String> function = request -> {
            invoked.add(request.text());
            return "echo:" + request.text();
        };
        return DeepSeekChatOptions.builder()
                .model("deepseek-chat")
                .toolCallbacks(FunctionToolCallback.builder("echo", function)
                        .description("回显").inputType(EchoRequest.class).build())
                .build();
    }

    private static void assertDefaultRequest(CapturedRequest request) {
        // DeepSeekApi 默认的路径、鉴权、Content-Type 没有因替换 builder 而丢失
        assertEquals("/chat/completions", request.path);
        assertEquals("Bearer test-key", request.headers.get("Authorization"));
        assertTrue(request.headers.get("Content-Type").startsWith(MediaType.APPLICATION_JSON_VALUE));
    }

    private static void assertContentLength(CapturedRequest request) {
        String transferEncoding = request.headers.get("Transfer-Encoding");
        if (transferEncoding != null && transferEncoding.toLowerCase().contains("chunked")) {
            return; // 分块传输：请求体完整性已由 JSON 解析保证
        }
        assertNotNull(request.headers.get("Content-Length"), "既不是分块传输，也没有 Content-Length");
        assertEquals(request.body.length, Integer.parseInt(request.headers.get("Content-Length")));
    }

    private static HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static String syncStop(String content) {
        return "{\"id\":\"r1\",\"object\":\"chat.completion\",\"created\":1,\"model\":\"deepseek-chat\","
                + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"" + content + "\"},"
                + "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":1,\"total_tokens\":2}}";
    }

    private static String syncToolCall() {
        return "{\"id\":\"r0\",\"object\":\"chat.completion\",\"created\":1,\"model\":\"deepseek-chat\","
                + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"\","
                + "\"tool_calls\":[{\"id\":\"call_1\",\"type\":\"function\",\"function\":{\"name\":\"echo\","
                + "\"arguments\":\"{\\\"text\\\":\\\"hi\\\"}\"}}]},\"finish_reason\":\"tool_calls\"}],"
                + "\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":1,\"total_tokens\":2}}";
    }

    private static String streamChunk(String content, String finishReason) {
        return "{\"id\":\"c1\",\"object\":\"chat.completion.chunk\",\"created\":1,\"model\":\"deepseek-chat\","
                + "\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"" + content + "\"},"
                + "\"finish_reason\":" + (finishReason == null ? "null" : "\"" + finishReason + "\"") + "}]}";
    }

    private static String streamToolCall() {
        return "{\"id\":\"c0\",\"object\":\"chat.completion.chunk\",\"created\":1,\"model\":\"deepseek-chat\","
                + "\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"\","
                + "\"tool_calls\":[{\"index\":0,\"id\":\"call_1\",\"type\":\"function\",\"function\":{\"name\":\"echo\","
                + "\"arguments\":\"{\\\"text\\\":\\\"hi\\\"}\"}}]},\"finish_reason\":\"tool_calls\"}]}";
    }

    public record EchoRequest(String text) {
    }

    private record CapturedRequest(String path, Map<String, String> headers, byte[] body) {

        JsonNode json() throws Exception {
            return MAPPER.readTree(new String(body, StandardCharsets.UTF_8));
        }

    }

    private record StubResponse(int status, String contentType, String body) {

        static StubResponse json(int status, String body) {
            return new StubResponse(status, "application/json", body);
        }

        static StubResponse sse(String... chunks) {
            StringBuilder sb = new StringBuilder();
            for (String chunk : chunks) {
                sb.append("data: ").append(chunk).append("\n\n");
            }
            sb.append("data: [DONE]\n\n");
            return new StubResponse(200, "text/event-stream;charset=UTF-8", sb.toString());
        }

    }

}
