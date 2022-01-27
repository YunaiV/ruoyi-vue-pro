package cn.iocoder.yudao.module.member.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.social.SysSocialUserDO;
import cn.iocoder.yudao.coreservice.modules.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.yudao.coreservice.modules.system.enums.logger.SysLoginResultEnum;
import cn.iocoder.yudao.coreservice.modules.system.service.auth.SysUserSessionCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.SysLoginLogCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.logger.dto.SysLoginLogCreateReqDTO;
import cn.iocoder.yudao.coreservice.modules.system.service.social.SysSocialCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.userserver.modules.member.dal.mysql.user.MbrUserMapper;
import cn.iocoder.yudao.userserver.modules.member.service.user.MbrUserService;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.*;
import cn.iocoder.yudao.userserver.modules.system.convert.auth.SysAuthConvert;
import cn.iocoder.yudao.userserver.modules.system.enums.sms.SysSmsSceneEnum;
import cn.iocoder.yudao.userserver.modules.system.service.auth.SysAuthService;
import cn.iocoder.yudao.userserver.modules.system.service.sms.SysSmsCodeService;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
import static cn.iocoder.yudao.userserver.modules.system.enums.SysErrorCodeConstants.*;

/**
 * Auth Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysAuthServiceImpl implements SysAuthService {

    private static final UserTypeEnum USER_TYPE_ENUM = UserTypeEnum.MEMBER;

    @Resource
    @Lazy // 延迟加载，因为存在相互依赖的问题
    private AuthenticationManager authenticationManager;

    @Resource
    private MbrUserService userService;
    @Resource
    private SysSmsCodeService smsCodeService;
    @Resource
    private SysLoginLogCoreService loginLogCoreService;
    @Resource
    private SysUserSessionCoreService userSessionCoreService;
    @Resource
    private SysSocialCoreService socialService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private MbrUserMapper userMapper;

    private static final UserTypeEnum userTypeEnum = UserTypeEnum.MEMBER;

    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
        // 获取 username 对应的 SysUserDO
        MbrUserDO user = userService.getUserByMobile(mobile);
        if (user == null) {
            throw new UsernameNotFoundException(mobile);
        }
        // 创建 LoginUser 对象
        return SysAuthConvert.INSTANCE.convert(user);
    }

    @Override
    public String login(SysAuthLoginReqVO reqVO, String userIp, String userAgent) {
        // 使用手机 + 密码，进行登录。
        LoginUser loginUser = this.login0(reqVO.getMobile(), reqVO.getPassword());

        // 缓存登录用户到 Redis 中，返回 sessionId 编号
        return userSessionCoreService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    @Transactional
    public String smsLogin(SysAuthSmsLoginReqVO reqVO, String userIp, String userAgent) {
        // 校验验证码
        smsCodeService.useSmsCode(reqVO.getMobile(), SysSmsSceneEnum.LOGIN_BY_SMS.getScene(),
                reqVO.getCode(), userIp);

        // 获得获得注册用户
        MbrUserDO user = userService.createUserIfAbsent(reqVO.getMobile(), userIp);
        Assert.notNull(user, "获取用户失败，结果为空");

        // 执行登陆
        this.createLoginLog(user.getMobile(), SysLoginLogTypeEnum.LOGIN_SMS, SysLoginResultEnum.SUCCESS);
        LoginUser loginUser = SysAuthConvert.INSTANCE.convert(user);

        // 缓存登录用户到 Redis 中，返回 sessionId 编号
        return userSessionCoreService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    public String socialLogin(MbrAuthSocialLoginReqVO reqVO, String userIp, String userAgent) {
        // 使用 code 授权码，进行登录
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        org.springframework.util.Assert.notNull(authUser, "授权用户不为空");

        // 如果未绑定 SysSocialUserDO 用户，则无法自动登录，进行报错
        String unionId = socialService.getAuthUserUnionId(authUser);
        List<SysSocialUserDO> socialUsers = socialService.getAllSocialUserList(reqVO.getType(), unionId, USER_TYPE_ENUM);
        if (CollUtil.isEmpty(socialUsers)) {
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.AUTH_THIRD_LOGIN_NOT_BIND);
        }

        // 自动登录
        MbrUserDO user = userService.getUser(socialUsers.get(0).getUserId());
        if (user == null) {
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_NOT_EXISTS);
        }
        this.createLoginLog(user.getMobile(), SysLoginLogTypeEnum.LOGIN_SOCIAL, SysLoginResultEnum.SUCCESS);

        // 创建 LoginUser 对象
        LoginUser loginUser = SysAuthConvert.INSTANCE.convert(user);

        // 绑定社交用户（更新）
        socialService.bindSocialUser(loginUser.getId(), reqVO.getType(), authUser, USER_TYPE_ENUM);

        // 缓存登录用户到 Redis 中，返回 sessionId 编号
        return userSessionCoreService.createUserSession(loginUser, userIp, userAgent);
    }

    @Override
    public String socialLogin2(MbrAuthSocialLogin2ReqVO reqVO, String userIp, String userAgent) {
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        org.springframework.util.Assert.notNull(authUser, "授权用户不为空");

        // 使用手机号、手机验证码登录
        SysAuthSmsLoginReqVO loginReqVO = SysAuthSmsLoginReqVO
                .builder()
                .mobile(reqVO.getMobile())
                .code(reqVO.getSmsCode())
                .build();
        String sessionId = this.smsLogin(loginReqVO, userIp, userAgent);
        LoginUser loginUser = userSessionCoreService.getLoginUser(sessionId);

        // 绑定社交用户（新增）
        socialService.bindSocialUser(loginUser.getId(), reqVO.getType(), authUser, USER_TYPE_ENUM);
        return sessionId;
    }

    @Override
    public void socialBind(Long userId, MbrAuthSocialBindReqVO reqVO) {
        // 使用 code 授权码，进行登录
        AuthUser authUser = socialService.getAuthUser(reqVO.getType(), reqVO.getCode(), reqVO.getState());
        org.springframework.util.Assert.notNull(authUser, "授权用户不为空");

        // 绑定社交用户（新增）
        socialService.bindSocialUser(userId, reqVO.getType(), authUser, USER_TYPE_ENUM);
    }

    private LoginUser login0(String username, String password) {
        final SysLoginLogTypeEnum logTypeEnum = SysLoginLogTypeEnum.LOGIN_USERNAME;
        // 用户验证
        Authentication authentication;
        try {
            // 调用 Spring Security 的 AuthenticationManager#authenticate(...) 方法，使用账号密码进行认证
            // 在其内部，会调用到 loadUserByUsername 方法，获取 User 信息
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException badCredentialsException) {
            this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.BAD_CREDENTIALS);
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.AUTH_LOGIN_BAD_CREDENTIALS);
        } catch (DisabledException disabledException) {
            this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.USER_DISABLED);
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.AUTH_LOGIN_USER_DISABLED);
        } catch (AuthenticationException authenticationException) {
            log.error("[login0][username({}) 发生未知异常]", username, authenticationException);
            this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.UNKNOWN_ERROR);
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.AUTH_LOGIN_FAIL_UNKNOWN);
        }
        // 登录成功的日志
        Assert.notNull(authentication.getPrincipal(), "Principal 不会为空");
        this.createLoginLog(username, logTypeEnum, SysLoginResultEnum.SUCCESS);
        return (LoginUser) authentication.getPrincipal();
    }

    private void createLoginLog(String mobile, SysLoginLogTypeEnum logTypeEnum, SysLoginResultEnum loginResult) {
        // 获得用户
        MbrUserDO user = userService.getUserByMobile(mobile);
        // 插入登录日志
        SysLoginLogCreateReqDTO reqDTO = new SysLoginLogCreateReqDTO();
        reqDTO.setLogType(logTypeEnum.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        if (user != null) {
            reqDTO.setUserId(user.getId());
        }
        reqDTO.setUsername(mobile);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogCoreService.createLoginLog(reqDTO);
        // 更新最后登录时间
        if (user != null && Objects.equals(SysLoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            userService.updateUserLogin(user.getId(), getClientIP());
        }
    }

    @Override
    public LoginUser verifyTokenAndRefresh(String token) {
        // 获得 LoginUser
        LoginUser loginUser = userSessionCoreService.getLoginUser(token);
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
                userSessionCoreService.getSessionTimeoutMillis() / 3) {
            return;
        }

        // 重新加载 MbrUserDO 信息
        MbrUserDO user = userService.getUser(loginUser.getId());
        if (user == null || CommonStatusEnum.DISABLE.getStatus().equals(user.getStatus())) {
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.AUTH_TOKEN_EXPIRED); // 校验 token 时，用户被禁用的情况下，也认为 token 过期，方便前端跳转到登录界面
        }

        // 刷新 LoginUser 缓存
        userSessionCoreService.refreshUserSession(token, loginUser);
    }

    @Override
    public LoginUser mockLogin(Long userId) {
        // 获取用户编号对应的 MbrUserDO
        MbrUserDO user = userService.getUser(userId);
        if (user == null) {
            throw new UsernameNotFoundException(String.valueOf(userId));
        }

        // 执行登陆
        this.createLoginLog(user.getMobile(), SysLoginLogTypeEnum.LOGIN_MOCK, SysLoginResultEnum.SUCCESS);

        // 创建 LoginUser 对象
        return SysAuthConvert.INSTANCE.convert(user);
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

    @Override
    public void updatePassword(Long userId,MbrAuthUpdatePasswordReqVO reqVO) {
        // 检验旧密码
        MbrUserDO userDO = checkOldPassword(userId, reqVO.getOldPassword());

        // 更新用户密码
        MbrUserDO mbrUserDO = MbrUserDO.builder().build();
        mbrUserDO.setId(userDO.getId());
        mbrUserDO.setPassword(passwordEncoder.encode(reqVO.getPassword()));
        userMapper.updateById(mbrUserDO);
    }

    @Override
    public void resetPassword(MbrAuthResetPasswordReqVO reqVO) {
        // 检验用户是否存在
        MbrUserDO userDO = checkUserIfExists(reqVO.getMobile());

        // 使用验证码
        smsCodeService.useSmsCode(reqVO.getMobile(),SysSmsSceneEnum.FORGET_MOBILE_BY_SMS.getScene(),reqVO.getCode(),getClientIP());

        // 更新密码
        MbrUserDO mbrUserDO = MbrUserDO.builder().build();
        mbrUserDO.setId(userDO.getId());
        mbrUserDO.setPassword(passwordEncoder.encode(reqVO.getPassword()));
        userMapper.updateById(mbrUserDO);
    }

    /**
     * 校验旧密码
     *
     * @param id          用户 id
     * @param oldPassword 旧密码
     * @return MbrUserDO 用户实体
     */
    @VisibleForTesting
    public MbrUserDO checkOldPassword(Long id, String oldPassword) {
        MbrUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_NOT_EXISTS);
        }
        // 参数：未加密密码，编码后的密码
        if (!passwordEncoder.matches(oldPassword,user.getPassword())) {
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_PASSWORD_FAILED);
        }
        return user;
    }

    public MbrUserDO checkUserIfExists(String mobile) {
        MbrUserDO user = userMapper.selectByMobile(mobile);
        if (user == null) {
            throw ServiceExceptionUtil.exception(SysErrorCodeConstants.USER_NOT_EXISTS);
        }
        return user;
    }


    private void createLogoutLog(Long userId, String username) {
        SysLoginLogCreateReqDTO reqDTO = new SysLoginLogCreateReqDTO();
        reqDTO.setLogType(SysLoginLogTypeEnum.LOGOUT_SELF.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(USER_TYPE_ENUM.getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(getClientIP());
        reqDTO.setResult(SysLoginResultEnum.SUCCESS.getResult());
        loginLogCoreService.createLoginLog(reqDTO);
    }

}
