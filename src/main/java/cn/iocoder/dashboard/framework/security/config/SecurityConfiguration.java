package cn.iocoder.dashboard.framework.security.config;

import cn.iocoder.dashboard.framework.security.core.filter.JwtAuthenticationTokenFilter;
import cn.iocoder.dashboard.framework.security.core.handler.AuthenticationEntryPointImpl;
import cn.iocoder.dashboard.framework.security.core.handler.LogoutSuccessHandlerImpl;
import cn.iocoder.dashboard.framework.web.config.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.Resource;

/**
 * spring security配置
 *
 * @author ruoyi
 */
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * 自定义用户认证逻辑
     */
    @Resource
    private UserDetailsService userDetailsService;

    /**
     * 认证失败处理类
     */
    @Resource
    private AuthenticationEntryPoint unauthorizedHandler;
    /**
     * 权限不够处理器
     */
    @Resource
    private AccessDeniedHandler accessDeniedHandler;
    /**
     * 退出处理类
     */
    @Resource
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    /**
     * Token 认证过滤器
     */
    @Resource
    private JwtAuthenticationTokenFilter authenticationTokenFilter;

    @Resource
    private WebProperties webProperties;

    /**
     * 由于 Spring Security 创建 AuthenticationManager 对象时，没声明 @Bean 注解，导致无法被注入
     * 通过覆写父类的该方法，添加 @Bean 注解，解决该问题
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Spring Security 加密器
     * 考虑到安全性，这里采用 BCryptPasswordEncoder 加密器
     *
     * @see <a href="http://stackabuse.com/password-encoding-with-spring-security/">Password Encoding with Spring Security</a>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 身份认证接口
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    /**
     * 配置 URL 的安全配置
     *
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CSRF 禁用，因为不使用 Session
                .csrf().disable()
                // 基于 token 机制，所以不需要 Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 一堆自定义的 Spring Security 处理器
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                    .accessDeniedHandler(accessDeniedHandler).and()
                // TODO 过滤请求
                .authorizeRequests()
                // 登陆的接口，可匿名访问
                .antMatchers(webProperties.getApiPrefix() + "/login").anonymous()
                // 通用的接口，可匿名访问
                .antMatchers( webProperties.getApiPrefix() + "/captcha/**").anonymous()
                // TODO
                .antMatchers(HttpMethod.GET, "/*.html", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()
                .antMatchers("/profile/**").anonymous()
                .antMatchers("/common/download**").anonymous()
                .antMatchers("/common/download/resource**").anonymous()
                .antMatchers("/swagger-ui.html").anonymous()
                .antMatchers("/swagger-resources/**").anonymous()
                .antMatchers("/webjars/**").anonymous()
                .antMatchers("/*/api-docs").anonymous()
                .antMatchers("/druid/**").hasAnyAuthority("druid") // TODO 芋艿，未来需要在拓展下
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated()
                .and()
                .headers().frameOptions().disable();
        httpSecurity.logout().logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler);
        // 添加 JWT Filter
        httpSecurity.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
