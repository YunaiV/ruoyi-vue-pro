package cn.iocoder.dashboard.framework.tracer.filter;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.framework.tracer.core.util.TracerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 对Spring Mvc 的请求拦截, 添加traceId.
 *
 * @author mashu
 */

@Slf4j
@Component
public class ServletTraceFilter implements Filter {

    @Value("${cn.iocoder.tracer.name:global-trace-id}")
    private String traceIdName;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        try {
            // 请求中traceId
            String reqTraceId = (String)httpServletRequest.getHeader(traceIdName);
            // skywalking中的traceId
            String skywalkingTraceId = TracerUtils.getSkywalkingTraceId();
            String traceId ;
            if (null == reqTraceId && StrUtil.isBlank(skywalkingTraceId)) {
                // 两者皆空,添加默认的.
                traceId = TracerUtils.getTraceId();
                httpServletResponse.setHeader(traceIdName, traceId);
            } else if (null == reqTraceId && StrUtil.isNotBlank(skywalkingTraceId)){
                // 若请求空,则添加,为没有skywalking的系统添加一个TraceId
                traceId = skywalkingTraceId;
                httpServletResponse.setHeader(traceIdName, traceId);
            } else if (null != reqTraceId && StrUtil.isBlank(skywalkingTraceId)) {
                // 请求非空, skywalking为空
                traceId = reqTraceId;
            } else {
                // 两者皆非空,不动请求头
                traceId = skywalkingTraceId;
            }
            TracerUtils.saveThreadTraceId(traceId);
            log.debug("请求进入,添加traceId[{}]", traceId);

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            // 请求结束,删除本地的链路流水号
            log.debug("请求结束,删除traceId[{}]", TracerUtils.getTraceId());
            TracerUtils.deleteThreadTraceId();
        }
    }
}
