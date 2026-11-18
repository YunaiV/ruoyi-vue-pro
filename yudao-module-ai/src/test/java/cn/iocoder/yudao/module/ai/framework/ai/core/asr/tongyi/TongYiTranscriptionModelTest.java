package cn.iocoder.yudao.module.ai.framework.ai.core.asr.tongyi;

import cn.hutool.core.codec.Base64;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.SpeechMockServer;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Request;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrStubServer.Response;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TongYiTranscriptionModel} 的协议测试
 *
 * @author 芋道源码
 */
public class TongYiTranscriptionModelTest {

    private static final String API_KEY = "sk-tongyi-asr-test";

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
        server.enqueue(Response.json(200, "{\"output\":{\"choices\":[{\"finish_reason\":\"stop\",\"message\":"
                + "{\"role\":\"assistant\",\"content\":[{\"text\":\"欢迎使用阿里云。\"}],"
                + "\"annotations\":[{\"language\":\"zh\",\"type\":\"audio_info\"}]}}]},"
                + "\"usage\":{\"seconds\":2},\"request_id\":\"r-1\"}"));
        byte[] audio = SpeechMockServer.wav(64);

        // 调用
        String text = buildModel(null).call(prompt(audio)).getResult().getOutput();

        // 断言：结果
        assertEquals("欢迎使用阿里云。", text);
        // 断言：请求
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/api/v1/services/aigc/multimodal-generation/generation", request.path());
        assertEquals("Bearer " + API_KEY, request.header("Authorization"));
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals(TongYiTranscriptionModel.DEFAULT_MODEL, body.path("model").asText());
        JsonNode message = body.path("input").path("messages").path(0);
        assertEquals("user", message.path("role").asText());
        assertEquals("data:audio/wav;base64," + Base64.encode(audio), message.path("content").path(0).path("audio").asText());
    }

    @Test
    public void testCall_errorResponse() {
        // 准备参数
        server.enqueue(Response.json(400, "{\"request_id\":\"r-2\",\"code\":\"InvalidParameter\","
                + "\"message\":\"The audio is too long\"}"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildModel("qwen3-asr-flash").call(prompt(SpeechMockServer.wav(64))));
        assertTrue(ex.getMessage().contains("HTTP 400"), ex.getMessage());
        assertTrue(ex.getMessage().contains("InvalidParameter The audio is too long"), ex.getMessage());
    }

    @Test
    public void testCall_invalidAudio() {
        TongYiTranscriptionModel model = buildModel(null);

        // 非 WAV
        assertThrows(IllegalArgumentException.class,
                () -> model.call(prompt("OggS-opus".getBytes(StandardCharsets.US_ASCII))));
        // 空音频
        assertThrows(IllegalArgumentException.class, () -> model.call(prompt(new byte[0])));
        // Base64 编码后超过 10 MB
        assertThrows(IllegalArgumentException.class,
                () -> model.call(prompt(SpeechMockServer.wav(TongYiTranscriptionModel.MAX_AUDIO_SIZE + 1))));
        // 断言：没有发出请求
        assertTrue(server.requests().isEmpty());
    }

    private TongYiTranscriptionModel buildModel(String model) {
        return new TongYiTranscriptionModel(server.baseUrl(), API_KEY, model, Duration.ofSeconds(5));
    }

    private static AudioTranscriptionPrompt prompt(byte[] audio) {
        return new AudioTranscriptionPrompt(new ByteArrayResource(audio));
    }

}
