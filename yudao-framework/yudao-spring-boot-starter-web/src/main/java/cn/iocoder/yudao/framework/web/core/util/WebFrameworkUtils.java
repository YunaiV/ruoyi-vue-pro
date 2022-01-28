package cn.iocoder.yudao.framework.web.core.util;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * 专属于 web 包的工具类
 *
 * @author 芋道源码
 */
public class WebFrameworkUtils {

    private static final String REQUEST_ATTRIBUTE_LOGIN_USER_ID = "login_user_id";
    private static final String REQUEST_ATTRIBUTE_LOGIN_USER_TYPE = "login_user_type";

    private static final String REQUEST_ATTRIBUTE_COMMON_RESULT = "common_result";

    public static void setLoginUserId(ServletRequest request, Long userId) {
        request.setAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_ID, userId);
    }

    public static void setLoginUserType(ServletRequest request, Integer userType) {
        request.setAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_TYPE, userType);
    }

    /**
     * 获得当前用户的编号，从请求中
     *
     * @param request 请求
     * @return 用户编号
     */
    public static Long getLoginUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (Long) request.getAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_ID);
    }

    /**
     * 获得当前用户的类型，从请求中
     *
     * @param request 请求
     * @return 用户编号
     */
    public static Integer getLoginUserType(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (Integer) request.getAttribute(REQUEST_ATTRIBUTE_LOGIN_USER_TYPE);
    }

    public static Integer getLoginUserType() {
        HttpServletRequest request = getRequest();
        return getLoginUserType(request);
    }

    public static Long getLoginUserId() {
        HttpServletRequest request = getRequest();
        return getLoginUserId(request);
    }

    public static void setCommonResult(ServletRequest request, CommonResult<?> result) {
        request.setAttribute(REQUEST_ATTRIBUTE_COMMON_RESULT, result);
    }

    public static CommonResult<?> getCommonResult(ServletRequest request) {
        return (CommonResult<?>) request.getAttribute(REQUEST_ATTRIBUTE_COMMON_RESULT);
    }

    private static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        return servletRequestAttributes.getRequest();
    }

}
