package cn.iocoder.dashboard.modules.system.service.auth;

import cn.iocoder.dashboard.framework.security.core.service.SecurityFrameworkService;

/**
 * 认证 Service 接口
 *
 * 提供用户的账号密码登陆、token 的校验等认证相关的功能
 *
 * @author 芋道源码
 */
public interface SysAuthService extends SecurityFrameworkService {

    String login(String username, String password, String captchaUUID, String captchaCode);

}
