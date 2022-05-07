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
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.social.SocialUserService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.validation.Validator;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(AdminAuthServiceImpl.class)
public class AuthServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminAuthServiceImpl authService;

    @MockBean
    private AdminUserService userService;
    @MockBean
    private CaptchaService captchaService;
    @MockBean
    private LoginLogService loginLogService;
    @MockBean
    private UserSessionService userSessionService;
    @MockBean
    private SocialUserService socialService;
    @MockBean
    private SmsCodeApi smsCodeApi;

    @MockBean
    private Validator validator;

    @BeforeEach
    public void setUp() {
        when(captchaService.isCaptchaEnable()).thenReturn(true);
    }

    @Test
    public void testLogin0_success() {
        // 准备参数
        String username = randomString();
        String password = randomString();
        // mock user 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);
        // mock password 匹配
        when(userService.isPasswordMatch(eq(password), eq(user.getPassword()))).thenReturn(true);

        // 调用
        LoginUser loginUser = authService.login0(username, password);
        // 校验
        assertPojoEquals(user, loginUser);
    }

    @Test
    public void testLogin0_userNotFound() {
        // 准备参数
        String username = randomString();
        String password = randomString();

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> authService.login0(username, password),
                AUTH_LOGIN_BAD_CREDENTIALS);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.BAD_CREDENTIALS.getResult())
                        && o.getUserId() == null)
        );
    }

    @Test
    public void testLogin0_badCredentials() {
        // 准备参数
        String username = randomString();
        String password = randomString();
        // mock user 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> authService.login0(username, password),
                AUTH_LOGIN_BAD_CREDENTIALS);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.BAD_CREDENTIALS.getResult())
                        && o.getUserId().equals(user.getId()))
        );
    }

    @Test
    public void testLogin0_userDisabled() {
        // 准备参数
        String username = randomString();
        String password = randomString();
        // mock user 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);
        // mock password 匹配
        when(userService.isPasswordMatch(eq(password), eq(user.getPassword()))).thenReturn(true);

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> authService.login0(username, password),
                AUTH_LOGIN_USER_DISABLED);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.USER_DISABLED.getResult())
                        && o.getUserId().equals(user.getId()))
        );
    }

    @Test
    public void testCaptcha_success() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);

        // mock 验证码正确
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());

        // 调用
        authService.verifyCaptcha(reqVO);
        // 断言
        verify(captchaService).deleteCaptchaCode(reqVO.getUuid());
    }

    @Test
    public void testCaptcha_notFound() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> authService.verifyCaptcha(reqVO), AUTH_LOGIN_CAPTCHA_NOT_FOUND);
        // 校验调用参数
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.CAPTCHA_NOT_FOUND.getResult()))
        );
    }

    @Test
    public void testCaptcha_codeError() {
        // 准备参数
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);

        // mock 验证码不正确
        String code = randomString();
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(code);

        // 调用, 并断言异常
        assertServiceException(() -> authService.verifyCaptcha(reqVO), AUTH_LOGIN_CAPTCHA_CODE_ERROR);
        // 校验调用参数
        verify(loginLogService).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.CAPTCHA_CODE_ERROR.getResult()))
        );
    }

    @Test
    public void testLogin_success() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class, o ->
                o.setUsername("test_username").setPassword("test_password"));

        // mock 验证码正确
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
        // mock user 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername("test_username")
                .setPassword("test_password").setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(userService.getUserByUsername(eq("test_username"))).thenReturn(user);
        // mock password 匹配
        when(userService.isPasswordMatch(eq("test_password"), eq(user.getPassword()))).thenReturn(true);
        // mock 缓存登录用户到 Redis
        String token = randomString();
        when(userSessionService.createUserSession(argThat(argument -> {
            AssertUtils.assertPojoEquals(user, argument);
            return true;
        }), eq(userIp), eq(userAgent))).thenReturn(token);

        // 调用, 并断言异常
        String result = authService.login(reqVO, userIp, userAgent);
        assertEquals(token, result);
        // 校验调用参数
        verify(loginLogService).createLoginLog(
            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(LoginResultEnum.SUCCESS.getResult())
                    && o.getUserId().equals(user.getId()))
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
