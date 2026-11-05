package cn.iocoder.yudao.module.report.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import cn.iocoder.yudao.module.report.framework.ureport2.core.web.UReport2SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Report 模块的 Security 配置
 */
@Configuration("reportSecurityConfiguration")
public class SecurityConfiguration {

    @Bean("reportAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                // 积木报表
                registry.requestMatchers("/jmreport/**").permitAll();
                // 积木仪表盘
                registry.requestMatchers("/drag/**").permitAll();
                registry.requestMatchers("/jimubi/**").permitAll();
                // UReport2 报表：仅放行设计器、预览页的静态资源；其它 /ureport/** 请求需要登录，并由 UReport2SecurityFilter 校验租户与权限
                registry.requestMatchers(HttpMethod.GET, UReport2SecurityFilter.STATIC_RESOURCE_PREFIX + "**").permitAll();
            }

        };
    }

}
