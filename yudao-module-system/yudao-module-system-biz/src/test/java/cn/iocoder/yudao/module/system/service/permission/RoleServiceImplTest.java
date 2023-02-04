package cn.iocoder.yudao.module.system.service.permission;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.permission.vo.role.RoleUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.enums.permission.DataScopeEnum;
import cn.iocoder.yudao.module.system.enums.permission.RoleTypeEnum;
import cn.iocoder.yudao.module.system.mq.producer.permission.RoleProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@Import(RoleServiceImpl.class)
public class RoleServiceImplTest extends BaseDbUnitTest {

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
        RoleDO roleDO1 = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO1);
        RoleDO roleDO2 = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO2);

        // 调用
        roleService.initLocalCache();
        // 断言 roleCache 缓存
        Map<Long, RoleDO> roleCache = roleService.getRoleCache();
        assertPojoEquals(roleDO1, roleCache.get(roleDO1.getId()));
        assertPojoEquals(roleDO2, roleCache.get(roleDO2.getId()));
    }

    @Test
    public void testCreateRole_success() {
        // 准备参数
        RoleCreateReqVO reqVO = randomPojo(RoleCreateReqVO.class);

        // 调用
        Long roleId = roleService.createRole(reqVO, null);
        // 断言
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
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setType(RoleTypeEnum.CUSTOM.getType()));
        roleMapper.insert(roleDO);
        // 准备参数
        Long id = roleDO.getId();
        RoleUpdateReqVO reqVO = randomPojo(RoleUpdateReqVO.class, o -> o.setId(id));

        // 调用
        roleService.updateRole(reqVO);
        // 断言
        RoleDO newRoleDO = roleMapper.selectById(id);
        assertPojoEquals(reqVO, newRoleDO);
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testUpdateRoleStatus_success() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setType(RoleTypeEnum.CUSTOM.getType()));
        roleMapper.insert(roleDO);

        // 准备参数
        Long roleId = roleDO.getId();

        // 调用
        roleService.updateRoleStatus(roleId, CommonStatusEnum.DISABLE.getStatus());
        // 断言
        RoleDO dbRoleDO = roleMapper.selectById(roleId);
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), dbRoleDO.getStatus());
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testUpdateRoleDataScope_success() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setType(RoleTypeEnum.CUSTOM.getType()));
        roleMapper.insert(roleDO);
        // 准备参数
        Long id = roleDO.getId();
        Integer dataScope = randomEle(DataScopeEnum.values()).getScope();
        Set<Long> dataScopeRoleIds = randomSet(Long.class);

        // 调用
        roleService.updateRoleDataScope(id, dataScope, dataScopeRoleIds);
        // 断言
        RoleDO dbRoleDO = roleMapper.selectById(id);
        assertEquals(dataScope, dbRoleDO.getDataScope());
        assertEquals(dataScopeRoleIds, dbRoleDO.getDataScopeDeptIds());
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testDeleteRole_success() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setType(RoleTypeEnum.CUSTOM.getType()));
        roleMapper.insert(roleDO);
        // 参数准备
        Long id = roleDO.getId();

        // 调用
        roleService.deleteRole(id);
        // 断言
        assertNull(roleMapper.selectById(id));
        // verify 删除相关数据
        verify(permissionService).processRoleDeleted(id);
        // verify 发送刷新消息
        verify(roleProducer).sendRoleRefreshMessage();
    }

    @Test
    public void testGetRoleFromCache() {
        // mock 数据（缓存）
        RoleDO roleDO = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO);
        roleService.initLocalCache();
        // 参数准备
        Long id = roleDO.getId();

        // 调用
        RoleDO dbRoleDO = roleService.getRoleFromCache(id);
        // 断言
        assertPojoEquals(roleDO, dbRoleDO);
    }

    @Test
    public void testGetRole() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO);
        // 参数准备
        Long id = roleDO.getId();

        // 调用
        RoleDO dbRoleDO = roleService.getRole(id);
        // 断言
        assertPojoEquals(roleDO, dbRoleDO);
    }

    @Test
    public void testGetRoleListByStatus_statusNotEmpty() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        roleMapper.insert(dbRole);
        // 测试 status 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用
        List<RoleDO> list = roleService.getRoleListByStatus(singleton(CommonStatusEnum.ENABLE.getStatus()));
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbRole, list.get(0));
    }

    @Test
    public void testGetRoleListByStatus_statusEmpty() {
        // mock 数据
        RoleDO dbRole01 = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        roleMapper.insert(dbRole01);
        RoleDO dbRole02 = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        roleMapper.insert(dbRole02);

        // 调用
        List<RoleDO> list = roleService.getRoleListByStatus(null);
        // 断言
        assertEquals(2, list.size());
        assertPojoEquals(dbRole01, list.get(0));
        assertPojoEquals(dbRole02, list.get(1));
    }

    @Test
    public void testGetRoleListFromCache() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        roleMapper.insert(dbRole);
        // 测试 id 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> {}));
        roleService.initLocalCache();
        // 准备参数
        Collection<Long> ids = singleton(dbRole.getId());

        // 调用
        List<RoleDO> list = roleService.getRoleListFromCache(ids);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbRole, list.get(0));
    }

    @Test
    public void testGetRoleList() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> { // 等会查询到
            o.setName("土豆");
            o.setCode("tudou");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2022, 2, 8));
        });
        roleMapper.insert(dbRole);
        // 测试 name 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setName("红薯")));
        // 测试 code 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setCode("hong")));
        // 测试 createTime 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setCreateTime(buildTime(2022, 2, 16))));
        // 准备参数
        RoleExportReqVO reqVO = new RoleExportReqVO();
        reqVO.setName("土豆");
        reqVO.setCode("tu");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2022, 2, 1, 2022, 2, 12));

        // 调用
        List<RoleDO> list = roleService.getRoleList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbRole, list.get(0));
    }

    @Test
    public void testGetRolePage() {
        // mock 数据
        RoleDO dbRole = randomPojo(RoleDO.class, o -> { // 等会查询到
            o.setName("土豆");
            o.setCode("tudou");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2022, 2, 8));
        });
        roleMapper.insert(dbRole);
        // 测试 name 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setName("红薯")));
        // 测试 code 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setCode("hong")));
        // 测试 createTime 不匹配
        roleMapper.insert(cloneIgnoreId(dbRole, o -> o.setCreateTime(buildTime(2022, 2, 16))));
        // 准备参数
        RolePageReqVO reqVO = new RolePageReqVO();
        reqVO.setName("土豆");
        reqVO.setCode("tu");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2022, 2, 1, 2022, 2, 12));

        // 调用
        PageResult<RoleDO> pageResult = roleService.getRolePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbRole, pageResult.getList().get(0));
    }

    @Test
    public void testHasAnySuperAdmin() {
        // 是超级
        assertTrue(roleService.hasAnySuperAdmin(singletonList(randomPojo(RoleDO.class,
                o -> o.setCode("super_admin")))));
        // 非超级
        assertFalse(roleService.hasAnySuperAdmin(singletonList(randomPojo(RoleDO.class,
                o -> o.setCode("tenant_admin")))));
    }

    @Test
    public void testValidateRoleDuplicate_success() {
        // 调用，不会抛异常
        roleService.validateRoleDuplicate(randomString(), randomString(), null);
    }

    @Test
    public void testValidateRoleDuplicate_nameDuplicate() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setName("role_name"));
        roleMapper.insert(roleDO);
        // 准备参数
        String name = "role_name";

        // 调用，并断言异常
        assertServiceException(() -> roleService.validateRoleDuplicate(name, randomString(), null),
                ROLE_NAME_DUPLICATE, name);
    }

    @Test
    public void testValidateRoleDuplicate_codeDuplicate() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setCode("code"));
        roleMapper.insert(roleDO);
        // 准备参数
        String code = "code";

        // 调用，并断言异常
        assertServiceException(() -> roleService.validateRoleDuplicate(randomString(), code, null),
                ROLE_CODE_DUPLICATE, code);
    }

    @Test
    public void testValidateUpdateRole_success() {
        RoleDO roleDO = randomPojo(RoleDO.class);
        roleMapper.insert(roleDO);
        // 准备参数
        Long id = roleDO.getId();

        // 调用，无异常
        roleService.validateRoleForUpdate(id);
    }

    @Test
    public void testValidateUpdateRole_roleIdNotExist() {
        assertServiceException(() -> roleService.validateRoleForUpdate(randomLongId()), ROLE_NOT_EXISTS);
    }

    @Test
    public void testValidateUpdateRole_systemRoleCanNotBeUpdate() {
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setType(RoleTypeEnum.SYSTEM.getType()));
        roleMapper.insert(roleDO);
        // 准备参数
        Long id = roleDO.getId();

        assertServiceException(() -> roleService.validateRoleForUpdate(id),
                ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
    }

    @Test
    public void testValidateRoleList_success() {
        // mock 数据
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        roleMapper.insert(roleDO);
        // 准备参数
        List<Long> ids = singletonList(roleDO.getId());

        // 调用，无需断言
        roleService.validateRoleList(ids);
    }

    @Test
    public void testValidateRoleList_notFound() {
        // 准备参数
        List<Long> ids = singletonList(randomLongId());

        // 调用, 并断言异常
        assertServiceException(() -> roleService.validateRoleList(ids), ROLE_NOT_EXISTS);
    }

    @Test
    public void testValidateRoleList_notEnable() {
        // mock 数据
        RoleDO RoleDO = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        roleMapper.insert(RoleDO);
        // 准备参数
        List<Long> ids = singletonList(RoleDO.getId());

        // 调用, 并断言异常
        assertServiceException(() -> roleService.validateRoleList(ids), ROLE_IS_DISABLE, RoleDO.getName());
    }
}
