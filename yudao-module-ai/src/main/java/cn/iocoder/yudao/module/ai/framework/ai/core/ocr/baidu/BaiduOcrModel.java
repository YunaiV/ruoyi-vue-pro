package cn.iocoder.yudao.module.ai.framework.ai.core.ocr.baidu;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResult;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.getText;

/**
 * 百度智能云通用文字识别（高精度版）的 {@link OcrModel} 实现（平台 BAIDU）
 *
 * 1. 鉴权，支持两种密钥：
 *    1.1 新版 API Key（bce-v3/ 开头，单段）：请求头 Authorization: Bearer API Key，不换取、不传 access_token；
 *    1.2 旧版 APIKey|SecretKey：换取 access_token（有效期 30 天），缓存在本实例中，过期前刷新；
 *        返回 110 / 111（token 失效 / 过期）时清掉缓存，重新换取后重试一次
 * 2. 识别：POST /rest/2.0/ocr/v1/accurate_basic，form 参数 image = base64
 *
 * 官方文档：https://ai.baidu.com/ai-doc/OCR/1k3h7y3db 、https://ai.baidu.com/ai-doc/REFERENCE/Rm64sd6cb（新版 API Key）、
 * https://ai.baidu.com/ai-doc/REFERENCE/Ck3dwjhhu（旧版 access_token）
 *
 * 注意：旧版的两个接口都把 client_secret / access_token 放在 URL 上，因此本类的异常、日志都不带 URL。
 *
 * @author 芋道源码
 */
public class BaiduOcrModel implements OcrModel {

    public static final String PLATFORM = "BAIDU";

    public static final String DEFAULT_BASE_URL = "https://aip.baidubce.com";
    private static final String TOKEN_PATH = "/oauth/2.0/token";

    /**
     * 默认（也是目前唯一支持的）服务代码：通用文字识别（高精度版）
     */
    public static final String DEFAULT_MODEL = "accurate_basic";
    private static final String OCR_PATH = "/rest/2.0/ocr/v1/" + DEFAULT_MODEL;

    /**
     * 支持 jpg / jpeg / png / bmp；base64 编码并 urlencode 后不超过 10M
     */
    static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/bmp");
    static final long MAX_ENCODED_LENGTH = 10L * 1024 * 1024;

    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    /**
     * access_token 提前刷新的时间
     */
    private static final Duration TOKEN_REFRESH_AHEAD = Duration.ofHours(1);

    /**
     * 错误码：access_token 失效 / 过期
     */
    private static final Set<String> TOKEN_INVALID_CODES = Set.of("110", "111");
    /**
     * 错误码：无权限、IAM 鉴权失败
     */
    private static final Set<String> AUTH_FAILED_CODES = Set.of("6", "14", "110", "111");
    /**
     * 错误码：集群 / 每天 / QPS / 总量超限额
     */
    private static final Set<String> RATE_LIMITED_CODES = Set.of("4", "17", "18", "19");
    /**
     * 错误码：图片为空 / 格式错误 / 大小错误
     */
    private static final Set<String> INVALID_IMAGE_CODES = Set.of("216200", "216201", "216202");

    /**
     * 新版 API Key 的前缀
     */
    private static final String API_KEY_PREFIX = "bce-v3/";

    /**
     * 新版 API Key；使用旧版密钥时为 null
     */
    private final String apiKey;
    /**
     * 旧版 API Key / Secret Key；使用新版 API Key 时为 null
     */
    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;
    private final Clock clock;

    /**
     * 缓存的 access_token 及其刷新时间；读写都在 synchronized 方法中
     */
    private CachedToken cachedToken;

    private record CachedToken(String value, Instant refreshAt) {
    }

    public BaiduOcrModel(String apiKey, String url, String model) {
        this(apiKey, url, model, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, Clock.systemUTC());
    }

