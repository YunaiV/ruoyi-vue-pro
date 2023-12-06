package cn.iocoder.yudao.module.report.framework.ureport.core;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.system.api.oauth2.OAuth2TokenApi;
import cn.iocoder.yudao.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * UReport 认证过滤器
 * @author 赤焰
 */
@Slf4j
@RequiredArgsConstructor
public class UReportFilter extends OncePerRequestFilter {

    private final static String TOKEN = "token";

    private final OAuth2TokenApi oauth2TokenApi;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if(log.isDebugEnabled()){
            log.debug("UReportFilter自定义过滤器");
        }

        Map<String, String> paramMap = ServletUtils.getParamMap(request);
        String requestURI = request.getRequestURI();
        boolean contains = requestURI.contains("/ureport");
        if (paramMap.containsKey(TOKEN)&&contains) {
           String token = request.getParameter(TOKEN);
            if(log.isDebugEnabled()){
                log.debug("UReportFilter自定义过滤器 token="+token);
            }

            response.addHeader(TOKEN,token);

            TenantContextHolder.setIgnore(true); // 忽略租户，保证可查询到 token 信息
            LoginUser user = null;
            try {
                OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token);
                if (accessToken != null) {
                    user = new LoginUser().setId(accessToken.getUserId()).setUserType(accessToken.getUserType())
                            .setTenantId(accessToken.getTenantId()).setScopes(accessToken.getScopes());
                    if (user != null) {
                        SecurityFrameworkUtils.setLoginUser(user, WebFrameworkUtils.getRequest());

                        // ② 参考 TenantContextWebFilter 实现（Tenant 的上下文清理，交给 TenantContextWebFilter 完成）
                        // 目的：基于 LoginUser 获得到的租户编号，设置到 Tenant 上下文，避免查询数据库时的报错
                        TenantContextHolder.setIgnore(false);
                        TenantContextHolder.setTenantId(user.getTenantId());
                    }
                }
            } catch (ServiceException ignored) {
                chain.doFilter(request, response);
            }

        }

        // 继续过滤
        chain.doFilter(request, response);

    }
}
