package cn.iocoder.yudao.module.ai.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * AI 模块的 Security 配置
 */
@Configuration(proxyBeanMethods = false, value = "aiSecurityConfiguration")
public class SecurityConfiguration {

    @Value("${spring.ai.mcp.server.sse-endpoint:/sse}")
    private String mcpSseEndpoint;
    @Value("${spring.ai.mcp.server.sse-message-endpoint:/mcp/message}")
    private String mcpSseMessageEndpoint;
    @Value("${spring.ai.mcp.server.streamable-http-endpoint:/mcp}")
    private String mcpStreamableHttpEndpoint;

    @Bean("aiAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                if (StrUtil.isNotBlank(mcpSseEndpoint)) {
                    registry.requestMatchers(mcpSseEndpoint).permitAll();
                }
                if (StrUtil.isNotBlank(mcpSseMessageEndpoint)) {
                    registry.requestMatchers(mcpSseMessageEndpoint).permitAll();
                }
                if (StrUtil.isNotBlank(mcpStreamableHttpEndpoint)) {
                    registry.requestMatchers(mcpStreamableHttpEndpoint).permitAll();
                }
            }

        };
    }

}
