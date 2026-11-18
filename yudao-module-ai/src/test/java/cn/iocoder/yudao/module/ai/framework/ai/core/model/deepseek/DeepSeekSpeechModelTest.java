package cn.iocoder.yudao.module.ai.framework.ai.core.model.deepseek;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.SpeechMockServer;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DeepSeekSpeechModel} 的协议测试
 *
 * @author 芋道源码
 */
public class DeepSeekSpeechModelTest {

    private static final String API_KEY = "deepseek-test-key";
    private static final String MODEL = "deepseek-tts";

    private static DeepSeekSpeechModel createModel(SpeechMockServer server) {
        return new DeepSeekSpeechModel(server.url(), API_KEY, MODEL, Duration.ofSeconds(5), 1024 * 1024);
    }

    private static TextToSpeechPrompt prompt(String text, String voice, Double speed) {
        return new TextToSpeechPrompt(text, TextToSpeechOptions.builder().voice(voice).speed(speed).build());
    }

    @Test
    public void testCall_success() throws Exception {
        byte[] wav = SpeechMockServer.wav(256);
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeResponse(out, 200, "audio/wav", wav))) {
            TextToSpeechResponse response = createModel(server).call(prompt("你好", "tide", null));

            assertArrayEquals(wav, response.getResult().getOutput());
            assertEquals("wav", response.getMetadata().get(AbstractAiSpeechModel.METADATA_FORMAT));
            SpeechMockServer.Request request = server.requests().get(0);
            assertEquals("/v1/tools/tts", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            JsonNode body = AiSpeechUtils.parseJson(request.body());
            assertEquals("你好", body.get("text").asText());
            assertEquals("tide", body.get("voice_id").asText());
            assertEquals("wav", body.get("format").asText());
            // 官方没有模型、语速参数
            assertFalse(body.has("model"));
            assertFalse(body.has("speed"));
        }
    }

    @Test
    public void testCall_errorResponse() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 400, "{\"error\":{\"message\":\"Content Exists Risk\","
                        + "\"type\":\"invalid_request_error\",\"code\":\"invalid_request_error\"}}"))) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createModel(server).call(prompt("你好", "stella", null)));

            assertTrue(ex.getMessage().contains("HTTP 400"), ex.getMessage());
            assertTrue(ex.getMessage().contains("Content Exists Risk"), ex.getMessage());
        }
    }

    @Test
    public void testCall_notWav() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeResponse(out, 200, "audio/wav", new byte[]{1, 2, 3}))) {
            assertThrows(IllegalStateException.class, () -> createModel(server).call(prompt("你好", "stella", null)));
        }
    }

    @Test
    public void testValidateOptions() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeResponse(out, 200, "audio/wav", SpeechMockServer.wav(16)))) {
            DeepSeekSpeechModel model = createModel(server);

            // 音色只能是 stella、tide
            assertThrows(IllegalArgumentException.class, () -> model.validateOptions("你好", "alloy", null, null));
            // 不支持调节语速
            assertThrows(IllegalArgumentException.class, () -> model.validateOptions("你好", "stella", null, 1.5));
            // 只支持 wav
            assertThrows(IllegalArgumentException.class, () -> model.validateOptions("你好", "stella", "mp3", null));
            // 文本超长
            assertThrows(IllegalArgumentException.class, () -> model.validateOptions(
                    new String(new char[5001]).replace('\0', '字'), "stella", null, null));
            // 合法
            model.validateOptions("你好", "stella", "wav", 1.0);
            // 断言：没有发出请求
            assertTrue(server.requests().isEmpty());
        }
    }

}
