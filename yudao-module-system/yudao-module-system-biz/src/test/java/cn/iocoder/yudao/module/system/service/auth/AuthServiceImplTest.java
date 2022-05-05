package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.module.system.api.sms.SmsCodeApi;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.auth.AuthLoginReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.service.common.CaptchaService;
import cn.iocoder.yudao.module.system.service.dept.PostService;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.social.SocialUserService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(AdminAuthServiceImpl.class)
public class AuthServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminAuthServiceImpl authService;

    @MockBean
    private AdminUserService userService;
    @MockBean
    private PermissionService permissionService;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private Authentication authentication;
    @MockBean
    private CaptchaService captchaService;
    @MockBean
    private LoginLogService loginLogService;
    @MockBean
    private UserSessionService userSessionService;
    @MockBean
    private SocialUserService socialService;
    @MockBean
    private PostService postService;
    @MockBean
    private SmsCodeApi smsCodeApi;

    @MockBean
    private Validator validator;

    @BeforeEach
    public void setUp() {
        when(captchaService.isCaptchaEnable()).thenReturn(true);
    }

    @Test
    public void testLoadUserByUsername_success() {
        // 准备参数
        String username = randomString();
        // mock 方法
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);

        // 调用
        LoginUser loginUser = (LoginUser) authService.loadUserByUsername(username);
        // 校验
        AssertUtils.assertPojoEquals(user, loginUser, "updateTime");
    }

    @Test
    public void testLoadUserByUsername_userNotFound() {
        // 准备参数
        String username = randomString();
        // mock 方法

        // 调用, 并断言异常
        assertThrows(UsernameNotFoundException.class, // 抛出 UsernameNotFoundException 异常
                () -> authService.loadUserByUsername(username),
                username); // 异常提示为 username
    }

    @Test
    public void testMockLogin_success() {
        // 准备参数
        Long userId = randomLongId();
        // mock 方法 01
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setId(userId));
        when(userService.getUser(eq(userId))).thenReturn(user);
        // mock 方法 02
        Set<Long> roleIds = randomSet(Long.class);
        when(permissionService.getUserRoleIds(eq(userId), eq(singleton(CommonStatusEnum.ENABLE.getStatus()))))
                .thenReturn(roleIds);

        // 调用
        LoginUser loginUser = authService.mockLogin(userId);
        // 断言
        AssertUtils.assertPojoEquals(user, loginUser, "updateTime");
        assertEquals(roleIds, loginUser.getRoleIds());
    }

    @Test
    public void testMockLogin_userNotFound() {
        // 准备参数
        Long userId = randomLongId();
        // mock 方法

        // 调用, 并断言异常
        assertThrows(UsernameNotFoundException.class, // 抛出 UsernameNotFoundException 异常
                () -> authService.mockLogin(userId),
                String.valueOf(userId)); // 异常提示为 userId
    }

    @Test
    public void testLogin_captchaNotFound() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
        String userIp = randomString();
        String userAgent = randomString();

        // 调用, 并断言异常
        assertServiceException(() -> authService.login(reqVO, userIp, userAgent), AUTH_LOGIN_CAPTCHA_NOT_FOUND);
        // 校验调用参数
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.CAPTCHA_NOT_FOUND.getResult()))
        );
    }

    @Test
    public void testLogin_captchaCodeError() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);

        // mock 验证码不正确
        String code = randomString();
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(code);

        // 调用, 并断言异常
        assertServiceException(() -> authService.login(reqVO, userIp, userAgent), AUTH_LOGIN_CAPTCHA_CODE_ERROR);
        // 校验调用参数
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.CAPTCHA_CODE_ERROR.getResult()))
        );
    }

    @Test
    public void testLogin_badCredentials() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
        // mock 验证码正确
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
        // mock 抛出异常
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(reqVO.getUsername(), reqVO.getPassword())))
                .thenThrow(new BadCredentialsException("测试账号或密码不正确"));

        // 调用, 并断言异常
        assertServiceException(() -> authService.login(reqVO, userIp, userAgent), AUTH_LOGIN_BAD_CREDENTIALS);
        // 校验调用参数
        verify(captchaService, times(1)).deleteCaptchaCode(reqVO.getUuid());
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.BAD_CREDENTIALS.getResult()))
        );
    }

    @Test
    public void testLogin_userDisabled() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);

        // mock 验证码正确
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
        // mock 抛出异常
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(reqVO.getUsername(), reqVO.getPassword())))
                .thenThrow(new DisabledException("测试用户被禁用"));

        // 调用, 并断言异常
        assertServiceException(() -> authService.login(reqVO, userIp, userAgent), AUTH_LOGIN_USER_DISABLED);
        // 校验调用参数
        verify(captchaService, times(1)).deleteCaptchaCode(reqVO.getUuid());
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.USER_DISABLED.getResult()))
        );
    }

    @Test
    public void testLogin_unknownError() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
        // mock 验证码正确
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
        // mock 抛出异常
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(reqVO.getUsername(), reqVO.getPassword())))
                .thenThrow(new AuthenticationException("测试未知异常") {});

        // 调用, 并断言异常
        assertServiceException(() -> authService.login(reqVO, userIp, userAgent), AUTH_LOGIN_FAIL_UNKNOWN);
        // 校验调用参数
        verify(captchaService, times(1)).deleteCaptchaCode(reqVO.getUuid());
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.UNKNOWN_ERROR.getResult()))
        );
    }

    @Test
    public void testLogin_success() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);

        // mock 验证码正确
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
        // mock authentication
        Long userId = randomLongId();
        Set<Long> userRoleIds = randomSet(Long.class);
        LoginUser loginUser = randomPojo(LoginUser.class, o -> {
            o.setId(userId);
            o.setRoleIds(userRoleIds);
        });
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(reqVO.getUsername(), reqVO.getPassword())))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        // mock 获得 User 拥有的角色编号数组
        when(permissionService.getUserRoleIds(userId, singleton(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(userRoleIds);
        // mock 缓存登录用户到 Redis
        String token = randomString();
        when(userSessionService.createUserSession(loginUser, userIp, userAgent)).thenReturn(token);

        // 调用, 并断言异常
        String login = authService.login(reqVO, userIp, userAgent);
        assertEquals(token, login);
        // 校验调用参数
        verify(captchaService, times(1)).deleteCaptchaCode(reqVO.getUuid());
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.SUCCESS.getResult()))
        );
    }

    @Test
    public void testLogout_success() {
        // 准备参数
        String token = randomString();
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock
        when(userSessionService.getLoginUser(token)).thenReturn(loginUser);
        // 调用
        authService.logout(token);
        // 校验调用参数
        verify(userSessionService, times(1)).deleteUserSession(token);
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGOUT_SELF.getType())
                    && o.getResult().equals(LoginResultEnum.SUCCESS.getResult()))
        );
    }

}
