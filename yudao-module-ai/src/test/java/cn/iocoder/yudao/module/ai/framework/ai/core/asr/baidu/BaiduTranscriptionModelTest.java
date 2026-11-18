package cn.iocoder.yudao.module.ai.framework.ai.core.asr.baidu;

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

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BaiduTranscriptionModel} 的协议测试
 *
 * @author 芋道源码
 */
public class BaiduTranscriptionModelTest {

    private static final String API_KEY = "bce-v3/ALTAK-baidu-asr-test";

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
        server.enqueue(Response.json(200, "{\"corpus_no\":\"1\",\"err_msg\":\"success.\",\"err_no\":0,"
                + "\"result\":[\"北京科技馆。\"],\"sn\":\"sn-1\"}"));
        byte[] audio = wav(16000, 64);

        // 调用
        String text = buildModel("1737").call(prompt(audio)).getResult().getOutput();

        // 断言：结果
        assertEquals("北京科技馆。", text);
        // 断言：请求
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("/server_api", request.path());
        assertEquals("Bearer " + API_KEY, request.header("Authorization"));
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals("wav", body.path("format").asText());
        assertEquals(16000, body.path("rate").asInt());
        assertEquals(1, body.path("channel").asInt());
        assertFalse(body.path("cuid").asText().isEmpty());
        assertEquals(1737, body.path("dev_pid").asInt());
        assertEquals(Base64.encode(audio), body.path("speech").asText());
        assertEquals(audio.length, body.path("len").asInt());
        assertTrue(body.path("token").isMissingNode());
    }

    @Test
    public void testCall_errorWithHttp200() {
        // 准备参数：失败时 HTTP 状态仍为 200
        server.enqueue(Response.json(200, "{\"err_msg\":\"speech quality error.\",\"err_no\":3301,\"sn\":\"sn-2\"}"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildModel(null).call(prompt(wav(16000, 64))));
        assertTrue(ex.getMessage().contains("3301 speech quality error."), ex.getMessage());
        assertEquals(1537, JsonUtils.parseTree(server.lastRequest().body()).path("dev_pid").asInt());
    }

    @Test
    public void testCall_invalid() {
        // 模型不是 dev_pid
        assertThrows(IllegalArgumentException.class, () -> buildModel("pro"));

        BaiduTranscriptionModel model = buildModel(null);
        // 非 WAV
        assertThrows(IllegalArgumentException.class, () -> model.call(prompt(SpeechMockServer.mp3(64))));
        // 采样率不是 16000 / 8000
        assertThrows(IllegalArgumentException.class, () -> model.call(prompt(wav(44100, 64))));
        // 超过大小上限
        assertThrows(IllegalArgumentException.class,
                () -> model.call(prompt(wav(16000, BaiduTranscriptionModel.MAX_AUDIO_SIZE))));
        // 断言：没有发出请求
        assertTrue(server.requests().isEmpty());
    }

    private BaiduTranscriptionModel buildModel(String model) {
        return new BaiduTranscriptionModel(server.baseUrl(), API_KEY, model, Duration.ofSeconds(5));
    }

    private static AudioTranscriptionPrompt prompt(byte[] audio) {
        return new AudioTranscriptionPrompt(new ByteArrayResource(audio));
    }

    /**
     * 最小的 WAV：44 字节文件头（第 24 ~ 27 字节为采样率，小端）+ 音频数据
     */
    private static byte[] wav(int sampleRate, int dataLength) {
        byte[] data = SpeechMockServer.wav(44 + dataLength);
        data[24] = (byte) sampleRate;
        data[25] = (byte) (sampleRate >> 8);
        data[26] = (byte) (sampleRate >> 16);
        data[27] = (byte) (sampleRate >> 24);
        return data;
    }

}
