package cn.iocoder.yudao.adminserver.modules.system.service.auth.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthLoginReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthSocialBindReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthSocialLogin2ReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.auth.SysAuthSocialLoginReqVO;
import cn.iocoder.yudao.adminserver.modules.system.convert.auth.SysAuthConvert;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.adminserver.modules.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.enums.logger.SysLoginResultEnum;
import cn.iocoder.yudao.adminserver.modules.system.service.auth.SysAuthService;
import cn.iocoder.yudao.adminserver.modules.system.service.common.SysCaptchaService;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysPostService;
import cn.iocoder.yudao.adminserver.modules.system.service.permission.SysPermissionService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.service.auth.SysUserSessionCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.SysLoginLogCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.system.service.social.SysSocialCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.user.SysUserCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthUser;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static java.util.Collections.singleton;

/**
 * Auth Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysAuthServiceImpl implements SysAuthService {

    private static final UserTypeEnum USER_TYPE_ENUM = UserTypeEnum.ADMIN;

    @Resource
    @Lazy // 延迟加载，因为存在相互依赖的问题
    private AuthenticationManager authenticationManager;

    @Resource
    private SysUserService userService;
    @Resource
    private SysUserCoreService userCoreService;
    @Resource
    private SysPermissionService permissionService;
    @Resource
    private SysCaptchaService captchaService;
    @Resource
    private SysLoginLogCoreService loginLogCoreService;
    @Resource
    private SysUserSessionCoreService userSessionCoreService;
    @Resource
    private SysPostService postService;
    @Resource
    private SysSocialCoreService socialService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 获取 username 对应的 SysUserDO
        SysUserDO user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        // 创建 LoginUser 对象
        return this.buildLoginUser(user);
    }

    @Override
    public LoginUser mockLogin(Long userId) {
        // 获取用户编号对应的 SysUserDO
        SysUserDO user = userCoreService.getUser(userId);
        if (user == null) {
            throw new UsernameNotFoundException(String.valueOf(userId));
        }
        this.createLoginLog(user.getUsername(), SysLoginLogTypeEnum.LOGIN_MOCK, SysLoginResultEnum.SUCCESS);

        // 创建 LoginUser 对象
        return this.buildLoginUser(user);
    }

    @Override
    public String login(SysAuthLoginReqVO reqVO, String userIp, String userAgent) {
        // 判断验证码是否正确
        this.verifyCaptcha(reqVO.getUsername(), reqVO.getUuid(), reqVO.getCode());

        // 使用账号密码，进行登录
        LoginUser loginUser = this.login0(reqVO.getUsername(), reqVO.getPassword());

        // 缓存登陆用户到 Redis 中，返回 sessionId 编号
        return userSessionCoreService.createUserSession(loginUser, userIp, userAgent);
    }

    private List<String> getUserPosts(Set<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyList();
        }
        return convertList(postService.getPosts(postIds), SysPostDO::getCode);
    }

    private void verifyCaptcha(String username, String captchaUUID, String captchaCode) {
        // 如果验证码关闭，则不进行校验
        if (!captchaService.isCaptchaEnable()) {
            return;
        }
        // 验证码不存在
        final SysLoginLogTypeEnum logTypeEnum = SysLoginLogTypeEnum.LOGIN_USERNAME;
        String code = captchaService.getCaptchaCode(captchaUUID);
        if (code == null) {
            // 创建登录失败日志（验证码不存在）
            this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.CAPTCHA_NOT_FOUND);
            throw exception(AUTH_LOGIN_CAPTCHA_NOT_FOUND);
        }
        // 验证码不正确
        if (!code.equals(captchaCode)) {
            // 创建登录失败日志（验证码不正确)
            this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.CAPTCHA_CODE_ERROR);
            throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR);
        }
        // 正确，所以要删除下验证码
        captchaService.deleteCaptchaCode(captchaUUID);
    }

    private LoginUser login0(String username, String password) {
        final SysLoginLogTypeEnum logTypeEnum = SysLoginLogTypeEnum.LOGIN_USERNAME;
        // 用户验证
        Authentication authentication;
        try {
            // 调用 Spring Security 的 AuthenticationManager#authenticate(...) 方法，使用账号密码进行认证
            // 在其内部，会调用到 loadUserByUsername 方法，获取 User 信息
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
           //  org.activiti.engine.impl.identity.Authentication.setAuthenticatedUserId(username);
        } catch (BadCredentialsException badCredentialsException) {
            this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        } catch (DisabledException disabledException) {
            this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        } catch (AuthenticationException authenticationException) {
            log.error("[login0][username({}) 发生未知异常]", username, authenticationException);
            this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.UNKNOWN_ERROR);
            throw exception(AUTH_LOGIN_FAIL_UNKNOWN);
        }
        // 登录成功的日志
        Assert.notNull(authentication.getPrincipal(), "Principal 不会为空");
        this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.SUCCESS);
        return (LoginUser) authentication.getPrincipal();
    }

    private void createLoginLog(String username, SysLoginLogTypeEnum logTypeEnum, SysLoginResultEnum loginResult) {
        // 获得用户
        SysUserDO user = userService.getUserByUsername(username);
        // 插入登录日志
        SysLoginLogCreateReqDTO reqDTO = new SysLoginLogCreateReqDTO();
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
        loginLogCoreService.createLoginLog(reqDTO);
        // 更新最后登录时间
        if (user != null && Objects.equals(SysLoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
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
    public String socialLogin(SysAuthSocialLoginReqVO reqVO, String userIp, String userAgent) {
        // 使用 code 授权码，进行登录
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        Assert.notNull(authUser, "授权用户不为空");

        // 如果未绑定 SysSocialUserDO 用户，则无法自动登录，进行报错
        String unionId = socialService.getAuthUserUnionId(authUser);
        List<SysSocialUserDO> socialUsers = socialService.getAllSocialUserList(reqVO.getType(), unionId, USER_TYPE_ENUM);
        if (CollUtil.isEmpty(socialUsers)) {
            throw exception(AUTH_THIRD_LOGIN_NOT_BIND);
        }

        // 自动登录
        SysUserDO user = userCoreService.getUser(socialUsers.get(0).getUserId());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        this.createLoginLog(user.getUsername(), SysLoginLogTypeEnum.LOGIN_SOCIAL, SysLoginResultEnum.SUCCESS);

        // 创建 LoginUser 对象
        LoginUser loginUser = this.buildLoginUser(user);

        // 绑定社交用户（更新）
        socialService.bindSocialUser(loginUser.getId(), reqVO.getType(), authUser, USER_TYPE_ENUM);

        // 缓存登录用户到 Redis 中，返回 sessionId 编号
        return userSessionCoreService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    public String socialLogin2(SysAuthSocialLogin2ReqVO reqVO, String userIp, String userAgent) {
        // 使用 code 授权码，进行登录
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        Assert.notNull(authUser, "授权用户不为空");

        // 使用账号密码，进行登录。
        LoginUser loginUser = this.login0(reqVO.getUsername(), reqVO.getPassword());

        // 绑定社交用户（新增）
        socialService.bindSocialUser(loginUser.getId(), reqVO.getType(), authUser, USER_TYPE_ENUM);

        // 缓存登录用户到 Redis 中，返回 sessionId 编号
        return userSessionCoreService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    public void socialBind(Long userId, SysAuthSocialBindReqVO reqVO) {
        // 使用 code 授权码，进行登录
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        Assert.notNull(authUser, "授权用户不为空");

        // 绑定社交用户（新增）
        socialService.bindSocialUser(userId, reqVO.getType(), authUser, USER_TYPE_ENUM);
    }

    @Override
    public void logout(String token) {
        // 查询用户信息
        LoginUser loginUser = userSessionCoreService.getLoginUser(token);
        if (loginUser == null) {
            return;
        }
        // 删除 session
        userSessionCoreService.deleteUserSession(token);
        // 记录登出日志
        this.createLogoutLog(loginUser.getId(), loginUser.getUsername());
    }

    private void createLogoutLog(Long userId, String username) {
        SysLoginLogCreateReqDTO reqDTO = new SysLoginLogCreateReqDTO();
        reqDTO.setLogType(SysLoginLogTypeEnum.LOGOUT_SELF.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(USER_TYPE_ENUM.getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(SysLoginResultEnum.SUCCESS.getResult());
        loginLogCoreService.createLoginLog(reqDTO);
    }

    @Override
    public LoginUser verifyTokenAndRefresh(String token) {
        // 获得 LoginUser
        LoginUser loginUser = userSessionCoreService.getLoginUser(token);
        if (loginUser == null) {
            return null;
        }
        // 刷新 LoginUser 缓存
        return this.refreshLoginUserCache(token, loginUser);
    }

    private LoginUser refreshLoginUserCache(String token, LoginUser loginUser) {
        // 每 1/3 的 Session 超时时间，刷新 LoginUser 缓存
        if (System.currentTimeMillis() - loginUser.getUpdateTime().getTime() <
                userSessionCoreService.getSessionTimeoutMillis() / 3) {
            return loginUser;
        }

        // 重新加载 SysUserDO 信息
        SysUserDO user = userCoreService.getUser(loginUser.getId());
        if (user == null || CommonStatusEnum.DISABLE.getStatus().equals(user.getStatus())) {
            throw exception(AUTH_TOKEN_EXPIRED); // 校验 token 时，用户被禁用的情况下，也认为 token 过期，方便前端跳转到登录界面
        }

        // 刷新 LoginUser 缓存
        LoginUser newLoginUser= this.buildLoginUser(user);
        userSessionCoreService.refreshUserSession(token, newLoginUser);
        return newLoginUser;
    }

    private LoginUser buildLoginUser(SysUserDO user) {
        LoginUser loginUser = SysAuthConvert.INSTANCE.convert(user);
        // 补全字段
        loginUser.setDeptId(user.getDeptId());
        loginUser.setRoleIds(this.getUserRoleIds(loginUser.getId()));
        loginUser.setGroups(this.getUserPosts(user.getPostIds()));
        return loginUser;
    }

}
