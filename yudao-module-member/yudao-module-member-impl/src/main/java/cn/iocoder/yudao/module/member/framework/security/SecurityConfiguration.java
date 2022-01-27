package cn.iocoder.yudao.module.member.framework.security;

import cn.iocoder.yudao.framework.web.config.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

import javax.annotation.Resource;

@Configuration
public class SecurityConfiguration {

    @Resource
    private WebProperties webProperties;

    @Bean
    public Customizer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry> authorizeRequestsCustomizer() {
        return registry -> {
            registry.antMatchers(api("/**")).permitAll(); // 默认 API 都是用户可访问
        };
    }

    private String api(String url) {
        return webProperties.getApiPrefix() + url;
    }

}
