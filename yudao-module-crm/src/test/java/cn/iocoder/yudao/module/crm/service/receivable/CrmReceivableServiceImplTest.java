package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.CrmReceivableMapper;
import cn.iocoder.yudao.module.crm.dal.redis.no.CrmNoRedisDAO;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.aop.CrmPermissionAspect;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_PERMISSION_DENIED;
import static java.util.Collections.singleton;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link CrmReceivableServiceImpl} 的单元测试类
 *
 * 通过 {@link AspectJProxyFactory} 代理真实的 Service，并织入真实的 {@link CrmPermissionAspect}，校验回款提交的数据权限
 *
 * @author 芋道源码
 */
public class CrmReceivableServiceImplTest extends BaseMockitoUnitTest {

    private static final Long RECEIVABLE_ID = 10L;
    private static final Long LOGIN_USER_ID = 100L;
    private static final Long OWNER_USER_ID = 200L;
    private static final Long SUBORDINATE_USER_ID = 300L;

    @InjectMocks
    private CrmReceivableServiceImpl receivableService;
    @InjectMocks
    private CrmPermissionAspect crmPermissionAspect;

    @Mock
    private CrmReceivableMapper receivableMapper;
    @Mock
    private CrmNoRedisDAO noRedisDAO;
    @Mock
    private CrmContractService contractService;
    @Mock
    private CrmReceivablePlanService receivablePlanService;
    @Mock
    private CrmPermissionService permissionService;
    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private BpmProcessInstanceApi bpmProcessInstanceApi;
    @Mock
    private PermissionCommonApi permissionCommonApi;

    /**
     * 织入 {@link CrmPermissionAspect} 后的 Service 代理
     */
    private CrmReceivableService receivableServiceProxy;

    private MockedStatic<SpringUtil> springUtilMockedStatic;

