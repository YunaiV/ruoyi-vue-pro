package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.siliconflow;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
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
 * {@link SiliconFlowOcrModel} 的单元测试：通过本地 HTTP 桩服务验证真实发出的请求和对响应的处理
 *
 * @author 芋道源码
 */
public class SiliconFlowOcrModelTest {

    private static final String API_KEY = "sk-siliconflow-test-key-456";

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
                {"id":"0192","object":"chat.completion","model":"deepseek-ai/DeepSeek-OCR",\
                "choices":[{"index":0,"message":{"role":"assistant","content":"\\nHello OCR\\n硅基流动\\n"}}]}"""));

        // 调用
        String text = buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：结果
        assertEquals("Hello OCR\n硅基流动", text);
        // 断言：请求
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/v1/chat/completions", request.path());
        assertEquals("Bearer " + API_KEY, request.header("Authorization"));
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals("deepseek-ai/DeepSeek-OCR", body.path("model").asText());
        assertFalse(body.path("stream").asBoolean(true));
        JsonNode content = body.path("messages").get(0).path("content");
        assertEquals("data:image/png;base64," + Base64.encode(IMAGE), content.get(0).path("image_url").path("url").asText());
        assertEquals("Free OCR.", content.get(1).path("text").asText());
    }

    @Test
    public void testCall_otherOcrModel() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"choices\":[{\"message\":{\"content\":\"x\"}}]}"));

        // 调用
        buildModel("PaddlePaddle/PaddleOCR-VL").call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));

        // 断言
        assertEquals("PaddlePaddle/PaddleOCR-VL", JsonUtils.parseTree(server.lastRequest().body()).path("model").asText());
    }

    @Test
    public void testCall_businessError() {
        // 准备参数：硅基流动的错误体 {"code":20015,"message":"...","data":null}
        server.enqueue(Response.json(400, """
                {"code":20015,"message":"The parameter is invalid. Please check again.","data":null}"""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.UPSTREAM_ERROR, ex.getType());
        assertEquals("20015", ex.getVendorCode());
        assertTrue(ex.getMessage().contains("The parameter is invalid"));
    }

    @Test
    public void testCall_unauthorized() {
        // 准备参数
        server.enqueue(Response.json(401, "\"Invalid token\""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.AUTH_FAILED, ex.getType());
        assertFalse(ex.getMessage().contains(API_KEY));
    }

    @Test
    public void testCall_rateLimited() {
        // 准备参数
        server.enqueue(Response.json(429, "{\"code\":50603,\"message\":\"System is too busy now.\"}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.RATE_LIMITED, ex.getType());
        assertEquals("50603", ex.getVendorCode());
    }

    @Test
    public void testCall_timeout() {
        // 准备参数
        server.enqueue(Response.delayed(1500));
        SiliconFlowOcrModel model = new SiliconFlowOcrModel(API_KEY, server.baseUrl(), null,
                Duration.ofSeconds(1), Duration.ofMillis(300));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class, () -> model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.TIMEOUT, ex.getType());
    }

    @Test
    public void testCall_invalidImage() {
        // 调用，并断言异常：类型不支持、内容为空
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_GIF, IMAGE))).getType());
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, new byte[0]))).getType());
        // 断言：没有发出请求
        assertTrue(server.requests().isEmpty());
    }

    @Test
    public void testConstructor_invalidConfig() {
        assertEquals(OcrException.Type.INVALID_CREDENTIAL, assertThrows(OcrException.class,
                () -> new SiliconFlowOcrModel(null, null, null)).getType());
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> new SiliconFlowOcrModel(API_KEY, null, "deepseek-ai/DeepSeek-V3")).getType());
    }

    /**
     * 真实调用：默认跳过；设置环境变量 YUDAO_OCR_SILICONFLOW_API_KEY 后才执行，只发 1 次请求（会计费）
     *
     * 可选：YUDAO_OCR_SILICONFLOW_URL、YUDAO_OCR_SILICONFLOW_MODEL
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "YUDAO_OCR_SILICONFLOW_API_KEY", matches = ".+")
    public void testCall_live() {
        // 准备参数
        SiliconFlowOcrModel model = new SiliconFlowOcrModel(System.getenv("YUDAO_OCR_SILICONFLOW_API_KEY"),
                System.getenv("YUDAO_OCR_SILICONFLOW_URL"), System.getenv("YUDAO_OCR_SILICONFLOW_MODEL"));

        // 调用
        String text = model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：识别出图片中的 YUDAO OCR 2026
        System.out.println("[testCall_live][SiliconFlow] " + text);
        String normalized = StrUtil.cleanBlank(text).toUpperCase();
        assertTrue(normalized.contains("YUDAO") && normalized.contains("2026"), text);
    }

    private SiliconFlowOcrModel buildModel(String model) {
        return new SiliconFlowOcrModel(API_KEY, server.baseUrl(), model, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    private static OcrPrompt buildPrompt(MimeType mimeType, byte[] image) {
        return new OcrPrompt(new Media(mimeType, new ByteArrayResource(image)));
    }

}
