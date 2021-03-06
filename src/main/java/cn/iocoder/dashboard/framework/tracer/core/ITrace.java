package cn.iocoder.dashboard.framework.tracer.core;

/**
 * 用于扩展获取traceId的场景,需要装载到Spring bean容器中.
 *
 * @author 麻薯
 */
public interface ITrace {

    /**
     * 用于接入三方traceId
     *
     * @return traceId
     */
    String getTraceId();
}
