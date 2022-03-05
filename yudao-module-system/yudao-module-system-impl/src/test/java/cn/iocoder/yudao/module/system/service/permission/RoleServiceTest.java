package cn.iocoder.yudao.module.system.service.permission;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.enums.permission.DataScopeEnum;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.enums.permission.RoleTypeEnum;
import cn.iocoder.yudao.module.system.mq.producer.permission.RoleProducer;
import cn.iocoder.yudao.module.system.test.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.max;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

// TODO @芋艿：单测的代码质量可以提升下
@Import(RoleServiceImpl.class)
public class RoleServiceTest extends BaseDbUnitTest {

    @Resource
    private RoleServiceImpl roleService;

    @Resource
    private RoleMapper roleMapper;

    @MockBean
    private PermissionService permissionService;
    @MockBean
    private RoleProducer roleProducer;

    @Test
    public void testInitLocalCache() {
        RoleDO roleDO1 = randomRole();
        roleMapper.insert(roleDO1);
        RoleDO roleDO2 = randomRole();
        roleMapper.insert(roleDO2);

        // 调用
        roleService.initLocalCache();
        // 断言 roleCache 缓存
        Map<Long, RoleDO> roleCache = roleService.getRoleCache();
        assertPojoEquals(roleDO1, roleCache.get(roleDO1.getId()));
        assertPojoEquals(roleDO2, roleCache.get(roleDO2.getId()));
        // 断言 maxUpdateTime 缓存
        assertEquals(max(roleDO1.getUpdateTime(), roleDO2.getUpdateTime()), roleService.getMaxUpdateTime());
    }

