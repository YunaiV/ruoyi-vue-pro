package cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tts.v20190823.TtsClient;
import com.tencentcloudapi.tts.v20190823.models.CreateTtsTaskRequest;
import com.tencentcloudapi.tts.v20190823.models.CreateTtsTaskResponse;
import com.tencentcloudapi.tts.v20190823.models.DescribeTtsTaskStatusRequest;
import com.tencentcloudapi.tts.v20190823.models.DescribeTtsTaskStatusResponse;
import com.tencentcloudapi.tts.v20190823.models.DescribeTtsTaskStatusRespData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 腾讯云语音合成 API：长文本语音合成（原生异步任务）
 *
 * 1. 使用官方 SDK（tencentcloud-sdk-java-tts，API 2019-08-23）的 CreateTtsTask 提交、DescribeTtsTaskStatus 查询，
 *    由 SDK 完成 TC3-HMAC-SHA256 签名；单次最多 10 万字符，服务端自行处理长文本，这里不手工切分、拼接音频
 * 2. 提交成功只表示任务已受理，不是音频已生成：返回任务编号，由调用方持久化后轮询
 * 3. 每次方法调用只发一次请求：提交不重试（关闭 OkHttp 的连接失败重试、不配置备用域名熔断），查询也只查一次；
 *    任务成功时有界下载结果地址（24 小时有效）为字节，不返回、不保存临时地址
 *    {@link #waitTask} 在时限内反复查询（只查询，免费），供业务以「提交后等待结果」的同步方式使用
 * 4. 凭证格式为 SecretId|SecretKey，由本类解析；只在内存中交给 SDK 签名，不出现在异常、日志中
 * 5. 语速：统一按倍数 0.6 ~ 2.5 输入（最多两位小数），按官方分段线性关系换算为 Speed 参数 [-2, 6]，保留两位小数
 *
 * @see <a href="https://cloud.tencent.com/document/api/1073/57373">长文本语音合成请求</a>
 * @see <a href="https://cloud.tencent.com/document/api/1073/57372">长文本语音合成结果查询</a>
 * @author 芋道源码
 */
@Slf4j
public class TencentTtsApi {

    public static final String DEFAULT_ENDPOINT = "tts.tencentcloudapi.com";

    /**
     * 朗读文本的最大字符数：接口上限 10 万字符（产品上限由业务层另行校验）
     */
    public static final int MAX_TEXT_LENGTH = 100000;
    public static final List<String> FORMATS = ListUtil.of(AiSpeechUtils.FORMAT_MP3, AiSpeechUtils.FORMAT_WAV);
    public static final BigDecimal MIN_SPEED = new BigDecimal("0.6");
    public static final BigDecimal MAX_SPEED = new BigDecimal("2.5");

    /**
     * 语速倍数到 Speed 参数的官方分段：{倍数下限, 倍数上限, 参数下限, 参数上限}，各段内线性
     *
     * 0.6 ~ 0.8 → -2 ~ -1；0.8 ~ 1.0 → -1 ~ 0；1.0 ~ 1.2 → 0 ~ 1；1.2 ~ 1.5 → 1 ~ 2；1.5 ~ 2.5 → 2 ~ 6
     */
    private static final BigDecimal[][] SPEED_SEGMENTS = {
            {new BigDecimal("0.6"), new BigDecimal("0.8"), new BigDecimal("-2"), new BigDecimal("-1")},
            {new BigDecimal("0.8"), new BigDecimal("1.0"), new BigDecimal("-1"), new BigDecimal("0")},
            {new BigDecimal("1.0"), new BigDecimal("1.2"), new BigDecimal("0"), new BigDecimal("1")},
            {new BigDecimal("1.2"), new BigDecimal("1.5"), new BigDecimal("1"), new BigDecimal("2")},
            {new BigDecimal("1.5"), new BigDecimal("2.5"), new BigDecimal("2"), new BigDecimal("6")},
    };

    /**
     * 音色：正整数
     */
    private static final Pattern VOICE_TYPE_PATTERN = Pattern.compile("[1-9][0-9]{0,17}");
    /**
     * 凭证分隔符
     */
    private static final String CREDENTIAL_SEPARATOR = "|";

    private final String secretId;
    private final String secretKey;
    private final TtsClient client;
    private final Duration timeout;
    private final int maxAudioSize;

    /**
     * @param apiKey 凭证，格式为 SecretId|SecretKey
     * @param url 接口地址，允许为空（默认 https://tts.tencentcloudapi.com）
     */
    public TencentTtsApi(String apiKey, String url) {
        this(apiKey, url, AiSpeechUtils.REQUEST_TIMEOUT, AiSpeechUtils.MAX_AUDIO_SIZE);
    }

    public TencentTtsApi(String apiKey, String url, Duration timeout, int maxAudioSize) {
        String[] credential = parseCredential(apiKey);
        this.secretId = credential[0];
        this.secretKey = credential[1];
        this.timeout = timeout;
        this.maxAudioSize = maxAudioSize;
        this.client = new TtsClient(new Credential(secretId, secretKey), "", buildClientProfile(url, timeout));
    }

    /**
     * 解析凭证：SecretId|SecretKey，两段都不能为空
     *
     * @return [SecretId, SecretKey]
     */
    static String[] parseCredential(String apiKey) {
        String[] parts = StrUtil.isBlank(apiKey) ? new String[0] : apiKey.split(Pattern.quote(CREDENTIAL_SEPARATOR), -1);
        if (parts.length != 2 || StrUtil.isBlank(parts[0]) || StrUtil.isBlank(parts[1])) {
            throw new IllegalArgumentException("腾讯云语音合成的密钥格式不正确，应为 SecretId|SecretKey");
        }
        return new String[]{parts[0].trim(), parts[1].trim()};
    }

    private static ClientProfile buildClientProfile(String url, Duration timeout) {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setReqMethod(HttpProfile.REQ_POST);
        if (StrUtil.isBlank(url)) {
            httpProfile.setProtocol(HttpProfile.REQ_HTTPS);
            httpProfile.setEndpoint(DEFAULT_ENDPOINT);
        } else {
            URI uri;
            try {
                uri = URI.create(url.trim());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("腾讯云语音合成的接口地址不正确");
            }
            String scheme = StrUtil.nullToEmpty(uri.getScheme()).toLowerCase();
            if (!("https".equals(scheme) || "http".equals(scheme)) || StrUtil.isEmpty(uri.getHost())
                    || !(StrUtil.isEmpty(uri.getPath()) || "/".equals(uri.getPath()))
                    || uri.getQuery() != null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("腾讯云语音合成的接口地址不正确，应为 https://tts.tencentcloudapi.com");
            }
            httpProfile.setProtocol(scheme + "://");
            httpProfile.setEndpoint(uri.getPort() > 0 ? uri.getHost() + ":" + uri.getPort() : uri.getHost());
        }
        // 自定义 OkHttpClient：关闭连接失败时的自动重试与重定向，避免付费提交被静默重发；不添加日志拦截器
        httpProfile.setHttpClient(new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .connectTimeout(Math.min(timeout.toMillis(), 10_000L), TimeUnit.MILLISECONDS)
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .callTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build());
        ClientProfile clientProfile = new ClientProfile(ClientProfile.SIGN_TC3_256, httpProfile);
        // 不开启 debug（会记录请求与响应），不设置 BackupEndpoint（会启用熔断切换域名）
        clientProfile.setDebug(false);
        return clientProfile;
    }

    /**
     * 校验合成参数：不发请求。业务可在保存记录、付费调用之前提前拒绝；{@link #createTask} 时同样校验
     *
     * @param text 朗读文本，最多 10 万字符
     * @param voice 音色 VoiceType（正整数）
     * @param format 音频格式：mp3、wav，为空时为 mp3
     * @param speed 语速倍数 0.6 ~ 2.5，最多两位小数，为空时为 1
     */
    public void validateOptions(String text, String voice, String format, Double speed) {
        AiSpeechUtils.validateText(text, MAX_TEXT_LENGTH);
        parseVoiceType(voice);
        AiSpeechUtils.validateFormat(format, AiSpeechUtils.FORMAT_MP3, FORMATS);
        AiSpeechUtils.validateSpeed(speed, MIN_SPEED, MAX_SPEED);
    }

    /**
     * 提交长文本语音合成任务：只请求一次，不重试
     *
     * 注意：网络异常或超时时，任务是否已在服务端创建是未知的，调用方不能自动重新提交
     *
     * @param text 朗读文本，最多 10 万字符
     * @param voice 音色 VoiceType（正整数）
     * @param format 音频格式：mp3、wav，为空时为 mp3
     * @param speed 语速倍数 0.6 ~ 2.5，最多两位小数，为空时为 1
     * @return 任务
     */
    public TtsTask createTask(String text, String voice, String format, Double speed) {
        // 1. 校验参数：全部在付费调用之前
        AiSpeechUtils.validateText(text, MAX_TEXT_LENGTH);
        long voiceType = parseVoiceType(voice);
        String codec = AiSpeechUtils.validateFormat(format, AiSpeechUtils.FORMAT_MP3, FORMATS);
        BigDecimal speedArg = toSpeedArg(AiSpeechUtils.validateSpeed(speed, MIN_SPEED, MAX_SPEED));

        // 2. 提交：音量、采样率、主语言、模型类型使用服务端默认值
        CreateTtsTaskRequest request = new CreateTtsTaskRequest();
        request.setText(text);
        request.setVoiceType(voiceType);
        request.setCodec(codec);
        request.setSpeed(speedArg.floatValue());
        CreateTtsTaskResponse response;
        try {
            response = client.CreateTtsTask(request);
        } catch (TencentCloudSDKException ex) {
            throw buildException("提交腾讯云语音合成任务", ex, true);
        }
        String taskId = response.getData() != null ? response.getData().getTaskId() : null;
        if (StrUtil.isBlank(taskId)) {
            throw new IllegalStateException(StrUtil.format("腾讯云语音合成任务提交后未返回任务编号，request_id={}",
                    StrUtil.blankToDefault(response.getRequestId(), "-")));
        }
        return new TtsTask(taskId, response.getRequestId());
    }

    /**
     * 查询任务一次；任务成功时，有界下载音频
     *
     * @param taskId 任务编号
     * @param format 提交时的音频格式，用于校验下载的音频
     * @return 任务结果
     */
    public TtsTaskResult getTask(String taskId, String format) {
        if (StrUtil.isBlank(taskId)) {
            throw new IllegalArgumentException("任务编号不能为空");
        }
        String codec = AiSpeechUtils.validateFormat(format, AiSpeechUtils.FORMAT_MP3, FORMATS);
        // 1. 查询
        DescribeTtsTaskStatusRequest request = new DescribeTtsTaskStatusRequest();
        request.setTaskId(taskId);
        DescribeTtsTaskStatusResponse response;
        try {
            response = client.DescribeTtsTaskStatus(request);
        } catch (TencentCloudSDKException ex) {
            throw buildException("查询腾讯云语音合成任务", ex, false);
        }
        String requestId = response.getRequestId();
        DescribeTtsTaskStatusRespData data = response.getData();
        if (data == null || data.getStatus() == null) {
            throw new IllegalStateException(StrUtil.format("腾讯云语音合成任务查询未返回状态，request_id={}",
                    StrUtil.blankToDefault(requestId, "-")));
        }
        if (StrUtil.isNotEmpty(data.getTaskId()) && !taskId.equals(data.getTaskId())) {
            throw new IllegalStateException(StrUtil.format("腾讯云语音合成任务查询返回的任务编号不一致，request_id={}",
                    StrUtil.blankToDefault(requestId, "-")));
        }
        TtsTaskStatus status = TtsTaskStatus.of(data.getStatus());
        if (status == null) {
            throw new IllegalStateException(StrUtil.format("腾讯云语音合成任务状态({})未知，request_id={}",
                    data.getStatus(), StrUtil.blankToDefault(requestId, "-")));
        }

        // 2. 按状态返回
        switch (status) {
            case SUCCESS:
                if (StrUtil.isBlank(data.getResultUrl())) {
                    throw new IllegalStateException(StrUtil.format("腾讯云语音合成任务成功但未返回音频地址，request_id={}",
                            StrUtil.blankToDefault(requestId, "-")));
                }
                byte[] audio = AiSpeechUtils.download(data.getResultUrl(), codec, timeout, maxAudioSize);
                return new TtsTaskResult(taskId, status, audio, null, requestId);
            case FAILED:
                return new TtsTaskResult(taskId, status, null,
                        StrUtil.emptyToNull(AiSpeechUtils.sanitize(data.getErrorMsg(), secretId, secretKey)), requestId);
            default:
                return new TtsTaskResult(taskId, status, null, null, requestId);
        }
    }

    /**
     * 在时限内轮询任务，直到结束：只查询，不重新提交
     *
     * 单次查询失败（网络抖动、下载失败等）记录后继续，直到时限；时限只约束是否发起下一次查询，进行中的一次查询受其自身超时约束
     *
     * @param taskId 任务编号
     * @param format 提交时的音频格式
     * @param timeout 等待时限
     * @param interval 查询间隔
     * @return 最后一次查询成功的结果：已结束（成功、失败），或时限到了仍为等待、处理中；一次都没查询成功时为 null
     */
    public TtsTaskResult waitTask(String taskId, String format, Duration timeout, Duration interval) {
        long deadline = System.nanoTime() + timeout.toNanos();
        TtsTaskResult result = null;
        while (true) {
            // 1. 查询一次：已结束的直接返回
            try {
                result = getTask(taskId, format);
                if (result.getStatus().isFinished()) {
                    return result;
                }
            } catch (IllegalStateException ex) {
                log.warn("[waitTask][task({}) 查询失败，继续等待：{}]", taskId, ex.getMessage());
            }
            // 2. 时限内，间隔后再查询
            long remainingMillis = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
            if (remainingMillis <= 0) {
                return result;
            }
            try {
                Thread.sleep(Math.min(interval.toMillis(), remainingMillis));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return result;
            }
        }
    }

    private IllegalStateException buildException(String action, TencentCloudSDKException ex, boolean billable) {
        String code = StrUtil.blankToDefault(ex.getErrorCode(), "-");
        String requestId = StrUtil.blankToDefault(ex.getRequestId(), "-");
        log.warn("[TencentTts][{}失败，code {}，requestId {}]", action, code, requestId);
        // 网络异常（含超时）：请求可能已到达服务端
        if (ex.getCause() instanceof IOException) {
            return new IllegalStateException(StrUtil.format("{}失败：网络异常（{}）{}", action,
                    ex.getCause().getClass().getSimpleName(), billable ? "，任务是否已创建未知，请勿自动重新提交" : ""));
        }
        return new IllegalStateException(StrUtil.format("{}失败（{}）：{}，request_id={}", action, code,
                AiSpeechUtils.sanitize(ex.getMessage(), secretId, secretKey), requestId));
    }

    /**
     * 解析音色 VoiceType：正整数
     */
    static long parseVoiceType(String voice) {
        AiSpeechUtils.validateVoice(voice);
        if (!VOICE_TYPE_PATTERN.matcher(voice).matches()) {
            throw new IllegalArgumentException(StrUtil.format("腾讯云语音合成的音色({})必须是正整数 VoiceType", voice));
        }
        return Long.parseLong(voice);
    }

    /**
     * 语速倍数换算为 Speed 参数：按官方分段线性换算，保留两位小数（四舍五入）
     *
     * @param speed 语速倍数，已校验在 0.6 ~ 2.5 且最多两位小数
     * @return Speed 参数，范围 [-2, 6]
     */
    static BigDecimal toSpeedArg(BigDecimal speed) {
        for (int i = 0; i < SPEED_SEGMENTS.length; i++) {
            BigDecimal[] segment = SPEED_SEGMENTS[i];
            boolean last = i == SPEED_SEGMENTS.length - 1;
            // 区间左闭右开，最后一段右闭
            if (speed.compareTo(segment[0]) >= 0
                    && (speed.compareTo(segment[1]) < 0 || last && speed.compareTo(segment[1]) == 0)) {
                BigDecimal ratio = speed.subtract(segment[0])
                        .divide(segment[1].subtract(segment[0]), 10, RoundingMode.HALF_UP);
                return segment[2].add(ratio.multiply(segment[3].subtract(segment[2])))
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }
        throw new IllegalArgumentException(StrUtil.format("语速({})超出范围 [{}, {}]", speed.toPlainString(),
                MIN_SPEED.toPlainString(), MAX_SPEED.toPlainString()));
    }

    /**
     * 任务状态：DescribeTtsTaskStatus 的 Status
     */
    public enum TtsTaskStatus {

        WAITING(0),
        RUNNING(1),
        SUCCESS(2),
        FAILED(3);

        private final long status;

        TtsTaskStatus(long status) {
            this.status = status;
        }

        public long getStatus() {
            return status;
        }

        public boolean isFinished() {
            return this == SUCCESS || this == FAILED;
        }

        public static TtsTaskStatus of(Long status) {
            for (TtsTaskStatus value : values()) {
                if (status != null && value.status == status) {
                    return value;
                }
            }
            return null;
        }

    }

    /**
     * 已受理的任务
     */
    @Getter
    @AllArgsConstructor
    public static class TtsTask {

        /**
         * 任务编号，可轮询
         */
        private final String taskId;
        /**
         * 提交请求的编号，仅用于排查
         */
        private final String requestId;

        @Override
        public String toString() {
            return "TtsTask{taskId=" + taskId + ", requestId=" + requestId + "}";
        }

    }

    /**
     * 任务查询结果
     */
    @Getter
    @AllArgsConstructor
    public static class TtsTaskResult {

        /**
         * 任务编号
         */
        private final String taskId;
        /**
         * 任务状态
         */
        private final TtsTaskStatus status;
        /**
         * 音频，仅 {@link TtsTaskStatus#SUCCESS} 时有值
         */
        private final byte[] audio;
        /**
         * 失败原因（已脱敏），仅 {@link TtsTaskStatus#FAILED} 且服务商返回时有值
         */
        private final String errorMessage;
        /**
         * 查询请求的编号，仅用于排查
         */
        private final String requestId;

        @Override
        public String toString() {
            return "TtsTaskResult{taskId=" + taskId + ", status=" + status + ", audioLength="
                    + (audio == null ? 0 : audio.length) + ", requestId=" + requestId + "}";
        }

    }

}
