package cn.iocoder.yudao.module.ai.framework.ai.core.asr.tongyi;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.AiAsrUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * 阿里云百炼（通义）的语音识别模型：录音文件识别 Qwen3-ASR-Flash，一次请求发送完整音频，返回完整文本
 *
 * 1. POST {baseUrl}/api/v1/services/aigc/multimodal-generation/generation，Bearer API Key（百炼的 API Key）
 * 2. 音频以 Base64 Data URL 传入；Base64 编码后不超过 10 MB，时长不超过 5 分钟
 * 3. 失败不自动重试
 *
 * @see <a href="https://help.aliyun.com/zh/model-studio/qwen-speech-recognition">通义千问 ASR 语音识别</a>
 * @author 芋道源码
 */
@Slf4j
public class TongYiTranscriptionModel implements TranscriptionModel {

    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com";
    public static final String DEFAULT_MODEL = "qwen3-asr-flash";
    private static final String PATH = "/api/v1/services/aigc/multimodal-generation/generation";

    /**
     * 音频 Base64 编码后的最大字节数：10 MB
     */
    public static final int MAX_BASE64_SIZE = 10 * 1024 * 1024;
    /**
     * 原始音频的最大字节数：由 Base64 上限换算
     */
    public static final int MAX_AUDIO_SIZE = MAX_BASE64_SIZE / 4 * 3;
    /**
     * 请求超时：需小于前端的 120 秒请求超时
     */
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(100);

    private final String apiKey;
    private final String model;
    private final WebClient webClient;
    private final Duration timeout;

    public TongYiTranscriptionModel(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, REQUEST_TIMEOUT);
    }

    public TongYiTranscriptionModel(String baseUrl, String apiKey, String model, Duration timeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("阿里云语音识别的 API Key 不能为空");
        }
        this.apiKey = apiKey;
        this.model = StrUtil.blankToDefault(model, DEFAULT_MODEL);
        this.timeout = timeout;
        this.webClient = AiSpeechUtils.buildWebClient(
                StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/"),
                AiSpeechUtils.MAX_JSON_SIZE, headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(apiKey);
                });
    }

    @Override
    public AudioTranscriptionResponse call(AudioTranscriptionPrompt prompt) {
        // 1. 校验音频：全部在付费调用之前
        Assert.notNull(prompt, "语音识别请求不能为空");
        byte[] audio = AiAsrUtils.validateWavAudio(AiAsrUtils.readAudio(prompt.getInstructions()), MAX_AUDIO_SIZE);

        // 2. 请求
        Map<String, Object> message = MapUtil.<String, Object>builder()
                .put("role", "user")
                .put("content", Collections.singletonList(
                        MapUtil.of("audio", "data:audio/wav;base64," + Base64.encode(audio))))
                .build();
        Map<String, Object> body = MapUtil.<String, Object>builder()
                .put("model", model)
                .put("input", MapUtil.of("messages", Collections.singletonList(message)))
                .build();
        AiSpeechUtils.HttpResult result = AiSpeechUtils.exchange(webClient.post().uri(PATH).bodyValue(body),
                timeout, "阿里云语音识别");

        // 3. 解析：output.choices[0].message.content[0].text
        JsonNode root = AiSpeechUtils.parseJson(result.getBody());
        JsonNode text = root != null ? root.path("output").path("choices").path(0)
                .path("message").path("content").path(0).path("text") : null;
        if (!result.is2xx() || text == null || !text.isTextual()) {
            String message0 = root != null ? StrUtil.format("{} {}", root.path("code").asText(""),
                    root.path("message").asText("")).trim() : "返回格式不正确";
            log.warn("[TongYiTranscription][识别失败，HTTP {}]", result.getStatus());
            throw new IllegalStateException(StrUtil.format("阿里云语音识别失败（HTTP {}）：{}",
                    result.getStatus(), AiSpeechUtils.sanitize(message0, apiKey)));
        }
        return new AudioTranscriptionResponse(new AudioTranscription(text.asText()));
    }

}
