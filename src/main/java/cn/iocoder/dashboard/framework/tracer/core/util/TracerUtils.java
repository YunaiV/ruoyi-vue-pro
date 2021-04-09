package cn.iocoder.dashboard.framework.tracer.core.util;

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
     * 直接返回skywalking 的TraceId 如果不存在的话为空字符串""
     * <p>
     *
     * @return 链路追踪编号
     */
    public static String getTraceId() {
        return TraceContext.traceId();
    }

}
