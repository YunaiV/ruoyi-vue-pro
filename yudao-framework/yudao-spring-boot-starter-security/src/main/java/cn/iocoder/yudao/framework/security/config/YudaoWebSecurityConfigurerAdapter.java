package cn.iocoder.yudao.framework.security.config;

import cn.iocoder.yudao.framework.security.core.filter.TokenAuthenticationFilter;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 自定义的 Spring Security 配置适配器实现
 *
 * @author 芋道源码
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class YudaoWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Resource
    private WebProperties webProperties;
    @Resource
    private SecurityProperties securityProperties;

    /**
     * 认证失败处理类 Bean
     */
    @Resource
    private AuthenticationEntryPoint authenticationEntryPoint;
    /**
     * 权限不够处理器 Bean
     */
    @Resource
    private AccessDeniedHandler accessDeniedHandler;
    /**
     * Token 认证过滤器 Bean
     */
    @Resource
    private TokenAuthenticationFilter authenticationTokenFilter;

    /**
     * 自定义的权限映射 Bean 们
     *
     * @see #configure(HttpSecurity)
     */
    @Resource
    private List<AuthorizeRequestsCustomizer> authorizeRequestsCustomizers;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 由于 Spring Security 创建 AuthenticationManager 对象时，没声明 @Bean 注解，导致无法被注入
     * 通过覆写父类的该方法，添加 @Bean 注解，解决该问题
     */
    @Override
    @Bean
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 配置 URL 的安全配置
     * <p>
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
        // 登出
        httpSecurity
                // 开启跨域
                .cors().and()
                // CSRF 禁用，因为不使用 Session
                .csrf().disable()
                // 基于 token 机制，所以不需要 Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .headers().frameOptions().disable().and()
                // 一堆自定义的 Spring Security 处理器
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);
        // 登录、登录暂时不使用 Spring Security 的拓展点，主要考虑一方面拓展多用户、多种登录方式相对复杂，一方面用户的学习成本较高

        // 获得 @PermitAll 带来的 URL 列表，免登录
        Multimap<HttpMethod, String> permitAllUrls = getPermitAllUrlsFromAnnotations();
        // 设置每个请求的权限
        httpSecurity
                // ①：全局共享规则
                .authorizeRequests()
                // 1.1 静态资源，可匿名访问
                .antMatchers(HttpMethod.GET, "/*.html", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()
                // 1.2 设置 @PermitAll 无需认证
                .antMatchers(HttpMethod.GET, permitAllUrls.get(HttpMethod.GET).toArray(new String[0])).permitAll()
                .antMatchers(HttpMethod.POST, permitAllUrls.get(HttpMethod.POST).toArray(new String[0])).permitAll()
                .antMatchers(HttpMethod.PUT, permitAllUrls.get(HttpMethod.PUT).toArray(new String[0])).permitAll()
                .antMatchers(HttpMethod.DELETE, permitAllUrls.get(HttpMethod.DELETE).toArray(new String[0])).permitAll()
                // 1.3 基于 yudao.security.permit-all-urls 无需认证
                .antMatchers(securityProperties.getPermitAllUrls().toArray(new String[0])).permitAll()
                // 1.4 设置 App API 无需认证
                .antMatchers(buildAppApi("/**")).permitAll()
                // 1.5 验证码captcha 允许匿名访问
                .antMatchers("/captcha/get", "/captcha/check").permitAll()
                // ②：每个项目的自定义规则
                .and().authorizeRequests(registry -> // 下面，循环设置自定义规则
                        authorizeRequestsCustomizers.forEach(customizer -> customizer.customize(registry)))
                // ③：兜底规则，必须认证
                .authorizeRequests()
                .anyRequest().authenticated()
        ;

        // 添加 Token Filter
        httpSecurity.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

    private String buildAppApi(String url) {
        return webProperties.getAppApi().getPrefix() + url;
    }

    private Multimap<HttpMethod, String> getPermitAllUrlsFromAnnotations() {
        Multimap<HttpMethod, String> result = HashMultimap.create();
        // 获得接口对应的 HandlerMethod 集合
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)
                applicationContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        // 获得有 @PermitAll 注解的接口
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!handlerMethod.hasMethodAnnotation(PermitAll.class)) {
                continue;
            }
            if (entry.getKey().getPatternsCondition() == null) {
                continue;
            }
            Set<String> urls = entry.getKey().getPatternsCondition().getPatterns();
            // 根据请求方法，添加到 result 结果
            entry.getKey().getMethodsCondition().getMethods().forEach(requestMethod -> {
                switch (requestMethod) {
                    case GET:
                        result.putAll(HttpMethod.GET, urls);
                        break;
                    case POST:
                        result.putAll(HttpMethod.POST, urls);
                        break;
                    case PUT:
                        result.putAll(HttpMethod.PUT, urls);
                        break;
                    case DELETE:
                        result.putAll(HttpMethod.DELETE, urls);
                        break;
                }
            });
        }
        return result;
    }

}
