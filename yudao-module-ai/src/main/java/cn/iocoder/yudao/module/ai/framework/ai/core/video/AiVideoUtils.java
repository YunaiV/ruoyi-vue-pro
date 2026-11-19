package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * AI 视频客户端的工具类：参数校验、JSON、脱敏、MP4 校验
 *
 * @author 芋道源码
 */
public class AiVideoUtils {

    /**
     * 错误信息的最大长度
     */
    private static final int MAX_ERROR_LENGTH = 300;

    /**
     * 地址中的查询参数：服务商返回的视频地址带临时签名
     */
    private static final Pattern URL_QUERY_PATTERN = Pattern.compile("(https?://[^\\s?#\"'<>]+)\\?[^\\s\"'<>]*");
    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)bearer\\s+[^\\s\"',;]+");
    /**
     * 火山引擎签名：Credential=AK/..., Signature=...
     */
    private static final Pattern CREDENTIAL_PATTERN = Pattern.compile("(?i)(credential|signature)=[^\\s\"',;]+");
    /**
     * Google OAuth 访问令牌
     */
    private static final Pattern GOOGLE_TOKEN_PATTERN = Pattern.compile("ya29\\.[A-Za-z0-9_\\-.]+");
    private static final Pattern CONTROL_PATTERN = Pattern.compile("[\\p{Cntrl}]+");
    /**
     * data URL 中的 base64 内容：错误信息里可能回显素材，截断
     */
    private static final Pattern DATA_URL_PATTERN = Pattern.compile("data:[^;,\\s\"]*;base64,[A-Za-z0-9+/=]{16,}");

    private AiVideoUtils() {
    }

    // ========== 参数校验 ==========

    public static String requireText(String value, String name) {
        if (StrUtil.isBlank(value)) {
            throw new IllegalArgumentException(name + "不能为空");
        }
        return value;
    }

    /**
     * 校验文本长度（按 Unicode 码点计）
     */
    public static String checkMaxLength(String value, int maxLength, String name) {
        if (value != null) {
            int length = value.codePointCount(0, value.length());
            if (length > maxLength) {
                throw new IllegalArgumentException(StrUtil.format("{}长度({})超过上限({})", name, length, maxLength));
            }
        }
        return value;
    }

    public static <T> T checkIn(T value, Collection<T> allowed, String name) {
        if (value != null && !allowed.contains(value)) {
            throw new IllegalArgumentException(StrUtil.format("{}({})不支持，可选：{}", name, value, allowed));
        }
        return value;
    }

    public static Integer checkRange(Integer value, long min, long max, String name) {
        if (value != null && (value < min || value > max)) {
            throw new IllegalArgumentException(StrUtil.format("{}({})超出范围 [{}, {}]", name, value, min, max));
        }
        return value;
    }

    public static Long checkRange(Long value, long min, long max, String name) {
        if (value != null && (value < min || value > max)) {
            throw new IllegalArgumentException(StrUtil.format("{}({})超出范围 [{}, {}]", name, value, min, max));
        }
        return value;
    }

    public static void checkArgument(boolean expression, String message, Object... params) {
        if (!expression) {
            throw new IllegalArgumentException(StrUtil.format(message, params));
        }
    }

    /**
     * 校验素材地址：允许 http / https、data:{mime};base64,...，以及服务商专有的协议（例如 asset://、mm_file://、gs://）
     *
     * @param url 素材地址
     * @param name 参数名
     * @param extraSchemes 额外允许的协议（小写，不含 ://）
     */
    public static String checkMediaUrl(String url, String name, String... extraSchemes) {
        requireText(url, name);
        String lower = url.trim().toLowerCase(Locale.ROOT);
        if (lower.startsWith("data:")) {
            checkArgument(lower.contains(";base64,"), "{}的 data URL 必须为 data:<mime>;base64,<内容>", name);
            return url;
        }
        int index = lower.indexOf("://");
        String scheme = index > 0 ? lower.substring(0, index) : "";
        if ("http".equals(scheme) || "https".equals(scheme)) {
            URI uri;
            try {
                uri = URI.create(url.trim());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(name + "不是合法的地址");
            }
            checkArgument(StrUtil.isNotEmpty(uri.getHost()), "{}不是合法的地址", name);
            return url;
        }
        if (ArrayUtil.contains(extraSchemes, scheme)) {
            return url;
        }
        throw new IllegalArgumentException(StrUtil.format("{}只支持 http(s)、data URL{}", name,
                ArrayUtil.isEmpty(extraSchemes) ? "" : "、" + StrUtil.join("://、", (Object[]) extraSchemes) + "://"));
    }

    // ========== JSON ==========

    public static ObjectNode newObject() {
        return JsonUtils.getObjectMapper().createObjectNode();
    }

    public static byte[] toJsonBytes(JsonNode node) {
        try {
            return JsonUtils.getObjectMapper().writeValueAsBytes(node);
        } catch (IOException ex) {
            throw new IllegalStateException("序列化请求失败", ex);
        }
    }

    /**
     * 解析 JSON：失败返回 null，不记录原始内容
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

    public static JsonNode parseJson(String body) {
        return body == null ? null : parseJson(body.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 读取文本字段：缺失、null 返回 null；数字也转为文本
     */
    public static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isMissingNode() || value.isContainerNode()) {
            return null;
        }
        String text = value.asText();
        return StrUtil.isEmpty(text) ? null : text;
    }

    public static Integer integer(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || !(value.isIntegralNumber() || (value.isTextual() && value.asText().matches("-?\\d{1,9}")))) {
            return null;
        }
        return value.asInt();
    }

    public static Double decimal(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return value.asDouble();
        }
        if (value.isTextual() && value.asText().matches("-?\\d+(\\.\\d+)?")) {
            return Double.valueOf(value.asText());
        }
        return null;
    }

    // ========== 脱敏 ==========

    /**
     * 脱敏服务商返回的错误信息：去掉凭证、地址参数、Bearer 值、签名、base64 内容、控制字符，并限制长度
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
        result = DATA_URL_PATTERN.matcher(result).replaceAll("data:***");
        result = URL_QUERY_PATTERN.matcher(result).replaceAll("$1?***");
        result = BEARER_PATTERN.matcher(result).replaceAll("Bearer ***");
        result = CREDENTIAL_PATTERN.matcher(result).replaceAll("$1=***");
        result = GOOGLE_TOKEN_PATTERN.matcher(result).replaceAll("ya29.***");
        result = CONTROL_PATTERN.matcher(result).replaceAll(" ").trim();
        return StrUtil.maxLength(result, MAX_ERROR_LENGTH);
    }

    /**
     * 隐藏地址的查询参数（临时签名），用于日志与 toString
     */
    public static String maskUrl(String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("data:")) {
            return "data:***";
        }
        int index = url.indexOf('?');
        return index >= 0 ? url.substring(0, index) + "?***" : url;
    }

    // ========== 视频校验 ==========

    /**
     * 按文件头判断是否为 MP4 / MOV（ISO BMFF）：第 4 ~ 8 字节为 ftyp
     */
    public static boolean isMp4(byte[] head) {
        return head != null && head.length >= 8
                && head[4] == 'f' && head[5] == 't' && head[6] == 'y' && head[7] == 'p';
    }

}
