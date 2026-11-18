package cn.iocoder.yudao.module.ai.framework.ai.core.model.deepseek;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AbstractAiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DeepSeek 的语音合成模型：整段合成 HTTP
 *
 * 1. POST {baseUrl}/v1/tools/tts，Bearer API Key；请求体只有 text、voice_id、format，没有模型参数
 * 2. 成功时响应体就是完整音频（audio/wav）；失败时为 OpenAI 兼容的 {"error":{"message"}}
 * 3. 不支持调节语速；失败不自动重试
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/tts">DeepSeek 语音合成</a>
 * @author 芋道源码
 */
@Slf4j
public class DeepSeekSpeechModel extends AbstractAiSpeechModel {

    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String PATH = "/v1/tools/tts";

    /**
     * 音色：stella 海星（女声）、tide 暗潮（男声）
     */
    public static final Set<String> VOICES = SetUtils.asSet("stella", "tide");
    /**
     * 朗读文本的最大字符数：官方最多 10000 个计费字符，汉字算 2 个，这里按 5000 个字符限制
     */
    private static final int MAX_TEXT_LENGTH = 5000;
    private static final List<String> FORMATS = ListUtil.of(AiSpeechUtils.FORMAT_WAV);
    /**
     * 不支持调节语速，只能为 1
     */
    private static final BigDecimal SPEED = BigDecimal.ONE;

    private final String apiKey;
    private final WebClient webClient;
    private final Duration timeout;

    public DeepSeekSpeechModel(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, AiSpeechUtils.REQUEST_TIMEOUT, AiSpeechUtils.MAX_AUDIO_SIZE);
    }

    public DeepSeekSpeechModel(String baseUrl, String apiKey, String model, Duration timeout, int maxAudioSize) {
        super(model); // 模型只用于本地标识，不传给 DeepSeek
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("DeepSeek 语音合成的 API Key 不能为空");
        }
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.webClient = AiSpeechUtils.buildWebClient(
                StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/"),
                maxAudioSize, headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(apiKey);
                });
    }

    @Override
    protected SpeechResult doCall(String text, String voice, String format, BigDecimal speed) {
        // 1. 请求
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", text);
        body.put("voice_id", voice);
        body.put("format", format);
        AiSpeechUtils.HttpResult result = AiSpeechUtils.exchange(webClient.post().uri(PATH)
                .bodyValue(JsonUtils.toJsonByte(body)), timeout, "DeepSeek 语音合成");

        // 2. 解析错误
        if (!result.is2xx()) {
            JsonNode root = AiSpeechUtils.parseJson(result.getBody());
            String message = root != null ? root.path("error").path("message").asText("") : "";
            log.warn("[DeepSeekSpeech][合成失败，HTTP {}]", result.getStatus());
            throw new IllegalStateException(StrUtil.format("DeepSeek 语音合成失败（HTTP {}）：{}",
                    result.getStatus(), AiSpeechUtils.sanitize(message, apiKey)));
        }
        // 3. 成功：响应体即音频，由父类校验 WAV 文件头
        return new SpeechResult(result.getBody(), Collections.emptyMap());
    }

    @Override
    protected String validateVoice(String voice) {
        Assert.isTrue(VOICES.contains(voice), "DeepSeek 语音合成不支持音色({})，可选：{}", voice, VOICES);
        return voice;
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
        return AiSpeechUtils.FORMAT_WAV;
    }

    @Override
    public BigDecimal getMinSpeed() {
        return SPEED;
    }

    @Override
    public BigDecimal getMaxSpeed() {
        return SPEED;
    }

}
