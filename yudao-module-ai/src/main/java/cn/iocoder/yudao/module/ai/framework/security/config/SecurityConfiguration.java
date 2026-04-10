package cn.iocoder.yudao.module.ai.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import jakarta.annotation.Resource;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerSseProperties;
import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

import java.util.Optional;

/**
 * AI 模块的 Security 配置
 */
@Configuration(proxyBeanMethods = false, value = "aiSecurityConfiguration")
public class SecurityConfiguration {

    @Resource
    private Optional<McpServerSseProperties> mcpServerSseProperties;
    @Resource
    private Optional<McpServerStreamableHttpProperties> mcpServerStreamableHttpProperties;

    @Bean("aiAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                mcpServerSseProperties.ifPresent(properties -> {
                    registry.requestMatchers(properties.getSseEndpoint()).permitAll();
                    registry.requestMatchers(properties.getSseMessageEndpoint()).permitAll();
                });
                mcpServerStreamableHttpProperties.ifPresent(properties ->
                        registry.requestMatchers(properties.getMcpEndpoint()).permitAll());
            }

        };
    }

}
