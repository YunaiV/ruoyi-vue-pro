package cn.iocoder.dashboard.framework.tracer.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.dashboard.framework.tracer.core.ITrace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

import java.util.UUID;

/**
 * 链路追踪工具类
 *
 * @author 芋道源码
 */
public class TracerUtils {

    /**
     * 私有化构造方法
     */
    private TracerUtils() {
    }

    /**
     * 获得链路追踪编号
     * <p>
     * 一般来说，通过链路追踪编号，可以将访问日志，错误日志，链路追踪日志，logger 打印日志等，结合在一起，从而进行排错。
     * <p>
     * 默认情况下，我们使用 Apache SkyWalking 的 traceId 作为链路追踪编号。当然，可能会存在并未引入 Skywalking 的情况，此时使用 UUID 。
     *
     * @return 链路追踪编号
     */
    public static String getTraceId() {
        // 通过自定义扩展的tracer产生traceId, 在Spring容器加载完成前会获取不到对应的Bean
        ITrace tracer = null;
        try {
            tracer = getTracer();
        } catch (Throwable ignore) {
        }
        if (null != tracer) {
            try {
                return tracer.getTraceId();
            } catch (Throwable ignored) {
            }
        }
        // 通过 SkyWalking 获取链路编号
        try {
            String traceId = TraceContext.traceId();
            if (StrUtil.isNotBlank(traceId)) {
                return traceId;
            }
        } catch (Throwable ignore) {
        }
        // TODO 芋艿 多次调用会问题

        return defaultTraceId();
    }

    /**
     * 从Spring 容器中获取 ITrace 类,返回可以为null
     *
     * @return ITrace
     */
    private static ITrace getTracer() {
        return SpringUtil.getBean(ITrace.class);
    }

    /**
     * 默认生成TraceId规则为UUID
     *
     * @return UUID
     */
    private static String defaultTraceId() {
        return "UUID:" + UUID.randomUUID().toString();
    }

}
