package cn.iocoder.yudao.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuBatchInsertMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleBatchInsertMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.enums.permission.DataScopeEnum;
import cn.iocoder.yudao.module.system.mq.producer.permission.PermissionProducer;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({PermissionServiceImpl.class,
        RoleMenuBatchInsertMapper.class, UserRoleBatchInsertMapper.class})
public class PermissionServiceTest extends BaseDbUnitTest {

    @Resource
    private PermissionServiceImpl permissionService;

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private RoleMenuBatchInsertMapper roleMenuBatchInsertMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserRoleBatchInsertMapper userRoleBatchInsertMapper;

    @MockBean
    private RoleService roleService;
    @MockBean
    private MenuService menuService;
    @MockBean
    private DeptService deptService;
    @MockBean
    private AdminUserService userService;

    @MockBean
    private PermissionProducer permissionProducer;

    @Test
    public void testInitRoleMenuLocalCache() {
        // mock 数据
        RoleMenuDO roleMenuDO01 = randomPojo(RoleMenuDO.class, o -> o.setRoleId(1L).setMenuId(10L));
        roleMenuMapper.insert(roleMenuDO01);
        RoleMenuDO roleMenuDO02 = randomPojo(RoleMenuDO.class, o -> o.setRoleId(1L).setMenuId(20L));
        roleMenuMapper.insert(roleMenuDO02);

        // 调用
        permissionService.initRoleMenuLocalCache();
        // 断言 roleMenuCache 缓存
        assertEquals(1, permissionService.getRoleMenuCache().keySet().size());
        assertEquals(asList(10L, 20L), permissionService.getRoleMenuCache().get(1L));
        // 断言 menuRoleCache 缓存
        assertEquals(2, permissionService.getMenuRoleCache().size());
        assertEquals(singletonList(1L), permissionService.getMenuRoleCache().get(10L));
        assertEquals(singletonList(1L), permissionService.getMenuRoleCache().get(20L));
        // 断言 maxUpdateTime 缓存
        LocalDateTime maxUpdateTime = permissionService.getRoleMenuMaxUpdateTime();
        assertEquals(ObjectUtils.max(roleMenuDO01.getUpdateTime(), roleMenuDO02.getUpdateTime()), maxUpdateTime);
    }

