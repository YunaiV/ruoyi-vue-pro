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
     * 短信验证码发送
     * @param userId
     * @param reqVO
     */
    public void sendSmsCode(Long userId, AuthSmsSendReqVO reqVO);

    /**
     * 短信登录
     *
     * @param reqVO 登录信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String smsLogin(AuthSmsLoginReqVO reqVO, String userIp, String userAgent) ;

    /**
     * 社交登录，使用 code 授权码
     *
     * @param reqVO 登录信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String socialLogin(@Valid AuthSocialLoginReqVO reqVO, String userIp, String userAgent);

    /**
     * 社交登录，使用 code 授权码 + 账号密码
     *
     * @param reqVO 登录信息
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return 身份令牌，使用 JWT 方式
     */
    String socialLogin2(@Valid AuthSocialLogin2ReqVO reqVO, String userIp, String userAgent);

    /**
     * 社交绑定，使用 code 授权码
     *
     * @param userId 用户编号
     * @param reqVO 绑定信息
     */
    void socialBind(Long userId, @Valid AuthSocialBindReqVO reqVO);

}
