package cn.iocoder.yudao.framework.tenant.core.security;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 多租户 Security Web 过滤器
 * 校验用户访问的租户，是否是其所在的租户，避免越权问题
 *
 * @author 芋道源码
 */
@Slf4j
public class TenantSecurityWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        LoginUser user = SecurityFrameworkUtils.getLoginUser();
        assert user != null; // shouldNotFilter 已经校验
        // 校验租户是否匹配。
        if (!Objects.equals(user.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("[doFilterInternal][租户({}) User({}/{}) 越权访问租户({}) URL({}/{})]",
                    user.getTenantId(), user.getId(), user.getUserType(),
                    TenantContextHolder.getTenantId(), request.getRequestURI(), request.getMethod());
            ServletUtils.writeJSON(response, CommonResult.error(GlobalErrorCodeConstants.FORBIDDEN.getCode(),
                    "您无权访问该租户的数据"));
            return;
        }
        // 继续过滤
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityFrameworkUtils.getLoginUser() == null;
    }

}
