package cn.iocoder.yudao.module.ai.framework.ai.core.model.baidu;

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
 * {@link BaiduSpeechModel} 的协议测试
 *
 * @author 芋道源码
 */
public class BaiduSpeechModelTest {

    private static final String API_KEY = "bce-v3/ALTAK-test/secret-part";
    private static final String MODEL = "baidu-tts";

    private static BaiduSpeechModel createModel(SpeechMockServer server) {
        // 本类不测超时：超时放宽，避免机器负载高时首个请求（冷启动）误报超时
        return new BaiduSpeechModel(server.url(), API_KEY, MODEL, Duration.ofSeconds(30), 1024 * 1024);
    }

    private static TextToSpeechPrompt prompt(String text, String format, Double speed) {
        return new TextToSpeechPrompt(text, TextToSpeechOptions.builder().voice("5003").format(format).speed(speed).build());
    }

    @Test
    public void testCall_success() throws Exception {
        byte[] mp3 = SpeechMockServer.mp3(64);
        byte[] wav = SpeechMockServer.wav(64);
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            String format = AiSpeechUtils.parseJson(request.body()).get("format").asText();
            if ("wav".equals(format)) {
                SpeechMockServer.writeResponse(out, 200, "audio/wav", wav);
            } else if (request.bodyAsString().contains("mpeg")) {
                SpeechMockServer.writeResponse(out, 200, "audio/mpeg", mp3);
            } else {
                SpeechMockServer.writeResponse(out, 200, "audio/mp3", mp3);
            }
        })) {
            BaiduSpeechModel model = createModel(server);
            TextToSpeechResponse response = model.call(prompt("你好", null, 1.5));

            assertArrayEquals(mp3, response.getResult().getOutput());
            assertEquals("mp3", response.getMetadata().get(AbstractAiSpeechModel.METADATA_FORMAT));
            SpeechMockServer.Request request = server.requests().get(0);
            assertEquals("/rest/2.0/speech/publiccloudspeech/v1/tts", request.path());
            // API Key 鉴权：Bearer 头，不同时传 access_token
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            assertNull(request.query());
            assertFalse(request.bodyAsString().contains("access_token"));
            JsonNode body = AiSpeechUtils.parseJson(request.body());
            assertEquals("你好", body.get("text").asText());
            assertTrue(body.get("voice").isTextual());
            assertEquals("5003", body.get("voice").asText());
            assertEquals("mp3", body.get("format").asText());
            assertEquals("1.5", body.get("speed").decimalValue().toPlainString());
            assertNull(body.get("lang"));
            assertNull(body.get("sample_rate"));
            assertNull(body.get("emotion"));

            // audio/mpeg 别名、wav
            assertArrayEquals(mp3, model.call(prompt("mpeg", null, null)).getResult().getOutput());
            assertArrayEquals(wav, model.call(prompt("你好", "wav", null)).getResult().getOutput());
        }
    }

    @Test
    public void testCall_errorResponses() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            String text = AiSpeechUtils.parseJson(request.body()).get("text").asText();
            switch (text) {
                // 失败时 HTTP 仍为 200
                case "auth":
                    SpeechMockServer.writeJson(out, 200, "{\"code\":100020,\"message\":\"auth failed "
                            + API_KEY + "\",\"sn\":\"sn-1\"}");
                    break;
                case "octet":
                    SpeechMockServer.writeResponse(out, 200, "application/octet-stream", SpeechMockServer.mp3(16));
                    break;
                case "fake":
                    SpeechMockServer.writeResponse(out, 200, "audio/mp3", "{\"code\":1}".getBytes());
                    break;
                case "empty":
                    SpeechMockServer.writeResponse(out, 200, "audio/mp3", new byte[0]);
                    break;
                case "wrongtype":
                    SpeechMockServer.writeResponse(out, 200, "audio/wav", SpeechMockServer.wav(16));
                    break;
                default:
                    SpeechMockServer.writeResponse(out, 502, "text/html", "<html>bad gateway</html>".getBytes());
                    break;
            }
        })) {
            BaiduSpeechModel model = createModel(server);
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("auth", null, null)));
            assertTrue(ex.getMessage().contains("100020"), ex.getMessage());
            assertTrue(ex.getMessage().contains("sn-1"));
            assertFalse(ex.getMessage().contains(API_KEY));
            assertFalse(ex.getMessage().contains("secret-part"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("octet", null, null)));
            assertTrue(ex.getMessage().contains("application/octet-stream"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("fake", null, null)));
            assertTrue(ex.getMessage().contains("不是 mp3 格式"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("empty", null, null)));
            assertTrue(ex.getMessage().contains("未返回音频"));
            // 请求 mp3 却返回 wav
            assertThrows(IllegalStateException.class, () -> model.call(prompt("wrongtype", null, null)));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("gateway", null, null)));
            assertTrue(ex.getMessage().contains("HTTP 502"));
            assertEquals(6, server.requests().size());
        }
    }

    @Test
    public void testValidateText_gbkBytes() throws Exception {
        assertEquals(1022, BaiduSpeechModel.getGbkLength(StrUtil.repeat("字", 511)));
        assertEquals(1023, BaiduSpeechModel.getGbkLength(StrUtil.repeat("a", 1023)));
        // 无法用 GBK 表示：报错而不是替换为问号
        assertThrows(IllegalArgumentException.class, () -> BaiduSpeechModel.getGbkLength("你好😀"));
        assertThrows(IllegalArgumentException.class, () -> BaiduSpeechModel.getGbkLength("𠀀"));

        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeResponse(out, 200, "audio/mp3", SpeechMockServer.mp3(16)))) {
            BaiduSpeechModel model = createModel(server);
            // 1024 字节（512 个汉字）、1024 个 ASCII、emoji：请求前拒绝
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt(StrUtil.repeat("字", 512), null, null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt(StrUtil.repeat("a", 1024), null, null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好😀", null, null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", "pcm", null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", null, 2.5)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", null, 0.555)));
            assertThrows(IllegalArgumentException.class, () -> model.call(new TextToSpeechPrompt("你好",
                    TextToSpeechOptions.builder().model("other").voice("5003").build())));
            assertEquals(0, server.requests().size());
            // 边界：1022 字节（511 个汉字）可以提交；120 字节的计费单位不是输入上限
            model.call(prompt(StrUtil.repeat("字", 511), null, null));
            assertEquals(1, server.requests().size());
        }
    }

}
