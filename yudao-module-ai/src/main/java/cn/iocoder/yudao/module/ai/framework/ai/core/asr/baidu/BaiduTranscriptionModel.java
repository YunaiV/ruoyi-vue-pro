package cn.iocoder.yudao.module.ai.framework.ai.core.asr.baidu;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
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
import java.util.Map;

/**
 * 百度智能云的语音识别模型：短语音识别标准版，一次请求发送完整音频，返回完整文本
 *
 * 1. POST {baseUrl}/server_api，请求头 Authorization: Bearer API Key；不做 OAuth 换 token，也不同时传 token
 * 2. 音频为 WAV（16k 或 8k 采样率、单声道），以 Base64 放在 speech 字段，len 为原始字节数；时长不超过 60 秒
 * 3. 模型即识别模型 dev_pid：1537 普通话（默认）、1737 英语、1637 粤语、1837 四川话
 * 4. 失败时 HTTP 状态仍为 200，响应的 err_no 不为 0
 *
 * @see <a href="https://ai.baidu.com/ai-doc/SPEECH/Jlbxdezuf">百度短语音识别标准版</a>
 * @see <a href="https://cloud.baidu.com/doc/AI_REFERENCE/s/Hm5us339w">百度 API Key 鉴权</a>
 * @author 芋道源码
 */
@Slf4j
public class BaiduTranscriptionModel implements TranscriptionModel {

    public static final String DEFAULT_BASE_URL = "https://vop.baidu.com";
    public static final String DEFAULT_MODEL = "1537";
    private static final String PATH = "/server_api";
    /**
     * 用户唯一标识：用来区分用户，必填
     */
    private static final String CUID = "yudao-ai";

    /**
     * 原始音频的最大字节数：16k 单声道 16 位 WAV 的 60 秒约 1.9 MB，这里留出余量
     */
    public static final int MAX_AUDIO_SIZE = 4 * 1024 * 1024;
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final String apiKey;
    private final int devPid;
    private final WebClient webClient;
    private final Duration timeout;

    public BaiduTranscriptionModel(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, REQUEST_TIMEOUT);
    }

    public BaiduTranscriptionModel(String baseUrl, String apiKey, String model, Duration timeout) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("百度语音识别的 API Key 不能为空");
        }
        String devPid = StrUtil.blankToDefault(StrUtil.trim(model), DEFAULT_MODEL);
        if (!NumberUtil.isInteger(devPid)) {
            throw new IllegalArgumentException("百度语音识别的模型必须是 dev_pid，例如 1537");
        }
        this.apiKey = apiKey;
        this.devPid = Integer.parseInt(devPid);
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
        int rate = getWavSampleRate(audio);
        Assert.isTrue(rate == 16000 || rate == 8000, "音频采样率({})必须是 16000 或 8000", rate);

        // 2. 请求
        Map<String, Object> body = MapUtil.<String, Object>builder()
                .put("format", "wav")
                .put("rate", rate)
                .put("channel", 1)
                .put("cuid", CUID)
                .put("dev_pid", devPid)
                .put("speech", Base64.encode(audio))
                .put("len", audio.length)
                .build();
        AiSpeechUtils.HttpResult result = AiSpeechUtils.exchange(webClient.post().uri(PATH).bodyValue(body),
                timeout, "百度语音识别");

        // 3. 解析：err_no 为 0 时，result 为识别结果
        JsonNode root = AiSpeechUtils.parseJson(result.getBody());
        JsonNode text = root != null ? root.path("result").path(0) : null;
        if (!result.is2xx() || root == null || root.path("err_no").asInt(-1) != 0 || text == null || !text.isTextual()) {
            String message = root != null ? StrUtil.format("{} {}",
                    root.has("err_no") ? root.path("err_no").asText() : root.path("error_code").asText(""),
                    root.has("err_msg") ? root.path("err_msg").asText() : root.path("error_msg").asText("")).trim()
                    : "返回格式不正确";
            log.warn("[BaiduTranscription][识别失败，HTTP {}]", result.getStatus());
            throw new IllegalStateException(StrUtil.format("百度语音识别失败（HTTP {}）：{}",
                    result.getStatus(), AiSpeechUtils.sanitize(message, apiKey)));
        }
        return new AudioTranscriptionResponse(new AudioTranscription(text.asText()));
    }

    /**
     * 读取 WAV 文件头的采样率：第 24 ~ 27 字节，小端
     */
    static int getWavSampleRate(byte[] wav) {
        Assert.isTrue(wav.length >= 44, "WAV 文件头不完整");
        return (wav[24] & 0xFF) | (wav[25] & 0xFF) << 8 | (wav[26] & 0xFF) << 16 | (wav[27] & 0xFF) << 24;
    }

}
