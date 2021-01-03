package cn.iocoder.dashboard.modules.system.service.auth;

import cn.iocoder.dashboard.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.SysAuthGetInfoRespVO;

import java.util.Set;

/**
 * 认证 Service 接口
 *
 * 提供用户的账号密码登陆、token 的校验等认证相关的功能
 *
 * @author 芋道源码
 */
public interface SysAuthService extends SecurityFrameworkService {

    String login(String username, String password, String captchaUUID, String captchaCode);

    /**
     * 获得用户的基本信息
     *
     * 这里传输 roleIds 参数的原因，是该参数是从 LoginUser 缓存中获取到的，而我们校验权限时也是从 LoginUser 缓存中获取 roleIds
     * 通过这样的方式，保持一致
     *
     * @param userId 用户编号
     * @param roleIds 用户拥有的角色编号数组
     * @return 用户的信息，包括角色权限和菜单权限
     */
    SysAuthGetInfoRespVO getInfo(Long userId, Set<Long> roleIds);

}
