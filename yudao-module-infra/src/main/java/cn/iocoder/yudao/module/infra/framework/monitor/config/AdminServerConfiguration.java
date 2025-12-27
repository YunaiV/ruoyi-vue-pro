package cn.iocoder.yudao.module.infra.framework.monitor.config;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Spring Boot Admin Server 配置
 *
 * 包含 Admin Server 的启用配置和安全配置
 * 安全配置独立于 {@link cn.iocoder.yudao.framework.security.config.YudaoWebSecurityConfigurerAdapter}，
 * 使用 HTTP Basic 认证保护 Admin Server 端点，不影响现有的 Token 认证机制
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
@EnableAdminServer
@ConditionalOnClass(name = "de.codecentric.boot.admin.server.config.AdminServerProperties") // 目的：按需启动 spring boot admin 监控服务
public class AdminServerConfiguration {

    @Value("${spring.boot.admin.context-path:''}")
    private String adminSeverContextPath;

    @Value("${spring.boot.admin.client.username:admin}")
    private String username;

    @Value("${spring.boot.admin.client.password:admin}")
    private String password;

    /**
     * Spring Boot Admin 专用的 InMemoryUserDetailsManager
     * 使用内存存储，与系统用户隔离
     */
    @Bean("adminUserDetailsManager")
    public InMemoryUserDetailsManager adminUserDetailsManager(PasswordEncoder passwordEncoder) {
        UserDetails adminUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles("ADMIN_SERVER")
                .build();
        return new InMemoryUserDetailsManager(adminUser);
    }

    /**
     * Spring Boot Admin Server 的 SecurityFilterChain
     * 使用 @Order(1) 确保优先于默认的 SecurityFilterChain 匹配
     */
    @Bean("adminServerSecurityFilterChain")
    @Order(1)
    public SecurityFilterChain adminServerSecurityFilterChain(HttpSecurity httpSecurity,
                                                               InMemoryUserDetailsManager adminUserDetailsManager) throws Exception {
        // 登录成功后的处理器
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminSeverContextPath + "/");

        // 配置 HttpSecurity 对象
        httpSecurity
                // 仅匹配 Admin Server 的路径
                .securityMatcher(adminSeverContextPath + "/**")
                // 使用独立的 UserDetailsManager
                .userDetailsService(adminUserDetailsManager)
                // 授权配置
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(adminSeverContextPath + "/assets/**").permitAll() // 静态资源允许匿名访问
                        .requestMatchers(adminSeverContextPath + "/login").permitAll() // 登录页面允许匿名访问
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll() // 异步请求允许
                        .anyRequest().authenticated() // 其他请求需要认证
                )
                // 表单登录配置（用于 Admin UI 访问）
                .formLogin(form -> form
                        .loginPage(adminSeverContextPath + "/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                // 登出配置
                .logout(logout -> logout
                        .logoutUrl(adminSeverContextPath + "/logout")
                        .logoutSuccessUrl(adminSeverContextPath + "/login")
                )
                // HTTP Basic 认证（用于 Admin Client 注册）
                .httpBasic(Customizer.withDefaults())
                // CSRF 配置
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                adminSeverContextPath + "/instances", // Admin Client 注册端点忽略 CSRF
                                adminSeverContextPath + "/actuator/**" // Actuator 端点忽略 CSRF
                        )
                );
        return httpSecurity.build();
    }

}
