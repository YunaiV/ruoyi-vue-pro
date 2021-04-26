package cn.iocoder.dashboard.framework.tracer.config;

import cn.iocoder.dashboard.framework.tracer.core.aop.BizTraceAspect;
import cn.iocoder.dashboard.framework.tracer.core.filter.TraceFilter;
import cn.iocoder.dashboard.framework.web.core.enums.FilterOrderEnum;
import io.opentracing.Tracer;
import org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tracer 配置类
 *
 * @author mashu
 */
@Configuration
@ConditionalOnClass({BizTraceAspect.class})
@EnableConfigurationProperties(TracerProperties.class)
@ConditionalOnProperty(prefix = "yudao.tracer", value = "enable", matchIfMissing = true)
public class TracerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TracerProperties bizTracerProperties() {
        return new TracerProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public BizTraceAspect bizTracingAop() {
        return new BizTraceAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer() {
        return new SkywalkingTracer();
    }

    /**
     * 创建 TraceFilter 过滤器，响应 header 设置 traceId
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilter() {
        FilterRegistrationBean<TraceFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TraceFilter());
        registrationBean.setOrder(FilterOrderEnum.TRACE_FILTER);
        return registrationBean;
    }

}
