package cn.iocoder.yudao.adminserver.framework.security;

import cn.iocoder.yudao.framework.web.config.WebProperties;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.boot.admin.context-path:''}")
    private String adminSeverContextPath;

    @Bean
    public Customizer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry> authorizeRequestsCustomizer() {
        return registry -> {
            // 通用的接口，可匿名访问 TODO 芋艿：需要抽象出去
            registry.antMatchers(api("/system/captcha/**")).anonymous();
            // Spring Boot Admin Server 的安全配置 TODO 芋艿：需要抽象出去
            registry.antMatchers(adminSeverContextPath).anonymous()
                    .antMatchers(adminSeverContextPath + "/**").anonymous();
            // 短信回调 API
            registry.antMatchers(api("/system/sms/callback/**")).anonymous();
        };
    }

    private String api(String url) {
        return webProperties.getApiPrefix() + url;
    }

}