    BaiduOcrModel(String apiKey, String url, String model, Duration connectTimeout, Duration readTimeout, Clock clock) {
        // 1. 解析凭据：新版单段 API Key（bce-v3/ 开头），或旧版 APIKey|SecretKey（严格两段、均非空）；异常消息不回显原值
        List<String> parts = StrUtil.split(apiKey, '|', true, false);
        if (parts.size() == 1 && StrUtil.startWith(parts.get(0), API_KEY_PREFIX)
                && parts.get(0).length() > API_KEY_PREFIX.length()) {
            this.apiKey = parts.get(0);
            this.clientId = null;
            this.clientSecret = null;
        } else if (parts.size() == 2 && !StrUtil.hasBlank(parts.get(0), parts.get(1))) {
            this.apiKey = null;
            this.clientId = parts.get(0);
            this.clientSecret = parts.get(1);
        } else {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CREDENTIAL,
                    "密钥格式应为新版 API Key（bce-v3/ 开头），或旧版 APIKey|SecretKey（两段，用 | 分隔，均不能为空）");
        }
        // 2. 校验服务代码
        if (StrUtil.isNotBlank(model) && !DEFAULT_MODEL.equals(model.trim())) {
            throw new OcrException(PLATFORM, OcrException.Type.INVALID_CONFIG,
                    "不支持的服务代码：" + model.trim() + "，支持：" + DEFAULT_MODEL);
        }
        this.restClient = OcrUtils.buildRestClient(StrUtil.blankToDefault(url, DEFAULT_BASE_URL),
                connectTimeout, readTimeout);
        this.clock = clock;
    }

    @Override
    public OcrResponse call(OcrPrompt prompt) {
        // 1. 校验图片，转 base64（仅内存中）
        byte[] image = OcrUtils.readImage(PLATFORM, prompt, ALLOWED_MIME_TYPES);
        String base64 = Base64.encode(image);
        OcrUtils.checkSize(PLATFORM, OcrUtils.urlEncodedLength(base64), MAX_ENCODED_LENGTH,
                "base64 编码并 urlencode 后的图片");

        // 2. 调用：新版 API Key 直接调用（Bearer 鉴权）；旧版密钥在 token 失效时刷新后重试一次
        JsonNode root;
        if (apiKey != null) {
            root = recognize(null, base64);
        } else {
            String token = getAccessToken();
            root = recognize(token, base64);
            if (TOKEN_INVALID_CODES.contains(StrUtil.nullToEmpty(getText(root, "error_code")))) {
                invalidateAccessToken(token);
                root = recognize(getAccessToken(), base64);
            }
        }
        String errorCode = getText(root, "error_code");
        if (errorCode != null) {
            throw new OcrException(PLATFORM, typeOfErrorCode(errorCode), errorCode, getText(root, "log_id"),
                    OcrUtils.sanitize(getText(root, "error_msg"), apiKey, clientId, clientSecret));
        }

        // 3. 解析：words_result[].words
        JsonNode wordsResult = root.get("words_result");
        if (wordsResult == null || !wordsResult.isArray()) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, null,
                    getText(root, "log_id"), "响应中没有 words_result");
        }
        List<String> lines = new ArrayList<>(wordsResult.size());
        wordsResult.forEach(item -> lines.add(getText(item, "words")));
        return new OcrResponse(new OcrResult(OcrUtils.joinLines(lines)));
    }

    /**
     * 调用识别接口
     *
     * @param token 旧版密钥的 access_token；新版 API Key 时为 null，改为 Bearer 鉴权
     */
    private JsonNode recognize(String token, String base64) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("image", base64);
        String body;
        try {
            body = restClient.post()
                    .uri(builder -> token != null
                            ? builder.path(OCR_PATH).queryParam("access_token", "{token}").build(token)
                            : builder.path(OCR_PATH).build())
                    .headers(headers -> {
                        if (apiKey != null) {
                            headers.setBearerAuth(apiKey);
                        }
                    })
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw OcrUtils.convertRestClientException(PLATFORM, ex, apiKey, clientId, clientSecret, token);
        }
        JsonNode root = JsonUtils.parseObjectQuietly(body, JsonNode.class);
        if (root == null || !root.isObject()) {
            throw new OcrException(PLATFORM, OcrException.Type.UPSTREAM_ERROR, "响应不是合法的 JSON");
        }
        return root;
    }

    // ========== access_token ==========

    /**
     * 获取 access_token：未过期时直接返回缓存，否则换取新的。加锁避免并发时重复换取
     */
    private synchronized String getAccessToken() {
        if (cachedToken != null && clock.instant().isBefore(cachedToken.refreshAt())) {
            return cachedToken.value();
        }
        return refreshAccessToken();
    }

    /**
     * 清掉失效的 access_token；如果其它线程已经换了新的，则保留
     */
    private synchronized void invalidateAccessToken(String value) {
        if (cachedToken != null && cachedToken.value().equals(value)) {
            cachedToken = null;
        }
    }

    private String refreshAccessToken() {
        // 1. 调用
        String body;
        try {
            body = restClient.post()
                    .uri(builder -> builder.path(TOKEN_PATH)
                            .queryParam("grant_type", "client_credentials")
                            .queryParam("client_id", "{clientId}")
                            .queryParam("client_secret", "{clientSecret}")
                            .build(clientId, clientSecret))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            // 换 token 失败时返回 {"error":"invalid_client","error_description":"..."}，4xx 表示 API Key / Secret Key 不正确
            int status = ex.getStatusCode().value();
            OcrException.Type type = ex.getStatusCode().is4xxClientError() && status != HttpStatus.TOO_MANY_REQUESTS.value()
                    ? OcrException.Type.AUTH_FAILED : OcrUtils.typeOfHttpStatus(status);
            throw buildTokenException(type, JsonUtils.parseObjectQuietly(ex.getResponseBodyAsString(), JsonNode.class),
                    "HTTP " + status);
        } catch (RestClientException ex) {
            throw OcrUtils.convertRestClientException(PLATFORM, ex, clientId, clientSecret);
        }

        // 2. 解析并缓存
        JsonNode root = JsonUtils.parseObjectQuietly(body, JsonNode.class);
        String token = getText(root, "access_token");
        if (StrUtil.isBlank(token)) {
            throw buildTokenException(getText(root, "error") != null
                    ? OcrException.Type.AUTH_FAILED : OcrException.Type.UPSTREAM_ERROR, root, "响应中没有 access_token");
        }
        long expiresIn = root.path("expires_in").asLong(0);
        Duration ttl = Duration.ofSeconds(expiresIn > 0 ? expiresIn : Duration.ofDays(30).toSeconds());
        // 提前刷新；有效期过短时取一半
        Duration ahead = ttl.compareTo(TOKEN_REFRESH_AHEAD.multipliedBy(2)) > 0 ? TOKEN_REFRESH_AHEAD : ttl.dividedBy(2);
        cachedToken = new CachedToken(token, clock.instant().plus(ttl).minus(ahead));
        return token;
    }

    private OcrException buildTokenException(OcrException.Type type, JsonNode body, String fallbackMessage) {
        String description = OcrUtils.sanitize(getText(body, "error_description"), clientId, clientSecret);
        return new OcrException(PLATFORM, type, getText(body, "error"), null,
                "获取 access_token 失败：" + StrUtil.blankToDefault(description, fallbackMessage));
    }

    private static OcrException.Type typeOfErrorCode(String errorCode) {
        if (AUTH_FAILED_CODES.contains(errorCode)) {
            return OcrException.Type.AUTH_FAILED;
        }
        if (RATE_LIMITED_CODES.contains(errorCode)) {
            return OcrException.Type.RATE_LIMITED;
        }
        if (INVALID_IMAGE_CODES.contains(errorCode)) {
            return OcrException.Type.INVALID_IMAGE;
        }
        return OcrException.Type.UPSTREAM_ERROR;
    }

}
