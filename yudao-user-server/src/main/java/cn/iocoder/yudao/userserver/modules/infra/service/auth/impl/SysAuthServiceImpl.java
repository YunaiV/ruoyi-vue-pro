package cn.iocoder.yudao.userserver.modules.infra.service.auth.impl;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.userserver.modules.infra.service.auth.SysAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Auth Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysAuthServiceImpl implements SysAuthService {

    @Override
    public LoginUser verifyTokenAndRefresh(String token) {
        return null;
    }

    @Override
    public LoginUser mockLogin(Long userId) {
        return null;
    }

    @Override
    public void logout(String token) {

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
