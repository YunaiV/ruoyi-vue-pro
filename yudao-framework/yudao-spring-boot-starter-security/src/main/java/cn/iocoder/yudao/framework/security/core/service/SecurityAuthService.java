package cn.iocoder.yudao.framework.security.core.service;

import cn.iocoder.yudao.framework.security.core.LoginUser;

import javax.servlet.http.HttpServletRequest;

/**
 * 安全认证的 Service 接口，对 security 组件提供统一的 Auth 相关的方法。
 * 主要是会基于 {@link HttpServletRequest} 参数，匹配对应的 {@link SecurityAuthFrameworkService} 实现，然后调用其方法。
 * 因此，在方法的定义上，和 {@link SecurityAuthFrameworkService} 差不多。
 *
 * @author 芋道源码
 */
public interface SecurityAuthService {

    /**
     * 校验 token 的有效性，并获取用户信息
     * 通过后，刷新 token 的过期时间
     *
     * @param request 请求
     * @param token token
     * @return 用户信息
     */
    LoginUser verifyTokenAndRefresh(HttpServletRequest request, String token);

    /**
     * 模拟指定用户编号的 LoginUser
     *
     * @param request 请求
     * @param userId 用户编号
     * @return 登录用户
     */
    LoginUser mockLogin(HttpServletRequest request, Long userId);

    /**
     * 基于 token 退出登录
     *
     * @param request 请求
     * @param token token
     */
    void logout(HttpServletRequest request, String token);

}
