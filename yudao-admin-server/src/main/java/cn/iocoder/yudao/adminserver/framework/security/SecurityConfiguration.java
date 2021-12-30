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
            // 验证码的接口
            registry.antMatchers(api("/system/captcha/**")).anonymous();
            // 获得租户编号的接口
            registry.antMatchers(api("/system/tenant/get-id-by-name")).anonymous();
            // Spring Boot Admin Server 的安全配置
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
