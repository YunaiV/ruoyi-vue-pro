package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.module.system.api.sms.SmsCodeApi;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.member.MemberService;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.social.SocialUserService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.anji.captcha.service.CaptchaService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(AdminAuthServiceImpl.class)
public class AdminAuthServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminAuthServiceImpl authService;

    @MockBean
    private AdminUserService userService;
    @MockBean
    private CaptchaService captchaService;
    @MockBean
    private LoginLogService loginLogService;
    @MockBean
    private SocialUserService socialService;
    @MockBean
    private SmsCodeApi smsCodeApi;
    @MockBean
    private OAuth2TokenService oauth2TokenService;
    @MockBean
    private MemberService memberService;

    @MockBean
    private Validator validator;

    @Test
    public void testAuthenticate_success() {
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
        AdminUserDO loginUser = authService.authenticate(username, password);
        // 校验
        assertPojoEquals(user, loginUser);
    }

    @Test
    public void testAuthenticate_userNotFound() {
        // 准备参数
        String username = randomString();
        String password = randomString();

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> authService.authenticate(username, password),
                AUTH_LOGIN_BAD_CREDENTIALS);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.BAD_CREDENTIALS.getResult())
                        && o.getUserId() == null)
        );
    }

    @Test
    public void testAuthenticate_badCredentials() {
        // 准备参数
        String username = randomString();
        String password = randomString();
        // mock user 数据
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);

        // 调用, 并断言异常
        AssertUtils.assertServiceException(() -> authService.authenticate(username, password),
                AUTH_LOGIN_BAD_CREDENTIALS);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.BAD_CREDENTIALS.getResult())
                        && o.getUserId().equals(user.getId()))
        );
    }

    @Test
    public void testAuthenticate_userDisabled() {
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
        AssertUtils.assertServiceException(() -> authService.authenticate(username, password),
                AUTH_LOGIN_USER_DISABLED);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.USER_DISABLED.getResult())
                        && o.getUserId().equals(user.getId()))
        );
    }

//    @Test
//    public void testCaptcha_success() {
//        // 准备参数
//        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
//
//        // mock 验证码正确
//        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
//
//        // 调用
//        authService.verifyCaptcha(reqVO);
//        // 断言
//        verify(captchaService).deleteCaptchaCode(reqVO.getUuid());
//    }
//
//    @Test
//    public void testCaptcha_notFound() {
//        // 准备参数
//        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
//
//        // 调用, 并断言异常
//        assertServiceException(() -> authService.verifyCaptcha(reqVO), AUTH_LOGIN_CAPTCHA_NOT_FOUND);
//        // 校验调用参数
//        verify(loginLogService, times(1)).createLoginLog(
//            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
//                    && o.getResult().equals(LoginResultEnum.CAPTCHA_NOT_FOUND.getResult()))
//        );
//    }

//    @Test
//    public void testCaptcha_codeError() {
//        // 准备参数
//        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
//
//        // mock 验证码不正确
//        String code = randomString();
//        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(code);
//
//        // 调用, 并断言异常
//        assertServiceException(() -> authService.verifyCaptcha(reqVO), AUTH_LOGIN_CAPTCHA_CODE_ERROR);
//        // 校验调用参数
//        verify(loginLogService).createLoginLog(
//            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
//                    && o.getResult().equals(LoginResultEnum.CAPTCHA_CODE_ERROR.getResult()))
//        );
//    }

//    @Test
//    public void testLogin_success() {
//        // 准备参数
//        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class, o ->
//                o.setUsername("test_username").setPassword("test_password"));
//
//        // mock 验证码正确
//        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
//        // mock user 数据
//        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setId(1L).setUsername("test_username")
//                .setPassword("test_password").setStatus(CommonStatusEnum.ENABLE.getStatus()));
//        when(userService.getUserByUsername(eq("test_username"))).thenReturn(user);
//        // mock password 匹配
//        when(userService.isPasswordMatch(eq("test_password"), eq(user.getPassword()))).thenReturn(true);
//        // mock 缓存登录用户到 Redis
//        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class, o -> o.setUserId(1L)
//                .setUserType(UserTypeEnum.ADMIN.getValue()));
//        when(oauth2TokenService.createAccessToken(eq(1L), eq(UserTypeEnum.ADMIN.getValue()), eq("default"), isNull()))
//                .thenReturn(accessTokenDO);
//
//        // 调用, 并断言异常
//        AuthLoginRespVO loginRespVO = authService.login(reqVO);
//        assertPojoEquals(accessTokenDO, loginRespVO);
//        // 校验调用参数
//        verify(loginLogService).createLoginLog(
//            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
//                    && o.getResult().equals(LoginResultEnum.SUCCESS.getResult())
//                    && o.getUserId().equals(user.getId()))
//        );
//    }

    @Test
    public void testLogout_success() {
        // 准备参数
        String token = randomString();
        // mock
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class, o -> o.setUserId(1L)
                .setUserType(UserTypeEnum.ADMIN.getValue()));
        when(oauth2TokenService.removeAccessToken(eq(token))).thenReturn(accessTokenDO);

        // 调用
        authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        // 校验调用参数
        verify(loginLogService).createLoginLog(argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGOUT_SELF.getType())
                    && o.getResult().equals(LoginResultEnum.SUCCESS.getResult()))
        );
    }

    @Test
    public void testLogout_fail() {
        // 准备参数
        String token = randomString();

        // 调用
        authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        // 校验调用参数
        verify(loginLogService, never()).createLoginLog(any());
    }

}
