package cn.iocoder.dashboard.framework.tracer.config;

import cn.iocoder.dashboard.framework.tracer.annotation.BizTracingAop;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({BizTracingAop.class})
@EnableConfigurationProperties(BizTracerProperties.class)
@ConditionalOnProperty(prefix = "yudao.tracer", value = "enable", matchIfMissing = true)
public class BizTracerAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public BizTracerProperties bizTracerProperties() {
        return new BizTracerProperties();
    }

    @Bean
    public BizTracingAop enableBizTracingAop() {
        return new BizTracingAop();
    }

}
