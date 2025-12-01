package cn.iocoder.yudao.module.system.dal.integration;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbIntegrationTest;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.PostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictTypeDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.dal.mysql.dept.PostMapper;
import cn.iocoder.yudao.module.system.dal.mysql.dict.DictDataMapper;
import cn.iocoder.yudao.module.system.dal.mysql.dict.DictTypeMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.MenuMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System 模块数据库结构集成测试
 *
 * 此测试类用于验证真实 PostgreSQL 数据库表结构与 Java 实体类的兼容性
 * 通过实际的 CRUD 操作验证：
 * 1. 表是否存在
 * 2. 字段类型是否匹配
 * 3. MyBatis Plus 映射是否正确
 *
 * @author AI Assistant
 */
@Import({
        AdminUserMapper.class,
        DeptMapper.class,
        PostMapper.class,
        RoleMapper.class,
        MenuMapper.class,
        DictTypeMapper.class,
        DictDataMapper.class,
        TenantMapper.class
})
@DisplayName("System模块-数据库结构集成测试")
public class SystemDbSchemaIntegrationTest extends BaseDbIntegrationTest {

    @Resource
    private AdminUserMapper adminUserMapper;

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private PostMapper postMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private DictTypeMapper dictTypeMapper;

    @Resource
    private DictDataMapper dictDataMapper;

    @Resource
    private TenantMapper tenantMapper;

    // ==================== 用户相关表测试 ====================

    @Test
    @DisplayName("验证 system_users 表结构 - 查询操作")
    void testAdminUserTable_Select() {
        // 测试查询操作，验证表存在且字段映射正确
        List<AdminUserDO> users = adminUserMapper.selectList();
        assertNotNull(users, "用户列表不应为空");
        System.out.println("system_users 表记录数: " + users.size());

        // 如果有数据，验证字段映射
        if (!users.isEmpty()) {
            AdminUserDO user = users.get(0);
            assertNotNull(user.getId(), "用户ID不应为空");
            System.out.println("示例用户: id=" + user.getId() + ", username=" + user.getUsername());
        }
    }

    @Test
    @DisplayName("验证 system_users 表结构 - 插入和删除操作")
    void testAdminUserTable_InsertAndDelete() {
        // 创建测试用户
        AdminUserDO user = new AdminUserDO();
        user.setUsername("test_integration_" + System.currentTimeMillis());
        user.setPassword("test_password");
        user.setNickname("集成测试用户");
        user.setStatus(0);

        // 插入
        int insertResult = adminUserMapper.insert(user);
        assertEquals(1, insertResult, "插入应成功");
        assertNotNull(user.getId(), "插入后应自动生成ID");

        // 查询验证
        AdminUserDO dbUser = adminUserMapper.selectById(user.getId());
        assertNotNull(dbUser, "应能查询到刚插入的用户");
        assertEquals(user.getUsername(), dbUser.getUsername(), "用户名应匹配");

        // 事务会自动回滚，无需手动删除
        System.out.println("system_users 表 CRUD 测试通过");
    }

    // ==================== 部门相关表测试 ====================

    @Test
    @DisplayName("验证 system_dept 表结构")
    void testDeptTable() {
        List<DeptDO> depts = deptMapper.selectList();
        assertNotNull(depts, "部门列表不应为空");
        System.out.println("system_dept 表记录数: " + depts.size());

        // 测试插入
        DeptDO dept = new DeptDO();
        dept.setName("测试部门_" + System.currentTimeMillis());
        dept.setParentId(0L);
        dept.setSort(999);
        dept.setStatus(0);

        int result = deptMapper.insert(dept);
        assertEquals(1, result, "部门插入应成功");
        System.out.println("system_dept 表 CRUD 测试通过");
    }

    @Test
    @DisplayName("验证 system_post 表结构")
    void testPostTable() {
        List<PostDO> posts = postMapper.selectList();
        assertNotNull(posts, "岗位列表不应为空");
        System.out.println("system_post 表记录数: " + posts.size());

        // 测试插入
        PostDO post = new PostDO();
        post.setCode("TEST_" + System.currentTimeMillis());
        post.setName("测试岗位");
        post.setSort(999);
        post.setStatus(0);

        int result = postMapper.insert(post);
        assertEquals(1, result, "岗位插入应成功");
        System.out.println("system_post 表 CRUD 测试通过");
    }

    // ==================== 权限相关表测试 ====================

