package cn.iocoder.yudao.module.ai.framework.ai.core.model.minimax;

import cn.hutool.core.util.HexUtil;
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
 * {@link MiniMaxSpeechModel} 的协议测试
 *
 * @author 芋道源码
 */
public class MiniMaxSpeechModelTest {

    private static final String API_KEY = "minimax-test-key";
    private static final String MODEL = "speech-2.8-hd";

    private static MiniMaxSpeechModel createModel(SpeechMockServer server, Duration timeout) {
        return new MiniMaxSpeechModel(server.url(), API_KEY, MODEL, timeout, 1024 * 1024);
    }

    private static TextToSpeechPrompt prompt(String text, String format, Double speed) {
        return new TextToSpeechPrompt(text, TextToSpeechOptions.builder().voice("male-qn-qingse")
                .format(format).speed(speed).build());
    }

    private static String success(String hex) {
        return "{\"data\":{\"audio\":\"" + hex + "\",\"status\":2},\"extra_info\":{\"audio_format\":\"mp3\"},"
                + "\"trace_id\":\"trace-1\",\"base_resp\":{\"status_code\":0,\"status_msg\":\"success\"}}";
    }

    @Test
    public void testCall_success() throws Exception {
        byte[] mp3 = SpeechMockServer.mp3(256);
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 200, success(HexUtil.encodeHexStr(mp3))))) {
            TextToSpeechResponse response = createModel(server, Duration.ofSeconds(5)).call(prompt("你好", null, 1.25));

            assertArrayEquals(mp3, response.getResult().getOutput());
            assertEquals("mp3", response.getMetadata().get(AbstractAiSpeechModel.METADATA_FORMAT));
            assertEquals("trace-1", response.getMetadata().get(MiniMaxSpeechModel.METADATA_TRACE_ID));
            SpeechMockServer.Request request = server.requests().get(0);
            assertEquals("/v1/t2a_v2", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            JsonNode body = AiSpeechUtils.parseJson(request.body());
            assertEquals(MODEL, body.get("model").asText());
            assertEquals("你好", body.get("text").asText());
            assertFalse(body.get("stream").asBoolean());
            assertEquals("hex", body.get("output_format").asText());
            assertEquals("male-qn-qingse", body.at("/voice_setting/voice_id").asText());
            assertEquals("1.25", body.at("/voice_setting/speed").decimalValue().toPlainString());
            assertEquals("mp3", body.at("/audio_setting/format").asText());
            // 不传未开放的参数
            assertTrue(body.at("/voice_setting/emotion").isMissingNode());
            assertTrue(body.at("/voice_setting/vol").isMissingNode());
        }
    }

    @Test
    public void testCall_errorResponses() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            String text = AiSpeechUtils.parseJson(request.body()).get("text").asText();
            switch (text) {
                case "auth":
                    SpeechMockServer.writeJson(out, 200, "{\"trace_id\":\"trace-a\",\"base_resp\":"
                            + "{\"status_code\":1004,\"status_msg\":\"invalid api key " + API_KEY + "\"}}");
                    break;
                case "running":
                    SpeechMockServer.writeJson(out, 200, "{\"data\":{\"audio\":\"fffb90\",\"status\":1},"
                            + "\"base_resp\":{\"status_code\":0,\"status_msg\":\"success\"}}");
                    break;
                case "badhex":
                    SpeechMockServer.writeJson(out, 200, success("fffbzz"));
                    break;
                case "oddhex":
                    SpeechMockServer.writeJson(out, 200, success("fffb9"));
                    break;
                case "empty":
                    SpeechMockServer.writeJson(out, 200, success(""));
                    break;
                case "notmp3":
                    SpeechMockServer.writeJson(out, 200, success(HexUtil.encodeHexStr(SpeechMockServer.wav(32))));
                    break;
                case "nobase":
                    SpeechMockServer.writeJson(out, 200, "{\"data\":{\"audio\":\"fffb90\",\"status\":2}}");
                    break;
                case "http500":
                    SpeechMockServer.writeResponse(out, 500, "text/plain", "gateway error".getBytes());
                    break;
                default:
                    SpeechMockServer.writeJson(out, 200, "not json");
                    break;
            }
        })) {
            MiniMaxSpeechModel model = createModel(server, Duration.ofSeconds(5));
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("auth", null, null)));
            assertTrue(ex.getMessage().contains("1004"));
            assertTrue(ex.getMessage().contains("trace-a"));
            assertFalse(ex.getMessage().contains(API_KEY));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("running", null, null)));
            assertTrue(ex.getMessage().contains("未完成"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("badhex", null, null)));
            assertTrue(ex.getMessage().contains("十六进制"));
            assertThrows(IllegalStateException.class, () -> model.call(prompt("oddhex", null, null)));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("empty", null, null)));
            assertTrue(ex.getMessage().contains("未返回音频"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("notmp3", null, null)));
            assertTrue(ex.getMessage().contains("不是 mp3 格式"));
            assertThrows(IllegalStateException.class, () -> model.call(prompt("nobase", null, null)));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("http500", null, null)));
            assertTrue(ex.getMessage().contains("HTTP 500"));
            assertThrows(IllegalStateException.class, () -> model.call(prompt("garbage", null, null)));
            // 每次只请求一次，不自动重试
            assertEquals(9, server.requests().size());
        }
    }

    @Test
    public void testCall_timeoutNoRetry() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> Thread.sleep(3000))) {
            MiniMaxSpeechModel model = createModel(server, Duration.ofMillis(300));
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("你好", null, null)));
            assertTrue(ex.getMessage().contains("超时"));
            assertTrue(ex.getMessage().contains("结果未知"));
            Thread.sleep(200);
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    public void testCall_connectionDroppedNoRetry() throws Exception {
        // 服务端读完完整请求后不响应、直接断开（FIN 与 RST 两种）：Reactor Netty 默认会重发一次，必须只发一次
        for (boolean reset : new boolean[]{false, true}) {
            try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
                if (reset) {
                    throw new SpeechMockServer.AbortConnection();
                }
            })) {
                // 超时设得足够长：断开应当立即报错，而不是等到超时
                IllegalStateException ex = assertThrows(IllegalStateException.class,
                        () -> createModel(server, Duration.ofSeconds(60)).call(prompt("你好", null, null)));
                assertTrue(ex.getMessage().contains("结果未知"), ex.getMessage());
                assertFalse(ex.getMessage().contains("超时"), ex.getMessage());
                assertFalse(ex.getMessage().contains(API_KEY));
                Thread.sleep(500);
                assertEquals(1, server.requests().size(), "reset=" + reset);
                assertEquals("你好", AiSpeechUtils.parseJson(server.requests().get(0).body()).get("text").asText());
            }
        }
    }

    @Test
    public void testCall_validateBeforePaidCall() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> SpeechMockServer.writeJson(out, 500, "{}"))) {
            MiniMaxSpeechModel model = createModel(server, Duration.ofSeconds(5));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", "ogg_opus", null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", "pcm", null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", null, 2.5)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", null, 0.4)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", null, 1.255)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt(StrUtil.repeat("字", 10000), null, null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(new TextToSpeechPrompt("你好",
                    TextToSpeechOptions.builder().model("speech-2.8-turbo").voice("v").build())));
            assertEquals(0, server.requests().size());
        }
        assertThrows(IllegalArgumentException.class, () -> new MiniMaxSpeechModel(null, API_KEY, "speech-3.0-hd"));
    }

}
