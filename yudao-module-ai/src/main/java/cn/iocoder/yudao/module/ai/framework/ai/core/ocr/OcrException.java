package cn.iocoder.yudao.module.ai.framework.ai.core.ocr;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OCR 调用异常
 *
 * 1. 消息已脱敏：不含密钥、access_token、请求 URL、图片内容和完整的请求 / 响应体；也不挂原始异常（cause）
 * 2. 保留厂商的错误码、错误信息和请求 ID，便于排查；业务层再按 {@link Type} 转成业务错误码
 *
 * @author 芋道源码
 */
@Getter
public class OcrException extends RuntimeException {

    /**
     * 错误类型
     */
    @Getter
    @AllArgsConstructor
    public enum Type {

        INVALID_CREDENTIAL(false), // 密钥格式不正确（如组合密钥不是两段），未发起调用
        INVALID_CONFIG(false), // 模型标识 / 服务代码不在支持范围内，或地址格式不正确，未发起调用
        INVALID_IMAGE(false), // 图片为空、类型或大小不符合厂商要求；或厂商判定图片无法识别
        AUTH_FAILED(false), // 厂商鉴权失败：密钥错误、过期、无权限、未开通服务等
        RATE_LIMITED(true), // 厂商限流或额度不足
        TIMEOUT(true), // 连接或读取超时
        UPSTREAM_ERROR(true); // 其它上游错误：网络异常、厂商内部错误、响应格式异常等

        /**
         * 是否建议用户稍后重试
         */
        private final boolean retryable;

    }

    /**
     * 平台，例如 TongYi、TencentOcr
     */
    private final String platform;
    /**
     * 错误类型
     */
    private final Type type;
    /**
     * 厂商错误码，可能为空
     */
    private final String vendorCode;
    /**
     * 厂商请求 ID，可能为空
     */
    private final String requestId;

    public OcrException(String platform, Type type, String message) {
        this(platform, type, null, null, message);
    }

    public OcrException(String platform, Type type, String vendorCode, String requestId, String message) {
        super(buildMessage(platform, vendorCode, requestId, message));
        this.platform = platform;
        this.type = type;
        this.vendorCode = vendorCode;
        this.requestId = requestId;
    }

    private static String buildMessage(String platform, String vendorCode, String requestId, String message) {
        StringBuilder sb = new StringBuilder("[").append(platform).append("] ").append(message);
        if (StrUtil.isNotEmpty(vendorCode)) {
            sb.append("（错误码：").append(vendorCode).append('）');
        }
        if (StrUtil.isNotEmpty(requestId)) {
            sb.append("（请求 ID：").append(requestId).append('）');
        }
        return sb.toString();
    }

}
