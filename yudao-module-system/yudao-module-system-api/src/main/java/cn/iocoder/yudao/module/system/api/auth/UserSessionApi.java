package cn.iocoder.yudao.module.system.api.auth;

import cn.iocoder.yudao.framework.security.core.LoginUser;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 在线用户 Session API 接口
 *
 * @author 芋道源码
 */
public interface UserSessionApi {

    /**
     * 创建在线用户 Session
     *
     * @param loginUser 登录用户
     * @param userIp 用户 IP
     * @param userAgent 用户 UA
     * @return Token 令牌
     */
    String createUserSession(@NotNull(message = "登录用户不能为空") LoginUser loginUser, String userIp, String userAgent);

    /**
     * 刷新在线用户 Session 的更新时间
     *
     * @param token Token 令牌
     * @param loginUser 登录用户
     */
    void refreshUserSession(@NotEmpty(message = "Token 令牌不能为空") String token,
                            @NotNull(message = "登录用户不能为空") LoginUser loginUser);

    /**
     * 删除在线用户 Session
     *
     * @param token Token 令牌
     */
    void deleteUserSession(String token);

    /**
     * 获得 Token 令牌对应的在线用户
     *
     * @param token Token 令牌
     * @return 在线用户
     */
    LoginUser getLoginUser(String token);

    /**
     * 获得 Session 超时时间，单位：毫秒
     *
     * @return 超时时间
     */
    Long getSessionTimeoutMillis();

}
