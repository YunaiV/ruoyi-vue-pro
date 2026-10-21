package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmProcessDefinitionInfoMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * {@link BpmProcessDefinitionServiceImpl} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmProcessDefinitionServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmProcessDefinitionServiceImpl processDefinitionService;

    @Mock
    private RepositoryService repositoryService;
    @Mock
    private BpmProcessDefinitionInfoMapper processDefinitionMapper;
    @Mock
    private AdminUserApi adminUserApi;
    @Mock
    private ProcessDefinitionQuery processDefinitionQuery;
    @Mock
    private DeploymentQuery deploymentQuery;

    @Test
    public void testCanUserStartProcessDefinition_null() {
        // 调用，并断言
        assertFalse(processDefinitionService.canUserStartProcessDefinition(null, 1L));
    }

    @Test
    public void testCanUserStartProcessDefinition_startUserIds() {
        // 准备参数
        BpmProcessDefinitionInfoDO definition = new BpmProcessDefinitionInfoDO().setStartUserIds(List.of(1L, 2L));

        // 调用，并断言
        assertTrue(processDefinitionService.canUserStartProcessDefinition(definition, 2L));
        assertFalse(processDefinitionService.canUserStartProcessDefinition(definition, 3L));
    }

    @Test
    public void testCanUserStartProcessDefinition_startDeptIdsMatch() {
        // 准备参数
        BpmProcessDefinitionInfoDO definition = new BpmProcessDefinitionInfoDO().setStartDeptIds(List.of(10L));
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setDeptId(10L));

        // 调用，并断言
        assertTrue(processDefinitionService.canUserStartProcessDefinition(definition, 1L));
    }

    @Test
    public void testCanUserStartProcessDefinition_startDeptIdsNotMatch() {
        // 准备参数
        BpmProcessDefinitionInfoDO definition = new BpmProcessDefinitionInfoDO().setStartDeptIds(List.of(10L));
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setDeptId(20L));

        // 调用，并断言
        assertFalse(processDefinitionService.canUserStartProcessDefinition(definition, 1L));
    }

    @Test
    public void testCanUserStartProcessDefinition_noLimit() {
        // 调用，并断言
        assertTrue(processDefinitionService.canUserStartProcessDefinition(new BpmProcessDefinitionInfoDO(), 1L));
    }

    @Test
    public void testGetProcessDefinitionByDeploymentId_empty() {
        // 调用，并断言
        assertNull(processDefinitionService.getProcessDefinitionByDeploymentId(null));
        assertNull(processDefinitionService.getProcessDefinitionByDeploymentId(""));
    }

    @Test
    public void testGetProcessDefinitionListByDeploymentIds_empty() {
        // 调用，并断言
        assertTrue(processDefinitionService.getProcessDefinitionListByDeploymentIds(Collections.emptySet()).isEmpty());
    }

    @Test
    public void testGetActiveProcessDefinition_success() {
        // 准备参数
        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.processDefinitionTenantId(anyString())).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.processDefinitionKey("leave")).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.active()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.singleResult()).thenReturn(definition);

        // 调用，并断言
        assertSame(definition, processDefinitionService.getActiveProcessDefinition("leave"));
    }

    @Test
    public void testGetDeploymentList_filterNotExists() {
        // 准备参数
        Deployment deployment = mock(Deployment.class);
        when(repositoryService.createDeploymentQuery()).thenReturn(deploymentQuery);
        when(deploymentQuery.deploymentId(anyString())).thenReturn(deploymentQuery);
        when(deploymentQuery.singleResult()).thenReturn(deployment, null);

        // 调用，并断言
        assertEquals(Collections.singletonList(deployment),
                processDefinitionService.getDeploymentList(Set.of("deployment-1", "deployment-2")));
    }

    @Test
    public void testUpdateProcessDefinitionState_activeAndSuspended() {
        // 准备参数
        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(repositoryService.getProcessDefinition("definition-1")).thenReturn(definition);

        // 调用：挂起状态恢复为激活
        when(definition.isSuspended()).thenReturn(true);
        processDefinitionService.updateProcessDefinitionState("definition-1", SuspensionState.ACTIVE.getStateCode());
        verify(repositoryService).activateProcessDefinitionById("definition-1", false, null);

        // 调用：激活状态挂起
        when(definition.isSuspended()).thenReturn(false);
        processDefinitionService.updateProcessDefinitionState("definition-1", SuspensionState.SUSPENDED.getStateCode());
        verify(repositoryService).suspendProcessDefinitionById("definition-1", false, null);
    }

    @Test
    public void testGetProcessDefinitionPage_empty() {
        // 准备参数
        BpmProcessDefinitionPageReqVO pageVO = new BpmProcessDefinitionPageReqVO();
        when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.processDefinitionTenantId(anyString())).thenReturn(processDefinitionQuery);
        when(processDefinitionQuery.count()).thenReturn(0L);

        // 调用，并断言
        PageResult<ProcessDefinition> result = processDefinitionService.getProcessDefinitionPage(pageVO);
        assertEquals(0L, result.getTotal());
        assertTrue(result.getList().isEmpty());
    }

}
