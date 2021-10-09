package cn.iocoder.yudao.userserver.modules.system.service.auth.impl;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.userserver.modules.system.service.auth.SysUserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 在线用户 Session Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysUserSessionServiceImpl implements SysUserSessionService {

    @Override
    public String createUserSession(LoginUser loginUser, String userIp, String userAgent) {
        return null;
    }

    @Override
    public void refreshUserSession(String sessionId, LoginUser loginUser) {

    }

    @Override
    public void deleteUserSession(String sessionId) {

    }

    @Override
    public LoginUser getLoginUser(String sessionId) {
        return null;
    }

    @Override
    public String getSessionId(String username) {
        return null;
    }

    @Override
    public Long getSessionTimeoutMillis() {
        return null;
    }

}
