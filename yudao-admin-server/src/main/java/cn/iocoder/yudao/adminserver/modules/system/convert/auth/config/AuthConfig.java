package cn.iocoder.yudao.adminserver.modules.system.convert.auth.config;

import cn.iocoder.yudao.adminserver.modules.system.convert.auth.handler.DefaultSignUpUrlAuthenticationSuccessHandler;
import cn.iocoder.yudao.framework.security.core.handler.AbstractSignUpUrlAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author weir
 */
@Configuration
public class AuthConfig {
    @Bean
    public AbstractSignUpUrlAuthenticationSuccessHandler authenticationSuccessHandler() {
        AbstractSignUpUrlAuthenticationSuccessHandler successHandler = new DefaultSignUpUrlAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/api/callback");
        return successHandler;
    }
}
