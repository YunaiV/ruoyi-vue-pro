package cn.iocoder.yudao.framework.security.core.authentication;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.service.SecurityAuthFrameworkService;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支持多用户类型的 AuthenticationProvider 实现类
 *
 * 为什么不用 {@link org.springframework.security.authentication.ProviderManager} 呢？
 * 原因是，需要每个用户类型实现对应的 {@link AuthenticationProvider} + authentication，略显麻烦。实际，也是可以实现的。
 *
 * 另外，额外支持 verifyTokenAndRefresh 校验令牌、logout 登出、mockLogin 模拟登陆等操作。
 * 实际上，它就是 {@link SecurityAuthFrameworkService} 定义的三个接口。
 * 因为需要支持多种类型，所以需要根据请求的 URL，判断出对应的用户类型，从而使用对应的 SecurityAuthFrameworkService 是吸纳
 *
 * @see cn.iocoder.yudao.framework.common.enums.UserTypeEnum
 * @author 芋道源码
 */
public class MultiUserDetailsAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final Map<UserTypeEnum, SecurityAuthFrameworkService> services = new HashMap<>();

    private final WebProperties properties;

    private final PasswordEncoder passwordEncoder;

    public MultiUserDetailsAuthenticationProvider(List<SecurityAuthFrameworkService> serviceList,
                                                  WebProperties properties, PasswordEncoder passwordEncoder) {
        serviceList.forEach(service -> services.put(service.getUserType(), service));
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    // ========== AuthenticationProvider 相关 ==========

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        // 执行用户的加载
        return selectService(authentication).loadUserByUsername(username);
    }

    private SecurityAuthFrameworkService selectService(UsernamePasswordAuthenticationToken authentication) {
        // 第一步，获得用户类型
        UserTypeEnum userType = getUserType(authentication);
        // 第二步，获得 SecurityAuthFrameworkService
        SecurityAuthFrameworkService service = services.get(userType);
        Assert.notNull(service, "用户类型({}) 找不到 SecurityAuthFrameworkService 实现类", userType);
        return service;
    }

    private UserTypeEnum getUserType(UsernamePasswordAuthenticationToken authentication) {
        Assert.isInstanceOf(MultiUsernamePasswordAuthenticationToken.class, authentication);
        MultiUsernamePasswordAuthenticationToken multiAuthentication = (MultiUsernamePasswordAuthenticationToken) authentication;
        UserTypeEnum userType = multiAuthentication.getUserType();
        Assert.notNull(userType, "用户类型不能为空");
        return userType;
    }

    @Override // copy 自 DaoAuthenticationProvider 的 additionalAuthenticationChecks 方法
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        // 校验 credentials
        if (authentication.getCredentials() == null) {
            this.logger.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException(this.messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
        // 校验 password
        String presentedPassword = authentication.getCredentials().toString();
        if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            this.logger.debug("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException(this.messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    // ========== SecurityAuthFrameworkService 相关 ==========

    /**
     * 校验 token 的有效性，并获取用户信息
     * 通过后，刷新 token 的过期时间
     *
     * @param request 请求
     * @param token token
     * @return 用户信息
     */
    public LoginUser verifyTokenAndRefresh(HttpServletRequest request, String token) {
        return selectService(request).verifyTokenAndRefresh(token);
    }

    /**
     * 模拟指定用户编号的 LoginUser
     *
     * @param request 请求
     * @param userId 用户编号
     * @return 登录用户
     */
    public LoginUser mockLogin(HttpServletRequest request, Long userId) {
        return selectService(request).mockLogin(userId);
    }

    /**
     * 基于 token 退出登录
     *
     * @param request 请求
     * @param token token
     */
    public void logout(HttpServletRequest request, String token) {
        selectService(request).logout(token);
    }

    private SecurityAuthFrameworkService selectService(HttpServletRequest request) {
        // 第一步，获得用户类型
        UserTypeEnum userType = getUserType(request);
        // 第二步，获得 SecurityAuthFrameworkService
        SecurityAuthFrameworkService service = services.get(userType);
        Assert.notNull(service, "URI({}) 用户类型({}) 找不到 SecurityAuthFrameworkService 实现类",
                request.getRequestURI(), userType);
        return service;
    }

    private UserTypeEnum getUserType(HttpServletRequest request) {
        if (request.getRequestURI().startsWith(properties.getAdminApi().getPrefix())) {
            return UserTypeEnum.ADMIN;
        }
        if (request.getRequestURI().startsWith(properties.getAppApi().getPrefix())) {
            return UserTypeEnum.MEMBER;
        }
        throw new IllegalArgumentException(StrUtil.format("URI({}) 找不到匹配的用户类型", request.getRequestURI()));
    }

}
