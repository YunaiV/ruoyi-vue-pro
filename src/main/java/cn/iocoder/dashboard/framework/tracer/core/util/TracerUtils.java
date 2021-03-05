package cn.iocoder.dashboard.framework.tracer.core.util;

import cn.hutool.core.util.StrUtil;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

import java.util.UUID;

/**
 * 链路追踪工具类
 *
 * @author 芋道源码
 */
public class TracerUtils {

    /**
     * 获得链路追踪编号
     *
     * 一般来说，通过链路追踪编号，可以将访问日志，错误日志，链路追踪日志，logger 打印日志等，结合在一起，从而进行排错。
     *
     * 默认情况下，我们使用 Apache SkyWalking 的 traceId 作为链路追踪编号。当然，可能会存在并未引入 Skywalking 的情况，此时使用 UUID 。
     *
     * @return 链路追踪编号
     */
    public static String getTraceId() {
        // 通过 SkyWalking 获取链路编号
        try {
            String traceId = TraceContext.traceId();
            if (StrUtil.isNotBlank(traceId)) {
                return traceId;
            }
        } catch (Throwable ignore) {}
        // TODO 芋艿 多次调用会问题

        // TODO 麻薯 定义一个给外部扩展的接口,默认在未接入Skywalking时,输出UUID
        return "UUID:" + UUID.randomUUID().toString();
    }

}
