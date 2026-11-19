package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.deepseek;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Request;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Response;
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
 * {@link DeepSeekOcrModel} 的单元测试：通过本地 HTTP 桩服务验证真实发出的请求和对响应的处理
 *
 * @author 芋道源码
 */
public class DeepSeekOcrModelTest {

    private static final String API_KEY = "sk-deepseek-test-key-789";

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
        // 准备参数：前导换行符（保活），页码乱序，带 ref / box 标记
        server.enqueue(Response.json(200, "\n\n\n{\"id\":\"ocr-1\",\"object\":\"ocr.result\",\"purpose\":\"text_recognition\","
                + "\"page_count\":2,\"pages\":["
                + "{\"index\":1,\"text\":\"第二页\",\"finish_reason\":\"stop\"},"
                + "{\"index\":0,\"text\":\"<｜ref｜>标题<｜/ref｜><｜box｜>[[52, 40, 948, 96]]<｜/box｜>\\r\\n正文\","
                + "\"finish_reason\":\"stop\"}]}"));

        // 调用
        String text = buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：结果
        assertEquals("标题\n正文\n第二页", text);
        // 断言：请求只有 file、purpose 两个字段，file 带文件名
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/v1/tools/ocr", request.path());
        assertEquals("Bearer " + API_KEY, request.header("Authorization"));
        assertTrue(request.header("Content-Type").startsWith("multipart/form-data"));
        assertTrue(request.body().contains("name=\"file\"; filename=\"image.png\""), request.header("Content-Type"));
        assertTrue(request.body().contains("name=\"purpose\""));
        assertTrue(request.body().contains("text_recognition"));
        assertEquals(2, StrUtil.count(request.body(), "Content-Disposition"));
        assertFalse(request.body().contains("name=\"model\""));
    }

    @Test
    public void testCall_richLayout() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"pages\":[{\"index\":0,\"text\":\"x\",\"finish_reason\":\"stop\"}]}"));

        // 调用
        buildModel("rich_layout_recognition").call(buildPrompt(MimeTypeUtils.IMAGE_JPEG, IMAGE));

        // 断言
        Request request = server.lastRequest();
        assertTrue(request.body().contains("rich_layout_recognition"));
        assertTrue(request.body().contains("filename=\"image.jpeg\""));
    }

    @Test
    public void testCall_errorWithStatus200() {
        // 准备参数：识别开始后失败，HTTP 200 + error 对象
        server.enqueue(Response.json(200, "\n\n{\"error\":{\"message\":\"OCR did not finish in time\","
                + "\"type\":\"gateway_timeout_error\",\"param\":null,\"code\":\"gateway_timeout_error\"}}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.TIMEOUT, ex.getType());
        assertEquals("gateway_timeout_error", ex.getVendorCode());
        assertTrue(ex.getMessage().contains("OCR did not finish in time"));
    }

    @Test
    public void testCall_httpError() {
        // 准备参数
        server.enqueue(Response.json(429, "{\"error\":{\"message\":\"Too many requests\","
                + "\"type\":\"rate_limit_error\",\"code\":\"rate_limit_error\"}}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.RATE_LIMITED, ex.getType());
        assertFalse(ex.getMessage().contains(API_KEY));
    }

    @Test
    public void testCall_invalidImage() {
        // 调用，并断言异常：类型不支持、内容为空
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.parseMimeType("image/svg+xml"), IMAGE))).getType());
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class,
                () -> buildModel(null).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, new byte[0]))).getType());
        // 断言：没有发出请求
        assertTrue(server.requests().isEmpty());
    }

    @Test
    public void testConstructor_invalidConfig() {
        assertEquals(OcrException.Type.INVALID_CREDENTIAL, assertThrows(OcrException.class,
                () -> new DeepSeekOcrModel(null, null, null)).getType());
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> new DeepSeekOcrModel(API_KEY, null, "deepseek-ocr")).getType());
    }

    @Test
    public void testCleanText() {
        assertEquals("标题 正文", DeepSeekOcrModel.cleanText(
                "<｜ref｜>标题<｜/ref｜><｜box｜>[[1, 2, 3, 4]]<｜/box｜> 正文"));
        assertEquals("纯文本", DeepSeekOcrModel.cleanText("纯文本"));
    }

    /**
     * 真实调用：默认跳过；设置环境变量 YUDAO_OCR_DEEPSEEK_API_KEY 后才执行，只发 1 次请求（会计费）
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "YUDAO_OCR_DEEPSEEK_API_KEY", matches = ".+")
    public void testCall_live() {
        // 准备参数
        DeepSeekOcrModel model = new DeepSeekOcrModel(System.getenv("YUDAO_OCR_DEEPSEEK_API_KEY"), null, null);

        // 调用
        String text = model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：识别出图片中的 YUDAO OCR 2026
        System.out.println("[testCall_live][DeepSeek] " + text);
        String normalized = StrUtil.cleanBlank(text).toUpperCase();
        assertTrue(normalized.contains("YUDAO") && normalized.contains("2026"), text);
    }

    private DeepSeekOcrModel buildModel(String model) {
        return new DeepSeekOcrModel(API_KEY, server.baseUrl(), model, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    private static OcrPrompt buildPrompt(MimeType mimeType, byte[] image) {
        return new OcrPrompt(new Media(mimeType, new ByteArrayResource(image)));
    }

}
