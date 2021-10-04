package cn.iocoder.yudao.adminserver.modules.system.service.auth.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthSocialLogin2ReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthSocialLoginReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.user.SysSocialUserDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.user.SysSocialUserMapper;
import cn.iocoder.yudao.adminserver.modules.system.dal.redis.auth.SysAuthSocialUserRedisDAO;
import cn.iocoder.yudao.adminserver.modules.system.enums.user.SysSocialTypeEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthLoginReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.convert.auth.SysAuthConvert;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.adminserver.modules.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.enums.logger.SysLoginResultEnum;
import cn.iocoder.yudao.adminserver.modules.system.service.auth.SysAuthService;
import cn.iocoder.yudao.adminserver.modules.system.service.auth.SysUserSessionService;
import cn.iocoder.yudao.adminserver.modules.system.service.common.SysCaptchaService;
import cn.iocoder.yudao.adminserver.modules.system.service.logger.SysLoginLogService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysPermissionService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import com.xkcoding.justauth.AuthRequestFactory;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static java.util.Collections.singleton;

/**
 * Auth Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysAuthServiceImpl implements SysAuthService {

    @Resource
    @Lazy // 延迟加载，因为存在相互依赖的问题
    private AuthenticationManager authenticationManager;

    @Resource
    private SysUserService userService;
    @Resource
    private SysPermissionService permissionService;
    @Resource
    private SysCaptchaService captchaService;
    @Resource
    private SysLoginLogService loginLogService;
    @Resource
    private SysUserSessionService userSessionService;

    @Resource
    private SysAuthSocialUserRedisDAO authSocialUserRedisDAO;
    @Resource
    private SysSocialUserMapper socialUserMapper;

    @Resource
    private AuthRequestFactory authRequestFactory;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 获取 username 对应的 SysUserDO
        SysUserDO user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // 创建 LoginUser 对象
        return SysAuthConvert.INSTANCE.convert(user);
    }

    @Override
    public LoginUser mockLogin(Long userId) {
        // 获取用户编号对应的 SysUserDO
        SysUserDO user = userService.getUser(userId);
        if (user == null) {
            throw new UsernameNotFoundException(String.valueOf(userId));
        }
        // 创建 LoginUser 对象
        LoginUser loginUser = SysAuthConvert.INSTANCE.convert(user);
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getId())); // 获取用户角色列表
        return loginUser;
    }

    @Override
    public String login(SysAuthLoginReqVO reqVO, String userIp, String userAgent) {
        // 判断验证码是否正确
        this.verifyCaptcha(reqVO.getUsername(), reqVO.getUuid(), reqVO.getCode());

        // 使用账号密码，进行登陆。
        LoginUser loginUser = this.login0(reqVO.getUsername(), reqVO.getPassword());
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getId())); // 获取用户角色列表

        // 缓存登陆用户到 Redis 中，返回 sessionId 编号
        return userSessionService.createUserSession(loginUser, userIp, userAgent);
    }

    private void verifyCaptcha(String username, String captchaUUID, String captchaCode) {
        String code = captchaService.getCaptchaCode(captchaUUID);
        // 验证码不存在
        if (code == null) {
            // 创建登陆失败日志（验证码不存在）
            this.createLoginLog(username, SysLoginResultEnum.CAPTCHA_NOT_FOUND);
            throw ServiceExceptionUtil.exception(AUTH_LOGIN_CAPTCHA_NOT_FOUND);
        }
        // 验证码不正确
        if (!code.equals(captchaCode)) {
            // 创建登陆失败日志（验证码不正确)
            this.createLoginLog(username, SysLoginResultEnum.CAPTCHA_CODE_ERROR);
            throw ServiceExceptionUtil.exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR);
        }
        // 正确，所以要删除下验证码
        captchaService.deleteCaptchaCode(captchaUUID);
    }

    private LoginUser login0(String username, String password) {
        // 用户验证
        Authentication authentication;
        try {
            // 调用 Spring Security 的 AuthenticationManager#authenticate(...) 方法，使用账号密码进行认证
            // 在其内部，会调用到 loadUserByUsername 方法，获取 User 信息
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException badCredentialsException) {
            this.createLoginLog(username, SysLoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        } catch (DisabledException disabledException) {
            this.createLoginLog(username, SysLoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        } catch (AuthenticationException authenticationException) {
            log.error("[login0][username({}) 发生未知异常]", username, authenticationException);
            this.createLoginLog(username, SysLoginResultEnum.UNKNOWN_ERROR);
            throw exception(AUTH_LOGIN_FAIL_UNKNOWN);
        }
        // 登陆成功
        Assert.notNull(authentication.getPrincipal(), "Principal 不会为空");
        this.createLoginLog(username, SysLoginResultEnum.SUCCESS);
        return (LoginUser) authentication.getPrincipal();
    }

    private void createLoginLog(String username, SysLoginResultEnum loginResult) {
        SysLoginLogCreateReqVO reqVO = new SysLoginLogCreateReqVO();
        reqVO.setLogType(SysLoginLogTypeEnum.LOGIN_USERNAME.getType());
        reqVO.setTraceId(TracerUtils.getTraceId());
        reqVO.setUsername(username);
        reqVO.setUserAgent(ServletUtils.getUserAgent());
        reqVO.setUserIp(ServletUtils.getClientIP());
        reqVO.setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqVO);
    }

    /**
     * 获得 User 拥有的角色编号数组
     *
     * @param userId 用户编号
     * @return 角色编号数组
     */
    private Set<Long> getUserRoleIds(Long userId) {
        return permissionService.getUserRoleIds(userId, singleton(CommonStatusEnum.ENABLE.getStatus()));
    }

    @Override
    public String socialLogin(SysAuthSocialLoginReqVO reqVO, String userIp, String userAgent) {
        // 使用 code 授权码，进行登陆
        AuthCallback authCallback = SysAuthConvert.INSTANCE.convert(reqVO);
        AuthUser authUser = this.obtainAuthUserFromCache(reqVO.getType(), authCallback);

        // 如果未绑定 SysSocialUserDO 用户，则无法自动登陆，进行报错
        String unionId = getAuthUserUnionId(authUser);
        List<SysSocialUserDO> socialUsers = socialUserMapper.selectListByTypeAndUnionId(UserTypeEnum.ADMIN.getValue(),
                reqVO.getType(), unionId);
        if (CollUtil.isEmpty(socialUsers)) {
            throw exception(AUTH_THIRD_LOGIN_NOT_BIND);
        }

        // 使用账号密码，进行登陆。
        SysUserDO user = userService.getUser(socialUsers.get(0).getUserId());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        LoginUser loginUser = SysAuthConvert.INSTANCE.convert(user);
        // TODO 芋艿：需要改造下，增加各种登陆方式
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getId())); // 获取用户角色列表

        // 保存社交用户
        this.saveSocialUser(reqVO.getType(), socialUsers, loginUser.getId(), authUser);

        // 缓存登陆用户到 Redis 中，返回 sessionId 编号
        return userSessionService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    public String socialLogin2(SysAuthSocialLogin2ReqVO reqVO, String userIp, String userAgent) {
        // 使用 code 授权码，进行登陆
        AuthCallback authCallback = SysAuthConvert.INSTANCE.convert(reqVO);
        AuthUser authUser = this.obtainAuthUserFromCache(reqVO.getType(), authCallback);

        // 查询社交对应的 SysSocialUserDO 用户
        String unionId = getAuthUserUnionId(authUser);
        List<SysSocialUserDO> socialUsers = socialUserMapper.selectListByTypeAndUnionId(UserTypeEnum.ADMIN.getValue(),
                reqVO.getType(), unionId);

        // 使用账号密码，进行登陆。
        LoginUser loginUser = this.login0(reqVO.getUsername(), reqVO.getPassword()); // TODO 芋艿：需要改造下，增加各种登陆方式
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getId())); // 获取用户角色列表

        // 保存社交用户
        this.saveSocialUser(reqVO.getType(), socialUsers, loginUser.getId(), authUser);

        // 缓存登陆用户到 Redis 中，返回 sessionId 编号
        return userSessionService.createUserSession(loginUser, userIp, userAgent);
    }

    private static String getAuthUserUnionId(AuthUser authUser) {
        return StrUtil.blankToDefault(authUser.getToken().getUnionId(), authUser.getUuid());
    }

    private AuthUser obtainAuthUserFromCache(Integer type, AuthCallback authCallback) {
        // 从缓存中获取
        AuthUser authUser = authSocialUserRedisDAO.get(type, authCallback);
        if (authUser != null) {
            return authUser;
        }

        // 请求获取
        authUser = this.obtainAuthUser(type, authCallback);
        authSocialUserRedisDAO.set(type, authCallback, authUser);
        return authUser;
    }

    private AuthUser obtainAuthUser(Integer type, AuthCallback authCallback) {
        AuthRequest authRequest = authRequestFactory.get(SysSocialTypeEnum.valueOfType(type).getSource());
        AuthResponse<?> authResponse = authRequest.login(authCallback);
        log.info("[obtainAuthUser][请求社交平台 type({}) request({}) response({})]", type, JsonUtils.toJsonString(authCallback),
                JsonUtils.toJsonString(authResponse));
        if (!authResponse.ok()) {
            throw exception(AUTH_THIRD_OAUTH_FAILURE, authResponse.getMsg());
        }
        return (AuthUser) authResponse.getData();
    }

    /**
     * 保存社交用户
     *
     * @param socialUsers 已存在的社交用户列表
     * @param userId 绑定的用户编号
     * @param authUser 需要保存的社交用户信息
     */
    private void saveSocialUser(Integer type, List<SysSocialUserDO> socialUsers, Long userId, AuthUser authUser) {
        // 逻辑一：如果 socialUsers 指定的 userId 改变，需要进行更新
        // 例如说，一个微信 unionId 对应了多个社交账号，结果其中有个关联了新的 userId，则其它也要跟着修改
        // 考虑到 socialUsers 一般比较少，直接 for 循环更新即可
        socialUsers.forEach(socialUser -> {
            if (Objects.equals(socialUser.getUserId(), userId)) {
                return;
            }
            socialUserMapper.updateById(new SysSocialUserDO().setUserId(socialUser.getUserId()).setUserId(userId));
        });

        // 逻辑二：如果 authUser 不存在于 socialUsers 中，则进行新增；否则，进行更新
        SysSocialUserDO saveSocialUser = CollUtil.findOneByField(socialUsers, "openid", authUser.getUuid());
        if (saveSocialUser == null) {
            saveSocialUser = new SysSocialUserDO();
            saveSocialUser.setUserId(userId).setUserType(UserTypeEnum.ADMIN.getValue());
            saveSocialUser.setType(type).setOpenid(authUser.getUuid()).setToken(authUser.getToken().getAccessToken())
                    .setUnionId(getAuthUserUnionId(authUser)).setRawTokenInfo(JsonUtils.toJsonString(authUser.getToken()));
            saveSocialUser.setNickname(authUser.getNickname()).setAvatar(authUser.getAvatar())
                            .setRawUserInfo(JsonUtils.toJsonString(authUser.getRawUserInfo()));
            socialUserMapper.insert(saveSocialUser);
        } else {
            saveSocialUser = new SysSocialUserDO().setId(saveSocialUser.getId());
            saveSocialUser.setToken(authUser.getToken().getAccessToken()).setUnionId(getAuthUserUnionId(authUser))
                    .setRawTokenInfo(JsonUtils.toJsonString(authUser.getToken()));
            saveSocialUser.setNickname(authUser.getNickname()).setAvatar(authUser.getAvatar())
                    .setRawUserInfo(JsonUtils.toJsonString(authUser.getRawUserInfo()));
            socialUserMapper.updateById(saveSocialUser);
        }
    }

    @Override
    public void logout(String token) {
        // 查询用户信息
        LoginUser loginUser = userSessionService.getLoginUser(token);
        if (loginUser == null) {
            return;
        }
        // 删除 session
        userSessionService.deleteUserSession(token);
        // 记录登出日子和
        this.createLogoutLog(loginUser.getUsername());
    }

    private void createLogoutLog(String username) {
        SysLoginLogCreateReqVO reqVO = new SysLoginLogCreateReqVO();
        reqVO.setLogType(SysLoginLogTypeEnum.LOGOUT_SELF.getType());
        reqVO.setTraceId(TracerUtils.getTraceId());
        reqVO.setUsername(username);
        reqVO.setUserAgent(ServletUtils.getUserAgent());
        reqVO.setUserIp(ServletUtils.getClientIP());
        reqVO.setResult(SysLoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqVO);
    }

    @Override
    public LoginUser verifyTokenAndRefresh(String token) {
        // 获得 LoginUser
        LoginUser loginUser = userSessionService.getLoginUser(token);
        if (loginUser == null) {
            return null;
        }
        // 刷新 LoginUser 缓存
        this.refreshLoginUserCache(token, loginUser);
        return loginUser;
    }

    private void refreshLoginUserCache(String token, LoginUser loginUser) {
        // 每 1/3 的 Session 超时时间，刷新 LoginUser 缓存
        if (System.currentTimeMillis() - loginUser.getUpdateTime().getTime() <
                userSessionService.getSessionTimeoutMillis() / 3) {
            return;
        }

        // 重新加载 SysUserDO 信息
        SysUserDO user = userService.getUser(loginUser.getId());
        if (user == null || CommonStatusEnum.DISABLE.getStatus().equals(user.getStatus())) {
            throw exception(TOKEN_EXPIRED); // 校验 token 时，用户被禁用的情况下，也认为 token 过期，方便前端跳转到登陆界面
        }

        // 刷新 LoginUser 缓存
        loginUser.setDeptId(user.getDeptId());
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getId()));
        userSessionService.refreshUserSession(token, loginUser);
    }

}
