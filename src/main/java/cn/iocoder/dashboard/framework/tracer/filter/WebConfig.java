package cn.iocoder.dashboard.framework.tracer.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
@Component
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private SpringMvcTraceFilter springMvcTraceFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.springMvcTraceFilter).addPathPatterns("/**");
    }

}