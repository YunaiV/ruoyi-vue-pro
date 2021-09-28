/*
 * MIT License
 * Copyright (c) 2020-2029 YongWu zheng (dcenter.top and gitee.com/pcore and github.com/ZeroOrInfinity)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cn.iocoder.yudao.adminserver.modules.system.convert.auth.handler;

import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthLoginRespVO;
import cn.iocoder.yudao.adminserver.modules.system.service.auth.SysUserSessionService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysPermissionService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.security.core.Auth2LoginUser;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.handler.AbstractSignUpUrlAuthenticationSuccessHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getUserAgent;
import static java.util.Collections.singleton;
import static top.dcenter.ums.security.core.oauth.util.MvcUtil.*;

/**
 * @author weir
 */
public class DefaultSignUpUrlAuthenticationSuccessHandler extends AbstractSignUpUrlAuthenticationSuccessHandler {
    private RequestCache requestCache = new HttpSessionRequestCache();
    @Autowired
    private SysUserSessionService userSessionService;
    @Resource
    private SysPermissionService permissionService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        final Object principal = authentication.getPrincipal();
        String token = userSessionService.createUserSession(defaultHandleUserRoles((LoginUser) principal), getClientIP(), getUserAgent());
        if(StringUtils.isNotBlank(token)) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        if (principal instanceof Auth2LoginUser) {
            new DefaultRedirectStrategy().sendRedirect(request, response, getUrl() + token);
            return;
        }
        if (isAjaxOrJson(request)) {
            responseWithJson(response, HttpStatus.OK.value(), toJsonString(success(SysAuthLoginRespVO.builder().token(token).build())));
            return;
        }
        try {
            requestCache.saveRequest(request, response);
            super.setRequestCache(requestCache);
            super.onAuthenticationSuccess(request, response, authentication);
        } catch (ServletException e) {
            e.printStackTrace();
        }

    }

    private String getUrl() {
//        return "http://localhost/oauthLogin/gitee?token=";
        return "http://127.0.0.1:1024/oauthLogin/gitee?token=";
    }

    /**
     * 默认处理方式处理用户角色列表；建议角色权限前置到 UserDetails
     *
     * @param loginUser 用户
     * @return
     */
    private LoginUser defaultHandleUserRoles(LoginUser loginUser) {
        Set<Long> roleIds = loginUser.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            Set<Long> userRoleIds = permissionService.getUserRoleIds(loginUser.getId(), singleton(CommonStatusEnum.ENABLE.getStatus()));
            loginUser.setRoleIds(userRoleIds);
        }
        return loginUser;
    }

    @Override
    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}
