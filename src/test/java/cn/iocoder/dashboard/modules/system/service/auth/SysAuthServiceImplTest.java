package cn.iocoder.dashboard.modules.system.service.auth;

import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.service.auth.impl.SysAuthServiceImpl;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.AssertUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.util.RandomUtils.randomString;
import static cn.iocoder.dashboard.util.RandomUtils.randomUserDO;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class SysAuthServiceImplTest extends BaseSpringBootUnitTest {

    @Resource
    private SysAuthServiceImpl authService;

    @MockBean
    private SysUserService userService;

    @Test
    public void testLoadUserByUsername_success() {
        // 准备参数
        String username = randomString();
        // mock 方法
        SysUserDO user = randomUserDO();
        when(userService.getUserByUserName(eq(username))).thenReturn(user);

        // 调用
        LoginUser loginUser = (LoginUser) authService.loadUserByUsername(username);
        // 校验
        AssertUtils.assertEquals(user, loginUser, "updateTime");
        assertNull(loginUser.getRoleIds()); // 此时不会加载角色，所以是空的
    }

}
