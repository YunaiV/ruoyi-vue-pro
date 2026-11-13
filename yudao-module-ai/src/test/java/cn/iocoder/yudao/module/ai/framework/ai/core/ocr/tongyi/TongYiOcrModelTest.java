package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.tongyi;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Request;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Response;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TongYiOcrModel} 的单元测试：DashScope SDK 真实发请求到本地 HTTP 桩服务，验证请求和对响应的处理
 *
 * @author 芋道源码
 */
public class TongYiOcrModelTest {

    private static final String API_KEY = "sk-tongyi-test-key-123";
    private static final String API_PATH = "/api/v1/services/aigc/multimodal-generation/generation";

    private static final byte[] IMAGE = ResourceUtil.readBytes("ocr/yudao-ocr-2026.png");

    private OcrStubServer server;

    @BeforeEach
    public void setUp() throws Exception {
        server = new OcrStubServer();
    }

    @AfterEach
    public void tearDown() {
        server.close();
    }

    @Test
    public void testCall_success() {
        // 准备参数
        server.enqueue(Response.json(200, """
                {"output":{"choices":[{"finish_reason":"stop","message":{"role":"assistant",\
                "content":[{"text":"第一行\\r\\n第二行  "}]}}]},"request_id":"req-1"}"""));

        // 调用
        OcrResponse response = buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));

        // 断言：结果（统一换行、去首尾空白）
        assertEquals("第一行\n第二行", response.getResult().getOutput());
        assertEquals(1, response.getResults().size());
        // 断言：请求
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals(API_PATH, request.path());
        assertEquals("Bearer " + API_KEY, request.header("Authorization"));
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals(TongYiOcrModel.DEFAULT_MODEL, body.path("model").asText());
        JsonNode message = body.path("input").path("messages").get(0);
        assertEquals("user", message.path("role").asText());
        assertEquals("data:image/png;base64," + Base64.encode(IMAGE), message.path("content").get(0).path("image").asText());
        assertEquals(TongYiOcrModel.PROMPT, message.path("content").get(1).path("text").asText());
    }

    @Test
    public void testCall_customModelAndUrlPrefix() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"output\":{\"choices\":[{\"message\":{\"content\":[{\"text\":\"ok\"}]}}]}}"));
        TongYiOcrModel model = new TongYiOcrModel(API_KEY, server.baseUrl() + "/proxy/", "qwen3.5-ocr",
                Duration.ofSeconds(2), Duration.ofSeconds(5));

        // 调用
        String text = model.call(buildPrompt(MimeTypeUtils.IMAGE_JPEG, IMAGE)).getResult().getOutput();

        // 断言
        assertEquals("ok", text);
        Request request = server.lastRequest();
        assertEquals("/proxy" + API_PATH, request.path());
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals("qwen3.5-ocr", body.path("model").asText());
        assertTrue(body.path("input").path("messages").get(0).path("content").get(0).path("image").asText()
                .startsWith("data:image/jpeg;base64,"));
    }

    @Test
    public void testCall_emptyContent() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"output\":{\"choices\":[{\"message\":{\"content\":[]}}]}}"));

        // 调用，并断言
        assertEquals("", buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput());
    }

    @Test
    public void testCall_unauthorized() {
        // 准备参数：百炼的错误体；消息里故意带上密钥，验证会被去掉
        server.enqueue(Response.json(401, """
                {"code":"InvalidApiKey","message":"Invalid API-key provided: sk-tongyi-test-key-123","request_id":"req-401"}"""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.AUTH_FAILED, ex.getType());
        assertEquals("InvalidApiKey", ex.getVendorCode());
        assertEquals("req-401", ex.getRequestId());
        assertTrue(ex.getMessage().contains("HTTP 401"));
        assertTrue(ex.getMessage().contains("Invalid API-key provided"));
        assertFalse(ex.getMessage().contains(API_KEY));
        assertFalse(ex.getMessage().contains(server.hostAndPort()));
        assertNull(ex.getCause());
    }

    @Test
    public void testCall_rateLimited() {
        // 准备参数
        server.enqueue(Response.json(429, """
                {"code":"Throttling.RateQuota","message":"Requests rate limit exceeded","request_id":"req-429"}"""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.RATE_LIMITED, ex.getType());
        assertTrue(ex.getType().isRetryable());
        assertEquals("Throttling.RateQuota", ex.getVendorCode());
    }

    @Test
    public void testCall_serverErrorWithoutJson() {
        // 准备参数
        server.enqueue(new Response(502, "text/html", "<html>bad gateway</html>", 0));

        // 调用，并断言异常：非 JSON 的响应体不进异常消息
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.UPSTREAM_ERROR, ex.getType());
        assertTrue(ex.getMessage().contains("HTTP 502"));
        assertFalse(ex.getMessage().contains("<html>"));
    }

    @Test
    public void testCall_timeout() {
        // 准备参数
        server.enqueue(Response.delayed(1500));
        TongYiOcrModel model = new TongYiOcrModel(API_KEY, server.baseUrl(), null,
                Duration.ofSeconds(1), Duration.ofMillis(300));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class, () -> model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.TIMEOUT, ex.getType());
        assertFalse(ex.getMessage().contains(server.hostAndPort()));
        assertNull(ex.getCause());
        assertEquals(1, server.requests().size()); // 超时不重发，避免重复计费
    }

    @Test
    public void testCall_malformedResponse() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"request_id\":\"req-x\"}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.UPSTREAM_ERROR, ex.getType());
    }

    @Test
    public void testCall_invalidImage() {
        // 调用，并断言异常：类型不支持
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_GIF, IMAGE))).getType());
        // 调用，并断言异常：base64 后约 10MB + data URL 前缀，超过上限
        byte[] tooLarge = new byte[(int) (TongYiOcrModel.MAX_DATA_URL_LENGTH / 4 * 3)];
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, tooLarge))).getType());
        // 断言：没有发出请求
        assertTrue(server.requests().isEmpty());
    }

    @Test
    public void testConstructor_invalidConfig() {
        assertEquals(OcrException.Type.INVALID_CREDENTIAL, assertThrows(OcrException.class,
                () -> new TongYiOcrModel(" ", null, null)).getType());
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> new TongYiOcrModel(API_KEY, null, "qwen-plus")).getType());
    }

    /**
     * 真实调用：默认跳过；设置环境变量 YUDAO_OCR_TONGYI_API_KEY 后才执行，只发 1 次请求（会计费）
     *
     * 可选：YUDAO_OCR_TONGYI_URL、YUDAO_OCR_TONGYI_MODEL
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "YUDAO_OCR_TONGYI_API_KEY", matches = ".+")
    public void testCall_live() {
        // 准备参数
        TongYiOcrModel model = new TongYiOcrModel(System.getenv("YUDAO_OCR_TONGYI_API_KEY"),
                System.getenv("YUDAO_OCR_TONGYI_URL"), System.getenv("YUDAO_OCR_TONGYI_MODEL"));

        // 调用
        String text = model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：识别出图片中的 YUDAO OCR 2026
        System.out.println("[testCall_live][TongYi] " + text);
        String normalized = StrUtil.cleanBlank(text).toUpperCase();
        assertTrue(normalized.contains("YUDAO") && normalized.contains("2026"), text);
    }

    private TongYiOcrModel buildModel(String model) {
        return new TongYiOcrModel(" " + API_KEY + " ", server.baseUrl(), model, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    private static OcrPrompt buildPrompt(MimeType mimeType, byte[] image) {
        return new OcrPrompt(new Media(mimeType, new ByteArrayResource(image)));
    }

}
