package cn.iocoder.dashboard.framework.security.core.util;

import cn.iocoder.dashboard.framework.security.core.LoginUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 安全服务工具类
 *
 * @author ruoyi
 */
public class SecurityUtils {

    /**
     * 从请求中，获得认证 Token
     *
     * @param request 请求
     * @param header 认证 Token 对应的 Header 名字
     * @return 认证 Token
     */
    public static String obtainAuthorization(HttpServletRequest request, String header) {
        String authorization = request.getHeader(header);
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        int index = authorization.indexOf("Bearer ");
        if (index == -1) { // 未找到
            return null;
        }
        return authorization.substring(index + 7).trim();
    }

    /**
     * 获取当前用户
     */
    public static LoginUser getLoginUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 设置当前用户
     *
     * @param loginUser 登陆用户
     * @param request 请求
     */
    public static void setLoginUser(LoginUser loginUser, HttpServletRequest request) {
        // 创建 UsernamePasswordAuthenticationToken 对象
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginUser, null, null);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // 设置到上下文
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

}
