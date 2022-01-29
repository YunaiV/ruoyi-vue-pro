package cn.iocoder.yudao.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.permission.SysRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.SysRoleMenuDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.permission.SysUserRoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.SysRoleMenuMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.SysUserRoleMapper;
import cn.iocoder.yudao.module.system.mq.producer.permission.SysPermissionProducer;
import cn.iocoder.yudao.module.system.service.dept.SysDeptService;
import cn.iocoder.yudao.framework.datapermission.core.dept.service.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.enums.DataScopeEnum;
import cn.iocoder.yudao.module.system.test.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(SysPermissionServiceImpl.class)
public class SysPermissionServiceTest extends BaseDbUnitTest {

    @Resource
    private SysPermissionServiceImpl permissionService;

    @Resource
    private SysRoleMenuMapper roleMenuMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;

    @MockBean
    private SysRoleService roleService;
    @MockBean
    private SysMenuService menuService;
    @MockBean
    private SysDeptService deptService;

    @MockBean
    private SysPermissionProducer permissionProducer;

    @Test
    public void testProcessRoleDeleted() {
        // 准备参数
        Long roleId = randomLongId();
        // mock 数据 UserRole
        SysUserRoleDO userRoleDO01 = randomPojo(SysUserRoleDO.class, o -> o.setRoleId(roleId)); // 被删除
        userRoleMapper.insert(userRoleDO01);
        SysUserRoleDO userRoleDO02 = randomPojo(SysUserRoleDO.class); // 不被删除
        userRoleMapper.insert(userRoleDO02);
        // mock 数据 RoleMenu
        SysRoleMenuDO roleMenuDO01 = randomPojo(SysRoleMenuDO.class, o -> o.setRoleId(roleId)); // 被删除
        roleMenuMapper.insert(roleMenuDO01);
        SysRoleMenuDO roleMenuDO02 = randomPojo(SysRoleMenuDO.class); // 不被删除
        roleMenuMapper.insert(roleMenuDO02);

        // 调用
        permissionService.processRoleDeleted(roleId);
        // 断言数据 RoleMenuDO
        List<SysRoleMenuDO> dbRoleMenus = roleMenuMapper.selectList();
        assertEquals(1, dbRoleMenus.size());
        assertPojoEquals(dbRoleMenus.get(0), roleMenuDO02);
        // 断言数据 UserRoleDO
        List<SysUserRoleDO> dbUserRoles = userRoleMapper.selectList();
        assertEquals(1, dbUserRoles.size());
        assertPojoEquals(dbUserRoles.get(0), userRoleDO02);
        // 断言调用
        verify(permissionProducer).sendRoleMenuRefreshMessage();
    }

    @Test
    public void testProcessMenuDeleted() {
        // 准备参数
        Long menuId = randomLongId();
        // mock 数据
        SysRoleMenuDO roleMenuDO01 = randomPojo(SysRoleMenuDO.class, o -> o.setMenuId(menuId)); // 被删除
        roleMenuMapper.insert(roleMenuDO01);
        SysRoleMenuDO roleMenuDO02 = randomPojo(SysRoleMenuDO.class); // 不被删除
        roleMenuMapper.insert(roleMenuDO02);

        // 调用
        permissionService.processMenuDeleted(menuId);
        // 断言数据
        List<SysRoleMenuDO> dbRoleMenus = roleMenuMapper.selectList();
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
        SysUserRoleDO userRoleDO01 = randomPojo(SysUserRoleDO.class, o -> o.setUserId(userId)); // 被删除
        userRoleMapper.insert(userRoleDO01);
        SysUserRoleDO userRoleDO02 = randomPojo(SysUserRoleDO.class); // 不被删除
        userRoleMapper.insert(userRoleDO02);

        // 调用
        permissionService.processUserDeleted(userId);
        // 断言数据
        List<SysUserRoleDO> dbUserRoles = userRoleMapper.selectList();
        assertEquals(1, dbUserRoles.size());
        assertPojoEquals(dbUserRoles.get(0), userRoleDO02);
    }