    @Test
    @DisplayName("验证 system_role 表结构")
    void testRoleTable() {
        List<RoleDO> roles = roleMapper.selectList();
        assertNotNull(roles, "角色列表不应为空");
        System.out.println("system_role 表记录数: " + roles.size());

        // 测试插入
        RoleDO role = new RoleDO();
        role.setName("测试角色_" + System.currentTimeMillis());
        role.setCode("TEST_ROLE_" + System.currentTimeMillis());
        role.setSort(999);
        role.setStatus(0);
        role.setType(1);
        role.setDataScope(1);

        int result = roleMapper.insert(role);
        assertEquals(1, result, "角色插入应成功");
        System.out.println("system_role 表 CRUD 测试通过");
    }

    @Test
    @DisplayName("验证 system_menu 表结构")
    void testMenuTable() {
        List<MenuDO> menus = menuMapper.selectList();
        assertNotNull(menus, "菜单列表不应为空");
        System.out.println("system_menu 表记录数: " + menus.size());

        // 测试插入
        MenuDO menu = new MenuDO();
        menu.setName("测试菜单_" + System.currentTimeMillis());
        menu.setParentId(0L);
        menu.setType(1);
        menu.setSort(999);
        menu.setStatus(0);

        int result = menuMapper.insert(menu);
        assertEquals(1, result, "菜单插入应成功");
        System.out.println("system_menu 表 CRUD 测试通过");
    }

    // ==================== 字典相关表测试 ====================

    @Test
    @DisplayName("验证 system_dict_type 表结构")
    void testDictTypeTable() {
        List<DictTypeDO> dictTypes = dictTypeMapper.selectList();
        assertNotNull(dictTypes, "字典类型列表不应为空");
        System.out.println("system_dict_type 表记录数: " + dictTypes.size());

        // 测试插入
        DictTypeDO dictType = new DictTypeDO();
        dictType.setName("测试字典_" + System.currentTimeMillis());
        dictType.setType("test_dict_" + System.currentTimeMillis());
        dictType.setStatus(0);

        int result = dictTypeMapper.insert(dictType);
        assertEquals(1, result, "字典类型插入应成功");
        System.out.println("system_dict_type 表 CRUD 测试通过");
    }

    @Test
    @DisplayName("验证 system_dict_data 表结构")
    void testDictDataTable() {
        List<DictDataDO> dictDataList = dictDataMapper.selectList();
        assertNotNull(dictDataList, "字典数据列表不应为空");
        System.out.println("system_dict_data 表记录数: " + dictDataList.size());

        // 测试插入
        DictDataDO dictData = new DictDataDO();
        dictData.setDictType("test_dict_type");
        dictData.setLabel("测试标签");
        dictData.setValue("test_value_" + System.currentTimeMillis());
        dictData.setSort(999);
        dictData.setStatus(0);

        int result = dictDataMapper.insert(dictData);
        assertEquals(1, result, "字典数据插入应成功");
        System.out.println("system_dict_data 表 CRUD 测试通过");
    }

    // ==================== 租户相关表测试 ====================

    @Test
    @DisplayName("验证 system_tenant 表结构")
    void testTenantTable() {
        List<TenantDO> tenants = tenantMapper.selectList();
        assertNotNull(tenants, "租户列表不应为空");
        System.out.println("system_tenant 表记录数: " + tenants.size());

        // 验证字段映射（通过查询现有数据）
        if (!tenants.isEmpty()) {
            TenantDO tenant = tenants.get(0);
            assertNotNull(tenant.getId(), "租户ID不应为空");
            assertNotNull(tenant.getName(), "租户名称不应为空");
            System.out.println("示例租户: id=" + tenant.getId() + ", name=" + tenant.getName());
        }
        System.out.println("system_tenant 表查询测试通过");
    }

    // ==================== 综合验证 ====================

    @Test
    @DisplayName("综合验证 - 所有核心表结构")
    void testAllCoreTables() {
        System.out.println("========== 开始综合验证所有核心表结构 ==========");

        // 验证各表查询不报错
        assertDoesNotThrow(() -> adminUserMapper.selectList(), "system_users 表查询失败");
        assertDoesNotThrow(() -> deptMapper.selectList(), "system_dept 表查询失败");
        assertDoesNotThrow(() -> postMapper.selectList(), "system_post 表查询失败");
        assertDoesNotThrow(() -> roleMapper.selectList(), "system_role 表查询失败");
        assertDoesNotThrow(() -> menuMapper.selectList(), "system_menu 表查询失败");
        assertDoesNotThrow(() -> dictTypeMapper.selectList(), "system_dict_type 表查询失败");
        assertDoesNotThrow(() -> dictDataMapper.selectList(), "system_dict_data 表查询失败");
        assertDoesNotThrow(() -> tenantMapper.selectList(), "system_tenant 表查询失败");

        System.out.println("========== 所有核心表结构验证通过 ==========");
    }
}
