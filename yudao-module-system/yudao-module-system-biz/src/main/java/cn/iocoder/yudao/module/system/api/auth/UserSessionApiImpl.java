package cn.iocoder.yudao.module.system.api.auth;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.system.service.auth.UserSessionService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 在线用户 Session API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class UserSessionApiImpl implements UserSessionApi {

    @Resource
    private UserSessionService userSessionService;

    @Override
    public String createUserSession(LoginUser loginUser, String userIp, String userAgent) {
        return userSessionService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    public void refreshUserSession(String token, LoginUser loginUser) {
        userSessionService.refreshUserSession(token, loginUser);
    }

    @Override
    public void deleteUserSession(String token) {
        userSessionService.deleteUserSession(token);
    }

    @Override
    public LoginUser getLoginUser(String token) {
        return userSessionService.getLoginUser(token);
    }

    @Override
    public Long getSessionTimeoutMillis() {
        return userSessionService.getSessionTimeoutMillis();
    }

}
