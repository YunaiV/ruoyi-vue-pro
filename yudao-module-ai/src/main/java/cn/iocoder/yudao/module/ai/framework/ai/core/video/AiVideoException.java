package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * AI 视频客户端的调用异常
 *
 * 1. 消息已脱敏：不含 API Key、Authorization、签名、带签名的临时地址，可以直接展示
 * 2. {@link #isSubmitOutcomeUnknown()} 为 true 时，表示「创建任务」请求已经发出但结果未知（超时、网络中断、5xx），
 *    服务端可能已经创建并计费，调用方不能自动重新提交
 *
 * @author 芋道源码
 */
@Getter
public class AiVideoException extends RuntimeException {

    /**
     * 「创建任务」是否结果未知：true 时不能自动重新提交
     */
    private final boolean submitOutcomeUnknown;

    public AiVideoException(String message) {
        this(message, false);
    }

    public AiVideoException(String message, boolean submitOutcomeUnknown) {
        super(message);
        this.submitOutcomeUnknown = submitOutcomeUnknown;
    }

    /**
     * 构建提交失败的原因：提交结果未知时，提示服务商可能已创建，不要重复提交
     *
     * @param e 异常
     * @return 失败原因
     */
    public static String buildSubmitErrorMessage(Exception e) {
        if (!(e instanceof AiVideoException)) {
            return "提交服务商失败，请稍后重试";
        }
        AiVideoException videoException = (AiVideoException) e;
        if (videoException.isSubmitOutcomeUnknown()) {
            return "提交结果未知：服务商可能已创建任务并计费，请勿重复提交，可稍后到服务商控制台核对";
        }
        return StrUtil.blankToDefault(videoException.getMessage(), "提交服务商失败");
    }

}
