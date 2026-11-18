package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.baidu;

import cn.hutool.core.codec.Base64;
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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BaiduOcrModel} 的单元测试：通过本地 HTTP 桩服务验证 access_token 换取 / 缓存 / 失效重试，以及识别请求
 *
 * @author 芋道源码
 */
public class BaiduOcrModelTest {

    private static final String CLIENT_ID = "baidu-ak-test";
    private static final String CLIENT_SECRET = "baidu-sk-secret-789";
    private static final String API_KEY = " " + CLIENT_ID + " | " + CLIENT_SECRET + " ";
    /**
     * 新版 API Key（单段，Bearer 鉴权）
     */
    private static final String API_KEY_V3 = "bce-v3/ALTAK-ocrTestAk001/ocrTestSecretPart002";

    private static final String TOKEN_RESPONSE = "{\"refresh_token\":\"r\",\"expires_in\":2592000,"
            + "\"scope\":\"public\",\"access_token\":\"%s\"}";
    private static final String OCR_RESPONSE = "{\"log_id\":1390582998516105216,\"words_result_num\":2,"
            + "\"words_result\":[{\"words\":\" OCR\"},{\"words\":\"百度通用文字识别高精度版\"}]}";

    private static final byte[] IMAGE = ResourceUtil.readBytes("ocr/yudao-ocr-2026.png");

    private OcrStubServer server;
    private MutableClock clock;

    @BeforeEach
    public void setUp() throws Exception {
        server = new OcrStubServer();
        clock = new MutableClock(Instant.parse("2026-11-12T00:00:00Z"));
    }

    @AfterEach
    public void tearDown() {
        server.close();
    }

    @Test
    public void testCall_tokenThenRecognize() {
        // 准备参数
        server.enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-1")))
                .enqueue(Response.json(200, OCR_RESPONSE));

        // 调用
        String text = buildModel().call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：结果
        assertEquals("OCR\n百度通用文字识别高精度版", text);
        List<Request> requests = server.requests();
        assertEquals(2, requests.size());
        // 断言：换 token（凭据已 trim）
        Request tokenRequest = requests.get(0);
        assertEquals("POST", tokenRequest.method());
        assertEquals("/oauth/2.0/token", tokenRequest.path());
        assertEquals("grant_type=client_credentials&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET,
                tokenRequest.rawQuery());
        // 断言：识别请求，form 参数 image 为 base64 的 urlencode
        Request ocrRequest = requests.get(1);
        assertEquals("POST", ocrRequest.method());
        assertEquals("/rest/2.0/ocr/v1/accurate_basic", ocrRequest.path());
        assertEquals("access_token=token-1", ocrRequest.rawQuery());
        assertTrue(ocrRequest.header("Content-Type").startsWith("application/x-www-form-urlencoded"));
        assertTrue(ocrRequest.body().startsWith("image="));
        assertEquals(Base64.encode(IMAGE),
                URLDecoder.decode(ocrRequest.body().substring("image=".length()), StandardCharsets.UTF_8));
    }

