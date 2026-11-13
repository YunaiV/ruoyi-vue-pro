package cn.iocoder.yudao.module.ai.framework.ai.core.model.doubao;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 豆包语音（火山引擎）的语音合成模型：HTTP Chunked 单向流式 V3
 *
 * 1. POST {baseUrl}/api/v3/tts/unidirectional（普通 HTTP 分块响应，不是 SSE）；
 *    请求头 X-Api-Key（新版控制台）、X-Api-Resource-Id（即绑定的模型）、X-Api-Request-Id（每次新生成的 UUID）
 * 2. 响应体是首尾相接的多个 JSON 对象，不假设按行或按 HTTP 分块切分：按字节扫描出完整对象后再按 UTF-8 解析
 * 3. code=0 的帧：data 为 base64 音频，逐帧解码后追加字节（不拼接 base64 字符串）；data 为 null 的句子帧跳过。
 *    code=20000000 为结束帧：可选的 data 照常处理，之后不能再有其它帧。其它 code 为失败
 * 4. 没收到结束帧就断开、格式错误、或没有任何音频，都按失败处理并丢弃已收到的部分音频
 * 5. 服务端的 X-Tt-Logid、客户端的请求编号只用于排查，不是可轮询的任务编号
 *
 * @see <a href="https://www.volcengine.com/docs/6561/1598757">豆包语音合成 HTTP Chunked 单向流式 V3</a>
 * @author 芋道源码
 */
@Slf4j
public class DouBaoSpeechModel extends AbstractAiSpeechModel {

    public static final String DEFAULT_BASE_URL = "https://openspeech.bytedance.com";
    private static final String PATH = "/api/v3/tts/unidirectional";

    /**
     * 支持的模型，即 X-Api-Resource-Id：语音合成 1.0、2.0；不包含声音复刻（seed-icl-*）
     */
    public static final Set<String> MODELS = SetUtils.asSet("seed-tts-2.0", "seed-tts-1.0", "seed-tts-1.0-concurr");
    /**
     * 朗读文本的最大字符数：接口未公开精确上限（超限返回 40402003），这里使用产品上限
     */
    private static final int MAX_TEXT_LENGTH = 10000;
    private static final List<String> FORMATS = ListUtil.of(AiSpeechUtils.FORMAT_MP3, AiSpeechUtils.FORMAT_OGG_OPUS);
    private static final BigDecimal MIN_SPEED = new BigDecimal("0.5");
    private static final BigDecimal MAX_SPEED = new BigDecimal("2");
    /**
     * ogg_opus 只支持 48000 采样率
     */
    private static final int OGG_OPUS_SAMPLE_RATE = 48000;
    /**
     * user.uid：调用层不读取登录用户，使用固定的非个人标识
     */
    private static final String USER_UID = "yudao-ai-speech";

    public static final String METADATA_LOG_ID = "logId";
    public static final String METADATA_CLIENT_REQUEST_ID = "clientRequestId";

    private static final String HEADER_API_KEY = "X-Api-Key";
    private static final String HEADER_RESOURCE_ID = "X-Api-Resource-Id";
    private static final String HEADER_REQUEST_ID = "X-Api-Request-Id";
    private static final String HEADER_LOG_ID = "X-Tt-Logid";

    private final String apiKey;
    private final WebClient webClient;
    private final Duration timeout;
    private final int maxAudioSize;

