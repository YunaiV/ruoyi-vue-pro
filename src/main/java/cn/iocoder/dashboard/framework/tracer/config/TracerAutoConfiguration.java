package cn.iocoder.dashboard.framework.tracer.config;

import cn.iocoder.dashboard.framework.tracer.core.annotation.BizTracingAop;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tracer 配置类
 *
 * @author mashu
 */
@Configuration
@ConditionalOnClass({BizTracingAop.class})
@EnableConfigurationProperties(BizTracerProperties.class)
@ConditionalOnProperty(prefix = "yudao.tracer", value = "enable", matchIfMissing = true)
public class TracerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BizTracerProperties bizTracerProperties() {
        return new BizTracerProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public BizTracingAop bizTracingAop() {
        return new BizTracingAop();
    }

}
