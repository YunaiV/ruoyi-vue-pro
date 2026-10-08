package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.definition.BpmProcessDefinitionInfoMapper;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveMethodEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelServiceImpl;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.MODEL_UPDATE_FAIL_NOT_MANAGER;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_DEFINITION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * 流程模型部署、挂起、排序、清理、删除的 Flowable 集成测试
 *
 * <p>使用真实的 {@link BpmModelServiceImpl}、{@link BpmProcessDefinitionServiceImpl}（Flowable + bpm_process_definition_info 表），
 * 并让流程实例 Service 依赖的 {@link #processDefinitionService} 委托给真实实现，验证【模型部署 → 发起 → 审批】的完整闭环。</p>
 *
 * @author HUIHUI
 */
@Sql(scripts = "/sql/flowable/create_tables.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/flowable/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BpmModelDeployFlowableTest extends BaseFlowableUnitTest {

    private static final String MODEL_KEY = "modelDeploy";

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;
    @Resource
    private TaskService flowableTaskService;
    @Resource
    private BpmProcessDefinitionInfoMapper processDefinitionMapper;

    @Resource
    private BpmTaskServiceImpl taskService;
    @Resource
    private BpmProcessInstanceServiceImpl processInstanceService;

    private final BpmProcessDefinitionServiceImpl processDefinitionServiceImpl = new BpmProcessDefinitionServiceImpl();
    private final BpmModelServiceImpl modelServiceImpl = new BpmModelServiceImpl();

    @BeforeEach
    public void setUp() {
        // 真实的流程定义、流程模型 Service，依赖的 Flowable、Mapper 使用 Spring 中的真实 Bean
        ReflectionTestUtils.setField(processDefinitionServiceImpl, "repositoryService", repositoryService);
        ReflectionTestUtils.setField(processDefinitionServiceImpl, "processDefinitionMapper", processDefinitionMapper);
        ReflectionTestUtils.setField(processDefinitionServiceImpl, "adminUserApi", adminUserApi);
        ReflectionTestUtils.setField(modelServiceImpl, "repositoryService", repositoryService);
        ReflectionTestUtils.setField(modelServiceImpl, "runtimeService", runtimeService);
        ReflectionTestUtils.setField(modelServiceImpl, "historyService", historyService);
        ReflectionTestUtils.setField(modelServiceImpl, "taskService", flowableTaskService);
        ReflectionTestUtils.setField(modelServiceImpl, "processDefinitionService", processDefinitionServiceImpl);
        ReflectionTestUtils.setField(modelServiceImpl, "bpmFormService", formService);
        ReflectionTestUtils.setField(modelServiceImpl, "taskCandidateInvoker", taskCandidateInvoker);
        ReflectionTestUtils.setField(modelServiceImpl, "processInstanceCopyService", processInstanceCopyService);

        // 发起、审批流程时，流程定义相关的查询委托给真实实现，读取部署后写入的 bpm_process_definition_info
        mockFlowableBackedServices(repositoryService);
        doAnswer(delegatesTo(processDefinitionServiceImpl)).when(processDefinitionService)
                .getActiveProcessDefinition(anyString());
        doAnswer(delegatesTo(processDefinitionServiceImpl)).when(processDefinitionService)
                .getProcessDefinition(anyString());
        doAnswer(delegatesTo(processDefinitionServiceImpl)).when(processDefinitionService)
                .getProcessDefinitionInfo(anyString());
        doAnswer(delegatesTo(processDefinitionServiceImpl)).when(processDefinitionService)
                .canUserStartProcessDefinition(any(), any());
    }

    @AfterEach
    public void tearDown() {
        repositoryService.createDeploymentQuery().deploymentKey(MODEL_KEY).list()
                .forEach(deployment -> repositoryService.deleteDeployment(deployment.getId(), true));
        repositoryService.createModelQuery().modelKey(MODEL_KEY).list()
                .forEach(model -> repositoryService.deleteModel(model.getId()));
    }

    @Test
    public void testDeployModel_redeploy() {
        // 准备参数：创建 Simple 模型
        String modelId = createModel();

        // 调用：首次部署
        modelServiceImpl.deployModel(1L, modelId);
        // 断言：生成流程定义及拓展信息，模型关联最新部署
        ProcessDefinition definition1 = getActiveProcessDefinition();
        assertEquals("模型部署", definition1.getName());
        assertEquals("OA", definition1.getCategory());
        assertEquals(definition1.getDeploymentId(), repositoryService.getModel(modelId).getDeploymentId());
        BpmProcessDefinitionInfoDO definitionInfo1 = processDefinitionMapper.selectByProcessDefinitionId(definition1.getId());
        assertEquals(modelId, definitionInfo1.getModelId());
        assertEquals(BpmModelTypeEnum.SIMPLE.getType(), definitionInfo1.getModelType());
        assertEquals(List.of(1L), definitionInfo1.getManagerUserIds());
        assertNotNull(definitionInfo1.getSimpleModel());
        // 断言：可基于该流程定义发起流程，停留在审批节点
        String processInstanceId1 = startProcessInstance();
        Task task1 = getRunningTask(processInstanceId1);
        assertEquals(definition1.getId(), task1.getProcessDefinitionId());
        assertEquals("approve", task1.getTaskDefinitionKey());

        // 调用：再次部署
        modelServiceImpl.deployModel(1L, modelId);
        // 断言：老的流程定义被挂起，新发起的流程使用新的流程定义
        ProcessDefinition definition2 = getActiveProcessDefinition();
        assertNotEquals(definition1.getId(), definition2.getId());
        assertTrue(repositoryService.getProcessDefinition(definition1.getId()).isSuspended());
        assertEquals(definition2.getDeploymentId(), repositoryService.getModel(modelId).getDeploymentId());
        String processInstanceId2 = startProcessInstance();
        assertEquals(definition2.getId(), getRunningTask(processInstanceId2).getProcessDefinitionId());

        // 调用 + 断言：老流程定义上进行中的流程不受挂起影响，仍可审批通过
        approveTask(task1);
        assertProcessInstanceStatus(processInstanceId1, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
        approveTask(getRunningTask(processInstanceId2));
        assertProcessInstanceStatus(processInstanceId2, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

        // 调用 + 断言：调整排序，同一模型的所有流程定义拓展信息同步更新
        modelServiceImpl.updateModelSortBatch(1L, List.of(modelId));
        Long sort = processDefinitionMapper.selectByProcessDefinitionId(definition2.getId()).getSort();
        assertEquals(sort, processDefinitionMapper.selectByProcessDefinitionId(definition1.getId()).getSort());
        assertTrue(sort > definitionInfo1.getSort());
    }

    @Test
    public void testDeployModel_notManager() {
        // 准备参数
        String modelId = createModel();

        // 调用，并断言异常：非模型管理员不允许部署
        assertServiceException(() -> modelServiceImpl.deployModel(2L, modelId), MODEL_UPDATE_FAIL_NOT_MANAGER, "模型部署");
        assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey(MODEL_KEY).count());
    }

    @Test
    public void testUpdateModelState_suspendAndActive() {
        // 准备参数
        String modelId = createModel();
        modelServiceImpl.deployModel(1L, modelId);
        String processDefinitionId = getActiveProcessDefinition().getId();

        // 调用 + 断言：挂起后不允许发起
        modelServiceImpl.updateModelState(1L, modelId, SuspensionState.SUSPENDED.getStateCode());
        assertTrue(repositoryService.getProcessDefinition(processDefinitionId).isSuspended());
        assertStartProcessInstanceFail(PROCESS_DEFINITION_NOT_EXISTS);
        // 调用 + 断言：激活后恢复发起
        modelServiceImpl.updateModelState(1L, modelId, SuspensionState.ACTIVE.getStateCode());
        assertFalse(repositoryService.getProcessDefinition(processDefinitionId).isSuspended());
        assertEquals("approve", getRunningTask(startProcessInstance()).getTaskDefinitionKey());
    }

    @Test
    public void testCleanModel_success() {
        // 准备参数：一个进行中的流程，一个已结束的流程
        String modelId = createModel();
        modelServiceImpl.deployModel(1L, modelId);
        String runningId = startProcessInstance();
        String finishedId = startProcessInstance();
        approveTask(getRunningTask(finishedId));
        assertProcessInstanceStatus(finishedId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());

        // 调用
        modelServiceImpl.cleanModel(1L, modelId);
        // 断言：流程实例、历史、任务、抄送全部清理，模型与流程定义保留
        assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(MODEL_KEY).count());
        assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKey(MODEL_KEY).count());
        assertEquals(0, flowableTaskService.createTaskQuery().processDefinitionKey(MODEL_KEY).count());
        verify(processInstanceCopyService).deleteProcessInstanceCopy(runningId);
        verify(processInstanceCopyService).deleteProcessInstanceCopy(finishedId);
        assertNotNull(repositoryService.getModel(modelId));
        assertNotNull(getActiveProcessDefinition());
    }

    @Test
    public void testDeleteModel_success() {
        // 准备参数：一个进行中的流程
        String modelId = createModel();
        modelServiceImpl.deployModel(1L, modelId);
        String processDefinitionId = getActiveProcessDefinition().getId();
        String processInstanceId = startProcessInstance();

        // 调用
        modelServiceImpl.deleteModel(1L, modelId);
        // 断言：模型被删除，流程定义被挂起，不允许再发起
        assertNull(repositoryService.getModel(modelId));
        assertTrue(repositoryService.getProcessDefinition(processDefinitionId).isSuspended());
        assertStartProcessInstanceFail(PROCESS_DEFINITION_NOT_EXISTS);
        // 断言：进行中的流程不受影响，仍可审批通过
        approveTask(getRunningTask(processInstanceId));
        assertProcessInstanceStatus(processInstanceId, BpmProcessInstanceStatusEnum.APPROVE.getStatus());
    }

    // ========== 私有方法 ==========

    /**
     * 创建 Simple 模型：发起人 -> 审批（用户 1）-> 结束，管理员为用户 1
     */
    private String createModel() {
        BpmSimpleModelNodeVO approveNode = new BpmSimpleModelNodeVO().setId("approve").setName("审批")
                .setType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                .setApproveType(BpmUserTaskApproveTypeEnum.USER.getType())
                .setApproveMethod(BpmUserTaskApproveMethodEnum.RANDOM.getMethod())
                .setCandidateStrategy(BpmTaskCandidateStrategyEnum.USER.getStrategy()).setCandidateParam("1")
                .setChildNode(new BpmSimpleModelNodeVO().setId("end").setType(BpmSimpleModelNodeTypeEnum.END_NODE.getType()));
        BpmSimpleModelNodeVO startUserNode = new BpmSimpleModelNodeVO().setId("start-user").setName("发起人")
                .setType(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType())
                .setChildNode(approveNode);
        BpmModelSaveReqVO reqVO = new BpmModelSaveReqVO();
        reqVO.setKey(MODEL_KEY);
        reqVO.setName("模型部署");
        reqVO.setCategory("OA");
        reqVO.setSimpleModel(startUserNode);
        reqVO.setType(BpmModelTypeEnum.SIMPLE.getType());
        reqVO.setFormType(BpmModelFormTypeEnum.CUSTOM.getType());
        reqVO.setFormCustomCreatePath("/bpm/oa/leave/create");
        reqVO.setFormCustomViewPath("/bpm/oa/leave/detail");
        reqVO.setVisible(true);
        reqVO.setManagerUserIds(List.of(1L));
        return modelServiceImpl.createModel(reqVO);
    }

    private ProcessDefinition getActiveProcessDefinition() {
        ProcessDefinition definition = processDefinitionServiceImpl.getActiveProcessDefinition(MODEL_KEY);
        assertNotNull(definition);
        return definition;
    }

    private String startProcessInstance() {
        return processInstanceService.createProcessInstance(1L, new BpmProcessInstanceCreateReqDTO()
                .setProcessDefinitionKey(MODEL_KEY).setBusinessKey("business-1"));
    }

    /**
     * 发起流程失败：ServiceException 被 FlowableUtils#executeAuthenticatedUserId 包装，由全局异常处理器解包
     */
    private void assertStartProcessInstanceFail(ErrorCode errorCode) {
        RuntimeException exception = assertThrows(RuntimeException.class, this::startProcessInstance);
        ServiceException serviceException = assertInstanceOf(ServiceException.class, exception.getCause());
        assertEquals(errorCode.getCode(), serviceException.getCode());
    }

    private Task getRunningTask(String processInstanceId) {
        List<Task> tasks = taskService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
        assertEquals(1, tasks.size());
        return tasks.get(0);
    }

    private void approveTask(Task task) {
        taskService.approveTask(Long.valueOf(task.getAssignee()), new BpmTaskApproveReqVO().setId(task.getId())
                .setReason("同意"));
    }

    private void assertProcessInstanceStatus(String processInstanceId, Integer status) {
        assertEquals(status, FlowableUtils.getProcessInstanceStatus(
                processInstanceService.getHistoricProcessInstance(processInstanceId)));
    }

}
