package cn.iocoder.yudao.module.crm.service.permission;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.permission.CrmPermissionMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * {@link CrmPermissionServiceImpl} 的单元测试类
 *
 * 重点覆盖 {@link CrmPermissionServiceImpl#transferPermission(CrmPermissionTransferReqBO)}：区分操作人的权限、数据实际的负责人
 *
 * @author 芋道源码
 */
public class CrmPermissionServiceImplTest extends BaseMockitoUnitTest {

    private static final Integer BIZ_TYPE = CrmBizTypeEnum.CRM_CUSTOMER.getType();
    private static final String BIZ_TYPE_NAME = CrmBizTypeEnum.CRM_CUSTOMER.getName();
    private static final Long BIZ_ID = 10L;

    private static final Long LOGIN_USER_ID = 100L;
    private static final Long OWNER_USER_ID = 200L;
    private static final Long NEW_OWNER_USER_ID = 300L;

    private static final Long OWNER_PERMISSION_ID = 1L;
    private static final Long LOGIN_USER_PERMISSION_ID = 2L;
    private static final Long NEW_OWNER_PERMISSION_ID = 3L;

    @InjectMocks
    private CrmPermissionServiceImpl permissionService;

    @Mock
    private CrmPermissionMapper permissionMapper;
    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private PermissionCommonApi permissionCommonApi;

    private MockedStatic<SpringUtil> springUtilMockedStatic;

    @BeforeEach
    public void setUp() {
        // mock CrmPermissionUtils#isCrmAdmin() 使用的 PermissionCommonApi，在 mockCrmAdmin 方法中按需 mock 返回值
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        // mock 登录用户
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(LOGIN_USER_ID), new MockHttpServletRequest());
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
        SecurityContextHolder.clearContext();
    }

    // ========== 负责人转移 ==========

    @Test
    public void testTransferPermission_ownerRemoveOldOwner() {
        // mock 数据：当前用户是负责人，新负责人不在团队中
        mockPermissions(buildPermission(LOGIN_USER_PERMISSION_ID, LOGIN_USER_ID, CrmPermissionLevelEnum.OWNER));

        // 调用：老负责人移除
        permissionService.transferPermission(buildTransferReqBO(NEW_OWNER_USER_ID, null));
        // 断言：新增新负责人；删除老负责人（当前用户）
        verify(adminUserApi).validateUserList(Collections.singletonList(NEW_OWNER_USER_ID));
        verify(permissionMapper).selectByBizTypeAndBizId(BIZ_TYPE, BIZ_ID);
        verifyNewOwnerInserted();
        verify(permissionMapper).deleteById(LOGIN_USER_PERMISSION_ID);
        verifyNoMoreInteractions(permissionMapper);
    }

    @Test
    public void testTransferPermission_ownerKeepOldOwnerAndPromoteTeamMember() {
        // mock 数据：当前用户是负责人，新负责人已有 READ 团队权限
        mockPermissions(buildPermission(LOGIN_USER_PERMISSION_ID, LOGIN_USER_ID, CrmPermissionLevelEnum.OWNER),
                buildPermission(NEW_OWNER_PERMISSION_ID, NEW_OWNER_USER_ID, CrmPermissionLevelEnum.READ));

        // 调用：老负责人保留为 WRITE
        permissionService.transferPermission(buildTransferReqBO(NEW_OWNER_USER_ID, CrmPermissionLevelEnum.WRITE.getLevel()));
        // 断言：新负责人的团队权限提升为负责人，不重复新增；老负责人（当前用户）改为 WRITE
        verify(permissionMapper).selectByBizTypeAndBizId(BIZ_TYPE, BIZ_ID);
        verifyLevelUpdated(NEW_OWNER_PERMISSION_ID, CrmPermissionLevelEnum.OWNER);
        verifyLevelUpdated(LOGIN_USER_PERMISSION_ID, CrmPermissionLevelEnum.WRITE);
        verifyNoMoreInteractions(permissionMapper);
    }

    @Test
    public void testTransferPermission_ownerToSelf() {
        // mock 数据：当前用户是负责人
        mockPermissions(buildPermission(LOGIN_USER_PERMISSION_ID, LOGIN_USER_ID, CrmPermissionLevelEnum.OWNER));

        // 调用，并断言异常：转移给自己
        assertServiceException(() -> permissionService.transferPermission(buildTransferReqBO(LOGIN_USER_ID, null)),
                CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS, BIZ_TYPE_NAME);
        assertNoPermissionWrite();
        verifyNoInteractions(adminUserApi);
    }

    // ========== 非负责人、非 CRM 管理员 ==========

    @Test
    public void testTransferPermission_teamMemberDenied() {
        // mock 数据：当前用户只有 WRITE 团队权限，不是 CRM 管理员
        mockPermissions(buildPermission(OWNER_PERMISSION_ID, OWNER_USER_ID, CrmPermissionLevelEnum.OWNER),
                buildPermission(LOGIN_USER_PERMISSION_ID, LOGIN_USER_ID, CrmPermissionLevelEnum.WRITE));
        mockCrmAdmin(false);

        // 调用，并断言异常
        assertServiceException(() -> permissionService.transferPermission(buildTransferReqBO(NEW_OWNER_USER_ID, null)),
                CRM_PERMISSION_DENIED, BIZ_TYPE_NAME);
        assertNoPermissionWrite();
        verifyNoInteractions(adminUserApi);
    }

    @Test
    public void testTransferPermission_noPermissionDenied() {
        // mock 数据：当前用户没有任何权限，不是 CRM 管理员
        mockPermissions(buildPermission(OWNER_PERMISSION_ID, OWNER_USER_ID, CrmPermissionLevelEnum.OWNER));
        mockCrmAdmin(false);

        // 调用，并断言异常
        assertServiceException(() -> permissionService.transferPermission(buildTransferReqBO(NEW_OWNER_USER_ID, null)),
                CRM_PERMISSION_DENIED, BIZ_TYPE_NAME);
        assertNoPermissionWrite();
        verifyNoInteractions(adminUserApi);
    }

    // ========== CRM 管理员转移 ==========

    @Test
    public void testTransferPermission_crmAdminWithoutPermission() {
        // mock 数据：CRM 管理员没有权限，数据的负责人是其他人（修复前：先新增新负责人，再因 NPE 失败）
        mockPermissions(buildPermission(OWNER_PERMISSION_ID, OWNER_USER_ID, CrmPermissionLevelEnum.OWNER));
        mockCrmAdmin(true);

        // 调用：老负责人移除
        permissionService.transferPermission(buildTransferReqBO(NEW_OWNER_USER_ID, null));
        // 断言：新增新负责人；删除实际的老负责人
        verify(adminUserApi).validateUserList(Collections.singletonList(NEW_OWNER_USER_ID));
        verify(permissionMapper).selectByBizTypeAndBizId(BIZ_TYPE, BIZ_ID);
        verifyNewOwnerInserted();
        verify(permissionMapper).deleteById(OWNER_PERMISSION_ID);
        verifyNoMoreInteractions(permissionMapper);
    }

    @Test
    public void testTransferPermission_crmAdminWithTeamPermission() {
        // mock 数据：CRM 管理员有 WRITE 团队权限，数据的负责人是其他人
        mockPermissions(buildPermission(OWNER_PERMISSION_ID, OWNER_USER_ID, CrmPermissionLevelEnum.OWNER),
                buildPermission(LOGIN_USER_PERMISSION_ID, LOGIN_USER_ID, CrmPermissionLevelEnum.WRITE));
        mockCrmAdmin(true);

        // 调用：老负责人保留为 READ
        permissionService.transferPermission(buildTransferReqBO(NEW_OWNER_USER_ID, CrmPermissionLevelEnum.READ.getLevel()));
        // 断言：新增新负责人；实际的老负责人改为 READ；管理员自己的团队权限不变
        verify(permissionMapper).selectByBizTypeAndBizId(BIZ_TYPE, BIZ_ID);
        verifyNewOwnerInserted();
        verifyLevelUpdated(OWNER_PERMISSION_ID, CrmPermissionLevelEnum.READ);
        verifyNoMoreInteractions(permissionMapper);
    }

    @Test
    public void testTransferPermission_crmAdminToSelf() {
        // mock 数据：CRM 管理员有 READ 团队权限，数据的负责人是其他人
        mockPermissions(buildPermission(OWNER_PERMISSION_ID, OWNER_USER_ID, CrmPermissionLevelEnum.OWNER),
                buildPermission(LOGIN_USER_PERMISSION_ID, LOGIN_USER_ID, CrmPermissionLevelEnum.READ));
        mockCrmAdmin(true);

        // 调用：转移给自己，老负责人移除
        permissionService.transferPermission(buildTransferReqBO(LOGIN_USER_ID, null));
        // 断言：管理员的团队权限提升为负责人，不重复新增；删除实际的老负责人
        verify(adminUserApi).validateUserList(Collections.singletonList(LOGIN_USER_ID));
        verify(permissionMapper).selectByBizTypeAndBizId(BIZ_TYPE, BIZ_ID);
        verifyLevelUpdated(LOGIN_USER_PERMISSION_ID, CrmPermissionLevelEnum.OWNER);
        verify(permissionMapper).deleteById(OWNER_PERMISSION_ID);
        verifyNoMoreInteractions(permissionMapper);
    }

    @Test
    public void testTransferPermission_crmAdminNewOwnerIsOwner() {
        // mock 数据：CRM 管理员没有权限，新负责人已经是负责人
        mockPermissions(buildPermission(OWNER_PERMISSION_ID, OWNER_USER_ID, CrmPermissionLevelEnum.OWNER));
        mockCrmAdmin(true);

        // 调用，并断言异常
        assertServiceException(() -> permissionService.transferPermission(buildTransferReqBO(OWNER_USER_ID, null)),
                CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS, BIZ_TYPE_NAME);
        assertNoPermissionWrite();
        verifyNoInteractions(adminUserApi);
    }

    @Test
    public void testTransferPermission_crmAdminOwnerNotExists() {
        // mock 数据：CRM 管理员有 WRITE 团队权限，但数据缺少负责人
        mockPermissions(buildPermission(LOGIN_USER_PERMISSION_ID, LOGIN_USER_ID, CrmPermissionLevelEnum.WRITE),
                buildPermission(NEW_OWNER_PERMISSION_ID, NEW_OWNER_USER_ID, CrmPermissionLevelEnum.READ));
        mockCrmAdmin(true);

        // 调用，并断言异常：在修改任何权限前失败
        assertServiceException(() -> permissionService.transferPermission(buildTransferReqBO(NEW_OWNER_USER_ID, null)),
                CRM_PERMISSION_NOT_EXISTS);
        assertNoPermissionWrite();
        verifyNoInteractions(adminUserApi);
    }

    @Test
    public void testTransferPermission_newOwnerNotExists() {
        // mock 数据：CRM 管理员没有权限，数据的负责人是其他人
        mockPermissions(buildPermission(OWNER_PERMISSION_ID, OWNER_USER_ID, CrmPermissionLevelEnum.OWNER));
        mockCrmAdmin(true);
        // mock 方法：新负责人不存在
        doThrow(exception(USER_NOT_EXISTS)).when(adminUserApi)
                .validateUserList(Collections.singletonList(NEW_OWNER_USER_ID));

        // 调用，并断言异常：在修改任何权限前失败
        assertServiceException(() -> permissionService.transferPermission(buildTransferReqBO(NEW_OWNER_USER_ID, null)),
                USER_NOT_EXISTS);
        assertNoPermissionWrite();
    }

    private void mockCrmAdmin(boolean crmAdmin) {
        springUtilMockedStatic.when(() -> SpringUtil.getBean(PermissionCommonApi.class)).thenReturn(permissionCommonApi);
        when(permissionCommonApi.hasAnyRoles(LOGIN_USER_ID, RoleCodeEnum.CRM_ADMIN.getCode())).thenReturn(crmAdmin);
    }

    private void mockPermissions(CrmPermissionDO... permissions) {
        List<CrmPermissionDO> list = Arrays.asList(permissions);
        when(permissionMapper.selectByBizTypeAndBizId(BIZ_TYPE, BIZ_ID)).thenReturn(list);
    }

    private void verifyNewOwnerInserted() {
        verify(permissionMapper).insert(argThat((CrmPermissionDO permission) ->
                permission.getId() == null
                        && Objects.equals(permission.getBizType(), BIZ_TYPE)
                        && Objects.equals(permission.getBizId(), BIZ_ID)
                        && Objects.equals(permission.getUserId(), NEW_OWNER_USER_ID)
                        && CrmPermissionLevelEnum.isOwner(permission.getLevel())));
    }

    private void verifyLevelUpdated(Long id, CrmPermissionLevelEnum level) {
        verify(permissionMapper).updateById(argThat((CrmPermissionDO permission) ->
                Objects.equals(permission.getId(), id) && Objects.equals(permission.getLevel(), level.getLevel())));
    }

    private void assertNoPermissionWrite() {
        verify(permissionMapper).selectByBizTypeAndBizId(BIZ_TYPE, BIZ_ID);
        verifyNoMoreInteractions(permissionMapper);
    }

    private static CrmPermissionTransferReqBO buildTransferReqBO(Long newOwnerUserId, Integer oldOwnerPermissionLevel) {
        return new CrmPermissionTransferReqBO(LOGIN_USER_ID, BIZ_TYPE, BIZ_ID, newOwnerUserId, oldOwnerPermissionLevel);
    }

    private static CrmPermissionDO buildPermission(Long id, Long userId, CrmPermissionLevelEnum level) {
        return new CrmPermissionDO().setId(id).setBizType(BIZ_TYPE).setBizId(BIZ_ID)
                .setUserId(userId).setLevel(level.getLevel());
    }

}
