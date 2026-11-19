package cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.SpeechMockServer;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TencentTtsApi} 的协议测试：本地 HTTP 服务模拟 tts.tencentcloudapi.com，客户端走真实的官方 SDK 与 OkHttp
 *
 * @author 芋道源码
 */
public class TencentTtsApiTest {

    private static final String SECRET_ID = "AKIDtestSecretId";
    private static final String SECRET_KEY = "testSecretKeyValue";
    private static final String API_KEY = SECRET_ID + "|" + SECRET_KEY;

    private static TencentTtsApi createApi(SpeechMockServer server, Duration timeout) {
        return new TencentTtsApi(API_KEY, server.url(), timeout, 1024 * 1024);
    }

    private static String response(String data) {
        return "{\"Response\":{" + (data != null ? "\"Data\":" + data + "," : "") + "\"RequestId\":\"req-1\"}}";
    }

    private static String error(String code, String message) {
        return "{\"Response\":{\"Error\":{\"Code\":\"" + code + "\",\"Message\":\"" + message + "\"},\"RequestId\":\"req-e\"}}";
    }

    private static void assertNoSecret(SpeechMockServer.Request request) {
        assertFalse(request.bodyAsString().contains(SECRET_KEY));
        request.headers().values().forEach(value -> assertFalse(value.contains(SECRET_KEY)));
    }

    // ========== createTask ==========