    public DouBaoSpeechModel(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, AiSpeechUtils.REQUEST_TIMEOUT, AiSpeechUtils.MAX_AUDIO_SIZE);
    }

    public DouBaoSpeechModel(String baseUrl, String apiKey, String model, Duration timeout, int maxAudioSize) {
        super(model);
        if (!MODELS.contains(model)) {
            throw new IllegalArgumentException(StrUtil.format("豆包语音合成不支持模型({})，可选：{}", model, MODELS));
        }
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("豆包语音合成的 API Key 不能为空");
        }
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.maxAudioSize = maxAudioSize;
        this.webClient = AiSpeechUtils.buildWebClient(
                StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/"),
                AiSpeechUtils.MAX_JSON_SIZE, headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set(HEADER_API_KEY, apiKey);
                    headers.set(HEADER_RESOURCE_ID, model);
                });
    }

    @Override
    protected SpeechResult doCall(String text, String voice, String format, BigDecimal speed) {
        // 1. 请求
        Map<String, Object> audioParams = new LinkedHashMap<>();
        audioParams.put("format", format);
        audioParams.put("speech_rate", toSpeechRate(speed));
        if (AiSpeechUtils.FORMAT_OGG_OPUS.equals(format)) {
            audioParams.put("sample_rate", OGG_OPUS_SAMPLE_RATE);
        }
        Map<String, Object> reqParams = new LinkedHashMap<>();
        reqParams.put("text", text);
        reqParams.put("speaker", voice);
        reqParams.put("audio_params", audioParams);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("user", Collections.singletonMap("uid", USER_UID));
        body.put("req_params", reqParams);
        String clientRequestId = UUID.randomUUID().toString();

        // 2. 流式解析：边收边按对象边界切帧
        FrameParser parser = new FrameParser(maxAudioSize, AiSpeechUtils.MAX_JSON_SIZE + maxAudioSize / 2 * 3, apiKey);
        String[] logId = new String[1];
        Mono<byte[]> mono = webClient.post().uri(PATH)
                .header(HEADER_REQUEST_ID, clientRequestId)
                .bodyValue(JsonUtils.toJsonByte(body))
                .exchangeToMono(response -> {
                    logId[0] = response.headers().asHttpHeaders().getFirst(HEADER_LOG_ID);
                    if (!response.statusCode().is2xxSuccessful()) {
                        int status = response.statusCode().value();
                        return response.bodyToMono(byte[].class).defaultIfEmpty(new byte[0])
                                .flatMap(errorBody -> Mono.error(buildHttpException(status, errorBody,
                                        logId[0], clientRequestId)));
                    }
                    return response.bodyToFlux(DataBuffer.class).doOnNext(buffer -> {
                        try {
                            byte[] bytes = new byte[buffer.readableByteCount()];
                            buffer.read(bytes);
                            parser.feed(bytes);
                        } finally {
                            DataBufferUtils.release(buffer);
                        }
                    }).then(Mono.fromCallable(parser::finish));
                });
        byte[] audio;
        try {
            audio = AiSpeechUtils.block(mono, timeout, "豆包语音合成");
        } catch (IllegalStateException ex) {
            log.warn("[DouBaoSpeech][合成失败，logId {}，clientRequestId {}]", logId[0], clientRequestId);
            throw new IllegalStateException(StrUtil.format("{}，logid={}，request_id={}", ex.getMessage(),
                    StrUtil.blankToDefault(logId[0], "-"), clientRequestId));
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put(METADATA_LOG_ID, logId[0]);
        metadata.put(METADATA_CLIENT_REQUEST_ID, clientRequestId);
        return new SpeechResult(audio, metadata);
    }

    private IllegalStateException buildHttpException(int status, byte[] errorBody, String logId, String clientRequestId) {
        JsonNode root = AiSpeechUtils.parseJson(errorBody);
        String code = root != null ? root.path("code").asText("-") : "-";
        // 非 JSON 的错误响应（如网关页面）不放进异常信息
        String message = root != null ? root.path("message").asText("") : "响应不是 JSON";
        log.warn("[DouBaoSpeech][HTTP {}，code {}，logId {}，clientRequestId {}]", status, code, logId, clientRequestId);
        return new IllegalStateException(StrUtil.format("豆包语音合成失败（HTTP {}，code={}）：{}",
                status, code, AiSpeechUtils.sanitize(message, apiKey)));
    }

    /**
     * 语速倍数转换为 speech_rate：(倍数 - 1) × 100，0.5 ~ 2 对应 -50 ~ 100
     *
     * 倍数已校验为最多两位小数，结果是精确整数；仍使用十进制运算，避免浮点误差
     */
    static int toSpeechRate(BigDecimal speed) {
        return speed.subtract(BigDecimal.ONE).movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    @Override
    protected int getMaxTextLength() {
        return MAX_TEXT_LENGTH;
    }

    @Override
    public Collection<String> getSupportedFormats() {
        return FORMATS;
    }

    @Override
    public String getDefaultFormat() {
        return AiSpeechUtils.FORMAT_MP3;
    }

    @Override
    public BigDecimal getMinSpeed() {
        return MIN_SPEED;
    }

    @Override
    public BigDecimal getMaxSpeed() {
        return MAX_SPEED;
    }

    /**
     * 响应帧解析器：从任意切分的字节流中按 JSON 对象边界切帧
     *
     * 只在字节层面识别 { } " \ 四个 ASCII 字符；UTF-8 多字节字符的每个字节都 ≥ 0x80，不会被误判，
     * 所以 HTTP 分块切在多字节字符中间也没有问题。完整对象收齐后再按 UTF-8 解码
     *
     * 非线程安全：一次请求一个实例
     */
    static class FrameParser {

        static final int CODE_AUDIO = 0;
        static final int CODE_FINISHED = 20000000;

        private final int maxAudioSize;
        private final int maxFrameSize;
        /**
         * 错误信息中需要打码的凭证
         */
        private final String apiKey;

        private final ByteArrayOutputStream frame = new ByteArrayOutputStream();
        private final ByteArrayOutputStream audio = new ByteArrayOutputStream();
        private int depth;
        private boolean inString;
        private boolean escaped;
        private boolean finished;

        FrameParser(int maxAudioSize, int maxFrameSize, String apiKey) {
            this.maxAudioSize = maxAudioSize;
            this.maxFrameSize = maxFrameSize;
            this.apiKey = apiKey;
        }

        void feed(byte[] bytes) {
            for (byte b : bytes) {
                feed(b);
            }
        }

        private void feed(byte b) {
            // 情况一：对象之外，只允许空白与下一个对象的开始
            if (depth == 0) {
                if (b == ' ' || b == '\n' || b == '\r' || b == '\t') {
                    return;
                }
                if (b != '{') {
                    throw new IllegalStateException("豆包语音合成返回格式不正确");
                }
                if (finished) {
                    throw new IllegalStateException("豆包语音合成在结束帧之后仍返回数据");
                }
                depth = 1;
                frame.write(b);
                return;
            }
            // 情况二：对象之内
            frame.write(b);
            if (frame.size() > maxFrameSize) {
                throw new IllegalStateException("豆包语音合成的响应帧超过大小上限");
            }
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (b == '\\') {
                    escaped = true;
                } else if (b == '"') {
                    inString = false;
                }
                return;
            }
            if (b == '"') {
                inString = true;
            } else if (b == '{') {
                depth++;
            } else if (b == '}') {
                depth--;
                if (depth == 0) {
                    byte[] bytes = frame.toByteArray();
                    frame.reset();
                    handleFrame(bytes);
                }
            }
        }

        private void handleFrame(byte[] bytes) {
            JsonNode node = AiSpeechUtils.parseJson(bytes);
            if (node == null || !node.isObject() || !node.path("code").isIntegralNumber()) {
                throw new IllegalStateException("豆包语音合成返回格式不正确");
            }
            long code = node.path("code").asLong();
            if (code == CODE_AUDIO) {
                appendAudio(node.get("data"));
                return;
            }
            if (code == CODE_FINISHED) {
                appendAudio(node.get("data"));
                finished = true;
                return;
            }
            throw new IllegalStateException(StrUtil.format("豆包语音合成失败（code={}）：{}",
                    code, AiSpeechUtils.sanitize(node.path("message").asText(""), apiKey)));
        }

        private void appendAudio(JsonNode data) {
            // 句子、时间戳等帧没有音频
            if (data == null || data.isNull()) {
                return;
            }
            if (!data.isTextual()) {
                throw new IllegalStateException("豆包语音合成返回的音频格式不正确");
            }
            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(data.asText());
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("豆包语音合成返回的音频不是合法的 base64");
            }
            if ((long) audio.size() + decoded.length > maxAudioSize) {
                throw new IllegalStateException("豆包语音合成失败：音频超过大小上限");
            }
            audio.write(decoded, 0, decoded.length);
        }

        /**
         * 响应结束：必须收到结束帧、没有残留的半个对象、且有音频
         */
        byte[] finish() {
            if (depth != 0 || !finished) {
                throw new IllegalStateException("豆包语音合成的响应提前结束，未收到结束帧，已丢弃部分音频");
            }
            if (audio.size() == 0) {
                throw new IllegalStateException("豆包语音合成未返回音频");
            }
            return audio.toByteArray();
        }

    }

}