    @Test
    public void testCall_apiKeyBearer() {
        // 准备参数：新版 API Key 只有识别这一个请求
        server.enqueue(Response.json(200, OCR_RESPONSE)).enqueue(Response.json(200, OCR_RESPONSE));
        BaiduOcrModel model = new BaiduOcrModel(" " + API_KEY_V3 + " ", server.baseUrl(), null,
                Duration.ofSeconds(2), Duration.ofSeconds(5), clock);

        // 调用：两次都直接识别，不换 token
        String text = model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();
        clock.advance(Duration.ofDays(31)); // 新版 API Key 没有 token 过期刷新
        model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));

        // 断言：结果
        assertEquals("OCR\n百度通用文字识别高精度版", text);
        // 断言：只调用识别接口，Bearer 鉴权（API Key 已 trim），URL 和请求体都不带 access_token
        List<Request> requests = server.requests();
        assertEquals(2, requests.size());
        for (Request request : requests) {
            assertEquals("POST", request.method());
            assertEquals("/rest/2.0/ocr/v1/accurate_basic", request.path());
            assertEquals("Bearer " + API_KEY_V3, request.header("Authorization"));
            assertNull(request.rawQuery());
            assertFalse(request.body().contains("access_token"));
            assertTrue(request.header("Content-Type").startsWith("application/x-www-form-urlencoded"));
            assertEquals(Base64.encode(IMAGE),
                    URLDecoder.decode(request.body().substring("image=".length()), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testCall_apiKeyAuthErrorNoTokenRetry() {
        // 准备参数：110 在旧版密钥下会触发换 token 重试；新版 API Key 不应重试。消息里故意带上 API Key，验证会被去掉
        server.enqueue(Response.json(200, "{\"error_code\":110,\"error_msg\":\"Access token invalid: "
                + API_KEY_V3 + "\",\"log_id\":2233}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class, () -> new BaiduOcrModel(API_KEY_V3, server.baseUrl(), null,
                Duration.ofSeconds(2), Duration.ofSeconds(5), clock).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.AUTH_FAILED, ex.getType());
        assertEquals("110", ex.getVendorCode());
        assertEquals("2233", ex.getRequestId());
        assertNoApiKey(ex);
        // 断言：只发了 1 次识别请求，没有调用 token 接口
        assertEquals(1, server.requests().size());
        assertEquals("/rest/2.0/ocr/v1/accurate_basic", server.lastRequest().path());
    }

    @Test
    public void testCall_apiKeyHttpUnauthorized() {
        // 准备参数
        server.enqueue(Response.json(401, "{\"error_code\":\"IamAuthFailed\",\"error_msg\":\"invalid api key "
                + API_KEY_V3 + "\"}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class, () -> new BaiduOcrModel(API_KEY_V3, server.baseUrl(), null,
                Duration.ofSeconds(2), Duration.ofSeconds(5), clock).call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.AUTH_FAILED, ex.getType());
        assertEquals("IamAuthFailed", ex.getVendorCode());
        assertTrue(ex.getMessage().contains("HTTP 401"));
        assertNoApiKey(ex);
        assertNull(ex.getCause());
        assertEquals(1, server.requests().size());
    }

    @Test
    public void testCall_tokenCachedAndRefreshedBeforeExpiry() {
        // 准备参数
        server.enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-1")))
                .enqueue(Response.json(200, OCR_RESPONSE))
                .enqueue(Response.json(200, OCR_RESPONSE))
                .enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-2")))
                .enqueue(Response.json(200, OCR_RESPONSE));
        BaiduOcrModel model = buildModel();

        // 调用
        model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));
        model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)); // 复用缓存的 token
        clock.advance(Duration.ofDays(30).minusMinutes(30)); // 进入提前刷新窗口（到期前 1 小时内）
        model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE));

        // 断言
        List<Request> requests = server.requests();
        assertEquals(5, requests.size());
        assertEquals("access_token=token-1", requests.get(1).rawQuery());
        assertEquals("access_token=token-1", requests.get(2).rawQuery());
        assertEquals("/oauth/2.0/token", requests.get(3).path());
        assertEquals("access_token=token-2", requests.get(4).rawQuery());
    }

    @Test
    public void testCall_tokenInvalidThenRetryOnce() {
        // 准备参数
        server.enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-1")))
                .enqueue(Response.json(200, "{\"error_code\":110,\"error_msg\":\"Access token invalid or no longer valid\"}"))
                .enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-2")))
                .enqueue(Response.json(200, OCR_RESPONSE));

        // 调用
        String text = buildModel().call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言
        assertEquals("OCR\n百度通用文字识别高精度版", text);
        List<Request> requests = server.requests();
        assertEquals(4, requests.size());
        assertEquals("access_token=token-2", requests.get(3).rawQuery());
    }

    @Test
    public void testCall_tokenStillInvalidAfterRetry() {
        // 准备参数
        server.enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-1")))
                .enqueue(Response.json(200, "{\"error_code\":111,\"error_msg\":\"Access token expired\"}"))
                .enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-2")))
                .enqueue(Response.json(200, "{\"error_code\":111,\"error_msg\":\"Access token expired\"}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel().call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.AUTH_FAILED, ex.getType());
        assertEquals("111", ex.getVendorCode());
        assertEquals(4, server.requests().size()); // 只重试一次
    }

    @Test
    public void testCall_qpsLimit() {
        // 准备参数
        server.enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-1")))
                .enqueue(Response.json(200, "{\"error_code\":18,\"error_msg\":\"Open api qps request limit reached\","
                        + "\"log_id\":12345}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel().call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.RATE_LIMITED, ex.getType());
        assertEquals("18", ex.getVendorCode());
        assertEquals("12345", ex.getRequestId());
        assertTrue(ex.getMessage().contains("Open api qps request limit reached"));
    }

    @Test
    public void testCall_imageFormatError() {
        // 准备参数
        server.enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-1")))
                .enqueue(Response.json(200, "{\"error_code\":216201,\"error_msg\":\"image format error\"}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel().call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.INVALID_IMAGE, ex.getType());
    }

    @Test
    public void testCall_tokenInvalidClient() {
        // 准备参数
        server.enqueue(Response.json(401, "{\"error\":\"invalid_client\",\"error_description\":\"Client authentication failed\"}"));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel().call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.AUTH_FAILED, ex.getType());
        assertEquals("invalid_client", ex.getVendorCode());
        assertTrue(ex.getMessage().contains("Client authentication failed"));
        assertNoSecret(ex);
        assertEquals(1, server.requests().size());
    }

    @Test
    public void testCall_ocrTimeoutDoesNotLeakToken() {
        // 准备参数
        server.enqueue(Response.json(200, TOKEN_RESPONSE.formatted("token-secret-1")))
                .enqueue(Response.delayed(1500));
        BaiduOcrModel model = new BaiduOcrModel(API_KEY, server.baseUrl(), null,
                Duration.ofSeconds(1), Duration.ofMillis(300), clock);

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class, () -> model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.TIMEOUT, ex.getType());
        assertNoSecret(ex);
        assertFalse(ex.getMessage().contains("token-secret-1"));
        assertNull(ex.getCause());
    }

    @Test
    public void testCall_tokenServerErrorDoesNotLeakSecret() {
        // 准备参数
        server.enqueue(new Response(500, "text/plain", "internal error", 0));

        // 调用，并断言异常
        OcrException ex = assertThrows(OcrException.class,
                () -> buildModel().call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)));
        assertEquals(OcrException.Type.UPSTREAM_ERROR, ex.getType());
        assertNoSecret(ex);
    }

    @Test
    public void testCall_invalidImage() {
        // 调用，并断言异常：类型不支持
        assertEquals(OcrException.Type.INVALID_IMAGE, assertThrows(OcrException.class, () -> buildModel()
                .call(buildPrompt(MimeTypeUtils.parseMimeType("image/webp"), IMAGE))).getType());
        // 断言：图片不合法时，连 token 都不换
        assertTrue(server.requests().isEmpty());
    }

    @Test
    public void testConstructor_invalidCredential() {
        for (String apiKey : new String[]{null, "", "ak-only-part", "ak-one|sk-one|extra-one", "ak-two|", "|sk-three", " | "}) {
            OcrException ex = assertThrows(OcrException.class,
                    () -> new BaiduOcrModel(apiKey, null, null), "apiKey=" + apiKey);
            assertEquals(OcrException.Type.INVALID_CREDENTIAL, ex.getType());
            // 异常消息不能回显密钥的任何一段
            for (String part : String.valueOf(apiKey).split("\\|")) {
                if (!part.isBlank() && apiKey != null) {
                    assertFalse(ex.getMessage().contains(part.trim()), "part=" + part + ", message=" + ex.getMessage());
                }
            }
        }
        // 只有新版前缀、没有内容的单段密钥也拒绝
        for (String apiKey : new String[]{"bce-v3/", " bce-v3/ ", "bce-v3/|"}) {
            assertEquals(OcrException.Type.INVALID_CREDENTIAL, assertThrows(OcrException.class,
                    () -> new BaiduOcrModel(apiKey, null, null), "apiKey=" + apiKey).getType());
        }
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> new BaiduOcrModel(API_KEY, null, "general_basic")).getType());
        assertEquals(OcrException.Type.INVALID_CONFIG, assertThrows(OcrException.class,
                () -> new BaiduOcrModel(API_KEY_V3, null, "general_basic")).getType());
    }

    /**
     * 真实调用：默认跳过；设置环境变量 YUDAO_OCR_BAIDU_API_KEY 后才执行（会计费）
     *
     * 1. 新版 API Key（bce-v3/ 开头，单段）：Bearer 鉴权，只识别 1 次
     * 2. 旧版 APIKey|SecretKey：换 1 次 token + 识别 1 次
     *
     * 可选：YUDAO_OCR_BAIDU_URL
     */
    @Test
    @EnabledIfEnvironmentVariable(named = "YUDAO_OCR_BAIDU_API_KEY", matches = ".+")
    public void testCall_live() {
        // 准备参数
        BaiduOcrModel model = new BaiduOcrModel(System.getenv("YUDAO_OCR_BAIDU_API_KEY"),
                System.getenv("YUDAO_OCR_BAIDU_URL"), null);

        // 调用
        String text = model.call(buildPrompt(MimeTypeUtils.IMAGE_PNG, IMAGE)).getResult().getOutput();

        // 断言：识别出图片中的 YUDAO OCR 2026
        System.out.println("[testCall_live][BaiduOcr] " + text);
        String normalized = StrUtil.cleanBlank(text).toUpperCase();
        assertTrue(normalized.contains("YUDAO") && normalized.contains("2026"), text);
    }

    private BaiduOcrModel buildModel() {
        return new BaiduOcrModel(API_KEY, server.baseUrl(), null, Duration.ofSeconds(2), Duration.ofSeconds(5), clock);
    }

    private static OcrPrompt buildPrompt(MimeType mimeType, byte[] image) {
        return new OcrPrompt(new Media(mimeType, new ByteArrayResource(image)));
    }

    private static void assertNoSecret(OcrException ex) {
        assertFalse(ex.getMessage().contains(CLIENT_SECRET), ex.getMessage());
        assertFalse(ex.getMessage().contains("client_secret"), ex.getMessage());
        assertFalse(ex.getMessage().contains("access_token="), ex.getMessage());
    }

    private static void assertNoApiKey(OcrException ex) {
        assertFalse(ex.getMessage().contains(API_KEY_V3), ex.getMessage());
        assertFalse(ex.getMessage().contains("ocrTestSecretPart002"), ex.getMessage());
    }

    /**
     * 可手动推进的时钟，用于验证 token 过期刷新
     */
    private static class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }

    }

}
