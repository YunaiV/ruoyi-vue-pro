package cn.iocoder.yudao.userserver.modules.system.service.auth;

import cn.iocoder.yudao.framework.security.core.service.SecurityAuthFrameworkService;
import cn.iocoder.yudao.userserver.modules.system.controller.auth.vo.SysAuthLoginReqVO;

import javax.validation.Valid;

/**
 * 用户前台的认证 Service 接口
 *
 * 提供用户的账号密码登录、token 的校验等认证相关的功能
 *
 * @author 芋道源码
 */
public interface SysAuthService extends SecurityAuthFrameworkService {

    /**
     * 手机 + 密码登录
     *
     * @param reqVO 登录信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String login(@Valid SysAuthLoginReqVO reqVO, String userIp, String userAgent);

}