    @Test // 测试从 context 获取的场景
    public void testGetDeptDataPermission_fromContext() {
        // 准备参数
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock 方法
        DeptDataPermissionRespDTO respDTO = new DeptDataPermissionRespDTO();
        loginUser.setContext(SysPermissionServiceImpl.CONTEXT_KEY, respDTO);

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(loginUser);
        // 断言
        assertSame(respDTO, result);
    }

    @Test
    public void testGetDeptDataPermission_All() {
        // 准备参数
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock 方法
        SysRoleDO roleDO = randomPojo(SysRoleDO.class, o -> o.setDataScope(DataScopeEnum.ALL.getScope()));
        when(roleService.getRolesFromCache(same(loginUser.getRoleIds()))).thenReturn(singletonList(roleDO));

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(loginUser);
        // 断言
        assertTrue(result.getAll());
        assertFalse(result.getSelf());
        assertTrue(CollUtil.isEmpty(result.getDeptIds()));
        assertSame(result, loginUser.getContext(SysPermissionServiceImpl.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
    }

    @Test
    public void testGetDeptDataPermission_DeptCustom() {
        // 准备参数
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock 方法
        SysRoleDO roleDO = randomPojo(SysRoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_CUSTOM.getScope()));
        when(roleService.getRolesFromCache(same(loginUser.getRoleIds()))).thenReturn(singletonList(roleDO));

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(loginUser);
        // 断言
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(roleDO.getDataScopeDeptIds().size() + 1, result.getDeptIds().size());
        assertTrue(CollUtil.containsAll(result.getDeptIds(), roleDO.getDataScopeDeptIds()));
        assertTrue(CollUtil.contains(result.getDeptIds(), loginUser.getDeptId()));
        assertSame(result, loginUser.getContext(SysPermissionServiceImpl.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
    }

    @Test
    public void testGetDeptDataPermission_DeptOnly() {
        // 准备参数
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock 方法
        SysRoleDO roleDO = randomPojo(SysRoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_ONLY.getScope()));
        when(roleService.getRolesFromCache(same(loginUser.getRoleIds()))).thenReturn(singletonList(roleDO));

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(loginUser);
        // 断言
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(1, result.getDeptIds().size());
        assertTrue(CollUtil.contains(result.getDeptIds(), loginUser.getDeptId()));
        assertSame(result, loginUser.getContext(SysPermissionServiceImpl.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
    }

    @Test
    public void testGetDeptDataPermission_DeptAndChild() {
        // 准备参数
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock 方法（角色）
        SysRoleDO roleDO = randomPojo(SysRoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_AND_CHILD.getScope()));
        when(roleService.getRolesFromCache(same(loginUser.getRoleIds()))).thenReturn(singletonList(roleDO));
        // mock 方法（部门）
        SysDeptDO deptDO = randomPojo(SysDeptDO.class);
        when(deptService.getDeptsByParentIdFromCache(eq(loginUser.getDeptId()), eq(true)))
                .thenReturn(singletonList(deptDO));

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(loginUser);
        // 断言
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(1, result.getDeptIds().size());
        assertTrue(CollUtil.contains(result.getDeptIds(), deptDO.getId()));
        assertSame(result, loginUser.getContext(SysPermissionServiceImpl.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
    }

    @Test
    public void testGetDeptDataPermission_Self() {
        // 准备参数
        LoginUser loginUser = randomPojo(LoginUser.class);
        // mock 方法
        SysRoleDO roleDO = randomPojo(SysRoleDO.class, o -> o.setDataScope(DataScopeEnum.SELF.getScope()));
        when(roleService.getRolesFromCache(same(loginUser.getRoleIds()))).thenReturn(singletonList(roleDO));

        // 调用
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(loginUser);
        // 断言
        assertFalse(result.getAll());
        assertTrue(result.getSelf());
        assertTrue(CollUtil.isEmpty(result.getDeptIds()));
        assertSame(result, loginUser.getContext(SysPermissionServiceImpl.CONTEXT_KEY, DeptDataPermissionRespDTO.class));
    }

}
