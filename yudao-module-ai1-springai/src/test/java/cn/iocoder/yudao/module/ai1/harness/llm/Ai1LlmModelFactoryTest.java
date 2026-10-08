package cn.iocoder.yudao.module.ai1.harness.llm;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring AI 1.x 适配回归：通过本地 HTTP 端点验证真实请求、流式分片和模型缓存。
 */
public class Ai1LlmModelFactoryTest {

    private final Ai1LlmModelFactory factory = new Ai1LlmModelFactory();
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    public void before() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.add(new Request(exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("X-Session"),
                    exchange.getRequestHeaders().getFirst("X-Provider"),
                    JsonUtils.parseTree(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8))));
            boolean embedding = exchange.getRequestURI().getPath().endsWith("/embeddings");
            String response = embedding
                    ? "{\"object\":\"list\",\"data\":[{\"object\":\"embedding\",\"index\":0,\"embedding\":[0.1,0.2]}],\"model\":\"test-model\",\"usage\":{\"prompt_tokens\":1,\"total_tokens\":1}}"
                    : sse("{\"role\":\"assistant\",\"reasoning_content\":\"想\"}")
                    + sse("{\"reasoning_content\":\"想\"}")
                    + sse("{\"reasoning_content\":\"想到\"}")
                    + sse("{\"content\":\"你好\"}")
                    + "data: [DONE]\n\n";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", embedding ? "application/json" : "text/event-stream");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    public void after() {
        server.stop(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/v1/", "/proxy/v1"})
    public void testChat_endpointHeadersAndReasoningDeltas(String prefix) {
        Ai1ModelRespBO model = model(baseUrl + prefix);
        Ai1LlmChatTool chatTool = new Ai1LlmChatTool();
        ReflectionTestUtils.setField(chatTool, "llmModelFactory", factory);
        List<String> thinking = new ArrayList<>();
        List<String> content = new ArrayList<>();

        Ai1LlmChatTool.ChatText result = chatTool.chat(model, "系统指令", List.<String[]>of(new String[]{"user", "历史"}),
                "当前消息", List.of(), List.of(), 42L, thinking::add, content::add);

        assertEquals("你好", result.getContent());
        assertEquals("想想想到", result.getThinking());
        assertEquals(List.of("想", "想", "想到"), thinking);
        assertEquals(List.of("你好"), content);
        assertEquals(1, requests.size());
        Request request = requests.get(0);
        String normalizedPrefix = prefix.isEmpty() ? "/v1" : prefix.replaceAll("/$", "");
        assertEquals(normalizedPrefix + "/chat/completions", request.path());
        assertEquals("Bearer test-key", request.authorization());
        assertEquals("42", request.session());
        assertEquals("provider", request.provider());
        assertEquals("test-model", request.body().get("model").asText());
        assertEquals(3, request.body().get("messages").size());
        assertTrue(request.body().get("stream").asBoolean());
    }

    @Test
    public void testEmbedding_endpointAndHeaders() {
        Ai1ModelRespBO model = model(baseUrl + "/proxy/v1/");
        EmbeddingModel embedding = factory.getOrCreateEmbeddingModel(model);

        assertArrayEquals(new float[]{0.1F, 0.2F}, embedding.embed("文档内容"));
        Request request = requests.get(0);
        assertEquals("/proxy/v1/embeddings", request.path());
        assertEquals("Bearer test-key", request.authorization());
        assertNull(request.session());
        assertEquals("provider", request.provider());
        assertEquals("test-model", request.body().get("model").asText());
        assertEquals("文档内容", request.body().get("input").get(0).asText());
    }

    @Test
    public void testCache_sessionIsolationAndInvalidation() {
        Ai1ModelRespBO model = model(baseUrl);
        Object chat = chatModel(model, 1L);
        assertSame(chat, chatModel(model, 1L));
        assertNotSame(chat, chatModel(model, 2L));
        EmbeddingModel embedding = factory.getOrCreateEmbeddingModel(model);
        assertSame(embedding, factory.getOrCreateEmbeddingModel(model));

        factory.evictByModelId(model.getModelId());
        assertNotSame(chat, chatModel(model, 1L));
        assertNotSame(embedding, factory.getOrCreateEmbeddingModel(model));
        model.setHeaders(List.of(Map.of("key", "X-Provider", "value", "provider")));
        chat = chatModel(model, 1L);
        assertSame(chat, chatModel(model, 2L));
        model.setApiKey("changed-key");
        assertNotSame(chat, chatModel(model, 1L));
        chat = chatModel(model, 1L);
        factory.evictByProviderId(model.getProviderId());
        assertNotSame(chat, chatModel(model, 1L));
    }

    private Object chatModel(Ai1ModelRespBO model, Long sessionId) {
        return ReflectionTestUtils.invokeMethod(factory, "getOrCreateChatModel", model, sessionId);
    }

    private static Ai1ModelRespBO model(String url) {
        return new Ai1ModelRespBO().setProviderId(1L).setModelId(2L).setBaseUrl(url).setApiKey("test-key")
                .setModel("test-model").setHeaders(List.of(Map.of("key", "X-Session", "value", "{session}"),
                        Map.of("key", "X-Provider", "value", "provider")));
    }

    private static String sse(String delta) {
        return "data: {\"id\":\"test\",\"object\":\"chat.completion.chunk\",\"created\":1,\"model\":\"test-model\","
                + "\"choices\":[{\"index\":0,\"delta\":" + delta + ",\"finish_reason\":null}]}\n\n";
    }

    private record Request(String path, String authorization, String session, String provider, JsonNode body) {
    }
}
