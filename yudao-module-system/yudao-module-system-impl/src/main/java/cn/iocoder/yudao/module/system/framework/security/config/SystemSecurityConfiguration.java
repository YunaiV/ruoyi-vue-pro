package cn.iocoder.yudao.module.system.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

/**
 * System 模块的 Security 配置
 */
@Configuration
public class SystemSecurityConfiguration {

    @Bean("systemAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
                // 登录的接口，可匿名访问
                registry.antMatchers(buildAdminApi("/system/login")).anonymous();
                // 验证码的接口
                registry.antMatchers(buildAdminApi("/system/captcha/**")).anonymous();
                // 获得租户编号的接口
                registry.antMatchers(buildAdminApi("/system/tenant/get-id-by-name")).anonymous();
                // 短信回调 API
                registry.antMatchers(buildAdminApi("/system/sms/callback/**")).anonymous();
            }

        };
    }

}
