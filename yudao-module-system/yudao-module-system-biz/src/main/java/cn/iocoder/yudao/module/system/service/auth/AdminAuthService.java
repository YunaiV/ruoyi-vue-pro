package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.module.system.controller.admin.auth.vo.auth.*;
import cn.iocoder.yudao.framework.security.core.service.SecurityAuthFrameworkService;

import javax.validation.Valid;

/**
 * 管理后台的认证 Service 接口
 *
 * 提供用户的账号密码登录、token 的校验等认证相关的功能
 *
 * @author 芋道源码
 */
public interface AdminAuthService extends SecurityAuthFrameworkService {

    /**
     * 账号登录
     *
     * @param reqVO 登录信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String login(@Valid AuthLoginReqVO reqVO, String userIp, String userAgent);

    /**
     * 社交快捷登录，使用 code 授权码
     *
     * @param reqVO 登录信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String socialLogin(@Valid AuthSocialQuickLoginReqVO reqVO, String userIp, String userAgent);

    /**
     * 社交绑定登录，使用 code 授权码 + 账号密码
     *
     * @param reqVO 登录信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String socialBindLogin(@Valid AuthSocialBindLoginReqVO reqVO, String userIp, String userAgent);

}
