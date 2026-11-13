package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.tencent;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TencentOcrModel} 的单元测试：官方 SDK 真实发请求到本地 HTTP 桩服务，验证请求（签名头、Action、请求体）和响应处理
 *
 * @author 芋道源码
 */
public class TencentOcrModelTest {

    private static final String SECRET_ID = "AKIDtencentTestId001";
    private static final String SECRET_KEY = "tencentTestSecretKey001";
    private static final String API_KEY = SECRET_ID + "|" + SECRET_KEY;

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
                {"Response":{"TextDetections":[{"DetectedText":"腾讯云","Confidence":99},\
                {"DetectedText":"通用文字识别 ","Confidence":98}],"Angle":0,"RequestId":"req-tc-1"}}"""));

        // 调用
        String text = buildModel(API_KEY).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：结果
        assertEquals("腾讯云\n通用文字识别", text);
        // 断言：请求
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/", request.path());
        assertEquals("GeneralAccurateOCR", request.header("X-TC-Action"));
        assertEquals("2018-11-19", request.header("X-TC-Version"));
        assertEquals(TencentOcrModel.DEFAULT_REGION, request.header("X-TC-Region"));
        // 断言：TC3 签名只带 SecretId，不带 SecretKey
        String authorization = request.header("Authorization");
        assertTrue(authorization.startsWith("TC3-HMAC-SHA256 Credential=" + SECRET_ID + "/"), authorization);
        assertFalse(authorization.contains(SECRET_KEY));
        assertFalse(request.body().contains(SECRET_KEY));
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals(Base64.encode(IMAGE), body.path("ImageBase64").asText());
        assertTrue(body.path("ImageUrl").isMissingNode() || body.path("ImageUrl").isNull());
    }

    @Test
    public void testCall_imageNoTextReturnsEmpty() {
        // 准备参数
        server.enqueue(Response.json(200, """
                {"Response":{"Error":{"Code":"FailedOperation.ImageNoText","Message":"图片中未检测到文本。"},\
                "RequestId":"req-tc-2"}}"""));

        // 调用，并断言
        assertEquals("", buildModel(API_KEY).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput());
    }

    @Test
    public void testCall_authFailure() {
        // 准备参数
        server.enqueue(Response.json(200, """
                {"Response":{"Error":{"Code":"AuthFailure.SecretIdNotFound","Message":"The SecretId is not found"},\
                "RequestId":"req-tc-3"}}"""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(API_KEY).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.AUTH_FAILED, ex.getType());
        assertEquals("AuthFailure.SecretIdNotFound", ex.getVendorCode());
        assertEquals("req-tc-3", ex.getRequestId());
        assertTrue(ex.getMessage().contains("The SecretId is not found"));
        assertFalse(ex.getMessage().contains(SECRET_KEY));
    }

    @Test
    public void testCall_rateLimited() {
        // 准备参数
        server.enqueue(Response.json(200, """
                {"Response":{"Error":{"Code":"RequestLimitExceeded","Message":"请求的次数超过了频率限制。"},\
                "RequestId":"req-tc-4"}}"""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(API_KEY).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.RATE_LIMITED, ex.getType());
    }

    @Test
    public void testCall_imageDecodeFailed() {
        // 准备参数
        server.enqueue(Response.json(200, """
                {"Response":{"Error":{"Code":"FailedOperation.ImageDecodeFailed","Message":"图片解码失败。"},\
                "RequestId":"req-tc-5"}}"""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(API_KEY).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.INVALID_IMAGE, ex.getType());
        assertEquals("FailedOperation.ImageDecodeFailed", ex.getVendorCode());
    }

    @Test
    public void testCall_timeout() {
        // 准备参数
        server.enqueue(Response.delayed(2500));
        TencentOcrModel model = new TencentOcrModel(API_KEY, server.baseUrl(), null,
                Duration.ofSeconds(1), Duration.ofSeconds(1));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class, () -> model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.TIMEOUT, ex.getType());
        assertFalse(ex.getMessage().contains(SECRET_KEY));
    }

    @Test
    public void testCall_credentialsIsolatedBetweenInstances() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"Response\":{\"TextDetections\":[],\"RequestId\":\"a\"}}"))
                .enqueue(Response.json(200, "{\"Response\":{\"TextDetections\":[],\"RequestId\":\"b\"}}"))
                .enqueue(Response.json(200, "{\"Response\":{\"TextDetections\":[],\"RequestId\":\"c\"}}"));
        TencentOcrModel modelA = buildModel("AKIDaaaa|secretA");
        TencentOcrModel modelB = buildModel("AKIDbbbb|secretB");

        // 调用
        modelA.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));
        modelB.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));
        modelA.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));

        // 断言
        List<Request> requests = server.requests();
        assertTrue(requests.get(0).header("Authorization").contains("Credential=AKIDaaaa/"));
        assertTrue(requests.get(1).header("Authorization").contains("Credential=AKIDbbbb/"));
        assertTrue(requests.get(2).header("Authorization").contains("Credential=AKIDaaaa/"));
    }

    @Test
    public void testCall_invalidImage() {
        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class, () -> buildModel(API_KEY)
                .call(buildPrompt(MimeTypeUtils.parseMimeType("image/webp"), IMAGE)));
        assertEquals(OcrException.Type.INVALID_IMAGE, ex.getType());
        // 断言：没有发出请求
        assertTrue(server.requests().isEmpty());
    }

    @Test
    public void testConstructor_invalidConfig() {
        for (String apiKey : new String[]{null, "", SECRET_ID, SECRET_ID + "|", "|" + SECRET_KEY, API_KEY + "|x"}) {
            OcrException ex = assertThrows(OcrException.class, () -> buildModel(apiKey), "apiKey=" + apiKey);
            assertEquals(OcrException.Type.INVALID_CREDENTIAL, ex.getType());
            assertFalse(ex.getMessage().contains(SECRET_KEY));
            assertFalse(ex.getMessage().contains(SECRET_ID));
        }
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> new TencentOcrModel(API_KEY, null, "GeneralBasicOCR")).getType());
    }

    /**
     * 真实调用：默认跳过；设置环境变量 YUDAO_OCR_TENCENT_API_KEY（SecretId|SecretKey）后才执行，只发 1 次请求（会计费）
     *
     * 可选：YUDAO_OCR_TENCENT_URL
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "YUDAO_OCR_TENCENT_API_KEY", matches = ".+")
    public void testCall_live() {
        // 准备参数
        TencentOcrModel model = new TencentOcrModel(System.getenv("YUDAO_OCR_TENCENT_API_KEY"),
                System.getenv("YUDAO_OCR_TENCENT_URL"), null);

        // 调用
        String text = model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：识别出图片中的 YUDAO OCR 2026
        System.out.println("[testCall_live][TencentOcr] " + text);
        String normalized = StrUtil.cleanBlank(text).toUpperCase();
        assertTrue(normalized.contains("YUDAO") && normalized.contains("2026"), text);
    }

    private TencentOcrModel buildModel(String apiKey) {
        return new TencentOcrModel(apiKey, server.baseUrl(), null, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    private static OcrPrompt buildPrompt(MimeType mimeType, byte[] image) {
        return new OcrPrompt(new Media(mimeType, new ByteArrayResource(image)));
    }

}
