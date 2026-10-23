package cn.iocoder.yudao.module.report.framework.jmreport.core.service;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link JmReportTokenServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class JmReportTokenServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private JmReportTokenServiceImpl jmReportTokenService;

    @Mock
    private OAuth2TokenCommonApi oauth2TokenApi;
    @Mock
    private PermissionCommonApi permissionApi;
    @Mock
    private SecurityProperties securityProperties;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    public void testGetPermissions_superAdmin() {
        // 准备参数
        Long userId = randomLongId();
        Long tenantId = randomLongId();
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId).setTenantId(tenantId),
                new MockHttpServletRequest());
        // mock 方法
        when(permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode())).thenReturn(true);

        // 调用
        String[] permissions = jmReportTokenService.getPermissions("token");
        // 断言：原有的大屏权限仍保留
        assertTrue(ArrayUtil.containsAll(permissions, "drag:datasource:testConnection", "drag:analysis:sql",
                "onl:drag:page:delete"));
        // 断言：大屏只校验权限、未配置时超级管理员会被拒绝的三处
        assertTrue(ArrayUtil.containsAll(permissions, "drag:datasource:queryById", "drag:datasource:deleteBatch",
                "onl:drag:getRawTableData"));
        // 断言：报表设计器的扩展权限
        assertTrue(ArrayUtil.containsAll(permissions, "jmreport:datasource:query", "jmreport:exportJob:export",
                "jmreport:ai:tableMeta"));
        assertEquals(permissions.length, ArrayUtil.distinct(permissions).length);
        assertEquals(tenantId, TenantContextHolder.getTenantId());
    }

    @Test
    public void testGetPermissions_notSuperAdmin() {
        // 准备参数
        Long userId = randomLongId();
        Long tenantId = randomLongId();
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId).setTenantId(tenantId),
                new MockHttpServletRequest());
        // mock 方法
        when(permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode())).thenReturn(false);

        // 调用
        String[] permissions = jmReportTokenService.getPermissions("token");
        // 断言
        assertNull(permissions);
        assertEquals(tenantId, TenantContextHolder.getTenantId());
    }

    @Test
    public void testGetPermissions_notLogin() {
        // 调用
        String[] permissions = jmReportTokenService.getPermissions("token");
        // 断言
        assertNull(permissions);
        assertNull(TenantContextHolder.getTenantId());
        verifyNoInteractions(permissionApi);
    }

    @Test
    public void testGetRoles_superAdmin() {
        // 准备参数
        Long userId = randomLongId();
        Long tenantId = randomLongId();
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId).setTenantId(tenantId),
                new MockHttpServletRequest());
        // mock 方法
        when(permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode())).thenReturn(true);

        // 调用
        String[] roles = jmReportTokenService.getRoles("token");
        // 断言
        assertArrayEquals(new String[]{"admin"}, roles);
        assertEquals(tenantId, TenantContextHolder.getTenantId());
    }

    @Test
    public void testGetRoles_notSuperAdmin() {
        // 准备参数
        Long userId = randomLongId();
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId).setTenantId(randomLongId()),
                new MockHttpServletRequest());
        // mock 方法
        when(permissionApi.hasAnyRoles(userId, RoleCodeEnum.SUPER_ADMIN.getCode())).thenReturn(false);

        // 调用
        String[] roles = jmReportTokenService.getRoles("token");
        // 断言
        assertNull(roles);
    }

    @Test
    public void testGetRoles_notLogin() {
        // 调用
        String[] roles = jmReportTokenService.getRoles("token");
        // 断言
        assertNull(roles);
        verifyNoInteractions(permissionApi);
    }

}