    @BeforeEach
    public void setUp() {
        // 代理真实的 Service，织入真实的数据权限切面
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(receivableService);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAspect(crmPermissionAspect);
        receivableServiceProxy = proxyFactory.getProxy();
        // mock CrmPermissionUtils#isCrmAdmin() 使用的 PermissionCommonApi
        springUtilMockedStatic = mockStatic(SpringUtil.class);
        springUtilMockedStatic.when(() -> SpringUtil.getBean(PermissionCommonApi.class)).thenReturn(permissionCommonApi);
        // mock 登录用户
        MockHttpServletRequest request = new MockHttpServletRequest();
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(LOGIN_USER_ID), request);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    public void tearDown() {
        springUtilMockedStatic.close();
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testSubmitReceivable_noPermission() {
        // mock 方法：该回款没有任何数据权限
        mockCrmAdmin(false);
        mockReceivablePermissions(Collections.emptyList());

        // 调用，并断言异常
        assertServiceException(() -> receivableServiceProxy.submitReceivable(RECEIVABLE_ID, LOGIN_USER_ID),
                CRM_PERMISSION_DENIED, CrmBizTypeEnum.CRM_RECEIVABLE.getName());
        // 断言：未读写回款，未发起审批流程
        verifyNoInteractions(receivableMapper, bpmProcessInstanceApi);
    }

    @Test
    public void testSubmitReceivable_otherOwner() {
        // mock 方法：该回款的负责人是其他人，当前用户不在团队内，也没有下级
        mockCrmAdmin(false);
        mockReceivablePermissions(Collections.singletonList(
                buildPermission(OWNER_USER_ID, CrmPermissionLevelEnum.OWNER)));
        when(adminUserApi.getUserListBySubordinate(LOGIN_USER_ID)).thenReturn(Collections.emptyList());

        // 调用，并断言异常
        assertServiceException(() -> receivableServiceProxy.submitReceivable(RECEIVABLE_ID, LOGIN_USER_ID),
                CRM_PERMISSION_DENIED, CrmBizTypeEnum.CRM_RECEIVABLE.getName());
        // 断言：未读写回款，未发起审批流程
        verifyNoInteractions(receivableMapper, bpmProcessInstanceApi);
    }

    @Test
    public void testSubmitReceivable_readOnly() {
        // mock 方法：当前用户只有 READ 权限
        mockCrmAdmin(false);
        mockReceivablePermissions(Arrays.asList(
                buildPermission(OWNER_USER_ID, CrmPermissionLevelEnum.OWNER),
                buildPermission(LOGIN_USER_ID, CrmPermissionLevelEnum.READ)));
        when(adminUserApi.getUserListBySubordinate(LOGIN_USER_ID)).thenReturn(Collections.emptyList());

        // 调用，并断言异常
        assertServiceException(() -> receivableServiceProxy.submitReceivable(RECEIVABLE_ID, LOGIN_USER_ID),
                CRM_PERMISSION_DENIED, CrmBizTypeEnum.CRM_RECEIVABLE.getName());
        // 断言：未读写回款，未发起审批流程
        verifyNoInteractions(receivableMapper, bpmProcessInstanceApi);
    }

    @Test
    public void testSubmitReceivable_write() {
        // mock 方法：当前用户有 WRITE 权限
        mockCrmAdmin(false);
        mockReceivablePermissions(Arrays.asList(
                buildPermission(OWNER_USER_ID, CrmPermissionLevelEnum.OWNER),
                buildPermission(LOGIN_USER_ID, CrmPermissionLevelEnum.WRITE)));
        mockDraftReceivableAndProcess();

        // 调用
        receivableServiceProxy.submitReceivable(RECEIVABLE_ID, LOGIN_USER_ID);
        // 断言
        assertReceivableSubmitted();
    }

    @Test
    public void testSubmitReceivable_owner() {
        // mock 方法：当前用户是负责人
        mockCrmAdmin(false);
        mockReceivablePermissions(Collections.singletonList(
                buildPermission(LOGIN_USER_ID, CrmPermissionLevelEnum.OWNER)));
        mockDraftReceivableAndProcess();

        // 调用
        receivableServiceProxy.submitReceivable(RECEIVABLE_ID, LOGIN_USER_ID);
        // 断言
        assertReceivableSubmitted();
    }

    @Test
    public void testSubmitReceivable_subordinateOwner() {
        // mock 方法：当前用户的下级是负责人
        mockCrmAdmin(false);
        mockReceivablePermissions(Collections.singletonList(
                buildPermission(SUBORDINATE_USER_ID, CrmPermissionLevelEnum.OWNER)));
        when(adminUserApi.getUserListBySubordinate(LOGIN_USER_ID)).thenReturn(Collections.singletonList(
                new AdminUserRespDTO().setId(SUBORDINATE_USER_ID)));
        mockDraftReceivableAndProcess();

        // 调用
        receivableServiceProxy.submitReceivable(RECEIVABLE_ID, LOGIN_USER_ID);
        // 断言
        assertReceivableSubmitted();
    }

    @Test
    public void testSubmitReceivable_crmAdmin() {
        // mock 方法：当前用户是 CRM 管理员，不在团队内
        mockCrmAdmin(true);
        mockReceivablePermissions(Collections.singletonList(
                buildPermission(OWNER_USER_ID, CrmPermissionLevelEnum.OWNER)));
        mockDraftReceivableAndProcess();

        // 调用
        receivableServiceProxy.submitReceivable(RECEIVABLE_ID, LOGIN_USER_ID);
        // 断言
        assertReceivableSubmitted();
        verifyNoInteractions(adminUserApi);
    }

    private void mockCrmAdmin(boolean crmAdmin) {
        when(permissionCommonApi.hasAnyRoles(LOGIN_USER_ID, RoleCodeEnum.CRM_ADMIN.getCode())).thenReturn(crmAdmin);
    }

    private void mockReceivablePermissions(List<CrmPermissionDO> permissions) {
        when(permissionService.getPermissionListByBiz(CrmBizTypeEnum.CRM_RECEIVABLE.getType(), singleton(RECEIVABLE_ID)))
                .thenReturn(permissions);
    }

    private void mockDraftReceivableAndProcess() {
        when(receivableMapper.selectById(RECEIVABLE_ID)).thenReturn(new CrmReceivableDO().setId(RECEIVABLE_ID)
                .setNo("HK001").setAuditStatus(CrmAuditStatusEnum.DRAFT.getStatus()));
        when(bpmProcessInstanceApi.createProcessInstance(eq(LOGIN_USER_ID), argThat((BpmProcessInstanceCreateReqDTO reqDTO) ->
                Objects.equals(reqDTO.getProcessDefinitionKey(), CrmReceivableServiceImpl.BPM_PROCESS_DEFINITION_KEY)
                        && Objects.equals(reqDTO.getBusinessKey(), String.valueOf(RECEIVABLE_ID)))))
                .thenReturn("process-instance-id");
    }

    private void assertReceivableSubmitted() {
        verify(receivableMapper).updateById(argThat((CrmReceivableDO receivable) ->
                Objects.equals(receivable.getId(), RECEIVABLE_ID)
                        && Objects.equals(receivable.getProcessInstanceId(), "process-instance-id")
                        && Objects.equals(receivable.getAuditStatus(), CrmAuditStatusEnum.PROCESS.getStatus())));
    }

    private static CrmPermissionDO buildPermission(Long userId, CrmPermissionLevelEnum level) {
        return new CrmPermissionDO().setBizType(CrmBizTypeEnum.CRM_RECEIVABLE.getType()).setBizId(RECEIVABLE_ID)
                .setUserId(userId).setLevel(level.getLevel());
    }

}
