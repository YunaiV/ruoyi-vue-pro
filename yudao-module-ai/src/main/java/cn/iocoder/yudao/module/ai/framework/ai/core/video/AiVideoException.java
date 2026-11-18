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

    // TODO DONE @AI：你看看 image 做了么？如果没有，一些可以简化。（image 没有错误分类；业务只用到消息和「提交结果未知」，已删除 Kind、HTTP 状态码、服务商错误码、请求编号，排查信息在客户端日志与消息中）

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

    // TODO DONE @AI：401、等等错误码，要不要使用 spring 的枚举；（按状态码分类的 kindOfHttpStatus 已删除，401、403、429 等具体状态码不再判断；只剩「>= 500 时提交结果未知」的区间判断，保留数字比较，Spring 的 HttpStatus.valueOf 遇到非标准状态码会抛异常）

    /**
     * 构建提交失败的原因：提交结果未知时，提示服务商可能已创建，不要重复提交
     *
     * @param e 异常
     * @return 失败原因
     */
    public static String buildSubmitErrorMessage(Exception e) {
        if (!(e instanceof AiVideoException videoException)) {
            return "提交服务商失败，请稍后重试";
        }
        if (videoException.isSubmitOutcomeUnknown()) {
            return "提交结果未知：服务商可能已创建任务并计费，请勿重复提交，可稍后到服务商控制台核对";
        }
        return StrUtil.blankToDefault(videoException.getMessage(), "提交服务商失败");
    }

}
