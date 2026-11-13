package cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.SpeechMockServer;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DouBaoSpeechModel} 的协议测试：本地 HTTP 服务按任意边界分块返回首尾相接的 JSON 帧
 *
 * @author 芋道源码
 */
public class DouBaoSpeechModelTest {

    private static final String API_KEY = "doubao-test-key";
    private static final String MODEL = "seed-tts-2.0";
    private static final String SPEAKER = "zh_female_vv_uranus_bigtts";

    private static final byte[] PART1 = SpeechMockServer.mp3(10);
    private static final byte[] PART2 = new byte[]{1, 2, 3, 4, 5, 6, 7};

    private static DouBaoSpeechModel createModel(SpeechMockServer server, Duration timeout) {
        return new DouBaoSpeechModel(server.url(), API_KEY, MODEL, timeout, 1024 * 1024);
    }

    private static TextToSpeechPrompt prompt(String text, String format, Double speed) {
        return new TextToSpeechPrompt(text, TextToSpeechOptions.builder().voice(SPEAKER).format(format).speed(speed).build());
    }

    private static String audioFrame(byte[] audio) {
        return "{\"code\":0,\"message\":\"\",\"data\":\"" + Base64.getEncoder().encodeToString(audio) + "\"}";
    }

    /**
     * 句子帧：含中文、转义引号与花括号，验证按字节切帧不受字符串内容影响
     */
    private static final String SENTENCE_FRAME = "{\"code\":0,\"message\":\"\",\"data\":null,"
            + "\"sentence\":{\"text\":\"他说\\\"{你好}\\\"，世界\",\"words\":[{\"word\":\"他\",\"startTime\":0.1}]}}";
    private static final String FINISH_FRAME = "{\"code\":20000000,\"message\":\"ok\",\"data\":null,\"usage\":{\"text_words\":4}}";

