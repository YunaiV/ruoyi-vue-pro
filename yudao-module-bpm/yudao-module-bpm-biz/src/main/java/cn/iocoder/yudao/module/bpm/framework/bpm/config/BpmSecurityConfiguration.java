package cn.iocoder.yudao.module.bpm.framework.bpm.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

/**
 * @author kemengkai
 * @create 2022-05-07 08:15
 */
@Configuration("bpmSecurityConfiguration")
public class BpmSecurityConfiguration {

    @Bean("bpmAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
                // 任务回退接口
                registry.antMatchers(buildAdminApi("/bpm/task/back")).permitAll();
            }

        };
    }
}