    @Test
    public void testCreateRole_success() {
        // 准备参数
        RoleCreateReqVO reqVO = randomPojo(RoleCreateReqVO.class);

        // 调用
        Long roleId = roleService.createRole(reqVO, null);
        // 断言
        assertNotNull(roleId);
        RoleDO roleDO = roleMapper.selectById(roleId);
        assertPojoEquals(reqVO, roleDO);
        assertEquals(RoleTypeEnum.CUSTOM.getType(), roleDO.getType());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), roleDO.getStatus());
        assertEquals(DataScopeEnum.ALL.getScope(), roleDO.getDataScope());
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testUpdateRole_success() {
        // mock 数据
        RoleDO roleDO = createRoleDO("role_name", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL);
        roleMapper.insert(roleDO);
        Long roleId = roleDO.getId();

        //调用
        RoleUpdateReqVO reqVO = randomPojo(RoleUpdateReqVO.class, o -> {
            o.setId(roleId);
            o.setCode("role_code");
            o.setName("update_name");
            o.setSort(999);
        });
        roleService.updateRole(reqVO);

        //断言
        RoleDO newRoleDO = roleMapper.selectById(roleId);
        assertPojoEquals(reqVO, newRoleDO);

        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testUpdateRoleStatus_success() {
        RoleDO roleDO = createRoleDO("role_name", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL, CommonStatusEnum.ENABLE.getStatus());
        roleMapper.insert(roleDO);
        Long roleId = roleDO.getId();

        //调用
        roleService.updateRoleStatus(roleId, CommonStatusEnum.DISABLE.getStatus());

        //断言
        RoleDO newRoleDO = roleMapper.selectById(roleId);
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), newRoleDO.getStatus());

        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testUpdateRoleDataScope_success() {
        RoleDO roleDO = createRoleDO("role_name", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL);
        roleMapper.insert(roleDO);
        Long roleId = roleDO.getId();

        //调用
        Set<Long> deptIdSet = Arrays.asList(1L, 2L, 3L, 4L, 5L).stream().collect(Collectors.toSet());
        roleService.updateRoleDataScope(roleId, DataScopeEnum.DEPT_CUSTOM.getScope(), deptIdSet);

        //断言
        RoleDO newRoleDO = roleMapper.selectById(roleId);
        assertEquals(DataScopeEnum.DEPT_CUSTOM.getScope(), newRoleDO.getDataScope());

        Set<Long> newDeptIdSet = newRoleDO.getDataScopeDeptIds();
        assertTrue(deptIdSet.size() == newDeptIdSet.size());
        deptIdSet.stream().forEach(d -> assertTrue(newDeptIdSet.contains(d)));

        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testDeleteRole_success() {
        RoleDO roleDO = createRoleDO("role_name", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL);
        roleMapper.insert(roleDO);
        Long roleId = roleDO.getId();

        //调用
        roleService.deleteRole(roleId);

        //断言
        RoleDO newRoleDO = roleMapper.selectById(roleId);
        assertNull(newRoleDO);

        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testGetRoles_success() {
        Map<Long, RoleDO> idRoleMap = new HashMap<>();
        // 验证查询状态为1的角色
        RoleDO roleDO1 = createRoleDO("role1", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL, 1);
        roleMapper.insert(roleDO1);
        idRoleMap.put(roleDO1.getId(), roleDO1);

        RoleDO roleDO2 = createRoleDO("role2", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL, 1);
        roleMapper.insert(roleDO2);
        idRoleMap.put(roleDO2.getId(), roleDO2);

        // 以下是排除的角色
        RoleDO roleDO3 = createRoleDO("role3", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL, 2);
        roleMapper.insert(roleDO3);

        //调用
        List<RoleDO> roles = roleService.getRoles(Arrays.asList(1));

        //断言
        assertEquals(2, roles.size());
        roles.stream().forEach(r -> assertPojoEquals(idRoleMap.get(r.getId()), r));

    }

    @Test
    public void testGetRolePage_success() {
        Map<Long, RoleDO> idRoleMap = new HashMap<>();
        // 验证名称包含"role", 状态为1,code为"code"的角色
        // 第一页
        RoleDO roleDO = createRoleDO("role1", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL, 1, "code");
        roleMapper.insert(roleDO);
        idRoleMap.put(roleDO.getId(), roleDO);
        // 第二页
        roleDO = createRoleDO("role2", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL, 1, "code");
        roleMapper.insert(roleDO);

        // 以下是排除的角色
        roleDO = createRoleDO("role3", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL, 2, "code");
        roleMapper.insert(roleDO);
        roleDO = createRoleDO("role4", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL, 1, "xxxxx");
        roleMapper.insert(roleDO);

        //调用
        RolePageReqVO reqVO = randomPojo(RolePageReqVO.class, o -> {
            o.setName("role");
            o.setCode("code");
            o.setStatus(1);
            o.setPageNo(1);
            o.setPageSize(1);
            o.setBeginTime(null);
            o.setEndTime(null);
        });
        PageResult<RoleDO> result = roleService.getRolePage(reqVO);
        assertEquals(2, result.getTotal());
        result.getList().stream().forEach(r -> assertPojoEquals(idRoleMap.get(r.getId()), r));
    }

    @Test
    public void testCheckDuplicateRole_success() {
        roleService.checkDuplicateRole(randomString(), randomString(), null);
    }

    @Test
    public void testCheckDuplicateRole_nameDuplicate() {
        RoleDO roleDO = createRoleDO("role_name", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL);
        roleMapper.insert(roleDO);

        String duplicateName = "role_name";

        assertServiceException(() -> roleService.checkDuplicateRole(duplicateName, randomString(), null), ROLE_NAME_DUPLICATE, duplicateName);
    }

    @Test
    public void testCheckDuplicateRole_codeDuplicate() {
        RoleDO roleDO = randomPojo(RoleDO.class, o -> {
            o.setName("role_999");
            o.setCode("code");
            o.setType(RoleTypeEnum.CUSTOM.getType());
            o.setStatus(1);
            o.setDataScope(DataScopeEnum.ALL.getScope());
        });
        roleMapper.insert(roleDO);

        String randomName = randomString();
        String duplicateCode = "code";

        assertServiceException(() -> roleService.checkDuplicateRole(randomName, duplicateCode, null), ROLE_CODE_DUPLICATE, duplicateCode);
    }

    @Test
    public void testCheckUpdateRole_success() {
        RoleDO roleDO = createRoleDO("role_name", RoleTypeEnum.CUSTOM, DataScopeEnum.ALL);
        roleMapper.insert(roleDO);
        Long roleId = roleDO.getId();

        roleService.checkUpdateRole(roleId);
    }

    @Test
    public void testCheckUpdateRole_roleIdNotExist() {
        assertServiceException(() -> roleService.checkUpdateRole(randomLongId()), ROLE_NOT_EXISTS);
    }

    @Test
    public void testCheckUpdateRole_systemRoleCanNotBeUpdate() {
        RoleDO roleDO = createRoleDO("role_name", RoleTypeEnum.SYSTEM, DataScopeEnum.ALL);
        roleMapper.insert(roleDO);
        Long roleId = roleDO.getId();

        assertServiceException(() -> roleService.checkUpdateRole(roleId), ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
    }

    private RoleDO createRoleDO(String name, RoleTypeEnum typeEnum, DataScopeEnum scopeEnum, Integer status) {
        return createRoleDO( name, typeEnum, scopeEnum, status, randomString());
    }

    private RoleDO createRoleDO(String name, RoleTypeEnum typeEnum, DataScopeEnum scopeEnum, Integer status, String code) {
        return createRoleDO(null, name, typeEnum, scopeEnum, status, code);
    }

    private RoleDO createRoleDO(String name, RoleTypeEnum typeEnum, DataScopeEnum scopeEnum) {
        return createRoleDO(null, name, typeEnum, scopeEnum, randomCommonStatus(), randomString());
    }

    private RoleDO createRoleDO(Long id, String name, RoleTypeEnum typeEnum, DataScopeEnum scopeEnum, Integer status, String code) {
        return randomPojo(RoleDO.class, o -> {
            o.setId(id);
            o.setName(name);
            o.setType(typeEnum.getType());
            o.setStatus(status);
            o.setDataScope(scopeEnum.getScope());
            o.setCode(code);
        });
    }

    private RoleDO randomRole() {
        return randomPojo(RoleDO.class,
                o -> o.setDataScope(RandomUtil.randomEle(DataScopeEnum.values()).getScope()));
    }

}
