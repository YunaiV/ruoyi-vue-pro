package cn.iocoder.yudao.framework.security.core.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.web.config.WebProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全认证的 Service 实现类，基于请求地址，计算到对应的 {@link UserTypeEnum} 枚举，从而拿到对应的 {@link SecurityAuthFrameworkService} 实现
 *
 * @author 芋道源码
 */
public class SecurityAuthServiceImpl implements SecurityAuthService {

    private final Map<UserTypeEnum, SecurityAuthFrameworkService> services = new HashMap<>();
    private final WebProperties properties;

    public SecurityAuthServiceImpl(List<SecurityAuthFrameworkService> serviceList, WebProperties properties) {
        serviceList.forEach(service -> services.put(service.getUserType(), service));
        this.properties = properties;
    }

    @Override
    public LoginUser verifyTokenAndRefresh(HttpServletRequest request, String token) {
        return selectService(request).verifyTokenAndRefresh(token);
    }

    @Override
    public LoginUser mockLogin(HttpServletRequest request, Long userId) {
        return selectService(request).mockLogin(userId);
    }

    @Override
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
