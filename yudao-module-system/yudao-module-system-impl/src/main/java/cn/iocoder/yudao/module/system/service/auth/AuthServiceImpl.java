package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.authentication.MultiUsernamePasswordAuthenticationToken;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.auth.AuthLoginReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.auth.AuthSocialBindReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.auth.AuthSocialLogin2ReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.auth.AuthSocialLoginReqVO;
import cn.iocoder.yudao.module.system.convert.auth.AuthConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.service.common.CaptchaService;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.logger.dto.LoginLogCreateReqDTO;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.social.SocialUserService;
import cn.iocoder.yudao.module.system.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;

/**
 * Auth Service 实现类
 *
 * @author 芋道源码
 */
@Service("adminAuthService")
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Resource
    @Lazy // 延迟加载，因为存在相互依赖的问题
    private AuthenticationManager authenticationManager;

    @Autowired
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection") // UserService 存在重名
    private UserService userService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private CaptchaService captchaService;
    @Resource
    private LoginLogService loginLogService;
    @Resource
    private UserSessionService userSessionService;
    @Resource
    private SocialUserService socialService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 获取 username 对应的 SysUserDO
        UserDO user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // 创建 LoginUser 对象
        return this.buildLoginUser(user);
    }

    @Override
    public LoginUser mockLogin(Long userId) {
        // 获取用户编号对应的 SysUserDO
        UserDO user = userService.getUser(userId);
        if (user == null) {
            throw new UsernameNotFoundException(String.valueOf(userId));
        }
        this.createLoginLog(user.getUsername(), LoginLogTypeEnum.LOGIN_MOCK, LoginResultEnum.SUCCESS);

        // 创建 LoginUser 对象
        return this.buildLoginUser(user);
    }

    @Override
    public String login(AuthLoginReqVO reqVO, String userIp, String userAgent) {
        // 判断验证码是否正确
        this.verifyCaptcha(reqVO.getUsername(), reqVO.getUuid(), reqVO.getCode());

        // 使用账号密码，进行登录
        LoginUser loginUser = this.login0(reqVO.getUsername(), reqVO.getPassword());

        // 缓存登陆用户到 Redis 中，返回 sessionId 编号
        return userSessionService.createUserSession(loginUser, userIp, userAgent);
    }

    private void verifyCaptcha(String username, String captchaUUID, String captchaCode) {
        // 如果验证码关闭，则不进行校验
        if (!captchaService.isCaptchaEnable()) {
            return;
        }
        // 验证码不存在
        final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
        String code = captchaService.getCaptchaCode(captchaUUID);
        if (code == null) {
            // 创建登录失败日志（验证码不存在）
            this.createLoginLog(username, logTypeEnum, LoginResultEnum.CAPTCHA_NOT_FOUND);
            throw exception(AUTH_LOGIN_CAPTCHA_NOT_FOUND);
        }
        // 验证码不正确
        if (!code.equals(captchaCode)) {
            // 创建登录失败日志（验证码不正确)
            this.createLoginLog(username, logTypeEnum, LoginResultEnum.CAPTCHA_CODE_ERROR);
            throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR);
        }
        // 正确，所以要删除下验证码
        captchaService.deleteCaptchaCode(captchaUUID);
    }

    private LoginUser login0(String username, String password) {
        final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
        // 用户验证
        Authentication authentication;
        try {
            // 调用 Spring Security 的 AuthenticationManager#authenticate(...) 方法，使用账号密码进行认证
            // 在其内部，会调用到 loadUserByUsername 方法，获取 User 信息
            authentication = authenticationManager.authenticate(new MultiUsernamePasswordAuthenticationToken(
                    username, password, getUserType()));
           //  org.activiti.engine.impl.identity.Authentication.setAuthenticatedUserId(username);
        } catch (BadCredentialsException badCredentialsException) {
            this.createLoginLog(username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        } catch (DisabledException disabledException) {
            this.createLoginLog(username, logTypeEnum, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        } catch (AuthenticationException authenticationException) {
            log.error("[login0][username({}) 发生未知异常]", username, authenticationException);
            this.createLoginLog(username, logTypeEnum, LoginResultEnum.UNKNOWN_ERROR);
            throw exception(AUTH_LOGIN_FAIL_UNKNOWN);
        }
        // 登录成功的日志
        Assert.notNull(authentication.getPrincipal(), "Principal 不会为空");
        this.createLoginLog(username, logTypeEnum, LoginResultEnum.SUCCESS);
        return (LoginUser) authentication.getPrincipal();
    }

    private void createLoginLog(String username, LoginLogTypeEnum logTypeEnum, LoginResultEnum loginResult) {
        // 获得用户
        UserDO user = userService.getUserByUsername(username);
        // 插入登录日志
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logTypeEnum.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        if (user != null) {
            reqDTO.setUserId(user.getId());
        }
        reqDTO.setUserType(UserTypeEnum.ADMIN.getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqDTO);
        // 更新最后登录时间
        if (user != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            userService.updateUserLogin(user.getId(), ServletUtils.getClientIP());
        }
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
    public String socialLogin(AuthSocialLoginReqVO reqVO, String userIp, String userAgent) {
        // 使用 code 授权码，进行登录
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        Assert.notNull(authUser, "授权用户不为空");

        // 如果未绑定 SysSocialUserDO 用户，则无法自动登录，进行报错
        String unionId = socialService.getAuthUserUnionId(authUser);
        List<SysSocialUserDO> socialUsers = socialService.getAllSocialUserList(reqVO.getType(), unionId, getUserType());
        if (CollUtil.isEmpty(socialUsers)) {
            throw exception(AUTH_THIRD_LOGIN_NOT_BIND);
        }

        // 自动登录
        UserDO user = userService.getUser(socialUsers.get(0).getUserId());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        this.createLoginLog(user.getUsername(), LoginLogTypeEnum.LOGIN_SOCIAL, LoginResultEnum.SUCCESS);

        // 创建 LoginUser 对象
        LoginUser loginUser = this.buildLoginUser(user);

        // 绑定社交用户（更新）
        socialService.bindSocialUser(loginUser.getId(), reqVO.getType(), authUser, getUserType());

        // 缓存登录用户到 Redis 中，返回 sessionId 编号
        return userSessionService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    public String socialLogin2(AuthSocialLogin2ReqVO reqVO, String userIp, String userAgent) {
        // 使用 code 授权码，进行登录
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        Assert.notNull(authUser, "授权用户不为空");

        // 使用账号密码，进行登录。
        LoginUser loginUser = this.login0(reqVO.getUsername(), reqVO.getPassword());

        // 绑定社交用户（新增）
        socialService.bindSocialUser(loginUser.getId(), reqVO.getType(), authUser, getUserType());

        // 缓存登录用户到 Redis 中，返回 sessionId 编号
        return userSessionService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    public void socialBind(Long userId, AuthSocialBindReqVO reqVO) {
        // 使用 code 授权码，进行登录
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        Assert.notNull(authUser, "授权用户不为空");

        // 绑定社交用户（新增）
        socialService.bindSocialUser(userId, reqVO.getType(), authUser, getUserType());
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
        // 记录登出日志
        this.createLogoutLog(loginUser.getId(), loginUser.getUsername());
    }

    @Override
    public UserTypeEnum getUserType() {
        return UserTypeEnum.ADMIN;
    }

    private void createLogoutLog(Long userId, String username) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(LoginLogTypeEnum.LOGOUT_SELF.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(getUserType().getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    @Override
    public LoginUser verifyTokenAndRefresh(String token) {
        // 获得 LoginUser
        LoginUser loginUser = userSessionService.getLoginUser(token);
        if (loginUser == null) {
            return null;
        }
        // 刷新 LoginUser 缓存
        return this.refreshLoginUserCache(token, loginUser);
    }

    private LoginUser refreshLoginUserCache(String token, LoginUser loginUser) {
        // 每 1/3 的 Session 超时时间，刷新 LoginUser 缓存
        if (System.currentTimeMillis() - loginUser.getUpdateTime().getTime() <
                userSessionService.getSessionTimeoutMillis() / 3) {
            return loginUser;
        }

        // 重新加载 SysUserDO 信息
        UserDO user = userService.getUser(loginUser.getId());
        if (user == null || CommonStatusEnum.DISABLE.getStatus().equals(user.getStatus())) {
            throw exception(AUTH_TOKEN_EXPIRED); // 校验 token 时，用户被禁用的情况下，也认为 token 过期，方便前端跳转到登录界面
        }

        // 刷新 LoginUser 缓存
        LoginUser newLoginUser= this.buildLoginUser(user);
        userSessionService.refreshUserSession(token, newLoginUser);
        return newLoginUser;
    }

    private LoginUser buildLoginUser(UserDO user) {
        LoginUser loginUser = AuthConvert.INSTANCE.convert(user);
        // 补全字段
        loginUser.setDeptId(user.getDeptId());
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getId()));
        return loginUser;
    }

}
