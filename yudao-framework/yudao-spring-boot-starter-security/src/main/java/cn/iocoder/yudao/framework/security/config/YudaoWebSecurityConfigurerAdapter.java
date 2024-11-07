package cn.iocoder.yudao.framework.security.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.security.core.filter.TokenAuthenticationFilter;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 自定义的 Spring Security 配置适配器实现
 *
 * @author 芋道源码
 */
@AutoConfiguration
@AutoConfigureOrder(-1) // 目的：先于 Spring Security 自动配置，避免一键改包后，org.* 基础包无法生效
@EnableMethodSecurity(securedEnabled = true)
public class YudaoWebSecurityConfigurerAdapter {

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
     * @see #filterChain(HttpSecurity)
     */
    @Resource
    private List<AuthorizeRequestsCustomizer> authorizeRequestsCustomizers;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 由于 Spring Security 创建 AuthenticationManager 对象时，没声明 @Bean 注解，导致无法被注入
     * 通过覆写父类的该方法，添加 @Bean 注解，解决该问题
     */
    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
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
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // 登出
        httpSecurity
                // 开启跨域
                .cors(Customizer.withDefaults())
                // CSRF 禁用，因为不使用 Session
                .csrf(AbstractHttpConfigurer::disable)
                // 基于 token 机制，所以不需要 Session
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(c -> c.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // 一堆自定义的 Spring Security 处理器
                .exceptionHandling(c -> c.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        // 登录、登录暂时不使用 Spring Security 的拓展点，主要考虑一方面拓展多用户、多种登录方式相对复杂，一方面用户的学习成本较高

        // 获得 @PermitAll 带来的 URL 列表，免登录
        Multimap<HttpMethod, String> permitAllUrls = getPermitAllUrlsFromAnnotations();
        // 设置每个请求的权限
        httpSecurity
                // ①：全局共享规则
                .authorizeHttpRequests(c -> c
                    // 1.1 静态资源，可匿名访问
                    .requestMatchers(HttpMethod.GET, "/*.html", "/*.html", "/*.css", "/*.js").permitAll()
                    // 1.2 设置 @PermitAll 无需认证
                    .requestMatchers(HttpMethod.GET, permitAllUrls.get(HttpMethod.GET).toArray(new String[0])).permitAll()
                    .requestMatchers(HttpMethod.POST, permitAllUrls.get(HttpMethod.POST).toArray(new String[0])).permitAll()
                    .requestMatchers(HttpMethod.PUT, permitAllUrls.get(HttpMethod.PUT).toArray(new String[0])).permitAll()
                    .requestMatchers(HttpMethod.DELETE, permitAllUrls.get(HttpMethod.DELETE).toArray(new String[0])).permitAll()
                    .requestMatchers(HttpMethod.HEAD, permitAllUrls.get(HttpMethod.HEAD).toArray(new String[0])).permitAll()
                    .requestMatchers(HttpMethod.PATCH, permitAllUrls.get(HttpMethod.PATCH).toArray(new String[0])).permitAll()
                    // 1.3 基于 yudao.security.permit-all-urls 无需认证
                    .requestMatchers(securityProperties.getPermitAllUrls().toArray(new String[0])).permitAll()
                )
                // ②：每个项目的自定义规则
                .authorizeHttpRequests(c -> authorizeRequestsCustomizers.forEach(customizer -> customizer.customize(c)))
                // ③：兜底规则，必须认证
                .authorizeHttpRequests(c -> c.anyRequest().authenticated());

        // 添加 Token Filter
        httpSecurity.addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
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
            Set<String> urls = new HashSet<>();
            if (entry.getKey().getPatternsCondition() != null) {
                urls.addAll(entry.getKey().getPatternsCondition().getPatterns());
            }
            if (entry.getKey().getPathPatternsCondition() != null) {
                urls.addAll(convertList(entry.getKey().getPathPatternsCondition().getPatterns(), PathPattern::getPatternString));
            }
            if (urls.isEmpty()) {
                continue;
            }

            // 特殊：使用 @RequestMapping 注解，并且未写 method 属性，此时认为都需要免登录
            Set<RequestMethod> methods = entry.getKey().getMethodsCondition().getMethods();
            if (CollUtil.isEmpty(methods)) {
                result.putAll(HttpMethod.GET, urls);
                result.putAll(HttpMethod.POST, urls);
                result.putAll(HttpMethod.PUT, urls);
                result.putAll(HttpMethod.DELETE, urls);
                result.putAll(HttpMethod.HEAD, urls);
                result.putAll(HttpMethod.PATCH, urls);
                continue;
            }
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
                    case HEAD:
                        result.putAll(HttpMethod.HEAD, urls);
                        break;
                    case PATCH:
                        result.putAll(HttpMethod.PATCH, urls);
                        break;
                }
            });
        }
        return result;
    }

}
