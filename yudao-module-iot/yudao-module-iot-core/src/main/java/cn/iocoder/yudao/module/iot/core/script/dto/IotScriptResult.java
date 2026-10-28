package cn.iocoder.yudao.module.iot.core.script.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IoT 数据解析脚本的一次调用结果
 * <p>
 * core 不依赖 biz / gateway 的错误码：由调用方按 {@link Status} 翻译为各自的 ErrorCode
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class IotScriptResult {

    /**
     * 调用结果的状态
     */
    public enum Status {

        /**
         * 执行成功（json 可能为 null：没有定义该函数，或返回了 null / undefined）
         */
        SUCCESS,
        /**
         * 脚本异常：语法错误、运行错误、throw、超出栈深等
         */
        ERROR,
        /**
         * 超时：排队超过截止时间、超过执行预算（时间或指令）
         */
        TIMEOUT,
        /**
         * 拒绝：JS 线程池已满、已熔断
         */
        REJECTED,
        /**
         * 输出不合法
         */
        OUTPUT_INVALID

    }

    // DONE @AI：字段注释，还是要写下
    /**
     * 状态
     */
    private final Status status;
    /**
     * 结果 JSON；只有 SUCCESS 时可能非空
     */
    private final String json;
    /**
     * 错误信息
     */
    private final String message;
    /**
     * 出错行号；未知时为 -1
     */
    private final int lineNumber;
    /**
     * 执行耗时（毫秒）
     */
    private final long durationMillis;

    public static IotScriptResult success(String json, long durationMillis) {
        return new IotScriptResult(Status.SUCCESS, json, null, -1, durationMillis);
    }

    public static IotScriptResult error(String message, long durationMillis) {
        return new IotScriptResult(Status.ERROR, null, message, -1, durationMillis);
    }

    public static IotScriptResult timeout(String message) {
        return new IotScriptResult(Status.TIMEOUT, null, message, -1, 0);
    }

    public static IotScriptResult rejected(String message) {
        return new IotScriptResult(Status.REJECTED, null, message, -1, 0);
    }

    public static IotScriptResult of(Status status, String message, int lineNumber, long durationMillis) {
        return new IotScriptResult(status, null, message, lineNumber, durationMillis);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isTimeout() {
        return status == Status.TIMEOUT;
    }

}
