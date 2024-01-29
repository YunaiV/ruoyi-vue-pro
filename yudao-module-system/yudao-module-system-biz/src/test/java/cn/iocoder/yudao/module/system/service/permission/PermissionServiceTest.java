package cn.iocoder.yudao.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.enums.permission.DataScopeEnum;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.hutool.core.collection.ListUtil.toList;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import({PermissionServiceImpl.class})
public class PermissionServiceTest extends BaseDbUnitTest {

    @Resource
    private PermissionServiceImpl permissionService;

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;

    @MockBean
    private RoleService roleService;
    @MockBean
    private MenuService menuService;
    @MockBean
    private DeptService deptService;
    @MockBean
    private AdminUserService userService;

    @Test
    public void testHasAnyPermissions_superAdmin() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            String[] roles = new String[]{"system:user:query", "system:user:create"};
            // mock 用户登录的角色
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(100L));
            RoleDO role = randomPojo(RoleDO.class, o -> o.setId(100L)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(singleton(100L)))).thenReturn(toList(role));
            // mock 其它方法
            when(roleService.hasAnySuperAdmin(eq(asSet(100L)))).thenReturn(true);

            // 调用，并断言
            assertTrue(permissionService.hasAnyPermissions(userId, roles));
        }
    }

    @Test
    public void testHasAnyPermissions_normal() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            String[] roles = new String[]{"system:user:query", "system:user:create"};
            // mock 用户登录的角色
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(100L));
            RoleDO role = randomPojo(RoleDO.class, o -> o.setId(100L)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(singleton(100L)))).thenReturn(toList(role));
            // mock 菜单
            Long menuId = 1000L;
            when(menuService.getMenuIdListByPermissionFromCache(
                    eq("system:user:create"))).thenReturn(singletonList(menuId));
            roleMenuMapper.insert(randomPojo(RoleMenuDO.class).setRoleId(100L).setMenuId(1000L));

            // 调用，并断言
            assertTrue(permissionService.hasAnyPermissions(userId, roles));
        }
    }

    @Test
    public void testHasAnyRoles() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            String[] roles = new String[]{"yunai", "tudou"};
            // mock 用户与角色的缓存
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(100L));
            RoleDO role = randomPojo(RoleDO.class, o -> o.setId(100L).setCode("tudou")
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(singleton(100L)))).thenReturn(toList(role));

            // 调用，并断言
            assertTrue(permissionService.hasAnyRoles(userId, roles));
        }
    }

    // ========== 角色-菜单的相关方法  ==========

    @Test
    public void testAssignRoleMenu() {
        // 准备参数
        Long roleId = 1L;
        Set<Long> menuIds = asSet(200L, 300L);
        // mock 数据
        RoleMenuDO roleMenu01 = randomPojo(RoleMenuDO.class).setRoleId(1L).setMenuId(100L);
        roleMenuMapper.insert(roleMenu01);
        RoleMenuDO roleMenu02 = randomPojo(RoleMenuDO.class).setRoleId(1L).setMenuId(200L);
        roleMenuMapper.insert(roleMenu02);

        // 调用
        permissionService.assignRoleMenu(roleId, menuIds);
        // 断言
        List<RoleMenuDO> roleMenuList = roleMenuMapper.selectList();
        assertEquals(2, roleMenuList.size());
        assertEquals(1L, roleMenuList.get(0).getRoleId());
        assertEquals(200L, roleMenuList.get(0).getMenuId());
        assertEquals(1L, roleMenuList.get(1).getRoleId());
        assertEquals(300L, roleMenuList.get(1).getMenuId());
    }

    @Test
    public void testProcessRoleDeleted() {
        // 准备参数
        Long roleId = randomLongId();
        // mock 数据 UserRole
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setRoleId(roleId)); // 被删除
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO userRoleDO02 = randomPojo(UserRoleDO.class); // 不被删除
        userRoleMapper.insert(userRoleDO02);
        // mock 数据 RoleMenu
        RoleMenuDO roleMenuDO01 = randomPojo(RoleMenuDO.class, o -> o.setRoleId(roleId)); // 被删除
        roleMenuMapper.insert(roleMenuDO01);
        RoleMenuDO roleMenuDO02 = randomPojo(RoleMenuDO.class); // 不被删除
        roleMenuMapper.insert(roleMenuDO02);

        // 调用
        permissionService.processRoleDeleted(roleId);
        // 断言数据 RoleMenuDO
        List<RoleMenuDO> dbRoleMenus = roleMenuMapper.selectList();
        assertEquals(1, dbRoleMenus.size());
        assertPojoEquals(dbRoleMenus.get(0), roleMenuDO02);
        // 断言数据 UserRoleDO
        List<UserRoleDO> dbUserRoles = userRoleMapper.selectList();
        assertEquals(1, dbUserRoles.size());
        assertPojoEquals(dbUserRoles.get(0), userRoleDO02);
    }

    @Test
    public void testProcessMenuDeleted() {
        // 准备参数
        Long menuId = randomLongId();
        // mock 数据
        RoleMenuDO roleMenuDO01 = randomPojo(RoleMenuDO.class, o -> o.setMenuId(menuId)); // 被删除
        roleMenuMapper.insert(roleMenuDO01);
        RoleMenuDO roleMenuDO02 = randomPojo(RoleMenuDO.class); // 不被删除
        roleMenuMapper.insert(roleMenuDO02);

        // 调用
        permissionService.processMenuDeleted(menuId);
        // 断言数据
        List<RoleMenuDO> dbRoleMenus = roleMenuMapper.selectList();
        assertEquals(1, dbRoleMenus.size());
        assertPojoEquals(dbRoleMenus.get(0), roleMenuDO02);
    }

    @Test
    public void testGetRoleMenuIds_superAdmin() {
        // 准备参数
        Long roleId = 100L;
        // mock 方法
        when(roleService.hasAnySuperAdmin(eq(singleton(100L)))).thenReturn(true);
        List<MenuDO> menuList = singletonList(randomPojo(MenuDO.class).setId(1L));
        when(menuService.getMenuList()).thenReturn(menuList);

        // 调用
        Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(roleId);
        // 断言
        assertEquals(singleton(1L), menuIds);
    }

    @Test
    public void testGetRoleMenuIds_normal() {
        // 准备参数
        Long roleId = 100L;
        // mock 数据
        RoleMenuDO roleMenu01 = randomPojo(RoleMenuDO.class).setRoleId(100L).setMenuId(1L);
        roleMenuMapper.insert(roleMenu01);
        RoleMenuDO roleMenu02 = randomPojo(RoleMenuDO.class).setRoleId(100L).setMenuId(2L);
        roleMenuMapper.insert(roleMenu02);

        // 调用
        Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(roleId);
        // 断言
        assertEquals(asSet(1L, 2L), menuIds);
    }

    @Test
    public void testGetMenuRoleIdListByMenuIdFromCache() {
        // 准备参数
        Long menuId = 1L;
        // mock 数据
        RoleMenuDO roleMenu01 = randomPojo(RoleMenuDO.class).setRoleId(100L).setMenuId(1L);
        roleMenuMapper.insert(roleMenu01);
        RoleMenuDO roleMenu02 = randomPojo(RoleMenuDO.class).setRoleId(200L).setMenuId(1L);
        roleMenuMapper.insert(roleMenu02);

        // 调用
        Set<Long> roleIds = permissionService.getMenuRoleIdListByMenuIdFromCache(menuId);
        // 断言
        assertEquals(asSet(100L, 200L), roleIds);
    }

    // ========== 用户-角色的相关方法  ==========

    @Test
    public void testAssignUserRole() {
        // 准备参数
        Long userId = 1L;
        Set<Long> roleIds = asSet(200L, 300L);
        // mock 数据
        UserRoleDO userRole01 = randomPojo(UserRoleDO.class).setUserId(1L).setRoleId(100L);
        userRoleMapper.insert(userRole01);
        UserRoleDO userRole02 = randomPojo(UserRoleDO.class).setUserId(1L).setRoleId(200L);
        userRoleMapper.insert(userRole02);

        // 调用
        permissionService.assignUserRole(userId, roleIds);
        // 断言
        List<UserRoleDO> userRoleDOList = userRoleMapper.selectList();
        assertEquals(2, userRoleDOList.size());
        assertEquals(1L, userRoleDOList.get(0).getUserId());
        assertEquals(200L, userRoleDOList.get(0).getRoleId());
        assertEquals(1L, userRoleDOList.get(1).getUserId());
        assertEquals(300L, userRoleDOList.get(1).getRoleId());
    }

    @Test
    public void testProcessUserDeleted() {
        // 准备参数
        Long userId = randomLongId();
        // mock 数据
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(userId)); // 被删除
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO userRoleDO02 = randomPojo(UserRoleDO.class); // 不被删除
        userRoleMapper.insert(userRoleDO02);

        // 调用
        permissionService.processUserDeleted(userId);
        // 断言数据
        List<UserRoleDO> dbUserRoles = userRoleMapper.selectList();
        assertEquals(1, dbUserRoles.size());
        assertPojoEquals(dbUserRoles.get(0), userRoleDO02);
    }

    @Test
    public void testGetUserRoleIdListByUserId() {
        // 准备参数
        Long userId = 1L;
        // mock 数据
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(10L));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(20L));
        userRoleMapper.insert(roleMenuDO02);

        // 调用
        Set<Long> result = permissionService.getUserRoleIdListByUserId(userId);
        // 断言
        assertEquals(asSet(10L, 20L), result);
    }

    @Test
    public void testGetUserRoleIdListByUserIdFromCache() {
        // 准备参数
        Long userId = 1L;
        // mock 数据
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(10L));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(20L));
        userRoleMapper.insert(roleMenuDO02);

        // 调用
        Set<Long> result = permissionService.getUserRoleIdListByUserIdFromCache(userId);
        // 断言
        assertEquals(asSet(10L, 20L), result);
    }

    @Test
    public void testGetUserRoleIdsFromCache() {
        // 准备参数
        Long userId = 1L;
        // mock 数据
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(10L));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(20L));
        userRoleMapper.insert(roleMenuDO02);

        // 调用
        Set<Long> result = permissionService.getUserRoleIdListByUserIdFromCache(userId);
        // 断言
        assertEquals(asSet(10L, 20L), result);
    }

    @Test
    public void testGetUserRoleIdListByRoleId() {
        // 准备参数
        Collection<Long> roleIds = asSet(10L, 20L);
        // mock 数据
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(10L));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId(2L).setRoleId(20L));
        userRoleMapper.insert(roleMenuDO02);

        // 调用
        Set<Long> result = permissionService.getUserRoleIdListByRoleId(roleIds);
        // 断言
        assertEquals(asSet(1L, 2L), result);
    }

    @Test
    public void testGetEnableUserRoleListByUserIdFromCache() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            // mock 用户登录的角色
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(100L));
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(200L));
            RoleDO role01 = randomPojo(RoleDO.class, o -> o.setId(100L)
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            RoleDO role02 = randomPojo(RoleDO.class, o -> o.setId(200L)
                    .setStatus(CommonStatusEnum.DISABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(asSet(100L, 200L))))
                    .thenReturn(toList(role01, role02));

            // 调用
            List<RoleDO> result = permissionService.getEnableUserRoleListByUserIdFromCache(userId);
            // 断言
            assertEquals(1, result.size());
            assertPojoEquals(role01, result.get(0));
        }
    }

    // ========== 用户-部门的相关方法  ==========

    @Test
    public void testAssignRoleDataScope() {
        // 准备参数
        Long roleId = 1L;
        Integer dataScope = 2;
        Set<Long> dataScopeDeptIds = asSet(10L, 20L);

        // 调用
        permissionService.assignRoleDataScope(roleId, dataScope, dataScopeDeptIds);
        // 断言
        verify(roleService).updateRoleDataScope(eq(roleId), eq(dataScope), eq(dataScopeDeptIds));
    }

    @Test
    public void testGetDeptDataPermission_All() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            // mock 用户的角色编号
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(2L));
            // mock 获得用户的角色
            RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.ALL.getScope())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(singleton(2L)))).thenReturn(toList(roleDO));

            // 调用
            DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
            // 断言
            assertTrue(result.getAll());
            assertFalse(result.getSelf());
            assertTrue(CollUtil.isEmpty(result.getDeptIds()));
        }
    }

    @Test
    public void testGetDeptDataPermission_DeptCustom() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            // mock 用户的角色编号
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(2L));
            // mock 获得用户的角色
            RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_CUSTOM.getScope())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(singleton(2L)))).thenReturn(toList(roleDO));
            // mock 部门的返回
            when(userService.getUser(eq(1L))).thenReturn(new AdminUserDO().setDeptId(3L),
                    null, null); // 最后返回 null 的目的，看看会不会重复调用

            // 调用
            DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
            // 断言
            assertFalse(result.getAll());
            assertFalse(result.getSelf());
            assertEquals(roleDO.getDataScopeDeptIds().size() + 1, result.getDeptIds().size());
            assertTrue(CollUtil.containsAll(result.getDeptIds(), roleDO.getDataScopeDeptIds()));
            assertTrue(CollUtil.contains(result.getDeptIds(), 3L));
        }
    }

    @Test
    public void testGetDeptDataPermission_DeptOnly() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            // mock 用户的角色编号
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(2L));
            // mock 获得用户的角色
            RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_ONLY.getScope())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(singleton(2L)))).thenReturn(toList(roleDO));
            // mock 部门的返回
            when(userService.getUser(eq(1L))).thenReturn(new AdminUserDO().setDeptId(3L),
                    null, null); // 最后返回 null 的目的，看看会不会重复调用

            // 调用
            DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
            // 断言
            assertFalse(result.getAll());
            assertFalse(result.getSelf());
            assertEquals(1, result.getDeptIds().size());
            assertTrue(CollUtil.contains(result.getDeptIds(), 3L));
        }
    }

    @Test
    public void testGetDeptDataPermission_DeptAndChild() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            // mock 用户的角色编号
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(2L));
            // mock 获得用户的角色
            RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_AND_CHILD.getScope())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(singleton(2L)))).thenReturn(toList(roleDO));
            // mock 部门的返回
            when(userService.getUser(eq(1L))).thenReturn(new AdminUserDO().setDeptId(3L),
                    null, null); // 最后返回 null 的目的，看看会不会重复调用
            // mock 方法（部门)
            DeptDO deptDO = randomPojo(DeptDO.class);
            when(deptService.getChildDeptIdListFromCache(eq(3L))).thenReturn(singleton(deptDO.getId()));

            // 调用
            DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
            // 断言
            assertFalse(result.getAll());
            assertFalse(result.getSelf());
            assertEquals(2, result.getDeptIds().size());
            assertTrue(CollUtil.contains(result.getDeptIds(), deptDO.getId()));
            assertTrue(CollUtil.contains(result.getDeptIds(), 3L));
        }
    }

    @Test
    public void testGetDeptDataPermission_Self() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(PermissionServiceImpl.class)))
                    .thenReturn(permissionService);

            // 准备参数
            Long userId = 1L;
            // mock 用户的角色编号
            userRoleMapper.insert(randomPojo(UserRoleDO.class).setUserId(userId).setRoleId(2L));
            // mock 获得用户的角色
            RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.SELF.getScope())
                    .setStatus(CommonStatusEnum.ENABLE.getStatus()));
            when(roleService.getRoleListFromCache(eq(singleton(2L)))).thenReturn(toList(roleDO));

            // 调用
            DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
            // 断言
            assertFalse(result.getAll());
            assertTrue(result.getSelf());
            assertTrue(CollUtil.isEmpty(result.getDeptIds()));
        }
    }

}
