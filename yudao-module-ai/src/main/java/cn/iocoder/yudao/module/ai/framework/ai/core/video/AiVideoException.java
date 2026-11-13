package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import lombok.Getter;

/**
 * AI 视频客户端的调用异常
 *
 * 1. 消息已脱敏：不含 API Key、Authorization、签名、带签名的临时地址
 * 2. {@link #isSubmitOutcomeUnknown()} 为 true 时，表示「创建任务」请求已经发出但结果未知（超时、网络中断、5xx），
 *    服务端可能已经创建并计费，调用方不能自动重新提交
 *
 * @author 芋道源码
 */
@Getter
public class AiVideoException extends RuntimeException {

    // TODO @AI：你看看 image 做了么？如果没有，一些可以简化。
    /**
     * 错误类别：用于区分密钥、授权、余额、地址、素材等问题
     */
    public enum Kind {

        /**
         * 鉴权失败：密钥无效、签名错误、令牌过期
         */
        AUTHENTICATION,
        /**
         * 无权限：模型未开通、账号无该接口权限
         */
        PERMISSION,
        /**
         * 余额或资源包不足
         */
        INSUFFICIENT_BALANCE,
        /**
         * 限流、并发超限
         */
        RATE_LIMITED,
        /**
         * 请求参数或素材不符合要求
         */
        INVALID_REQUEST,
        /**
         * 内容审核未通过（输入或输出）
         */
        CONTENT_REJECTED,
        /**
         * 任务、模型或接口不存在（含地址错误）
         */
        NOT_FOUND,
        /**
         * 服务商内部错误
         */
        SERVER_ERROR,
        /**
         * 请求超时：结果未知
         */
        TIMEOUT,
        /**
         * 网络异常：结果未知
         */
        NETWORK,
        /**
         * 响应格式不符合文档：结果未知
         */
        INVALID_RESPONSE,

    }

    private final String platform;
    private final Kind kind;
    /**
     * HTTP 状态码，未收到响应时为 null
     */
    private final Integer httpStatus;
    /**
     * 服务商错误码
     */
    private final String code;
    /**
     * 服务商请求编号，便于排查
     */
    private final String requestId;
    /**
     * 「创建任务」是否结果未知：true 时不能自动重新提交
     */
    private final boolean submitOutcomeUnknown;

    public AiVideoException(String platform, Kind kind, Integer httpStatus, String code, String requestId,
                            boolean submitOutcomeUnknown, String message) {
        super(message);
        this.platform = platform;
        this.kind = kind;
        this.httpStatus = httpStatus;
        this.code = code;
        this.requestId = requestId;
        this.submitOutcomeUnknown = submitOutcomeUnknown;
    }

    /**
     * 按 HTTP 状态码推断错误类别；服务商有更精确的错误码时，由各客户端自行判断
     */
    // TODO @AI：401、等等错误码，要不要使用 spring 的枚举；
    public static Kind kindOfHttpStatus(int status) {
        if (status == 401) {
            return Kind.AUTHENTICATION;
        }
        if (status == 402) {
            return Kind.INSUFFICIENT_BALANCE;
        }
        if (status == 403) {
            return Kind.PERMISSION;
        }
        if (status == 404) {
            return Kind.NOT_FOUND;
        }
        if (status == 429) {
            return Kind.RATE_LIMITED;
        }
        if (status >= 500) {
            return Kind.SERVER_ERROR;
        }
        if (status >= 400) {
            return Kind.INVALID_REQUEST;
        }
        return Kind.INVALID_RESPONSE;
    }

}
