package cn.iocoder.yudao.module.ai.framework.ai.core.model.speech;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * AI 语音合成的工具类
 *
 * 各平台共用的参数校验、音频校验、有界下载、错误信息脱敏。不读写文件、数据库、登录用户与租户上下文
 *
 * @author 芋道源码
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AiSpeechUtils {

    /**
     * 单次 HTTP 请求的超时时间（提交、查询、下载各自独立计时；不用于腾讯云异步任务的整体时长）
     */
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(180);
    /**
     * 音频的最大字节数
     */
    public static final int MAX_AUDIO_SIZE = 100 * 1024 * 1024;
    /**
     * JSON 接口响应（不含音频）的最大字节数
     */
    public static final int MAX_JSON_SIZE = 1024 * 1024;
    /**
     * 错误信息的最大长度
     */
    private static final int MAX_ERROR_LENGTH = 200;
    /**
     * 语速的最大小数位数
     */
    private static final int SPEED_MAX_SCALE = 2;

    public static final String FORMAT_MP3 = "mp3";
    public static final String FORMAT_WAV = "wav";
    public static final String FORMAT_FLAC = "flac";
    public static final String FORMAT_OGG_OPUS = "ogg_opus";

    /**
     * 地址中的查询参数：服务商返回的音频地址带临时签名，写入错误信息前打码
     */
    private static final Pattern URL_QUERY_PATTERN = Pattern.compile("(https?://[^\\s?#\"'<>]+)\\?[^\\s\"'<>]*");
    /**
     * Bearer 凭证
     */
    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)bearer\\s+[^\\s\"',;]+");
    /**
     * 控制字符
     */
    private static final Pattern CONTROL_PATTERN = Pattern.compile("[\\p{Cntrl}]+");

    // ========== 参数校验 ==========

    /**
     * 校验朗读文本：不能为空，字符数（按 Unicode 码点计）不超过上限
     */
    public static String validateText(String text, int maxLength) {
        Assert.notBlank(text, "朗读文本不能为空");
        int length = text.codePointCount(0, text.length());
        Assert.isTrue(length <= maxLength, "朗读文本长度({})超过上限({})", length, maxLength);
        return text;
    }

    /**
     * 校验音色：不能为空
     */
    public static String validateVoice(String voice) {
        return Assert.notBlank(voice, "音色不能为空");
    }

    /**
     * 校验请求里的模型：模型在创建客户端时绑定，请求只能不传或传相同的值，不能切换
     */
    public static void validateModel(String boundModel, String requestModel) {
        Assert.isTrue(requestModel == null || requestModel.equals(boundModel),
                "请求模型({})与已配置的模型({})不一致", requestModel, boundModel);
    }

    /**
     * 校验音频格式：不传时使用默认格式，不支持的格式直接拒绝
     */
    public static String validateFormat(String format, String defaultFormat, Collection<String> supportedFormats) {
        if (format == null) {
            return defaultFormat;
        }
        Assert.isTrue(supportedFormats.contains(format), "不支持的音频格式({})，可选：{}", format, supportedFormats);
        return format;
    }

    /**
     * 校验语速倍数：不传时为 1；最多两位小数，超出范围直接拒绝，不静默截断
     *
     * @return 精确的十进制语速
     */
    public static BigDecimal validateSpeed(Double speed, BigDecimal min, BigDecimal max) {
        if (speed == null) {
            return BigDecimal.ONE;
        }
        Assert.isTrue(Double.isFinite(speed), "语速不是有效数字");
        // Double.toString 的最短表示：1.2 → 1.2，1.2000000000000002 会保留全部位数并被拒绝
        BigDecimal value = BigDecimal.valueOf(speed).stripTrailingZeros();
        Assert.isTrue(value.scale() <= SPEED_MAX_SCALE, "语速({})最多保留 {} 位小数", speed, SPEED_MAX_SCALE);
        Assert.isTrue(min.compareTo(max) != 0 || value.compareTo(min) == 0,
                "该模型不支持调节语速，只能为 {}", min.toPlainString());
        Assert.isTrue(value.compareTo(min) >= 0 && value.compareTo(max) <= 0, "语速({})超出范围 [{}, {}]",
                value.toPlainString(), min.toPlainString(), max.toPlainString());
        return value.scale() < 0 ? value.setScale(0) : value;
    }

    /**
     * 获得音频格式对应的文件扩展名：ogg_opus 为 opus
     */
    public static String getFileExtension(String format) {
        return FORMAT_OGG_OPUS.equals(format) ? "opus" : format;
    }

    // ========== 音频校验 ==========

    /**
     * 校验音频：不能为空，且文件头与格式一致
     */
    public static byte[] validateAudio(byte[] audio, String format) {
        if (ArrayUtil.isEmpty(audio)) {
            throw new IllegalStateException("服务商未返回音频");
        }
        if (!isAudioFormat(audio, format)) {
            throw new IllegalStateException(StrUtil.format("服务商返回的音频不是 {} 格式", format));
        }
        return audio;
    }

    /**
     * 按文件头判断音频格式
     */
    public static boolean isAudioFormat(byte[] audio, String format) {
        if (audio == null || format == null) {
            return false;
        }
        switch (format) {
            case FORMAT_MP3:
                // ID3 标签，或 MPEG 帧同步（11 位 1）
                return startsWith(audio, 0, "ID3")
                        || (audio.length >= 2 && (audio[0] & 0xFF) == 0xFF && (audio[1] & 0xE0) == 0xE0);
            case FORMAT_WAV:
                return audio.length >= 12 && startsWith(audio, 0, "RIFF") && startsWith(audio, 8, "WAVE");
            case FORMAT_FLAC:
                return startsWith(audio, 0, "fLaC");
            case FORMAT_OGG_OPUS:
                return startsWith(audio, 0, "OggS");
            default:
                return false;
        }
    }

    private static boolean startsWith(byte[] data, int offset, String magic) {
        byte[] bytes = magic.getBytes(StandardCharsets.US_ASCII);
        if (data.length < offset + bytes.length) {
            return false;
        }
        for (int i = 0; i < bytes.length; i++) {
            if (data[offset + i] != bytes[i]) {
                return false;
            }
        }
        return true;
    }

    // ========== HTTP ==========

    /**
     * HTTP 响应：状态、响应头、有界读取的响应体
     */
    @Getter
    @AllArgsConstructor
    public static class HttpResult {

        private final int status;
        private final HttpHeaders headers;
        private final byte[] body;

        public boolean is2xx() {
            return status >= 200 && status < 300;
        }

        @Override
        public String toString() {
            // 不输出响应体：可能含音频
            return "HttpResult{status=" + status + ", bodyLength=" + (body == null ? 0 : body.length) + "}";
        }

    }

    /**
     * 创建底层 HTTP 客户端：关闭 Reactor Netty 默认的"TCP 中断后重发一次"，不跟随重定向
     *
     * 付费请求只能发一次：网络异常时服务端是否已处理是未知的，由调用方决定，而不是传输层自动重发
     */
    public static HttpClient buildHttpClient() {
        return HttpClient.create()
                .disableRetry(true)
                .followRedirect(false);
    }

    /**
     * 创建 WebClient：响应体超过上限时报错，不会无界缓存；传输层不自动重试、不跟随重定向
     *
     * @param baseUrl 根地址，允许为空
     * @param maxInMemorySize 响应体的最大字节数
     * @param defaultHeaders 默认请求头，允许为空
     */
    public static WebClient buildWebClient(String baseUrl, int maxInMemorySize, Consumer<HttpHeaders> defaultHeaders) {
        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(buildHttpClient()))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize));
        if (StrUtil.isNotEmpty(baseUrl)) {
            builder.baseUrl(baseUrl);
        }
        if (defaultHeaders != null) {
            builder.defaultHeaders(defaultHeaders);
        }
        return builder.build();
    }

    /**
     * 发送请求，并读取状态、响应头与有界响应体；非 2xx 不抛异常，由各平台按自己的错误格式解析
     */
    public static HttpResult exchange(WebClient.RequestHeadersSpec<?> request, Duration timeout, String action) {
        Mono<HttpResult> mono = request.exchangeToMono(response -> readResult(response));
        return block(mono, timeout, action);
    }

    private static Mono<HttpResult> readResult(ClientResponse response) {
        HttpStatusCode status = response.statusCode();
        HttpHeaders headers = response.headers().asHttpHeaders();
        return response.bodyToMono(byte[].class).defaultIfEmpty(new byte[0])
                .map(body -> new HttpResult(status.value(), headers, body));
    }

    /**
     * 等待结果，把超时、超限、网络异常转换为不含地址参数与凭证的异常
     *
     * 注意：已发出的付费请求超时或网络中断时，服务端是否已经处理是未知的，调用方不能自动重试
     */
    public static <T> T block(Mono<T> mono, Duration timeout, String action) {
        try {
            return mono.timeout(timeout).block();
        } catch (RuntimeException ex) {
            Throwable cause = Exceptions.unwrap(ex);
            if (cause instanceof TimeoutException) {
                throw new IllegalStateException(StrUtil.format("{}超时（{} 秒），结果未知，请勿自动重试",
                        action, timeout.getSeconds()));
            }
            if (cause instanceof DataBufferLimitException) {
                throw new IllegalStateException(StrUtil.format("{}失败：响应超过大小上限", action));
            }
            if (cause instanceof WebClientRequestException) {
                throw new IllegalStateException(StrUtil.format("{}失败：网络异常（{}），结果未知，请勿自动重试",
                        action, cause.getCause() != null ? cause.getCause().getClass().getSimpleName()
                                : cause.getClass().getSimpleName()));
            }
            if (cause instanceof IllegalStateException || cause instanceof IllegalArgumentException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException(StrUtil.format("{}失败：{}", action, sanitize(cause.getMessage())));
        }
    }

    /**
     * 有界下载服务商返回的临时音频地址：不携带任何鉴权头，不跟随重定向，超时与大小受限，并校验音频格式
     *
     * @param url 临时音频地址，只允许 http、https
     * @param format 期望的音频格式
     */
    public static byte[] download(String url, String format, Duration timeout, int maxSize) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("服务商返回的音频地址不正确");
        }
        String scheme = uri.getScheme();
        if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme) || StrUtil.isEmpty(uri.getHost())) {
            throw new IllegalStateException("服务商返回的音频地址不正确");
        }
        WebClient client = buildWebClient(null, maxSize, null);
        HttpResult result = exchange(client.get().uri(uri), timeout, "下载音频");
        if (!result.is2xx()) {
            throw new IllegalStateException(StrUtil.format("下载音频失败（HTTP {}）", result.getStatus()));
        }
        return validateAudio(result.getBody(), format);
    }

    // ========== JSON ==========

    /**
     * 解析服务商的 JSON 响应：按字节解析；失败返回 null，不记录原始内容（可能含音频）
     *
     * 注意：{@link JsonUtils#parseTree(byte[])} 失败时会把原始内容写入错误日志，所以这里不使用
     */
    public static JsonNode parseJson(byte[] body) {
        if (ArrayUtil.isEmpty(body)) {
            return null;
        }
        try {
            return JsonUtils.getObjectMapper().readTree(body);
        } catch (IOException ex) {
            return null;
        }
    }

    // ========== 脱敏 ==========

    /**
     * 脱敏服务商返回的错误信息：去掉凭证、地址参数、Bearer 值、控制字符，并限制长度
     *
     * @param text 错误信息
     * @param secrets 需要打码的凭证
     */
    public static String sanitize(String text, String... secrets) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        String result = text;
        if (secrets != null) {
            for (String secret : secrets) {
                if (StrUtil.isNotBlank(secret)) {
                    result = result.replace(secret, "***");
                }
            }
        }
        result = URL_QUERY_PATTERN.matcher(result).replaceAll("$1?***");
        result = BEARER_PATTERN.matcher(result).replaceAll("Bearer ***");
        result = CONTROL_PATTERN.matcher(result).replaceAll(" ").trim();
        return StrUtil.maxLength(result, MAX_ERROR_LENGTH);
    }

}
