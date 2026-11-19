package cn.iocoder.yudao.module.ai.compatibility;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.AiModelFactoryImpl;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.openai.OpenAiDelegateChatModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageApi;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.siliconflow.SiliconFlowImageOptions;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StreamUtils;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring AI JDK8 路线的本地 HTTP 协议回归：业务工厂创建的客户端请求本地 HttpServer，不访问外部模型
 *
 * Gemini 在这条路线上保持 OpenAI 兼容方式，且地址固定为官方地址，无法指向本地，因此不在这里覆盖
 *
 * @author 芋道源码
 */
public class CrossVersionAiHttpTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> azureApiKey = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private HttpServer server;
    private String baseUrl;
    private volatile String response;
    private volatile String contentType;

    private ApplicationContext previousContext;
    private GenericApplicationContext context;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            azureApiKey.set(exchange.getRequestHeaders().getFirst("api-key"));
            requestPath.set(exchange.getRequestURI().toString());
            requestBody.set(StreamUtils.copyToString(exchange.getRequestBody(), StandardCharsets.UTF_8));
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            // 逐字节写出并 flush，验证客户端能正确拼接被拆开的 UTF-8 字符和 SSE 事件
            try (OutputStream out = exchange.getResponseBody()) {
                for (byte value : bytes) {
                    out.write(value);
                    out.flush();
                }
            } finally {
                exchange.close();
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();

        // AiModelFactoryImpl 通过 SpringUtil 获取 ToolCallingManager 等 Bean，这里提供一个最小的容器
        context = new GenericApplicationContext();
        context.registerBean(ToolCallingManager.class, () -> ToolCallingManager.builder().build());
        context.refresh();
        previousContext = SpringUtil.getApplicationContext();
        new SpringUtil().setApplicationContext(context);
    }

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
        new SpringUtil().setApplicationContext(previousContext);
        if (context != null) {
            context.close();
        }
    }

    // ========== OpenAI ==========

    /**
     * 数据库中的 OpenAI 地址沿用 Spring AI 1.x 的写法（不带 /v1），请求路径仍需是 /v1/chat/completions
     */
    @Test
    void openAiChatWithLegacyUrlPreservesPathTextUsageAndCredentials() {
        ChatModel model = new AiModelFactoryImpl().getOrCreateChatModel(AiPlatformEnum.OPENAI, "local-test-key",
                baseUrl, null);
        assertInstanceOf(OpenAiDelegateChatModel.class, model);
        contentType = "application/json";
        response = "{\"id\":\"local-call\",\"object\":\"chat.completion\",\"created\":1,\"model\":\"local-model\","
                + "\"choices\":[{\"index\":0,\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\",\"content\":\"hello\"}}],"
                + "\"usage\":{\"prompt_tokens\":2,\"completion_tokens\":3,\"total_tokens\":5}}";

        ChatResponse result = model.call(prompt());

        assertEquals("hello", result.getResult().getOutput().getText());
        assertEquals(5, result.getMetadata().getUsage().getTotalTokens());
        assertEquals("/v1/chat/completions", requestPath.get());
        assertRequest();
    }

    /**
     * 已带 /v1 的地址不重复拼接
     */
    @Test
    void openAiChatWithV1UrlDoesNotDuplicatePath() {
        ChatModel model = new AiModelFactoryImpl().getOrCreateChatModel(AiPlatformEnum.OPENAI, "local-test-key",
                baseUrl + "/v1/", null);
        contentType = "application/json";
        response = "{\"id\":\"local-call\",\"object\":\"chat.completion\",\"created\":1,\"model\":\"local-model\","
                + "\"choices\":[{\"index\":0,\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\",\"content\":\"ok\"}}]}";

        assertEquals("ok", model.call(prompt()).getResult().getOutput().getText());
        assertEquals("/v1/chat/completions", requestPath.get());
    }

    @Test
    void openAiStreamingPreservesUtf8Deltas() {
        ChatModel model = new AiModelFactoryImpl().getOrCreateChatModel(AiPlatformEnum.OPENAI, "local-test-key",
                baseUrl, null);
        contentType = "text/event-stream";
        response = "data: " + chunk("汉", false, null) + "\r\n\r\n"
                + "data: " + chunk("字", true, null) + "\r\n\r\n"
                + "data: [DONE]\r\n\r\n";

        List<ChatResponse> results = model.stream(prompt()).collectList().block(Duration.ofSeconds(10));

        assertNotNull(results);
        assertEquals("汉字", joinText(results));
        assertEquals("/v1/chat/completions", requestPath.get());
        assertRequest();
    }

    /**
     * Spring AI 2.x 的 reasoningContent 元数据是累计值，业务需要的是增量（OpenAiDelegateChatModel 计算）
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void openAiStreamingPreservesReasoningDeltas(boolean useDefaultModel) {
        contentType = "text/event-stream";
        response = "data: " + chunk("one", false, "first") + "\n\n"
                + "data: " + chunk("two", true, "second") + "\n\n"
                + "data: [DONE]\n\n";
        ChatModel model;
        if (useDefaultModel) {
            // 默认模型来自 Spring AI 自动配置的 OpenAiChatModel Bean，其 base-url 需带 /v1
            OpenAiChatModel nativeModel = OpenAiChatModel.builder()
                    .options(OpenAiChatOptions.builder().apiKey("local-test-key").model("local-model")
                            .baseUrl(baseUrl + "/v1").build())
                    .build();
            context.getBeanFactory().registerSingleton("openAiChatModel", nativeModel);
            model = new AiModelFactoryImpl().getDefaultChatModel(AiPlatformEnum.OPENAI);
        } else {
            model = new AiModelFactoryImpl().getOrCreateChatModel(AiPlatformEnum.OPENAI, "local-test-key",
                    baseUrl, null);
        }

        List<ChatResponse> results = model.stream(prompt()).collectList().block(Duration.ofSeconds(10));

        assertNotNull(results);
        StringBuilder reasoning = new StringBuilder();
        for (ChatResponse item : results) {
            String delta = AiChatUtils.getChatResponseReasoningContent(item);
            if (delta != null) {
                reasoning.append(delta);
            }
        }
        assertEquals("firstsecond", reasoning.toString());
        assertEquals("onetwo", joinText(results));
        assertEquals("Bearer local-test-key", authorization.get());
    }

    // ========== Azure OpenAI：由 OpenAI 官方 SDK 的 Azure 模式承担 ==========

    @Test
    void azureChatPreservesDeploymentAndApiKeyHeader() {
        ChatModel model = new AiModelFactoryImpl().getOrCreateChatModel(AiPlatformEnum.AZURE, "local-test-key",
                baseUrl, null);
        contentType = "application/json";
        response = "{\"id\":\"local-call\",\"object\":\"chat.completion\",\"created\":1,\"model\":\"local-deployment\","
                + "\"choices\":[{\"index\":0,\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\",\"content\":\"azure\"}}]}";

        ChatResponse result = model.call(new Prompt("question", AiChatUtils.buildChatOptions(AiPlatformEnum.AZURE,
                "local-deployment", 0.2, 128, null, null, false, null)));

        assertEquals("azure", result.getResult().getOutput().getText());
        assertEquals("local-test-key", azureApiKey.get());
        assertTrue(requestPath.get().startsWith("/openai/deployments/local-deployment/chat/completions"),
                requestPath.get());
    }

    @Test
    void azureEmbeddingPreservesDeploymentAndApiKeyHeader() {
        EmbeddingModel embedding = new AiModelFactoryImpl().getOrCreateEmbeddingModel(AiPlatformEnum.AZURE,
                "local-test-key", baseUrl, "local-deployment");
        contentType = "application/json";
        response = "{\"object\":\"list\",\"model\":\"local-deployment\",\"data\":[{\"object\":\"embedding\",\"index\":0,"
                + "\"embedding\":[1.0,0.5]}],\"usage\":{\"prompt_tokens\":2,\"total_tokens\":2}}";

        EmbeddingResponse result = embedding.call(new EmbeddingRequest(Collections.singletonList("question"), null));

        assertArrayEquals(new float[]{1.0f, 0.5f}, result.getResult().getOutput());
        assertEquals("local-test-key", azureApiKey.get());
        assertTrue(requestPath.get().startsWith("/openai/deployments/local-deployment/embeddings"), requestPath.get());
        assertTrue(requestBody.get().contains("question"));
    }

    // ========== Grok：OpenAI 兼容 + 自定义地址 ==========

    @Test
    void grokChatPreservesCustomUrlAndPath() {
        ChatModel model = new AiModelFactoryImpl().getOrCreateChatModel(AiPlatformEnum.GROK, "local-test-key",
                baseUrl, null);
        assertInstanceOf(OpenAiDelegateChatModel.class, model);
        contentType = "application/json";
        response = "{\"id\":\"local-call\",\"object\":\"chat.completion\",\"created\":1,\"model\":\"grok\","
                + "\"choices\":[{\"index\":0,\"finish_reason\":\"stop\",\"message\":{\"role\":\"assistant\",\"content\":\"grok\"}}]}";

        ChatResponse result = model.call(new Prompt("question", AiChatUtils.buildChatOptions(AiPlatformEnum.GROK,
                "local-model", 0.2, 128, null, null, false, null)));

        assertEquals("grok", result.getResult().getOutput().getText());
        assertEquals("/v1/chat/completions", requestPath.get());
        assertRequest();
    }

    // ========== 硅基流动绘图 ==========

    @Test
    void siliconFlowImagePreservesDimensionsAndReferenceImage() throws Exception {
        verifySiliconFlowImage(SiliconFlowImageOptions.builder().model("local-image").width(1024).height(768)
                .image("data:image/png;base64,aGVsbG8=").build(), "1024x768", "data:image/png;base64,aGVsbG8=");
    }

    @Test
    void siliconFlowImagePreservesExplicitImageSize() throws Exception {
        verifySiliconFlowImage(SiliconFlowImageOptions.builder().model("local-image").imageSize("960x1280").build(),
                "960x1280", null);
    }

    private void verifySiliconFlowImage(SiliconFlowImageOptions options, String size, String reference) throws Exception {
        contentType = "application/json";
        response = "{\"created\":1,\"data\":[{\"url\":\"https://example.test/test.png\"}]}";
        SiliconFlowImageModel imageModel = new SiliconFlowImageModel(new SiliconFlowImageApi(baseUrl, "local-test-key"));

        ImageResponse result = imageModel.call(new ImagePrompt("image question", options));

        assertEquals("https://example.test/test.png", result.getResult().getOutput().getUrl());
        assertEquals("/v1/images/generations", requestPath.get());
        JsonNode request = OBJECT_MAPPER.readTree(requestBody.get());
        assertEquals("image question", request.path("prompt").asText());
        assertEquals("local-image", request.path("model").asText());
        assertEquals(size, request.path("image_size").asText());
        if (reference != null) {
            assertEquals(reference, request.path("image").asText());
        } else {
            assertFalse(request.has("image"));
        }
        assertEquals("Bearer local-test-key", authorization.get());
    }

    // ========== 通义（DashScope 原生协议） ==========

    @Test
    void dashScopeEmbeddingPreservesNativeProtocol() throws Exception {
        contentType = "application/json";
        response = "{\"request_id\":\"local-embedding\",\"output\":{\"embeddings\":[{\"text_index\":0,\"embedding\":[1.0,0.5]}]},"
                + "\"usage\":{\"total_tokens\":2}}";
        DashScopeApi.Builder local = DashScopeApi.builder().baseUrl(baseUrl);
        try (MockedStatic<DashScopeApi> api = Mockito.mockStatic(DashScopeApi.class, Mockito.CALLS_REAL_METHODS)) {
            api.when(DashScopeApi::builder).thenReturn(local);
            EmbeddingModel embedding = new AiModelFactoryImpl().getOrCreateEmbeddingModel(AiPlatformEnum.ALIBABA,
                    "local-test-key", baseUrl, "text-embedding-v3");

            EmbeddingResponse result = embedding.call(new EmbeddingRequest(Collections.singletonList("question"), null));

            assertArrayEquals(new float[]{1.0f, 0.5f}, result.getResult().getOutput());
            assertEquals("Bearer local-test-key", authorization.get());
            JsonNode body = OBJECT_MAPPER.readTree(requestBody.get());
            assertEquals("text-embedding-v3", body.path("model").asText());
            assertEquals("question", body.path("input").path("texts").get(0).asText());
        }
    }

    @Test
    void dashScopeImagePreservesNativeProtocol() throws Exception {
        contentType = "application/json";
        response = "{\"request_id\":\"local-image\",\"output\":{\"task_id\":\"local-task\",\"task_status\":\"SUCCEEDED\","
                + "\"results\":[{\"url\":\"https://example.test/native.png\"}]},\"usage\":{\"image_count\":1}}";
        DashScopeImageApi.Builder local = DashScopeImageApi.builder().baseUrl(baseUrl);
        try (MockedStatic<DashScopeImageApi> api = Mockito.mockStatic(DashScopeImageApi.class, Mockito.CALLS_REAL_METHODS)) {
            api.when(DashScopeImageApi::builder).thenReturn(local);
            ImageModel image = new AiModelFactoryImpl().getOrCreateImageModel(AiPlatformEnum.ALIBABA,
                    "local-test-key", baseUrl, null);

            ImageResponse result = image.call(new ImagePrompt("image question"));

            assertEquals("https://example.test/native.png", result.getResult().getOutput().getUrl());
            assertEquals("Bearer local-test-key", authorization.get());
        }
    }

    // ========== 工具方法 ==========

    private static Prompt prompt() {
        return new Prompt("question", OpenAiChatOptions.builder().model("local-model").build());
    }

    private void assertRequest() {
        assertEquals("Bearer local-test-key", authorization.get());
        assertTrue(requestBody.get().contains("local-model"), requestBody.get());
        assertTrue(requestBody.get().contains("question"), requestBody.get());
    }

    private static String joinText(List<ChatResponse> results) {
        StringBuilder text = new StringBuilder();
        for (ChatResponse item : results) {
            if (item.getResult() != null && item.getResult().getOutput().getText() != null) {
                text.append(item.getResult().getOutput().getText());
            }
        }
        return text.toString();
    }

    private static String chunk(String text, boolean stop, String reasoning) {
        return "{\"id\":\"local-stream\",\"object\":\"chat.completion.chunk\",\"created\":1,\"model\":\"local-model\","
                + "\"choices\":[{\"index\":0,\"finish_reason\":" + (stop ? "\"stop\"" : "null")
                + ",\"delta\":{\"role\":\"assistant\",\"content\":\"" + text + "\""
                + (reasoning != null ? ",\"reasoning_content\":\"" + reasoning + "\"" : "")
                + "}}]}";
    }

}
