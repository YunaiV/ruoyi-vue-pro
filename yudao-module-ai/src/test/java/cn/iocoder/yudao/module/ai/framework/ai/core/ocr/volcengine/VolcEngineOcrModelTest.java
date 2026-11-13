package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.volcengine;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Request;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Response;
import com.volcengine.model.ServiceInfo;
import com.volcengine.service.visual.VisualConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link VolcEngineOcrModel} 的单元测试：官方视觉 SDK 真实发请求到本地 HTTP 桩服务，
 * 验证签名、Action / Version、form 请求体、响应处理，以及不同凭据 / 地址的实例互不影响
 *
 * @author 芋道源码
 */
public class VolcEngineOcrModelTest {

    private static final String ACCESS_KEY_ID = "AKLTvolcTestId001";
    private static final String SECRET_ACCESS_KEY = "volcTestSecretKey001==";
    private static final String API_KEY = ACCESS_KEY_ID + "|" + SECRET_ACCESS_KEY;

    private static final String SUCCESS_RESPONSE = """
            {"code":10000,"data":{"line_texts":["火山引擎","通用文字识别 "],"line_rects":[]},\
            "message":"Success","request_id":"req-volc-1","time_elapsed":"20ms"}""";

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
        server.enqueue(Response.json(200, SUCCESS_RESPONSE));

