package cn.iocoder.yudao.module.ai.framework.ai.core.asr.tencent;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.asr.AiAsrUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import tools.jackson.databind.JsonNode;
import com.tencentcloudapi.common.CommonClient;
import com.tencentcloudapi.common.CommonRequest;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 腾讯云的语音识别模型：一句话识别 SentenceRecognition，一次请求发送完整音频，返回完整文本
 *
 * 1. 使用官方 SDK 的 CommonClient（asr，API 2019-06-14）调用，由 SDK 完成 TC3-HMAC-SHA256 签名，不新增依赖
 * 2. 音频为 WAV，以 Base64 放在 Data 字段，DataLen 为原始字节数；时长不超过 60 秒，Base64 编码后不超过 3 MB
 * 3. 模型即引擎类型 EngSerViceType：前端统一转为 16k 采样率，所以只能使用 16k 引擎，例如 16k_zh（默认）、16k_en、16k_zh_dialect
 * 4. 凭证格式为 SecretId|SecretKey；关闭 OkHttp 的连接失败重试，失败不自动重试
 *
 * @see <a href="https://cloud.tencent.com/document/api/1093/35646">一句话识别</a>
 * @author 芋道源码
 */
@Slf4j
public class TencentTranscriptionModel implements TranscriptionModel {

    public static final String DEFAULT_ENDPOINT = "asr.tencentcloudapi.com";
    public static final String DEFAULT_MODEL = "16k_zh";
    private static final String PRODUCT = "asr";
    private static final String VERSION = "2019-06-14";
    private static final String ACTION = "SentenceRecognition";

    /**
     * 音频 Base64 编码后的最大字节数：3 MB
     */
    public static final int MAX_BASE64_SIZE = 3 * 1024 * 1024;
    /**
     * 原始音频的最大字节数：由 Base64 上限换算
     */
    public static final int MAX_AUDIO_SIZE = MAX_BASE64_SIZE / 4 * 3;
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final String secretId;
    private final String secretKey;
    private final String engineType;
    private final CommonClient client;

    public TencentTranscriptionModel(String url, String apiKey, String model) {
        this(url, apiKey, model, REQUEST_TIMEOUT);
    }

    public TencentTranscriptionModel(String url, String apiKey, String model, Duration timeout) {
        // 凭证：SecretId|SecretKey
        String[] parts = StrUtil.isBlank(apiKey) ? new String[0] : apiKey.split(Pattern.quote("|"), -1);
        if (parts.length != 2 || StrUtil.isBlank(parts[0]) || StrUtil.isBlank(parts[1])) {
            throw new IllegalArgumentException("腾讯云语音识别的密钥格式不正确，应为 SecretId|SecretKey");
        }
        this.secretId = parts[0].trim();
        this.secretKey = parts[1].trim();
        // 引擎：前端统一转为 16k 采样率
        this.engineType = StrUtil.blankToDefault(StrUtil.trim(model), DEFAULT_MODEL);
        if (!engineType.startsWith("16k_")) {
            throw new IllegalArgumentException("腾讯云语音识别的模型必须是 16k 引擎，例如 16k_zh");
        }
        this.client = new CommonClient(PRODUCT, VERSION, new Credential(secretId, secretKey), "",
                buildClientProfile(url, timeout));
    }

    private static ClientProfile buildClientProfile(String url, Duration timeout) {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setReqMethod(HttpProfile.REQ_POST);
        if (StrUtil.isBlank(url)) {
            httpProfile.setProtocol(HttpProfile.REQ_HTTPS);
            httpProfile.setEndpoint(DEFAULT_ENDPOINT);
        } else {
            URI uri = URI.create(url.trim());
            String scheme = StrUtil.nullToEmpty(uri.getScheme()).toLowerCase();
            if (!("https".equals(scheme) || "http".equals(scheme)) || StrUtil.isEmpty(uri.getHost())) {
                throw new IllegalArgumentException("腾讯云语音识别的接口地址不正确，应为 https://asr.tencentcloudapi.com");
            }
            httpProfile.setProtocol(scheme + "://");
            httpProfile.setEndpoint(uri.getPort() > 0 ? uri.getHost() + ":" + uri.getPort() : uri.getHost());
        }
        httpProfile.setHttpClient(new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .connectTimeout(Math.min(timeout.toMillis(), 10_000L), TimeUnit.MILLISECONDS)
                .callTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build());
        return new ClientProfile(ClientProfile.SIGN_TC3_256, httpProfile);
    }

    @Override
    public AudioTranscriptionResponse call(AudioTranscriptionPrompt prompt) {
        // 1. 校验音频：全部在付费调用之前
        Assert.notNull(prompt, "语音识别请求不能为空");
        byte[] audio = AiAsrUtils.validateWavAudio(AiAsrUtils.readAudio(prompt.getInstructions()), MAX_AUDIO_SIZE);

        // 2. 请求：用 set 设置参数，避免 JSON 解析把整数变成浮点数
        CommonRequest request = new CommonRequest("{}");
        request.set("EngSerViceType", engineType);
        request.set("SourceType", 1);
        request.set("VoiceFormat", "wav");
        request.set("Data", Base64.encode(audio));
        request.set("DataLen", audio.length);
        String response;
        try {
            response = client.commonRequest(request, ACTION);
        } catch (TencentCloudSDKException ex) {
            throw buildException(ex);
        }

        // 3. 解析：Response.Result
        JsonNode root = AiSpeechUtils.parseJson(StrUtil.bytes(response, StandardCharsets.UTF_8));
        JsonNode result = root != null ? root.path("Response").path("Result") : null;
        if (result == null || !result.isTextual()) {
            throw new IllegalStateException(StrUtil.format("腾讯云语音识别失败：返回格式不正确，request_id={}",
                    root != null ? root.path("Response").path("RequestId").asText("-") : "-"));
        }
        return new AudioTranscriptionResponse(new AudioTranscription(result.asText()));
    }

    private IllegalStateException buildException(TencentCloudSDKException ex) {
        String code = StrUtil.blankToDefault(ex.getErrorCode(), "-");
        String requestId = StrUtil.blankToDefault(ex.getRequestId(), "-");
        log.warn("[TencentTranscription][识别失败，code {}，requestId {}]", code, requestId);
        if (ex.getCause() instanceof IOException) {
            return new IllegalStateException(StrUtil.format("腾讯云语音识别失败：网络异常（{}）",
                    ex.getCause().getClass().getSimpleName()));
        }
        return new IllegalStateException(StrUtil.format("腾讯云语音识别失败（{}）：{}，request_id={}", code,
                AiSpeechUtils.sanitize(ex.getMessage(), secretId, secretKey), requestId));
    }

}
