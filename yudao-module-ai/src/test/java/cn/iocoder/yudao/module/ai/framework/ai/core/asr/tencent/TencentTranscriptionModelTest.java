package cn.iocoder.yudao.module.ai.framework.ai.core.asr.tencent;

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
 * {@link TencentTranscriptionModel} 的单元测试：官方 SDK 真实发请求到本地 HTTP 桩服务，验证请求（签名头、Action、请求体）和响应处理
 *
 * @author 芋道源码
 */
public class TencentTranscriptionModelTest {

    private static final String SECRET_ID = "AKIDtencentAsrTestId001";
    private static final String SECRET_KEY = "tencentAsrTestSecretKey001";
    private static final String API_KEY = SECRET_ID + "|" + SECRET_KEY;

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
        server.enqueue(Response.json(200, "{\"Response\":{\"Result\":\"腾讯云语音识别\",\"AudioDuration\":2000,"
                + "\"WordSize\":0,\"WordList\":null,\"RequestId\":\"req-asr-1\"}}"));
        byte[] audio = SpeechMockServer.wav(64);

        // 调用
        String text = buildModel(null).call(prompt(audio)).getResult().getOutput();

        // 断言：结果
        assertEquals("腾讯云语音识别", text);
        // 断言：请求
        Request request = server.lastRequest();
        assertEquals("POST", request.method());
        assertEquals("SentenceRecognition", request.header("X-TC-Action"));
        assertEquals("2019-06-14", request.header("X-TC-Version"));
        // 断言：TC3 签名只带 SecretId，不带 SecretKey
        String authorization = request.header("Authorization");
        assertTrue(authorization.startsWith("TC3-HMAC-SHA256 Credential=" + SECRET_ID + "/"), authorization);
        assertFalse(authorization.contains(SECRET_KEY));
        assertFalse(request.body().contains(SECRET_KEY));
        // 断言：请求体，整数参数不能变成浮点数
        JsonNode body = JsonUtils.parseTree(request.body());
        assertEquals(TencentTranscriptionModel.DEFAULT_MODEL, body.path("EngSerViceType").asText());
        assertTrue(body.path("SourceType").isInt(), request.body());
        assertEquals(1, body.path("SourceType").asInt());
        assertEquals("wav", body.path("VoiceFormat").asText());
        assertEquals(Base64.encode(audio), body.path("Data").asText());
        assertTrue(body.path("DataLen").isInt(), request.body());
        assertEquals(audio.length, body.path("DataLen").asInt());
    }

    @Test
    public void testCall_errorResponse() {
        // 准备参数
        server.enqueue(Response.json(200, "{\"Response\":{\"Error\":{\"Code\":\"FailedOperation.UserNotRegistered\","
                + "\"Message\":\"service not open\"},\"RequestId\":\"req-asr-2\"}}"));

        // 调用，并断言异常
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> buildModel("16k_en").call(prompt(SpeechMockServer.wav(64))));
        assertTrue(ex.getMessage().contains("FailedOperation.UserNotRegistered"), ex.getMessage());
        assertTrue(ex.getMessage().contains("req-asr-2"), ex.getMessage());
        assertFalse(ex.getMessage().contains(SECRET_KEY));
    }

    @Test
    public void testCall_invalid() {
        // 密钥格式不正确
        assertThrows(IllegalArgumentException.class,
                () -> new TencentTranscriptionModel(server.baseUrl(), SECRET_ID, null));
        // 不是 16k 引擎
        assertThrows(IllegalArgumentException.class, () -> buildModel("8k_zh"));

        TencentTranscriptionModel model = buildModel(null);
        // 非 WAV
        assertThrows(IllegalArgumentException.class, () -> model.call(prompt(SpeechMockServer.mp3(64))));
        // Base64 编码后超过 3 MB
        assertThrows(IllegalArgumentException.class,
                () -> model.call(prompt(SpeechMockServer.wav(TencentTranscriptionModel.MAX_AUDIO_SIZE + 1))));
        // 断言：没有发出请求
        assertTrue(server.requests().isEmpty());
    }

    private TencentTranscriptionModel buildModel(String model) {
        return new TencentTranscriptionModel(server.baseUrl(), API_KEY, model, Duration.ofSeconds(5));
    }

    private static AudioTranscriptionPrompt prompt(byte[] audio) {
        return new AudioTranscriptionPrompt(new ByteArrayResource(audio));
    }

}
