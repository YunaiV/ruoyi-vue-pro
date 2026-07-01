package cn.iocoder.yudao.framework.tracer.core.util;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 链路追踪 Util
 *
 * @author 芋道源码
 */
public class TracerFrameworkUtils {

    /**
     * 将异常记录到 Span 中，参考自 com.aliyuncs.utils.TraceUtils
     *
     * @param throwable 异常
     * @param span Span
     */
    public static void onError(Throwable throwable, Span span) {
        // 忽略无效 Span
        if (span == null || !span.getSpanContext().isValid()) {
            return;
        }
        // 标记异常状态
        if (throwable == null) {
            span.setStatus(StatusCode.ERROR);
            return;
        }

        // 记录异常事件
        span.recordException(throwable);
        String message = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
        span.setStatus(StatusCode.ERROR, message == null ? "" : message);
        // 记录异常堆栈
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        span.setAttribute("error.stack", sw.toString());
    }

}
