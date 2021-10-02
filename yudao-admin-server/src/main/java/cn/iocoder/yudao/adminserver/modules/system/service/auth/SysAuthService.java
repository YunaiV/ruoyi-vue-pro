package cn.iocoder.yudao.adminserver.modules.system.service.auth;

import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthThirdLoginReqVO;
import cn.iocoder.yudao.framework.security.core.service.SecurityAuthFrameworkService;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthLoginReqVO;

/**
 * 认证 Service 接口
 *
 * 提供用户的账号密码登陆、token 的校验等认证相关的功能
 *
 * @author 芋道源码
 */
public interface SysAuthService extends SecurityAuthFrameworkService {

    /**
     * 登陆用户
     *
     * @param reqVO 登陆信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String login(SysAuthLoginReqVO reqVO, String userIp, String userAgent);

    /**
     * 三方登陆用户，使用 code 授权码
     *
     * @param reqVO 登陆信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String thirdLogin(SysAuthThirdLoginReqVO reqVO, String userIp, String userAgent);

}
