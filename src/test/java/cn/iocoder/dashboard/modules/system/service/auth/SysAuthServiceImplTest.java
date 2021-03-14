package cn.iocoder.dashboard.modules.system.service.auth;

import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.Set;

import static cn.iocoder.dashboard.util.RandomUtils.*;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
        SysUserDO user = randomUserDO(o -> o.setUsername(username));
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
        SysUserDO user = randomUserDO(o -> o.setId(userId));
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

}