    private static byte[] bytes(String... frames) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (String frame : frames) {
            byte[] data = frame.getBytes(StandardCharsets.UTF_8);
            out.write(data, 0, data.length);
        }
        return out.toByteArray();
    }

    private static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.write(part, 0, part.length);
        }
        return out.toByteArray();
    }

    @Test
    public void testCall_successWithArbitraryChunks() throws Exception {
        // 帧首尾相接（中间只有一处换行），分块边界精确落在：UTF-8 多字节字符中间、base64 中间、两帧之间、结束帧中间
        byte[] stream = bytes(SENTENCE_FRAME, audioFrame(PART1), "\n", audioFrame(PART2), FINISH_FRAME);
        int sentenceLength = SENTENCE_FRAME.getBytes(StandardCharsets.UTF_8).length;
        // 「他」的 UTF-8 编码为 3 字节，切在第 1 个字节之后
        int insideChinese = SENTENCE_FRAME.substring(0, SENTENCE_FRAME.indexOf('他')).getBytes(StandardCharsets.UTF_8).length + 1;
        int insideBase64 = sentenceLength + audioFrame(PART1).indexOf("\"data\":\"") + 12;
        int betweenFrames = sentenceLength + audioFrame(PART1).length();
        int insideFinish = stream.length - 10;
        List<byte[]> chunks = SpeechMockServer.splitAt(stream, insideChinese, insideBase64, betweenFrames, insideFinish);
        assertEquals(5, chunks.size());
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeChunked(out, 200, "application/json", chunks, true, "X-Tt-Logid", "logid-1"))) {
            TextToSpeechResponse response = createModel(server, Duration.ofSeconds(30)).call(prompt("你好，世界", null, 1.2));

            // 断言：逐帧解码后追加字节（两段 base64 都带填充，直接拼接字符串无法解码）
            assertArrayEquals(concat(PART1, PART2), response.getResult().getOutput());
            assertEquals("mp3", response.getMetadata().get(AbstractAiSpeechModel.METADATA_FORMAT));
            assertEquals("logid-1", response.getMetadata().get(DouBaoSpeechModel.METADATA_LOG_ID));
            // 断言：请求头
            SpeechMockServer.Request request = server.requests().get(0);
            assertEquals("/api/v3/tts/unidirectional", request.path());
            assertEquals(API_KEY, request.header("X-Api-Key"));
            assertEquals(MODEL, request.header("X-Api-Resource-Id"));
            String requestId = request.header("X-Api-Request-Id");
            assertEquals(requestId, UUID.fromString(requestId).toString());
            assertEquals(requestId, response.getMetadata().get(DouBaoSpeechModel.METADATA_CLIENT_REQUEST_ID));
            assertNull(request.header("Authorization"));
            assertNull(request.header("X-Api-App-Id"));
            // 断言：请求体
            JsonNode body = AiSpeechUtils.parseJson(request.body());
            assertFalse(body.at("/user/uid").asText().isEmpty());
            assertEquals("你好，世界", body.at("/req_params/text").asText());
            assertEquals(SPEAKER, body.at("/req_params/speaker").asText());
            assertEquals("mp3", body.at("/req_params/audio_params/format").asText());
            assertTrue(body.at("/req_params/audio_params/speech_rate").isInt());
            assertEquals(20, body.at("/req_params/audio_params/speech_rate").asInt());
            assertTrue(body.at("/req_params/audio_params/sample_rate").isMissingNode());
            assertTrue(body.at("/req_params/model").isMissingNode());
        }
    }

    @Test
    public void testCall_finishFrameWithDataAndOggOpus() throws Exception {
        byte[] ogg = "OggS-opus-page".getBytes(StandardCharsets.US_ASCII);
        String finish = "{\"code\":20000000,\"message\":\"ok\",\"data\":\"" + Base64.getEncoder().encodeToString(ogg) + "\"}";
        try (SpeechMockServer server = new SpeechMockServer((request, out) ->
                SpeechMockServer.writeChunked(out, 200, "application/json", Arrays.asList(bytes(finish)), true))) {
            TextToSpeechResponse response = createModel(server, Duration.ofSeconds(5)).call(prompt("你好", "ogg_opus", 0.75));

            assertArrayEquals(ogg, response.getResult().getOutput());
            JsonNode body = AiSpeechUtils.parseJson(server.requests().get(0).body());
            assertEquals("ogg_opus", body.at("/req_params/audio_params/format").asText());
            assertEquals(48000, body.at("/req_params/audio_params/sample_rate").asInt());
            assertEquals(-25, body.at("/req_params/audio_params/speech_rate").asInt());
        }
    }

    @Test
    public void testCall_failures() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            String text = AiSpeechUtils.parseJson(request.body()).at("/req_params/text").asText();
            switch (text) {
                // 错误帧：在已收到音频之后
                case "error":
                    SpeechMockServer.writeChunked(out, 200, "application/json", Arrays.asList(bytes(audioFrame(PART1),
                            "{\"code\":45000000,\"message\":\"speaker permission denied " + API_KEY + "\",\"data\":null}")), true);
                    break;
                // 正常结束了 HTTP 响应，但没有结束帧
                case "nofinish":
                    SpeechMockServer.writeChunked(out, 200, "application/json",
                            Arrays.asList(bytes(audioFrame(PART1))), true);
                    break;
                // 连接在帧中间断开
                case "eof":
                    SpeechMockServer.writeChunked(out, 200, "application/json",
                            SpeechMockServer.split(bytes(audioFrame(PART1), audioFrame(PART2)), 9).subList(0, 3), false);
                    break;
                case "badbase64":
                    SpeechMockServer.writeChunked(out, 200, "application/json",
                            Arrays.asList(bytes("{\"code\":0,\"data\":\"@@@\"}", FINISH_FRAME)), true);
                    break;
                case "garbage":
                    SpeechMockServer.writeChunked(out, 200, "application/json",
                            Arrays.asList(bytes(audioFrame(PART1), "data: x", FINISH_FRAME)), true);
                    break;
                case "noaudio":
                    SpeechMockServer.writeChunked(out, 200, "application/json",
                            Arrays.asList(bytes(SENTENCE_FRAME, FINISH_FRAME)), true);
                    break;
                case "afterfinish":
                    SpeechMockServer.writeChunked(out, 200, "application/json",
                            Arrays.asList(bytes(audioFrame(PART1), FINISH_FRAME, audioFrame(PART2))), true);
                    break;
                case "nocode":
                    SpeechMockServer.writeChunked(out, 200, "application/json",
                            Arrays.asList(bytes("{\"data\":\"AAAA\"}", FINISH_FRAME)), true);
                    break;
                case "notmp3":
                    SpeechMockServer.writeChunked(out, 200, "application/json",
                            Arrays.asList(bytes(audioFrame(PART2), FINISH_FRAME)), true);
                    break;
                default:
                    SpeechMockServer.writeJson(out, 401, "{\"code\":45000010,\"message\":\"invalid X-Api-Key\"}",
                            "X-Tt-Logid", "logid-401");
                    break;
            }
        })) {
            DouBaoSpeechModel model = createModel(server, Duration.ofSeconds(5));
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("error", null, null)));
            assertTrue(ex.getMessage().contains("45000000"));
            assertFalse(ex.getMessage().contains(API_KEY));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("nofinish", null, null)));
            assertTrue(ex.getMessage().contains("未收到结束帧"));
            assertThrows(IllegalStateException.class, () -> model.call(prompt("eof", null, null)));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("badbase64", null, null)));
            assertTrue(ex.getMessage().contains("base64"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("garbage", null, null)));
            assertTrue(ex.getMessage().contains("格式不正确"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("noaudio", null, null)));
            assertTrue(ex.getMessage().contains("未返回音频"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("afterfinish", null, null)));
            assertTrue(ex.getMessage().contains("结束帧之后"));
            assertThrows(IllegalStateException.class, () -> model.call(prompt("nocode", null, null)));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("notmp3", null, null)));
            assertTrue(ex.getMessage().contains("不是 mp3 格式"));
            ex = assertThrows(IllegalStateException.class, () -> model.call(prompt("http401", null, null)));
            assertTrue(ex.getMessage().contains("HTTP 401"));
            assertTrue(ex.getMessage().contains("45000010"));
            assertTrue(ex.getMessage().contains("logid-401"));
            assertEquals(10, server.requests().size());
        }
    }

    @Test
    public void testCall_timeout() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> {
            SpeechMockServer.writeChunked(out, 200, "application/json", Arrays.asList(bytes(audioFrame(PART1))), false);
            Thread.sleep(3000);
        })) {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> createModel(server, Duration.ofMillis(500)).call(prompt("你好", null, null)));
            assertTrue(ex.getMessage().contains("超时"));
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
                assertEquals("你好", AiSpeechUtils.parseJson(server.requests().get(0).body()).at("/req_params/text").asText());
            }
        }
    }

    @Test
    public void testFrameParser_byteByByte() {
        byte[] stream = bytes(SENTENCE_FRAME, " \r\n", audioFrame(PART1), audioFrame(PART2), FINISH_FRAME, "\n");
        DouBaoSpeechModel.FrameParser parser = new DouBaoSpeechModel.FrameParser(1024, 4096, API_KEY);
        for (byte b : stream) {
            parser.feed(new byte[]{b});
        }
        assertArrayEquals(concat(PART1, PART2), parser.finish());
        // 音频超过上限
        DouBaoSpeechModel.FrameParser small = new DouBaoSpeechModel.FrameParser(12, 4096, API_KEY);
        assertThrows(IllegalStateException.class, () -> small.feed(bytes(audioFrame(PART1), audioFrame(PART2))));
        // 单帧超过上限
        DouBaoSpeechModel.FrameParser smallFrame = new DouBaoSpeechModel.FrameParser(1024, 16, API_KEY);
        assertThrows(IllegalStateException.class, () -> smallFrame.feed(bytes(audioFrame(PART1))));
    }

    @Test
    public void testToSpeechRate() {
        assertEquals(-50, DouBaoSpeechModel.toSpeechRate(new BigDecimal("0.5")));
        assertEquals(-25, DouBaoSpeechModel.toSpeechRate(new BigDecimal("0.75")));
        assertEquals(0, DouBaoSpeechModel.toSpeechRate(BigDecimal.ONE));
        assertEquals(20, DouBaoSpeechModel.toSpeechRate(new BigDecimal("1.2")));
        assertEquals(25, DouBaoSpeechModel.toSpeechRate(new BigDecimal("1.25")));
        assertEquals(33, DouBaoSpeechModel.toSpeechRate(new BigDecimal("1.33")));
        assertEquals(100, DouBaoSpeechModel.toSpeechRate(new BigDecimal("2")));
    }

    @Test
    public void testCall_validateBeforePaidCall() throws Exception {
        try (SpeechMockServer server = new SpeechMockServer((request, out) -> SpeechMockServer.writeJson(out, 500, "{}"))) {
            DouBaoSpeechModel model = createModel(server, Duration.ofSeconds(5));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", "wav", null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", "pcm", null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", null, 2.01)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt("你好", null, 1.005)));
            assertThrows(IllegalArgumentException.class, () -> model.call(prompt(StrUtil.repeat("字", 10001), null, null)));
            assertThrows(IllegalArgumentException.class, () -> model.call(new TextToSpeechPrompt("你好",
                    TextToSpeechOptions.builder().model("seed-tts-1.0").voice(SPEAKER).build())));
            assertEquals(0, server.requests().size());
        }
        // 声音复刻资源不在本次范围
        assertThrows(IllegalArgumentException.class, () -> new DouBaoSpeechModel(null, API_KEY, "seed-icl-2.0"));
    }

}
