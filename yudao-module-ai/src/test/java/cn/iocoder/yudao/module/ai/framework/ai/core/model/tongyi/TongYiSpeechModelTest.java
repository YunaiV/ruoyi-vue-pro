package cn.iocoder.yudao.module.ai.framework.ai.core.model.tongyi;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.SpeechMockServer;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TongYiSpeechModel} 的协议测试：本地 HTTP 服务模拟 DashScope 与 OSS 临时地址
 *
 * @author 芋道源码
 */
public class TongYiSpeechModelTest {

    private static final String API_KEY = "sk-tongyi-test-key";
    private static final String MODEL = "qwen3-tts-flash";

    private static TongYiSpeechModel createModel(SpeechMockServer server) {
        return new TongYiSpeechModel(server.url(), API_KEY, MODEL, Duration.ofSeconds(5), 1024 * 1024);
    }

    private static TextToSpeechPrompt prompt(String text, String voice, String format, Double speed) {
        return new TextToSpeechPrompt(text, TextToSpeechOptions.builder().voice(voice).format(format).speed(speed).build());
    }

    @Test
    public void testCall_success() throws Exception {
        byte[] wav = SpeechMockServer.wav(128);
        String[] audioUrl = new String[1];
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            if (request.path().equals("/audio/a.wav")) {
                SpeechMockServer.writeResponse(out, 200, "audio/wav", wav);
                return;
            }
            SpeechMockServer.writeJson(out, 200, "{\"request_id\":\"req-1\",\"output\":{\"finish_reason\":\"stop\","
                    + "\"audio\":{\"url\":\"" + audioUrl[0] + "\",\"data\":\"\",\"id\":\"audio_req-1\",\"expires_at\":1}},"
                    + "\"usage\":{\"characters\":2}}");
        })) {
            audioUrl[0] = server.url() + "/audio/a.wav?Expires=1&Signature=oss-sign";
            TongYiSpeechModel model = createModel(server);

            TextToSpeechResponse response = model.call(prompt("你好", "Cherry", null, null));

            // 断言：返回下载后的字节，而不是临时地址
            assertArrayEquals(wav, response.getResult().getOutput());
            assertEquals("wav", response.getMetadata().get(AbstractAiSpeechModel.METADATA_FORMAT));
            assertEquals("req-1", response.getMetadata().get(TongYiSpeechModel.METADATA_REQUEST_ID));
            // 断言：合成请求
            SpeechMockServer.Request request = server.requests().get(0);
            assertEquals("POST", request.method());
            assertEquals("/api/v1/services/aigc/multimodal-generation/generation", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            assertNull(request.header("X-DashScope-SSE"));
            JsonNode body = AiSpeechUtils.parseJson(request.body());
            assertEquals(MODEL, body.get("model").asText());
            assertEquals("你好", body.at("/input/text").asText());
            assertEquals("Cherry", body.at("/input/voice").asText());
            assertTrue(body.at("/input/language_type").isMissingNode());
            assertTrue(body.at("/parameters").isMissingNode());
            // 断言：下载临时地址不携带 API Key
            SpeechMockServer.Request download = server.requests().get(1);
            assertEquals("/audio/a.wav", download.path());
            assertNull(download.header("Authorization"));
        }
    }

    @Test
    public void testCall_errorResponses() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            String body = request.bodyAsString();
            switch (body.contains("http400") ? "400" : body.contains("code200") ? "code" : body.contains("nourl") ? "nourl"
                    : body.contains("gateway") ? "gateway" : "notwav") {
                case "gateway":
                    SpeechMockServer.writeResponse(out, 502, "text/html",
                            "<html>bad gateway raw-upstream-body</html>".getBytes());
                    break;
                case "400":
                    SpeechMockServer.writeJson(out, 400, "{\"request_id\":\"req-400\",\"code\":\"InvalidParameter\","
                            + "\"message\":\"bad voice, key " + API_KEY + "\"}");
                    break;
                case "code":
                    SpeechMockServer.writeJson(out, 200, "{\"request_id\":\"req-c\",\"code\":\"Throttling\",\"message\":\"too many\"}");
                    break;
                case "nourl":
                    SpeechMockServer.writeJson(out, 200, "{\"request_id\":\"req-n\",\"output\":{\"audio\":{\"url\":\"\"}}}");
                    break;
                default:
                    if (request.path().startsWith("/audio")) {
                        SpeechMockServer.writeResponse(out, 200, "audio/wav", SpeechMockServer.mp3(32));
                    } else {
                        SpeechMockServer.writeJson(out, 200, "{\"request_id\":\"req-w\",\"output\":{\"audio\":{\"url\":\""
                                + "http://" + request.header("Host") + "/audio/x.wav\"}}}");
                    }
                    break;
            }
        })) {
            TongYiSpeechModel model = createModel(server);
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("http400", "Cherry", null, null)));
            assertTrue(ex.getMessage().contains("InvalidParameter"));
            assertTrue(ex.getMessage().contains("req-400"));
            assertFalse(ex.getMessage().contains(API_KEY));
            // 2xx 但没有 output（带 code）：不能当作成功
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("code200", "Cherry", null, null)));
            assertTrue(ex.getMessage().contains("未返回音频地址"));
            assertTrue(ex.getMessage().contains("req-c"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("nourl", "Cherry", null, null)));
            assertTrue(ex.getMessage().contains("未返回音频地址"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("gateway", "Cherry", null, null)));
            assertTrue(ex.getMessage().contains("HTTP 502"));
            assertFalse(ex.getMessage().contains("raw-upstream-body"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("notwav", "Cherry", null, null)));
            assertTrue(ex.getMessage().contains("不是 wav 格式"));
        }
    }

    @Test
    public void testCall_noRetryNoRedirect() throws Exception {
        // 重定向：不跟随，按失败处理，只发出一次请求
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> SpeechMockServer.writeResponse(out, 302,
                "text/plain", new byte[0], "Location", "http://" + request.header("Host") + "/other"))) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createModel(server).call(prompt("你好", "Cherry", null, null)));
            assertTrue(ex.getMessage().contains("HTTP 302"));
            assertEquals(1, server.requests().size());
        }
        // 连接中断：传输层不自动重发
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            throw new SpeechMockServer.AbortConnection();
        })) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createModel(server).call(prompt("你好", "Cherry", null, null)));
            assertTrue(ex.getMessage().contains("请勿自动重试"));
            assertEquals(1, server.requests().size());
        }
        // 超时：结果未知，不自动重试，只发出一次请求
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            Thread.sleep(3000);
            SpeechMockServer.writeJson(out, 200, "{}");
        })) {
            TongYiSpeechModel model = new TongYiSpeechModel(server.url(), API_KEY, MODEL, Duration.ofSeconds(1), 1024);
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> model.call(prompt("你好", "Cherry", null, null)));
            assertTrue(ex.getMessage().contains("请勿自动重试"));
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testCall_validateBeforePaidCall() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> SpeechMockServer.writeJson(out, 500, "{}"))) {
            TongYiSpeechModel model = createModel(server);
            // 不支持语速、mp3、超长文本、缺音色、切换模型：全部在请求前拒绝
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", "Cherry", null, 1.2)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", "Cherry", "mp3", null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt(StrUtil.repeat("字", 601), "Cherry", null, null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", " ", null, null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(new TextToSpeechPrompt("你好",
                    TextToSpeechOptions.builder().model("qwen-audio-3.1-tts-flash").voice("Cherry").build())));
            assertEquals(0, server.requests().size());
            // 语速 1、格式 wav、600 字是允许的边界（这里服务端返回 500，只验证校验通过并发出了请求）
            assertThrows(IllegalStateException.class, () -> model.call(prompt(StrUtil.repeat("字", 600), "Cherry", "wav", 1.0)));
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testConstructor_rejectUnlistedModel() {
        assertThrows(IllegalArgumentException.class, () -> new TongYiSpeechModel(null, API_KEY, "cosyvoice-v2"));
        assertThrows(IllegalArgumentException.class, () -> new TongYiSpeechModel(null, API_KEY, "qwen-audio-3.1-tts-flash"));
        assertThrows(IllegalArgumentException.class, () -> new TongYiSpeechModel(null, " ", MODEL));
        assertThrows(UnsupportedOperationException.class,
                () -> new TongYiSpeechModel(null, API_KEY, MODEL).stream(prompt("你好", "Cherry", null, null)).blockFirst());
    }

}
