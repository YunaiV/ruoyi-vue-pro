package cn.iocoder.yudao.module.ai.framework.ai.core.model.minimax;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * MiniMax 的语音合成模型：同步 HTTP T2A v2
 *
 * 1. POST {baseUrl}/v1/t2a_v2，Bearer API Key；stream=false，output_format=hex
 * 2. 成功必须同时满足：base_resp.status_code 为 0、data.status 为 2（合成结束）、data.audio 为非空的合法十六进制
 * 3. 失败不自动重试，也不降级到其它模型
 *
 * @see <a href="https://platform.minimax.cn/docs/api-reference/speech-t2a-http">MiniMax 同步语音合成 HTTP</a>
 * @author 芋道源码
 */
@Slf4j
public class MiniMaxSpeechModel extends AbstractAiSpeechModel {

    public static final String DEFAULT_BASE_URL = "https://api.minimax.cn";
    private static final String PATH = "/v1/t2a_v2";

    /**
     * 支持的模型：接口文档 model 枚举
     */
    public static final Set<String> MODELS = SetUtils.asSet("speech-2.8-hd", "speech-2.8-turbo",
            "speech-2.6-hd", "speech-2.6-turbo", "speech-02-hd", "speech-02-turbo", "speech-01-hd", "speech-01-turbo");
    /**
     * 朗读文本的最大字符数：接口要求小于 10000 字符
     */
    private static final int MAX_TEXT_LENGTH = 9999;
    private static final List<String> FORMATS = ListUtil.of(AiSpeechUtils.FORMAT_MP3, AiSpeechUtils.FORMAT_WAV,
            AiSpeechUtils.FORMAT_FLAC);
    private static final BigDecimal MIN_SPEED = new BigDecimal("0.5");
    /**
     * 合法的十六进制音频：偶数长度，不含空白（HexUtil 会补位、去空白，所以先严格校验）
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{2})+$");
    private static final BigDecimal MAX_SPEED = new BigDecimal("2");

    private static final int STATUS_CODE_SUCCESS = 0;
    /**
     * data.status：2 表示合成结束
     */
    private static final int DATA_STATUS_FINISHED = 2;

    public static final String METADATA_TRACE_ID = "traceId";

    private final String apiKey;
    private final WebClient webClient;
    private final Duration timeout;
    private final int maxAudioSize;

    public MiniMaxSpeechModel(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, AiSpeechUtils.REQUEST_TIMEOUT, AiSpeechUtils.MAX_AUDIO_SIZE);
    }

    public MiniMaxSpeechModel(String baseUrl, String apiKey, String model, Duration timeout, int maxAudioSize) {
        super(model);
        if (!MODELS.contains(model)) {
            throw new IllegalArgumentException(StrUtil.format("MiniMax 语音合成不支持模型({})，可选：{}", model, MODELS));
        }
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("MiniMax 语音合成的 API Key 不能为空");
        }
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.maxAudioSize = maxAudioSize;
        // 十六进制音频是原始字节的 2 倍，另留 JSON 余量
        int maxResponseSize = (int) Math.min(Integer.MAX_VALUE, 2L * maxAudioSize + AiSpeechUtils.MAX_JSON_SIZE);
        this.webClient = AiSpeechUtils.buildWebClient(
                StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/"),
                maxResponseSize, headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(apiKey);
                });
    }

    @Override
    protected SpeechResult doCall(String text, String voice, String format, BigDecimal speed) {
        // 1. 请求
        Map<String, Object> voiceSetting = new LinkedHashMap<>();
        voiceSetting.put("voice_id", voice);
        voiceSetting.put("speed", speed);
        Map<String, Object> audioSetting = new LinkedHashMap<>();
        audioSetting.put("format", format);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("text", text);
        body.put("stream", false);
        body.put("output_format", "hex");
        body.put("voice_setting", voiceSetting);
        body.put("audio_setting", audioSetting);
        AiSpeechUtils.HttpResult result = AiSpeechUtils.exchange(webClient.post().uri(PATH)
                .bodyValue(JsonUtils.toJsonByte(body)), timeout, "MiniMax 语音合成");

        // 2. 解析错误
        JsonNode root = AiSpeechUtils.parseJson(result.getBody());
        String traceId = root != null ? StrUtil.emptyToNull(root.path("trace_id").asText(null)) : null;
        JsonNode baseResp = root != null ? root.path("base_resp") : null;
        if (!result.is2xx() || root == null || !baseResp.path("status_code").isIntegralNumber()
                || baseResp.path("status_code").asInt() != STATUS_CODE_SUCCESS) {
            String code = baseResp != null && !baseResp.path("status_code").isMissingNode()
                    ? baseResp.path("status_code").asText() : "-";
            String message = baseResp != null ? baseResp.path("status_msg").asText("") : "";
            if (root == null) {
                message = "返回格式不正确";
            }
            log.warn("[MiniMaxSpeech][合成失败，HTTP {}，status_code {}，traceId {}]", result.getStatus(), code, traceId);
            throw new IllegalStateException(StrUtil.format("MiniMax 语音合成失败（HTTP {}，status_code={}）：{}，trace_id={}",
                    result.getStatus(), code, AiSpeechUtils.sanitize(message, apiKey), StrUtil.blankToDefault(traceId, "-")));
        }

        // 3. 解析音频：必须合成结束，且为非空的合法十六进制
        JsonNode data = root.path("data");
        if (!data.path("status").isIntegralNumber() || data.path("status").asInt() != DATA_STATUS_FINISHED) {
            throw new IllegalStateException(StrUtil.format("MiniMax 语音合成未完成（data.status={}），trace_id={}",
                    data.path("status").asText("-"), StrUtil.blankToDefault(traceId, "-")));
        }
        byte[] audio = decodeHex(data.path("audio").asText(""), traceId);
        if (audio.length > maxAudioSize) {
            throw new IllegalStateException("MiniMax 语音合成失败：音频超过大小上限");
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put(METADATA_TRACE_ID, traceId);
        return new SpeechResult(audio, metadata);
    }

    private static byte[] decodeHex(String hex, String traceId) {
        if (StrUtil.isEmpty(hex)) {
            throw new IllegalStateException(StrUtil.format("MiniMax 语音合成未返回音频，trace_id={}",
                    StrUtil.blankToDefault(traceId, "-")));
        }
        if (!HEX_PATTERN.matcher(hex).matches()) {
            throw new IllegalStateException(StrUtil.format("MiniMax 语音合成返回的音频不是合法的十六进制，trace_id={}",
                    StrUtil.blankToDefault(traceId, "-")));
        }
        return HexUtil.decodeHex(hex);
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

}
