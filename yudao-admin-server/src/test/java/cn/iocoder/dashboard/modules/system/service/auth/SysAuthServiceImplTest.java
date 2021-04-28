package cn.iocoder.dashboard.modules.system.service.auth;

import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.auth.SysAuthLoginReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.enums.logger.SysLoginLogTypeEnum;
import cn.iocoder.dashboard.modules.system.enums.logger.SysLoginResultEnum;
import cn.iocoder.dashboard.modules.system.service.auth.impl.SysAuthServiceImpl;
import cn.iocoder.dashboard.modules.system.service.common.SysCaptchaService;
import cn.iocoder.dashboard.modules.system.service.logger.SysLoginLogService;
import cn.iocoder.dashboard.modules.system.service.permission.SysPermissionService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.AssertUtils;
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
import java.util.Set;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.dashboard.util.AssertUtils.assertServiceException;
import static cn.iocoder.dashboard.util.RandomUtils.*;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link SysAuthServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(SysAuthServiceImpl.class)
public class SysAuthServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SysAuthServiceImpl authService;

    @MockBean
    private SysUserService userService;
    @MockBean
    private SysPermissionService permissionService;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private Authentication authentication;
    @MockBean
    private SysCaptchaService captchaService;
    @MockBean
    private SysLoginLogService loginLogService;
    @MockBean
    private SysUserSessionService userSessionService;

    @Test
    public void testLoadUserByUsername_success() {
        // 准备参数
        String username = randomString();
        // mock 方法
        SysUserDO user = randomPojo(SysUserDO.class, o -> o.setUsername(username));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);

        // 调用
        LoginUser loginUser = (LoginUser) authService.loadUserByUsername(username);
        // 校验
        AssertUtils.assertPojoEquals(user, loginUser, "updateTime");
        assertNull(loginUser.getRoleIds()); // 此时不会加载角色，所以是空的
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
        SysUserDO user = randomPojo(SysUserDO.class, o -> o.setId(userId));
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
        SysAuthLoginReqVO reqVO = randomPojo(SysAuthLoginReqVO.class);
        String userIp = randomString();
        String userAgent = randomString();
        // 调用, 并断言异常
        assertServiceException(() -> authService.login(reqVO, userIp, userAgent), AUTH_LOGIN_CAPTCHA_NOT_FOUND);
        // 校验调用参数
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(SysLoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(SysLoginResultEnum.CAPTCHA_NOT_FOUND.getResult()))
        );
    }

    @Test
    public void testLogin_captchaCodeError() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        String code = randomString();
        SysAuthLoginReqVO reqVO = randomPojo(SysAuthLoginReqVO.class);
        // mock 验证码不正确
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(code);
        // 调用, 并断言异常
        assertServiceException(() -> authService.login(reqVO, userIp, userAgent), AUTH_LOGIN_CAPTCHA_CODE_ERROR);
        // 校验调用参数
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(SysLoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(SysLoginResultEnum.CAPTCHA_CODE_ERROR.getResult()))
        );
    }

    @Test
    public void testLogin_badCredentials() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        SysAuthLoginReqVO reqVO = randomPojo(SysAuthLoginReqVO.class);
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
            argThat(o -> o.getLogType().equals(SysLoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(SysLoginResultEnum.BAD_CREDENTIALS.getResult()))
        );
    }

    @Test
    public void testLogin_userDisabled() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        SysAuthLoginReqVO reqVO = randomPojo(SysAuthLoginReqVO.class);
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
            argThat(o -> o.getLogType().equals(SysLoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(SysLoginResultEnum.USER_DISABLED.getResult()))
        );
    }

    @Test
    public void testLogin_unknownError() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        SysAuthLoginReqVO reqVO = randomPojo(SysAuthLoginReqVO.class);
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
            argThat(o -> o.getLogType().equals(SysLoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(SysLoginResultEnum.UNKNOWN_ERROR.getResult()))
        );
    }

    @Test
    public void testLogin_success() {
        // 准备参数
        String userIp = randomString();
        String userAgent = randomString();
        Long userId = randomLongId();
        Set<Long> userRoleIds = randomSet(Long.class);
        String sessionId = randomString();
        SysAuthLoginReqVO reqVO = randomPojo(SysAuthLoginReqVO.class);
        LoginUser loginUser = randomPojo(LoginUser.class, o -> {
            o.setId(userId);
            o.setRoleIds(userRoleIds);
        });
        // mock 验证码正确
        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
        // mock authentication
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(reqVO.getUsername(), reqVO.getPassword())))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        // mock 获得 User 拥有的角色编号数组
        when(permissionService.getUserRoleIds(userId, singleton(CommonStatusEnum.ENABLE.getStatus()))).thenReturn(userRoleIds);
        // mock 缓存登陆用户到 Redis
        when(userSessionService.createUserSession(loginUser, userIp, userAgent)).thenReturn(sessionId);
        // 调用, 并断言异常
        String login = authService.login(reqVO, userIp, userAgent);
        assertEquals(sessionId, login);
        // 校验调用参数
        verify(captchaService, times(1)).deleteCaptchaCode(reqVO.getUuid());
        verify(loginLogService, times(1)).createLoginLog(
            argThat(o -> o.getLogType().equals(SysLoginLogTypeEnum.LOGIN_USERNAME.getType())
                    && o.getResult().equals(SysLoginResultEnum.SUCCESS.getResult()))
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
            argThat(o -> o.getLogType().equals(SysLoginLogTypeEnum.LOGOUT_SELF.getType())
                    && o.getResult().equals(SysLoginResultEnum.SUCCESS.getResult()))
        );
    }

}
