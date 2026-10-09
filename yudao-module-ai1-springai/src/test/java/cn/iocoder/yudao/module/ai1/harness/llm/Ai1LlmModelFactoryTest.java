package cn.iocoder.yudao.module.ai1.harness.llm;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai1.service.model.bo.Ai1ModelRespBO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.JsonNode;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link Ai1LlmModelFactory} 的单元测试
 */
public class Ai1LlmModelFactoryTest {

    private final Ai1LlmModelFactory factory = new Ai1LlmModelFactory();
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private String baseUrl;
    private String requestedTool;

    @BeforeEach
    public void before() throws Exception {
        // mock HTTP 服务
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.add(new Request(exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("X-Session"),
                    exchange.getRequestHeaders().getFirst("X-Provider"),
                    JsonUtils.parseTree(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8))));
            boolean embedding = exchange.getRequestURI().getPath().endsWith("/embeddings");
            boolean callTool = requestedTool != null && requests.size() == 1;
            String response = callTool ? toolCallSse(requestedTool) : embedding
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
        // 准备参数
        Ai1ModelRespBO model = model(baseUrl + prefix);
        Ai1LlmChatTool chatTool = new Ai1LlmChatTool();
        ReflectionTestUtils.setField(chatTool, "llmModelFactory", factory);
        List<String> thinking = new ArrayList<>();
        List<String> content = new ArrayList<>();

        // 调用
        Ai1LlmChatTool.ChatText result = chatTool.chat(model, "系统指令", List.<String[]>of(new String[]{"user", "历史"}),
                "当前消息", List.of(), List.of(), 42L, thinking::add, content::add);

        // 断言
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
        // 准备参数
        Ai1ModelRespBO model = model(baseUrl + "/proxy/v1/");
        EmbeddingModel embedding = factory.getOrCreateEmbeddingModel(model);

        // 调用
        float[] result = embedding.embed("文档内容");
        // 断言
        assertArrayEquals(new float[]{0.1F, 0.2F}, result);
        Request request = requests.get(0);
        assertEquals("/proxy/v1/embeddings", request.path());
        assertEquals("Bearer test-key", request.authorization());
        assertNull(request.session());
        assertEquals("provider", request.provider());
        assertEquals("test-model", request.body().get("model").asText());
        assertEquals("文档内容", request.body().get("input").get(0).asText());
    }

    @Test
    public void testChat_skillCallback(@TempDir Path root) throws Exception {
        // 准备参数
        Files.writeString(root.resolve("SKILL.md"), "---\nname: probe\ndescription: test\n---\nunique-skill-content\n");
        ToolCallback skill = SkillsTool.builder().addSkillsDirectory(root.toString()).build();
        requestedTool = skill.getToolDefinition().name();
        Ai1LlmChatTool chat = new Ai1LlmChatTool();
        ReflectionTestUtils.setField(chat, "llmModelFactory", factory);

        // 调用
        Ai1LlmChatTool.ChatText result = chat.chat(model(baseUrl), "system", List.of(), "use skill",
                List.of(skill), List.of(), 42L, null, null);
        // 断言
        assertEquals("你好", result.getContent());
        assertEquals(2, requests.size());
        assertEquals(requestedTool, requests.get(0).body().get("tools").get(0).get("function").get("name").asText());
        boolean hasToolResult = false;
        for (JsonNode message : requests.get(1).body().get("messages")) {
            if ("tool".equals(message.get("role").asText())) {
                hasToolResult = true;
                assertTrue(message.get("content").asText().contains("unique-skill-content"));
            }
        }
        assertTrue(hasToolResult);
        assertEquals("42", requests.get(1).session());
    }

    @Test
    public void testCache_sessionIsolationAndInvalidation() {
        // 准备参数
        Ai1ModelRespBO model = model(baseUrl);

        // 调用
        Object chat = chatModel(model, 1L);
        // 断言
        assertSame(chat, chatModel(model, 1L));
        assertNotSame(chat, chatModel(model, 2L));
        EmbeddingModel embedding = factory.getOrCreateEmbeddingModel(model);
        assertSame(embedding, factory.getOrCreateEmbeddingModel(model));

        // 调用
        factory.evictByModelId(model.getModelId());
        // 断言
        assertNotSame(chat, chatModel(model, 1L));
        assertNotSame(embedding, factory.getOrCreateEmbeddingModel(model));

        // 准备参数
        model.setHeaders(Map.of("X-Provider", "provider"));
        // 调用
        chat = chatModel(model, 1L);
        // 断言
        assertSame(chat, chatModel(model, 2L));

        // 调用
        model.setApiKey("changed-key");
        // 断言
        assertNotSame(chat, chatModel(model, 1L));

        // 调用
        chat = chatModel(model, 1L);
        factory.evictByProviderId(model.getProviderId());
        // 断言
        assertNotSame(chat, chatModel(model, 1L));
    }

    // ========== 工具方法 ==========

    private Object chatModel(Ai1ModelRespBO model, Long sessionId) {
        return ReflectionTestUtils.invokeMethod(factory, "getOrCreateChatModel", model, sessionId);
    }

    private static Ai1ModelRespBO model(String url) {
        return new Ai1ModelRespBO().setProviderId(1L).setModelId(2L).setBaseUrl(url).setApiKey("test-key")
                .setModel("test-model").setHeaders(Map.of("X-Session", "{session}", "X-Provider", "provider"));
    }

    private static String sse(String delta) {
        return "data: {\"id\":\"test\",\"object\":\"chat.completion.chunk\",\"created\":1,\"model\":\"test-model\","
                + "\"choices\":[{\"index\":0,\"delta\":" + delta + ",\"finish_reason\":null}]}\n\n";
    }

    private static String toolCallSse(String name) {
        String delta = JsonUtils.toJsonString(Map.of("role", "assistant", "tool_calls", List.of(Map.of(
                "index", 0, "id", "call-probe", "type", "function", "function", Map.of(
                        "name", name, "arguments", "{\"command\":\"probe\"}")))));
        return sse(delta)
                + "data: {\"id\":\"test\",\"object\":\"chat.completion.chunk\",\"created\":1,\"model\":\"test-model\","
                + "\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"tool_calls\"}]}\n\n"
                + "data: [DONE]\n\n";
    }

    private record Request(String path, String authorization, String session, String provider, JsonNode body) {
    }
}
