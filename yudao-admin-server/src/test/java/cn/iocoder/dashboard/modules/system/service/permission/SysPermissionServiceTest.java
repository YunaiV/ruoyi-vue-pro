package cn.iocoder.dashboard.modules.system.service.permission;

import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.modules.system.dal.dataobject.permission.SysRoleMenuDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.permission.SysUserRoleDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.permission.SysRoleMenuMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.permission.SysUserRoleMapper;
import cn.iocoder.dashboard.modules.system.mq.producer.permission.SysPermissionProducer;
import cn.iocoder.dashboard.modules.system.service.permission.impl.SysPermissionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.dashboard.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.dashboard.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

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

}