    @Test
    public void testInitUserRoleLocalCache() {
        // mock 数据
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(10L));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(20L));
        userRoleMapper.insert(roleMenuDO02);

        // 调用
        permissionService.initUserRoleLocalCache();
        // 断言 roleMenuCache 缓存
        assertEquals(1, permissionService.getUserRoleCache().size());
        assertEquals(asSet(10L, 20L), permissionService.getUserRoleCache().get(1L));
        // 断言 maxUpdateTime 缓存
        LocalDateTime maxUpdateTime = permissionService.getUserRoleMaxUpdateTime();
        assertEquals(ObjectUtils.max(userRoleDO01.getUpdateTime(), roleMenuDO02.getUpdateTime()), maxUpdateTime);
    }

    @Test
    public void testGetRoleMenuListFromCache_superAdmin() {
        // 准备参数
        Collection<Long> roleIds = singletonList(100L);
        Collection<Integer> menuTypes = asList(2, 3);
        Collection<Integer> menusStatuses = asList(0, 1);
        // mock 方法
        List<RoleDO> roleList = singletonList(randomPojo(RoleDO.class, o -> o.setId(100L)));
        when(roleService.getRolesFromCache(eq(roleIds))).thenReturn(roleList);
        when(roleService.hasAnySuperAdmin(same(roleList))).thenReturn(true);
        List<MenuDO> menuList = randomPojoList(MenuDO.class);
        when(menuService.getMenuListFromCache(eq(menuTypes), eq(menusStatuses))).thenReturn(menuList);

        // 调用
        List<MenuDO> result = permissionService.getRoleMenuListFromCache(roleIds, menuTypes, menusStatuses);
        // 断言
        assertSame(menuList, result);
    }

    @Test
    public void testGetRoleMenuListFromCache_normal() {
        // 准备参数
        Collection<Long> roleIds = asSet(100L, 200L);
        Collection<Integer> menuTypes = asList(2, 3);
        Collection<Integer> menusStatuses = asList(0, 1);
        // mock 方法
        Multimap<Long, Long> roleMenuCache = ImmutableMultimap.<Long, Long>builder().put(100L, 1000L)
                .put(200L, 2000L).put(200L, 2001L).build();
        permissionService.setRoleMenuCache(roleMenuCache);
        List<MenuDO> menuList = randomPojoList(MenuDO.class);
        when(menuService.getMenuListFromCache(eq(asList(1000L, 2000L, 2001L)), eq(menuTypes), eq(menusStatuses))).thenReturn(menuList);

        // 调用
        List<MenuDO> result = permissionService.getRoleMenuListFromCache(roleIds, menuTypes, menusStatuses);
        // 断言
        assertSame(menuList, result);
    }

    @Test
    public void testGetUserRoleIdsFromCache() {
        // 准备参数
        Long userId = 1L;
        Collection<Integer> roleStatuses = singleton(CommonStatusEnum.ENABLE.getStatus());
        // mock 方法
        Map<Long, Set<Long>> userRoleCache = MapUtil.<Long, Set<Long>>builder()
                .put(1L, asSet(10L, 20L)).build();
        permissionService.setUserRoleCache(userRoleCache);
        RoleDO roleDO01 = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq(10L))).thenReturn(roleDO01);
        RoleDO roleDO02 = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        when(roleService.getRoleFromCache(eq(20L))).thenReturn(roleDO02);

        // 调用
        Set<Long> roleIds = permissionService.getUserRoleIdsFromCache(userId, roleStatuses);
        // 断言
        assertEquals(asSet(10L), roleIds);
    }

    @Test
    public void testGetRoleMenuIds_superAdmin() {
        // 准备参数
        Long roleId = 100L;
        // mock 方法
        when(roleService.hasAnySuperAdmin(eq(singleton(100L)))).thenReturn(true);
        List<MenuDO> menuList = singletonList(randomPojo(MenuDO.class).setId(1L));
        when(menuService.getMenus()).thenReturn(menuList);

        // 调用
        Set<Long> menuIds = permissionService.getRoleMenuIds(roleId);
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
        Set<Long> menuIds = permissionService.getRoleMenuIds(roleId);
        // 断言
        assertEquals(asSet(1L, 2L), menuIds);
    }

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
        verify(permissionProducer).sendRoleMenuRefreshMessage();
    }

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
        verify(permissionProducer).sendUserRoleRefreshMessage();
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
    public void testGetUserRoleIdListByRoleIds() {
        // 准备参数
        Collection<Long> roleIds = asSet(10L, 20L);
        // mock 数据
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(1L).setRoleId(10L));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId(2L).setRoleId(20L));
        userRoleMapper.insert(roleMenuDO02);

        // 调用
        Set<Long> result = permissionService.getUserRoleIdListByRoleIds(roleIds);
        // 断言
        assertEquals(asSet(1L, 2L), result);
    }

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
        // 断言调用
        verify(permissionProducer).sendRoleMenuRefreshMessage();
        verify(permissionProducer).sendUserRoleRefreshMessage();
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
        // 断言调用
        verify(permissionProducer).sendRoleMenuRefreshMessage();
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
        // 断言调用
        verify(permissionProducer).sendUserRoleRefreshMessage();
    }

    @Test
    public void testHasAnyPermissions_superAdmin() {
        // 准备参数
        Long userId = 1L;
        String[] roles = new String[]{"system:user:query", "system:user:create"};
        // mock 用户与角色的缓存
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(100L)).build());
        RoleDO role = randomPojo(RoleDO.class, o -> o.setId(100L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq(100L))).thenReturn(role);
        // mock 其它方法
        when(roleService.hasAnySuperAdmin(eq(asSet(100L)))).thenReturn(true);

        // 调用
        boolean has = permissionService.hasAnyPermissions(userId, roles);
        // 断言
        assertTrue(has);
    }

    @Test
    public void testHasAnyPermissions_normal() {
        // 准备参数
        Long userId = 1L;
        String[] roles = new String[]{"system:user:query", "system:user:create"};
        // mock 用户与角色的缓存
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(100L)).build());
        RoleDO role = randomPojo(RoleDO.class, o -> o.setId(100L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq(100L))).thenReturn(role);
        // mock 其它方法
        MenuDO menu = randomPojo(MenuDO.class, o -> o.setId(1000L));
        when(menuService.getMenuListByPermissionFromCache(eq("system:user:create"))).thenReturn(singletonList(menu));
        permissionService.setMenuRoleCache(ImmutableMultimap.<Long, Long>builder().put(1000L, 100L).build());


        // 调用
        boolean has = permissionService.hasAnyPermissions(userId, roles);
        // 断言
        assertTrue(has);
    }

    @Test
    public void testHasAnyRoles_superAdmin() {
        // 准备参数
        Long userId = 1L;
        String[] roles = new String[]{"yunai", "tudou"};
        // mock 用户与角色的缓存
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(100L)).build());
        RoleDO role = randomPojo(RoleDO.class, o -> o.setId(100L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq(100L))).thenReturn(role);
        // mock 其它方法
        when(roleService.hasAnySuperAdmin(eq(asSet(100L)))).thenReturn(true);

        // 调用
        boolean has = permissionService.hasAnyRoles(userId, roles);
        // 断言
        assertTrue(has);
    }

    @Test
    public void testHasAnyRoles_normal() {
        // 准备参数
        Long userId = 1L;
        String[] roles = new String[]{"yunai", "tudou"};
        // mock 用户与角色的缓存
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(100L)).build());
        RoleDO role = randomPojo(RoleDO.class, o -> o.setId(100L).setCode("yunai")
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq(100L))).thenReturn(role);
        // mock 其它方法
        when(roleService.getRolesFromCache(eq(asSet(100L)))).thenReturn(singletonList(role));

        // 调用
        boolean has = permissionService.hasAnyRoles(userId, roles);
        // 断言
        assertTrue(has);
    }

    @Test
    public void testGetDeptDataPermission_All() {
        // 准备参数
        Long userId = 1L;
        // mock 用户的角色编号
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(2L)).build());
        // mock 获得用户的角色
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.ALL.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton(2L)))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq(2L))).thenReturn(roleDO);

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // 断言
        assertTrue(result.getAll());
        assertFalse(result.getSelf());
        assertTrue(CollUtil.isEmpty(result.getDeptIds()));
    }

    @Test
    public void testGetDeptDataPermission_DeptCustom() {
        // 准备参数
        Long userId = 1L;
        // mock 用户的角色编号
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(2L)).build());
        // mock 获得用户的角色
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_CUSTOM.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton(2L)))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq(2L))).thenReturn(roleDO);
        // mock 部门的返回
        when(userService.getUser(eq(1L))).thenReturn(new AdminUserDO().setDeptId(3L), null, null); // 最后返回 null 的目的，看看会不会重复调用

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // 断言
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(roleDO.getDataScopeDeptIds().size() + 1, result.getDeptIds().size());
        assertTrue(CollUtil.containsAll(result.getDeptIds(), roleDO.getDataScopeDeptIds()));
        assertTrue(CollUtil.contains(result.getDeptIds(), 3L));
    }

    @Test
    public void testGetDeptDataPermission_DeptOnly() {
        // 准备参数
        Long userId = 1L;
        // mock 用户的角色编号
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(2L)).build());
        // mock 获得用户的角色
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_ONLY.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton(2L)))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq(2L))).thenReturn(roleDO);
        // mock 部门的返回
        when(userService.getUser(eq(1L))).thenReturn(new AdminUserDO().setDeptId(3L), null, null); // 最后返回 null 的目的，看看会不会重复调用

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // 断言
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(1, result.getDeptIds().size());
        assertTrue(CollUtil.contains(result.getDeptIds(), 3L));
    }

    @Test
    public void testGetDeptDataPermission_DeptAndChild() {
        // 准备参数
        Long userId = 1L;
        // mock 用户的角色编号
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(2L)).build());
        // mock 获得用户的角色
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_AND_CHILD.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton(2L)))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq(2L))).thenReturn(roleDO);
        // mock 部门的返回
        when(userService.getUser(eq(1L))).thenReturn(new AdminUserDO().setDeptId(3L), null, null); // 最后返回 null 的目的，看看会不会重复调用
        // mock 方法（部门）
        DeptDO deptDO = randomPojo(DeptDO.class);
        when(deptService.getDeptsByParentIdFromCache(eq(3L), eq(true)))
                .thenReturn(singletonList(deptDO));

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // 断言
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(2, result.getDeptIds().size());
        assertTrue(CollUtil.contains(result.getDeptIds(), deptDO.getId()));
        assertTrue(CollUtil.contains(result.getDeptIds(), 3L));
    }

    @Test
    public void testGetDeptDataPermission_Self() {
        // 准备参数
        Long userId = 1L;
        // mock 用户的角色编号
        permissionService.setUserRoleCache(MapUtil.<Long, Set<Long>>builder().put(1L, asSet(2L)).build());
        // mock 获得用户的角色
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.SELF.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton(2L)))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq(2L))).thenReturn(roleDO);

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // 断言
        assertFalse(result.getAll());
        assertTrue(result.getSelf());
        assertTrue(CollUtil.isEmpty(result.getDeptIds()));
    }

}