    @Test
    public void testCreateTask_success() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"gz-task-1\"}")))) {
            // 长文本（> 150 字）原样提交，由服务端处理，不在客户端切分
            String text = StrUtil.repeat("这是一段用于验证长文本语音合成的测试文字。", 20);
            TencentTtsApi.TtsTask task = createApi(server, Duration.ofSeconds(5)).createTask(text, "101001", "wav", 1.3);

            assertEquals("gz-task-1", task.getTaskId());
            assertEquals("req-1", task.getRequestId());
            assertEquals(1, server.requests().size());
            SpeechMockServer.Request request = server.requests().get(0);
            assertEquals("POST", request.method());
            assertEquals("CreateTtsTask", request.header("X-TC-Action"));
            assertEquals("2019-08-23", request.header("X-TC-Version"));
            assertTrue(request.header("Authorization").startsWith("TC3-HMAC-SHA256 Credential=" + SECRET_ID + "/"));
            // 签名范围中的服务名取自请求主机的第一段：生产为 tts.tencentcloudapi.com → tts；本地测试主机为 127.0.0.1 → 127
            assertTrue(request.header("Authorization").contains("/127/tc3_request"));
            assertTrue(request.header("Authorization").contains("SignedHeaders=content-type;host"));
            assertNoSecret(request);
            JsonNode body = AiSpeechUtils.parseJson(request.body());
            assertEquals(text, body.get("Text").asText());
            assertEquals(101001L, body.get("VoiceType").asLong());
            assertTrue(body.get("VoiceType").isIntegralNumber());
            assertEquals("wav", body.get("Codec").asText());
            // 1.3 倍 → 1 + (1.3 - 1.2) / 0.3 = 1.333... → 1.33
            assertEquals("1.33", body.get("Speed").decimalValue().toPlainString());
            // 未开放的参数不下发
            assertNull(body.get("CallbackUrl"));
            assertNull(body.get("EmotionCategory"));
            assertNull(body.get("Volume"));
            assertNull(body.get("SampleRate"));
        }
    }

    @Test
    public void testCreateTask_defaults() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"gz-task-2\"}")))) {
            createApi(server, Duration.ofSeconds(5)).createTask("你好", "101001", null, null);

            JsonNode body = AiSpeechUtils.parseJson(server.requests().get(0).body());
            assertEquals("mp3", body.get("Codec").asText());
            assertEquals(0, body.get("Speed").decimalValue().compareTo(BigDecimal.ZERO));
        }
    }

    @Test
    public void testCreateTask_errorResponses() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            String text = AiSpeechUtils.parseJson(request.body()).get("Text").asText();
            switch (text) {
                case "auth":
                    SpeechMockServer.writeJson(out, 200, error("AuthFailure.InvalidAuthorization",
                            "invalid secret " + SECRET_KEY));
                    break;
                case "notask":
                    SpeechMockServer.writeJson(out, 200, response("{}"));
                    break;
                default:
                    SpeechMockServer.writeResponse(out, 500, "text/plain", "internal".getBytes(StandardCharsets.UTF_8));
                    break;
            }
        })) {
            TencentTtsApi api = createApi(server, Duration.ofSeconds(5));
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> api.createTask("auth", "101001", null, null));
            assertTrue(ex.getMessage().contains("AuthFailure.InvalidAuthorization"));
            assertTrue(ex.getMessage().contains("req-e"));
            assertFalse(ex.getMessage().contains(SECRET_KEY));
            ex = assertThrows(IllegalStateException.class, () -> api.createTask("notask", "101001", null, null));
            assertTrue(ex.getMessage().contains("未返回任务编号"));
            assertThrows(IllegalStateException.class, () -> api.createTask("http500", "101001", null, null));
            // 失败不自动重试：3 次调用 3 个请求
            assertEquals(3, server.requests().size());
        }
    }

    @Test
    public void testCreateTask_connectionDroppedNoRetry() throws Exception {
        // 读完请求后直接断开、不返回任何响应：OkHttp 默认会对这种情况重试，这里必须只发一次
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
        })) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createApi(server, Duration.ofSeconds(5)).createTask("你好", "101001", null, null));
            assertTrue(ex.getMessage().contains("任务是否已创建未知"));
            Thread.sleep(300);
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testCreateTask_timeoutNoRetry() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> Thread.sleep(3000))) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createApi(server, Duration.ofMillis(500)).createTask("你好", "101001", null, null));
            assertTrue(ex.getMessage().contains("任务是否已创建未知"));
            Thread.sleep(300);
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testCreateTask_validateBeforePaidCall() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"x\"}")))) {
            TencentTtsApi api = createApi(server, Duration.ofSeconds(5));
            assertThrows(IllegalArgumentException.class, () -> api.createTask("你好", "abc", null, null));
            assertThrows(IllegalArgumentException.class, () -> api.createTask("你好", "0", null, null));
            assertThrows(IllegalArgumentException.class, () -> api.createTask("你好", "-1", null, null));
            assertThrows(IllegalArgumentException.class, () -> api.createTask("你好", " ", null, null));
            assertThrows(IllegalArgumentException.class, () -> api.createTask("你好", "101001", "pcm", null));
            assertThrows(IllegalArgumentException.class, () -> api.createTask("你好", "101001", null, 0.59));
            assertThrows(IllegalArgumentException.class, () -> api.createTask("你好", "101001", null, 2.51));
            assertThrows(IllegalArgumentException.class, () -> api.createTask("你好", "101001", null, 1.234));
            assertThrows(IllegalArgumentException.class, () -> api.createTask(StrUtil.repeat("字", 100001), "101001", null, null));
            assertThrows(IllegalArgumentException.class, () -> api.createTask(" ", "101001", null, null));
            assertEquals(0, server.requests().size());
        }
    }

    // ========== getTask ==========

    @Test
    public void testGetTask_pendingAndFailed() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            String taskId = AiSpeechUtils.parseJson(request.body()).get("TaskId").asText();
            switch (taskId) {
                case "waiting":
                    SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"waiting\",\"Status\":0,"
                            + "\"StatusStr\":\"waiting\",\"ResultUrl\":\"\",\"ErrorMsg\":\"\"}"));
                    break;
                case "doing":
                    SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"doing\",\"Status\":1,"
                            + "\"StatusStr\":\"doing\",\"ResultUrl\":\"\",\"ErrorMsg\":\"\"}"));
                    break;
                case "failed":
                    SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"failed\",\"Status\":3,"
                            + "\"StatusStr\":\"failed\",\"ResultUrl\":\"\",\"ErrorMsg\":\"internal error " + SECRET_KEY + "\"}"));
                    break;
                case "unknown":
                    SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"unknown\",\"Status\":9}"));
                    break;
                case "mismatch":
                    SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"other\",\"Status\":0}"));
                    break;
                case "nourl":
                    SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"nourl\",\"Status\":2,\"ResultUrl\":\"\"}"));
                    break;
                default:
                    SpeechMockServer.writeJson(out, 200, error("FailedOperation.NoSuchTask", "task not exist"));
                    break;
            }
        })) {
            TencentTtsApi api = createApi(server, Duration.ofSeconds(5));
            TencentTtsApi.TtsTaskResult result = api.getTask("waiting", "mp3");
            assertEquals(TencentTtsApi.TtsTaskStatus.WAITING, result.getStatus());
            assertNull(result.getAudio());
            assertFalse(result.getStatus().isFinished());
            result = api.getTask("doing", "mp3");
            assertEquals(TencentTtsApi.TtsTaskStatus.RUNNING, result.getStatus());
            assertNull(result.getAudio());
            result = api.getTask("failed", "mp3");
            assertEquals(TencentTtsApi.TtsTaskStatus.FAILED, result.getStatus());
            assertTrue(result.getStatus().isFinished());
            assertTrue(result.getErrorMessage().contains("internal error"));
            assertFalse(result.getErrorMessage().contains(SECRET_KEY));

            assertThrows(IllegalStateException.class, () -> api.getTask("unknown", "mp3"));
            assertThrows(IllegalStateException.class, () -> api.getTask("mismatch", "mp3"));
            assertThrows(IllegalStateException.class, () -> api.getTask("nourl", "mp3"));
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> api.getTask("missing", "mp3"));
            assertTrue(ex.getMessage().contains("FailedOperation.NoSuchTask"));
            // 每次查询只发一次请求
            assertEquals(7, server.requests().size());
            assertEquals("DescribeTtsTaskStatus", server.requests().get(0).header("X-TC-Action"));
            server.requests().forEach(TencentTtsApiTest::assertNoSecret);
        }
        TencentTtsApi api = new TencentTtsApi(API_KEY, null);
        assertThrows(IllegalArgumentException.class, () -> api.getTask(" ", "mp3"));
        assertThrows(IllegalArgumentException.class, () -> api.getTask("gz-1", "pcm"));
    }

    @Test
    public void testGetTask_successDownloadsAudio() throws Exception {
        byte[] mp3 = SpeechMockServer.mp3(128);
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            if (request.path().startsWith("/result")) {
                SpeechMockServer.writeResponse(out, 200, "audio/mpeg", mp3);
                return;
            }
            SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"gz-ok\",\"Status\":2,\"StatusStr\":\"success\","
                    + "\"ResultUrl\":\"http://" + request.header("Host") + "/result/gz-ok.mp3?sign=cos-sign\",\"ErrorMsg\":\"\"}"));
        })) {
            TencentTtsApi.TtsTaskResult result = createApi(server, Duration.ofSeconds(5)).getTask("gz-ok", "mp3");

            assertEquals(TencentTtsApi.TtsTaskStatus.SUCCESS, result.getStatus());
            assertArrayEquals(mp3, result.getAudio());
            assertFalse(result.toString().contains("cos-sign"));
            // 下载结果地址：GET，不携带签名头
            SpeechMockServer.Request download = server.requests().get(1);
            assertEquals("GET", download.method());
            assertEquals("/result/gz-ok.mp3", download.path());
            assertNull(download.header("Authorization"));
        }
    }

    @Test
    public void testGetTask_successButNotAudio() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            if (request.path().startsWith("/result")) {
                SpeechMockServer.writeResponse(out, 200, "audio/wav", SpeechMockServer.mp3(32));
                return;
            }
            SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"gz-wav\",\"Status\":2,"
                    + "\"ResultUrl\":\"http://" + request.header("Host") + "/result/a.wav\"}"));
        })) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createApi(server, Duration.ofSeconds(5)).getTask("gz-wav", "wav"));
            assertTrue(ex.getMessage().contains("不是 wav 格式"));
        }
    }

    // ========== waitTask ==========

    @Test
    public void testWaitTask_untilSuccess() throws Exception {
        byte[] mp3 = SpeechMockServer.mp3(64);
        AtomicInteger queries = new AtomicInteger();
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            if (request.path().startsWith("/result")) {
                SpeechMockServer.writeResponse(out, 200, "audio/mpeg", mp3);
                return;
            }
            // 第 1 次等待中；第 2 次查询失败（继续等待）；第 3 次成功
            int index = queries.incrementAndGet();
            if (index == 1) {
                SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"gz-w\",\"Status\":0}"));
            } else if (index == 2) {
                SpeechMockServer.writeResponse(out, 500, "text/plain", "internal".getBytes(StandardCharsets.UTF_8));
            } else {
                SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"gz-w\",\"Status\":2,"
                        + "\"ResultUrl\":\"http://" + request.header("Host") + "/result/gz-w.mp3\"}"));
            }
        })) {
            TencentTtsApi.TtsTaskResult result = createApi(server, Duration.ofSeconds(5))
                    .waitTask("gz-w", "mp3", Duration.ofSeconds(5), Duration.ofMillis(10));

            assertEquals(TencentTtsApi.TtsTaskStatus.SUCCESS, result.getStatus());
            assertArrayEquals(mp3, result.getAudio());
            // 只查询，不提交
            assertEquals(3, queries.get());
            server.requests().stream().filter(request -> request.header("X-TC-Action") != null)
                    .forEach(request -> assertEquals("DescribeTtsTaskStatus", request.header("X-TC-Action")));
        }
    }

    @Test
    public void testWaitTask_timeout() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 200, response("{\"TaskId\":\"gz-r\",\"Status\":1}")))) {
            TencentTtsApi.TtsTaskResult result = createApi(server, Duration.ofSeconds(5))
                    .waitTask("gz-r", "mp3", Duration.ofMillis(300), Duration.ofMillis(50));

            // 时限到了仍在处理中：返回最后一次的状态，由调用方之后按任务编号继续查询
            assertEquals(TencentTtsApi.TtsTaskStatus.RUNNING, result.getStatus());
            assertTrue(server.requests().size() >= 2);
            server.requests().forEach(request -> assertEquals("DescribeTtsTaskStatus", request.header("X-TC-Action")));
        }
        // 一次都没查询成功：返回 null
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeResponse(out, 500, "text/plain", "internal".getBytes(StandardCharsets.UTF_8)))) {
            assertNull(createApi(server, Duration.ofSeconds(5))
                    .waitTask("gz-e", "mp3", Duration.ofMillis(100), Duration.ofMillis(50)));
        }
    }

    // ========== 凭证、地址、语速 ==========

    @Test
    public void testParseCredential() {
        assertArrayEquals(new String[]{SECRET_ID, SECRET_KEY}, TencentTtsApi.parseCredential(API_KEY));
        assertArrayEquals(new String[]{SECRET_ID, SECRET_KEY}, TencentTtsApi.parseCredential(" " + SECRET_ID + " | " + SECRET_KEY + " "));
        for (String invalid : new String[]{null, "", SECRET_ID, SECRET_ID + "|", "|" + SECRET_KEY, "a|b|c",
                "{\"secretId\":\"a\",\"secretKey\":\"b\"}"}) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> TencentTtsApi.parseCredential(invalid));
            assertTrue(ex.getMessage().contains("SecretId|SecretKey"));
            if (invalid != null && !invalid.isEmpty()) {
                assertFalse(ex.getMessage().contains(invalid));
            }
        }
        assertThrows(IllegalArgumentException.class, () -> new TencentTtsApi(API_KEY, "https://tts.tencentcloudapi.com/path"));
        assertThrows(IllegalArgumentException.class, () -> new TencentTtsApi(API_KEY, "ftp://tts.tencentcloudapi.com"));
        new TencentTtsApi(API_KEY, "https://tts.tencentcloudapi.com");
        new TencentTtsApi(API_KEY, "");
    }

    @Test
    public void testToSpeedArg_officialPiecewiseMapping() {
        String[][] cases = {
                {"0.6", "-2.00"}, {"0.7", "-1.50"}, {"0.61", "-1.95"}, {"0.8", "-1.00"}, {"0.9", "-0.50"},
                {"1", "0.00"}, {"1.01", "0.05"}, {"1.1", "0.50"}, {"1.2", "1.00"}, {"1.21", "1.03"},
                {"1.22", "1.07"}, {"1.3", "1.33"}, {"1.4", "1.67"}, {"1.49", "1.97"}, {"1.5", "2.00"},
                {"1.75", "3.00"}, {"2", "4.00"}, {"2.25", "5.00"}, {"2.49", "5.96"}, {"2.5", "6.00"},
        };
        for (String[] item : cases) {
            assertEquals(item[1], TencentTtsApi.toSpeedArg(new BigDecimal(item[0])).toPlainString(), item[0]);
        }
        assertThrows(IllegalArgumentException.class, () -> TencentTtsApi.toSpeedArg(new BigDecimal("0.59")));
        assertThrows(IllegalArgumentException.class, () -> TencentTtsApi.toSpeedArg(new BigDecimal("2.51")));
    }

}
