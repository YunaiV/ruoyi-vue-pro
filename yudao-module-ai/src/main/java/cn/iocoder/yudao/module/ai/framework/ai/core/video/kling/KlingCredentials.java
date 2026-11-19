package cn.iocoder.yudao.module.ai.framework.ai.core.video.kling;

import cn.hutool.core.util.StrUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.util.Base64;

/**
 * 可灵的凭证
 *
 * 1. API Key：所有模型可用，请求头 Authorization: Bearer {API Key}
 * 2. AccessKey + SecretKey：只适用于旧版接口（model_name 传模型的接口），每次请求按 JWT（HS256）生成令牌：
 *    header {"alg":"HS256","typ":"JWT"}，payload {"iss": AccessKey, "exp": 当前 + 1800 秒, "nbf": 当前 - 5 秒}
 *
 * 凭证只保存在内存中，{@link #toString()} 不输出
 *
 * @see <a href="https://kling.ai/document-api/api/get-started/authentication">可灵鉴权</a>
 * @author 芋道源码
 */
public class KlingCredentials {

    /**
     * JWT 有效期（秒）
     */
    static final long JWT_TTL_SECONDS = 1800;
    /**
     * JWT 生效时间提前量（秒），容忍时钟偏差
     */
    static final long JWT_NOT_BEFORE_SKEW_SECONDS = 5;

    private final String apiKey;
    private final String accessKey;
    private final String secretKey;
    private final Clock clock;

    private KlingCredentials(String apiKey, String accessKey, String secretKey, Clock clock) {
        this.apiKey = apiKey;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.clock = clock;
    }

    public static KlingCredentials ofApiKey(String apiKey) {
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("可灵的 API Key 不能为空");
        }
        return new KlingCredentials(apiKey.trim(), null, null, Clock.systemUTC());
    }

    public static KlingCredentials ofAccessKey(String accessKey, String secretKey) {
        return ofAccessKey(accessKey, secretKey, Clock.systemUTC());
    }

    static KlingCredentials ofAccessKey(String accessKey, String secretKey, Clock clock) {
        if (StrUtil.isBlank(accessKey) || StrUtil.isBlank(secretKey)) {
            throw new IllegalArgumentException("可灵的 AccessKey、SecretKey 不能为空");
        }
        return new KlingCredentials(null, accessKey.trim(), secretKey.trim(), clock);
    }

    public boolean isApiKey() {
        return apiKey != null;
    }

    /**
     * 生成 Authorization 请求头的值
     */
    public String getAuthorization() {
        return "Bearer " + (apiKey != null ? apiKey : createJwt());
    }

    /**
     * 生成 JWT（HS256）
     */
    String createJwt() {
        long now = clock.millis() / 1000;
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"iss\":\"" + escapeJson(accessKey) + "\",\"exp\":" + (now + JWT_TTL_SECONDS)
                + ",\"nbf\":" + (now - JWT_NOT_BEFORE_SKEW_SECONDS) + "}");
        String signingInput = header + "." + payload;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
            return signingInput + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("生成可灵 JWT 失败：" + ex.getClass().getSimpleName());
        }
    }

    private static String base64Url(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 需要在错误信息中打码的凭证
     */
    String[] secrets() {
        return new String[]{apiKey, accessKey, secretKey};
    }

    @Override
    public String toString() {
        return "KlingCredentials{type=" + (apiKey != null ? "apiKey" : "accessKey") + "}";
    }

}
