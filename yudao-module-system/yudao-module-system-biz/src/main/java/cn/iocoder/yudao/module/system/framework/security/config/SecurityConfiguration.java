package cn.iocoder.yudao.module.system.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

/**
 * System 模块的 Security 配置
 */
@Configuration("systemSecurityConfiguration")
public class SecurityConfiguration {

    @Bean("systemAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
                // 登录的接口
                registry.antMatchers(buildAdminApi("/system/auth/login")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/logout")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/refresh-token")).permitAll();
                // 社交登陆的接口
                registry.antMatchers(buildAdminApi("/system/auth/social-auth-redirect")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/social-login")).permitAll();
                // 登录登录的接口
                registry.antMatchers(buildAdminApi("/system/auth/sms-login")).permitAll();
                registry.antMatchers(buildAdminApi("/system/auth/send-sms-code")).permitAll();
                // 验证码的接口
                registry.antMatchers(buildAdminApi("/system/captcha/**")).permitAll();
                // 获得租户编号的接口
                registry.antMatchers(buildAdminApi("/system/tenant/get-id-by-name")).permitAll();
                // 短信回调 API
                registry.antMatchers(buildAdminApi("/system/sms/callback/**")).permitAll();
                // OAuth2 API
                registry.antMatchers(buildAdminApi("/system/oauth2/token")).permitAll();
                registry.antMatchers(buildAdminApi("/system/oauth2/check-token")).permitAll();
            }

        };
    }

}
