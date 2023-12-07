package cn.iocoder.yudao.ssodemo.framework.config;

import cn.iocoder.yudao.ssodemo.framework.core.filter.TokenAuthenticationFilter;
import cn.iocoder.yudao.ssodemo.framework.core.handler.AccessDeniedHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.annotation.Resource;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfiguration {

    @Resource
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Resource
    private AccessDeniedHandlerImpl accessDeniedHandler;
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 设置 URL 安全权限
        httpSecurity
                // 开启跨域
                .cors(Customizer.withDefaults())
                // CSRF 禁用，因为不使用 Session
                .csrf(AbstractHttpConfigurer::disable)
                // 一堆自定义的 Spring Security 处理器
                .exceptionHandling(c -> c.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        // 设置每个请求的权限
        httpSecurity.authorizeHttpRequests(c -> c
                // 1. 静态资源，可匿名访问
                .requestMatchers(HttpMethod.GET, "/*.html", "/*.html", "/*.css", "/*.js").permitAll()
                // 2. 登录相关的接口，可匿名访问
                .requestMatchers("/auth/login-by-code").permitAll()
                .requestMatchers("/auth/refresh-token").permitAll()
                .requestMatchers("/auth/logout").permitAll())
                // 3. 兜底规则，必须认证
                .authorizeHttpRequests(c -> c.anyRequest().authenticated());

        // 添加 Token Filter
        httpSecurity.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

}
