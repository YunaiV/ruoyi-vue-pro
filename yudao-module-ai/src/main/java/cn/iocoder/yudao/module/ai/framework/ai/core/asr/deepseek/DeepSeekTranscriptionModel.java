package cn.iocoder.yudao.module.ai.framework.ai.core.asr.deepseek;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.AiAsrUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * DeepSeek 的语音识别模型：一次请求发送完整音频，返回完整转写文本
 *
 * 1. POST {baseUrl}/v1/tools/asr，Bearer API Key；multipart/form-data 只有 file 字段，没有模型参数
 * 2. 音频必须是 16 位 PCM WAV 或 Ogg Opus，最大 8 MiB，最长 120 秒
 * 3. 失败不自动重试
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/asr">DeepSeek 语音识别</a>
 * @author 芋道源码
 */
@Slf4j
public class DeepSeekTranscriptionModel implements TranscriptionModel {

    public static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String PATH = "/v1/tools/asr";

    /**
     * 音频最大字节数：8 MiB
     */
    public static final int MAX_AUDIO_SIZE = 8 * 1024 * 1024;
    /**
     * 请求超时：需小于前端的 120 秒请求超时
     */
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(100);

    private final String apiKey;
    private final WebClient webClient;
    private final Duration timeout;

    public DeepSeekTranscriptionModel(String baseUrl, String apiKey) {
        this(baseUrl, apiKey, REQUEST_TIMEOUT);
    }

    public DeepSeekTranscriptionModel(String baseUrl, String apiKey, Duration timeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("DeepSeek 语音识别的 API Key 不能为空");
        }
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.webClient = AiSpeechUtils.buildWebClient(
                StrUtil.removeSuffix(StrUtil.blankToDefault(baseUrl, DEFAULT_BASE_URL), "/"),
                AiSpeechUtils.MAX_JSON_SIZE, headers -> headers.setBearerAuth(apiKey));
    }

    @Override
    public AudioTranscriptionResponse call(AudioTranscriptionPrompt prompt) {
        // 1. 校验音频：全部在付费调用之前
        Assert.notNull(prompt, "语音识别请求不能为空");
        byte[] audio = AiAsrUtils.readAudio(prompt.getInstructions());
        final String filename;
        if (AiSpeechUtils.isAudioFormat(audio, AiSpeechUtils.FORMAT_WAV)) {
            filename = "speech.wav";
        } else if (AiSpeechUtils.isAudioFormat(audio, AiSpeechUtils.FORMAT_OGG_OPUS)) {
            filename = "speech.ogg";
        } else {
            throw new IllegalArgumentException("音频必须是 16 位 PCM WAV 或 Ogg Opus 格式");
        }
        Assert.isTrue(audio.length <= MAX_AUDIO_SIZE, "音频大小({} 字节)超过上限({} 字节)", audio.length, MAX_AUDIO_SIZE);

        // 2. 请求：file 必须带文件名
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(audio) {

            @Override
            public String getFilename() {
                return filename;
            }

        });
        AiSpeechUtils.HttpResult result = AiSpeechUtils.exchange(webClient.post().uri(PATH)
                .body(BodyInserters.fromMultipartData(builder.build())), timeout, "DeepSeek 语音识别");

        // 3. 解析
        JsonNode root = AiSpeechUtils.parseJson(result.getBody());
        if (!result.is2xx() || root == null || !root.path("text").isTextual()) {
            String message = root != null ? root.path("error").path("message").asText("") : "返回格式不正确";
            log.warn("[DeepSeekTranscription][识别失败，HTTP {}]", result.getStatus());
            throw new IllegalStateException(StrUtil.format("DeepSeek 语音识别失败（HTTP {}）：{}",
                    result.getStatus(), AiSpeechUtils.sanitize(message, apiKey)));
        }
        return new AudioTranscriptionResponse(new AudioTranscription(root.get("text").asText()));
    }

}
