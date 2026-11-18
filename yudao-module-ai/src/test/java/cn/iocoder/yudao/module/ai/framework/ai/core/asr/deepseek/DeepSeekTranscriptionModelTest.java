package cn.iocoder.yudao.module.ai.framework.ai.core.asr.deepseek;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.SpeechMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DeepSeekTranscriptionModel} 的协议测试
 *
 * @author 芋道源码
 */
public class DeepSeekTranscriptionModelTest {

    private static final String API_KEY = "deepseek-test-key";

    private static DeepSeekTranscriptionModel createModel(SpeechMockServer server) {
        return new DeepSeekTranscriptionModel(server.url(), API_KEY, Duration.ofSeconds(5));
    }

    private static AudioTranscriptionPrompt prompt(byte[] audio) {
        return new AudioTranscriptionPrompt(new ByteArrayResource(audio));
    }

    @Test
    public void testCall_success() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 200, "{\"request_id\":\"r-1\",\"text\":\"上海今天天气怎么样？\","
                        + "\"duration_ms\":3200}"))) {
            String text = createModel(server).call(prompt(SpeechMockServer.wav(64))).getResult().getOutput();

            assertEquals("上海今天天气怎么样？", text);
            SpeechMockServer.Request request = server.requests().get(0);
            assertEquals("/v1/tools/asr", request.path());
            assertEquals("Bearer " + API_KEY, request.header("Authorization"));
            assertTrue(request.header("Content-Type").startsWith("multipart/form-data"));
            // 只有 file 一个字段，带文件名；没有模型参数
            String body = new String(request.body(), StandardCharsets.ISO_8859_1);
            assertEquals(1, StrUtil.count(body, "Content-Disposition"));
            assertTrue(body.contains("name=\"file\"; filename=\"speech.wav\""), body);
        }
    }

    @Test
    public void testCall_ogg() throws Exception {
        byte[] ogg = "OggS-opus-test".getBytes(StandardCharsets.US_ASCII);
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 200, "{\"text\":\"hello\"}"))) {
            assertEquals("hello", createModel(server).call(prompt(ogg)).getResult().getOutput());
            String body = new String(server.requests().get(0).body(), StandardCharsets.ISO_8859_1);
            assertTrue(body.contains("filename=\"speech.ogg\""), body);
        }
    }

    @Test
    public void testCall_errorResponse() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 400, "{\"error\":{\"message\":\"audio is not 16-bit PCM WAV\","
                        + "\"type\":\"invalid_request_error\"}}"))) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createModel(server).call(prompt(SpeechMockServer.wav(64))));

            assertTrue(ex.getMessage().contains("HTTP 400"), ex.getMessage());
            assertTrue(ex.getMessage().contains("audio is not 16-bit PCM WAV"), ex.getMessage());
        }
    }

    @Test
    public void testCall_invalidAudio() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeJson(out, 200, "{\"text\":\"x\"}"))) {
            DeepSeekTranscriptionModel model = createModel(server);

            // 非 WAV / Ogg
            assertThrows(IllegalArgumentException.class,
                    () -> model.call(prompt("not audio".getBytes(StandardCharsets.US_ASCII))));
            // 空音频
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt(new byte[0])));
            // 超过 8 MiB
            assertThrows(IllegalArgumentException.class,
                    () -> model.call(prompt(SpeechMockServer.wav(DeepSeekTranscriptionModel.MAX_AUDIO_SIZE + 1))));
            // 断言：没有发出请求
            assertTrue(server.requests().isEmpty());
        }
    }

}
