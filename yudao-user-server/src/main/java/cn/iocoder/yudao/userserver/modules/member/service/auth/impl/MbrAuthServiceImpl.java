package cn.iocoder.yudao.userserver.modules.member.service.auth.impl;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.userserver.modules.member.controller.auth.vo.MbrAuthLoginReqVO;
import cn.iocoder.yudao.userserver.modules.member.convert.user.MbrAuthConvert;
import cn.iocoder.yudao.userserver.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.userserver.modules.member.service.auth.MbrAuthService;
import cn.iocoder.yudao.userserver.modules.member.service.user.MbrUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Auth Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class MbrAuthServiceImpl implements MbrAuthService {

    @Resource
    @Lazy // 延迟加载，因为存在相互依赖的问题
    private AuthenticationManager authenticationManager;

    @Resource
    private MbrUserService userService;

    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
        // 获取 username 对应的 SysUserDO
        MbrUserDO user = userService.getUserByMobile(mobile);
        if (user == null) {
            throw new UsernameNotFoundException(mobile);
        }
        // 创建 LoginUser 对象
        return MbrAuthConvert.INSTANCE.convert(user);
    }

    @Override
    public String login(MbrAuthLoginReqVO reqVO, String userIp, String userAgent) {
        return null;
    }

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

}
