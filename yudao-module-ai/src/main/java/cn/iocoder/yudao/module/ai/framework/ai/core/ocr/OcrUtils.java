package cn.iocoder.yudao.module.ai.framework.ai.core.ocr;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ai.content.Media;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.MimeType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.InterruptedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.getText;

/**
 * OCR 调用的通用辅助方法：图片校验、文本整理、错误脱敏
 *
 * 注意：密钥的解析由各 {@link OcrModel} 自己负责（平台决定格式），不放在这里。
 *
 * @author 芋道源码
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OcrUtils {

    /**
     * 写进异常消息的厂商错误信息的最大长度
     */
    private static final int MAX_MESSAGE_LENGTH = 300;

    private static final String REDACTED = "******";

    // ========== 图片 ==========

    /**
     * 读取并校验图片：非空、MIME 类型在厂商支持范围内
     *
     * @param platform         平台（用于异常消息）
     * @param prompt           请求
     * @param allowedMimeTypes 允许的 MIME 类型，小写，例如 image/png
     * @return 图片字节
     */
    public static byte[] readImage(String platform, OcrPrompt prompt, Set<String> allowedMimeTypes) {
        // 1. 校验 MIME 类型
        Media media = prompt.getInstructions();
        String mimeType = normalizeMimeType(media.getMimeType());
        if (mimeType == null || !allowedMimeTypes.contains(mimeType)) {
            throw new OcrException(platform, OcrException.Type.INVALID_IMAGE,
                    "不支持的图片类型：" + mimeType + "，支持：" + String.join("、", allowedMimeTypes));
        }
        // 2. 只接受服务端读出的图片字节，不把 URL 透传给厂商
        if (!(media.getData() instanceof byte[] bytes)) {
            throw new OcrException(platform, OcrException.Type.INVALID_IMAGE, "图片必须以字节形式提供");
        }
        if (bytes.length == 0) {
            throw new OcrException(platform, OcrException.Type.INVALID_IMAGE, "图片内容为空");
        }
        return bytes;
    }

    /**
     * 统一 MIME 类型的写法：去掉参数，image/jpg 视为 image/jpeg（Spring 的 {@link MimeType} 已转为小写）
     */
    public static String normalizeMimeType(MimeType mimeType) {
        if (mimeType == null) {
            return null;
        }
        String value = mimeType.getType() + "/" + mimeType.getSubtype();
        return "image/jpg".equals(value) ? "image/jpeg" : value;
    }

    /**
     * 计算 base64 字符串再做 urlencode 后的长度：只有 + / = 三个字符会被编码成 %XX，各多出 2 个字符
     *
     * 不直接 URLUtil.encodeAll 再取长度：图片可能接近 10MB，没必要为了算长度再复制一份
     */
    public static long urlEncodedLength(String base64) {
        return base64.length() + 2L * (StrUtil.count(base64, '+') + StrUtil.count(base64, '/') + StrUtil.count(base64, '='));
    }

    /**
     * 校验大小，超过时抛出 {@link OcrException.Type#INVALID_IMAGE}
     *
     * @param description 被校验的对象，例如“base64 编码后的图片”
     */
    public static void checkSize(String platform, long actual, long max, String description) {
        if (actual > max) {
            throw new OcrException(platform, OcrException.Type.INVALID_IMAGE,
                    StrUtil.format("{}大小 {} 字节，超过上限 {} 字节", description, actual, max));
        }
    }

    // ========== 文本 ==========

    /**
     * 按行拼接识别结果：去掉 null，每行统一换行并去首尾空白
     */
    public static String joinLines(List<String> lines) {
        return CollUtil.emptyIfNull(lines).stream().filter(Objects::nonNull)
                .map(OcrUtils::normalizeText)
                .collect(Collectors.joining("\n")).strip();
    }

    /**
     * 统一换行符（\r\n、\r 转为 \n）并去掉首尾空白；null 视为空字符串
     */
    public static String normalizeText(String text) {
        return StrUtil.nullToEmpty(text).replace("\r\n", "\n").replace('\r', '\n').strip();
    }

    // ========== 错误 ==========

    /**
     * 整理厂商错误信息：去掉密钥、压缩空白、截断
     *
     * @param message 厂商返回的错误信息
     * @param secrets 需要从消息中去掉的密钥等敏感值
     */
    public static String sanitize(String message, String... secrets) {
        if (StrUtil.isBlank(message)) {
            return "";
        }
        String result = message;
        for (String secret : secrets) {
            if (StrUtil.isNotEmpty(secret)) {
                result = result.replace(secret, REDACTED);
            }
        }
        return StrUtil.maxLength(result.replaceAll("\\s+", " ").strip(), MAX_MESSAGE_LENGTH);
    }

    /**
     * 把网络层异常（连接失败、超时等）转成 {@link OcrException}
     *
     * 只保留最底层的异常类名，不带原始消息：消息里可能有请求 URL，百度的 URL 中有 access_token / client_secret
     */
    public static OcrException convertNetworkException(String platform, Throwable ex) {
        String rootCause = ExceptionUtil.getRootCause(ex).getClass().getSimpleName();
        // SocketTimeoutException、Apache ConnectTimeoutException、OkHttp 的 timeout 都是 InterruptedIOException
        if (ExceptionUtil.isCausedBy(ex, InterruptedIOException.class, HttpTimeoutException.class)) {
            return new OcrException(platform, OcrException.Type.TIMEOUT, "调用超时（" + rootCause + "）");
        }
        return new OcrException(platform, OcrException.Type.UPSTREAM_ERROR, "网络异常（" + rootCause + "）");
    }

    /**
     * 按 HTTP 状态码推断错误类型
     */
    public static OcrException.Type typeOfHttpStatus(int statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            return OcrException.Type.UPSTREAM_ERROR;
        }
        return switch (status) {
            case UNAUTHORIZED, FORBIDDEN -> OcrException.Type.AUTH_FAILED;
            case TOO_MANY_REQUESTS -> OcrException.Type.RATE_LIMITED;
            case REQUEST_TIMEOUT, GATEWAY_TIMEOUT -> OcrException.Type.TIMEOUT;
            default -> OcrException.Type.UPSTREAM_ERROR;
        };
    }

    // ========== HTTP（RestClient） ==========

    /**
     * 创建带超时的 RestClient
     *
     * 1. 使用 JDK HttpClient：HttpURLConnection 会在读超时等情况下静默重发 POST，可能导致重复计费，因此不用
     * 2. 使用独立的 builder，不挂 Observation，避免把带 access_token 的 URL 写进指标或日志
     * 3. 去掉 baseUrl 末尾的 /，调用方的路径统一以 / 开头
     */
    public static RestClient buildRestClient(String baseUrl, Duration connectTimeout, Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(StrUtil.removeSuffix(baseUrl.trim(), "/"))
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 把 RestClient 异常转成 {@link OcrException}
     *
     * 1. 不把原异常作为 cause：Spring 的异常消息里带请求 URL，百度的 URL 中有 access_token / client_secret
     * 2. HTTP 错误保留状态码、厂商错误码、错误信息和请求 ID
     * 3. 响应体用 {@link JsonUtils#parseObjectQuietly(String, Class)} 解析：失败时不打印原文（原文可能包含识别结果）
     *
     * @param secrets 需要从消息中去掉的密钥
     */
    public static OcrException convertRestClientException(String platform, RestClientException ex, String... secrets) {
        if (ex instanceof ResourceAccessException) {
            return convertNetworkException(platform, ex);
        }
        if (!(ex instanceof RestClientResponseException responseEx)) {
            return new OcrException(platform, OcrException.Type.UPSTREAM_ERROR,
                    "调用失败（" + ex.getClass().getSimpleName() + "）");
        }
        // 兼容 {"error":{"code","message"}}、{"code","message"}、百度的 {"error_code","error_msg"} / {"error","error_description"}
        int status = responseEx.getStatusCode().value();
        JsonNode body = JsonUtils.parseObjectQuietly(responseEx.getResponseBodyAsString(), JsonNode.class);
        JsonNode error = body != null && body.path("error").isObject() ? body.get("error") : body;
        String vendorCode = StrUtil.blankToDefault(getText(error, "code"), getText(body, "error_code"));
        String vendorMessage = StrUtil.blankToDefault(getText(error, "message"),
                StrUtil.blankToDefault(getText(body, "error_msg"), getText(body, "error_description")));
        String requestId = StrUtil.blankToDefault(getText(body, "request_id"), getText(body, "requestId"));
        String message = "HTTP " + status + (StrUtil.isNotBlank(vendorMessage) ? "：" + sanitize(vendorMessage, secrets) : "");
        return new OcrException(platform, typeOfHttpStatus(status), vendorCode, requestId, message);
    }

}