        // 调用
        String text = buildModel(" " + ACCESS_KEY_ID + " | " + SECRET_ACCESS_KEY + " ", server)
                .call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：结果
        assertEquals("火山引擎\n通用文字识别", text);
        // 断言：请求
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/", request.path());
        Map<String, String> query = parseForm(request.rawQuery());
        assertEquals("OCRNormal", query.get("Action"));
        assertEquals("2020-08-26", query.get("Version"));
        // 断言：HMAC-SHA256 签名 Credential=AK/日期/cn-north-1/cv/request，不带 SK
        String authorization = request.header("Authorization");
        assertTrue(authorization.startsWith("HMAC-SHA256 Credential=" + ACCESS_KEY_ID + "/"), authorization);
        assertTrue(authorization.contains("/cn-north-1/cv/request"), authorization);
        assertFalse(authorization.contains(SECRET_ACCESS_KEY));
        assertFalse(request.body().contains(SECRET_ACCESS_KEY));
        // 断言：form 请求体 image_base64
        assertTrue(request.header("Content-Type").startsWith("application/x-www-form-urlencoded"));
        assertEquals(Base64.encode(IMAGE), parseForm(request.body()).get("image_base64"));
    }

    @Test
    public void testCall_emptyLines() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"code\":10000,\"data\":{\"line_texts\":[]},\"message\":\"Success\"}"));

        // 调用，并断言
        assertEquals("", buildModel(API_KEY, server).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput());
    }

    @Test
    public void testCall_businessErrorRateLimited() {
        // 准备参数
        server.enqueue(Response.json(200, """
                {"code":50429,"data":null,"message":"Request Has Reached API Limit","request_id":"req-volc-2"}"""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(API_KEY, server).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.RATE_LIMITED, ex.getType());
        assertEquals("50429", ex.getVendorCode());
        assertEquals("req-volc-2", ex.getRequestId());
        assertTrue(ex.getMessage().contains("Request Has Reached API Limit"));
    }

    @Test
    public void testCall_signatureError() {
        // 准备参数
        server.enqueue(Response.json(401, """
                {"ResponseMetadata":{"RequestId":"req-volc-3","Action":"OCRNormal","Version":"2020-08-26",\
                "Service":"cv","Region":"cn-north-1","Error":{"CodeN":100010,"Code":"SignatureDoesNotMatch",\
                "Message":"The request signature we calculated does not match the signature you provided."}}}"""));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(API_KEY, server).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.AUTH_FAILED, ex.getType());
        assertEquals("SignatureDoesNotMatch", ex.getVendorCode());
        assertEquals("req-volc-3", ex.getRequestId());
        assertFalse(ex.getMessage().contains(SECRET_ACCESS_KEY));
    }

    @Test
    public void testCall_httpErrorWithBusinessBody() {
        // 准备参数
        server.enqueue(Response.json(500, "{\"code\":50500,\"message\":\"Internal Error\",\"request_id\":\"req-volc-4\"}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel(API_KEY, server).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.UPSTREAM_ERROR, ex.getType());
        assertEquals("50500", ex.getVendorCode());
        assertEquals("req-volc-4", ex.getRequestId());
    }

    @Test
    public void testCall_timeout() {
        // 准备参数
        server.enqueue(Response.delayed(1500));
        VolcEngineOcrModel model = new VolcEngineOcrModel(API_KEY, server.baseUrl(), null,
                Duration.ofSeconds(1), Duration.ofMillis(300));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class, () -> model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.TIMEOUT, ex.getType());
        assertFalse(ex.getMessage().contains(SECRET_ACCESS_KEY));
    }

    /**
     * 不同凭据、不同地址的实例互不影响，且不会改动 SDK 静态共享的 ServiceInfo
     */
    @Test
    public void testCall_instancesIsolated() throws Exception {
        // 准备参数
        ServiceInfo shared = VisualConfig.serviceInfoMap.get("cn-north-1");
        String sharedHost = shared.getHost();
        String sharedScheme = shared.getScheme();
        String sharedAccessKey = shared.getCredentials().getAccessKeyID();
        try (OcrStubServer otherServer = new OcrStubServer()) {
            server.enqueue(Response.json(200, SUCCESS_RESPONSE)).enqueue(Response.json(200, SUCCESS_RESPONSE));
            otherServer.enqueue(Response.json(200, SUCCESS_RESPONSE));
            VolcEngineOcrModel modelA = buildModel("AKLTaaaa|secretA", server);
            VolcEngineOcrModel modelB = buildModel("AKLTbbbb|secretB", otherServer);

            // 调用
            modelA.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));
            modelB.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));
            modelA.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));

            // 断言：各自的地址、各自的凭据
            assertEquals(2, server.requests().size());
            assertEquals(1, otherServer.requests().size());
            server.requests().forEach(request -> assertTrue(
                    request.header("Authorization").contains("Credential=AKLTaaaa/")));
            assertTrue(otherServer.lastRequest().header("Authorization").contains("Credential=AKLTbbbb/"));
        }
        // 断言：SDK 的静态共享配置没有被修改
        assertEquals(sharedHost, shared.getHost());
        assertEquals(sharedScheme, shared.getScheme());
        assertEquals(sharedAccessKey, shared.getCredentials().getAccessKeyID());
        assertEquals("visual.volcengineapi.com", shared.getHost());
    }

    @Test
    public void testCall_invalidImage() {
        // 调用，并断言异常：base64 后超过 8MB
        byte[] tooLarge = new byte[(int) (VolcEngineOcrModel.MAX_ENCODED_LENGTH / 4 * 3) + 3];
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class, () -> buildModel(API_KEY, server)
                .call(buildPrompt(MimeTypeUtils.IMAGE_PNG, tooLarge))).getType());
        // 调用，并断言异常：类型不支持
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class, () -> buildModel(API_KEY, server)
                .call(buildPrompt(MimeTypeUtils.IMAGE_GIF, IMAGE))).getType());
        // 断言：没有发出请求
        assertTrue(server.requests().isEmpty());
    }

    @Test
    public void testConstructor_invalidConfig() {
        for (String apiKey : new String[]{null, "", ACCESS_KEY_ID, ACCESS_KEY_ID + "|", "|" + SECRET_ACCESS_KEY,
                API_KEY + "|x"}) {
            OcrException ex = assertThrows(OcrException.class, () -> buildModel(apiKey, server), "apiKey=" + apiKey);
            assertEquals(OcrException.Type.INVALID_CREDENTIAL, ex.getType());
            assertFalse(ex.getMessage().contains(SECRET_ACCESS_KEY));
            assertFalse(ex.getMessage().contains(ACCESS_KEY_ID));
        }
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> new VolcEngineOcrModel(API_KEY, null, "OCRPdf")).getType());
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> new VolcEngineOcrModel(API_KEY, "visual.volcengineapi.com", null)).getType());
    }

    /**
     * 真实调用：默认跳过；设置环境变量 YUDAO_OCR_VOLCENGINE_API_KEY（AccessKeyId|SecretAccessKey）后才执行，只发 1 次请求（会计费）
     *
     * 可选：YUDAO_OCR_VOLCENGINE_URL
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "YUDAO_OCR_VOLCENGINE_API_KEY", matches = ".+")
    public void testCall_live() {
        // 准备参数
        VolcEngineOcrModel model = new VolcEngineOcrModel(System.getenv("YUDAO_OCR_VOLCENGINE_API_KEY"),
                System.getenv("YUDAO_OCR_VOLCENGINE_URL"), null);

        // 调用
        String text = model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：识别出图片中的 YUDAO OCR 2026
        System.out.println("[testCall_live][VolcEngineOcr] " + text);
        String normalized = StrUtil.cleanBlank(text).toUpperCase();
        assertTrue(normalized.contains("YUDAO") && normalized.contains("2026"), text);
    }

    private static VolcEngineOcrModel buildModel(String apiKey, OcrStubServer target) {
        return new VolcEngineOcrModel(apiKey, target.baseUrl(), null, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    private static OcrPrompt buildPrompt(MimeType mimeType, byte[] image) {
        return new OcrPrompt(new Media(mimeType, new ByteArrayResource(image)));
    }

    private static Map<String, String> parseForm(String form) {
        return Arrays.stream(form.split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair -> pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : ""));
    }

}
