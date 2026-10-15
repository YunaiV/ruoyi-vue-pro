package cn.iocoder.yudao.module.bpm.service.task;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.task.BpmProcessInstanceCopyMapper;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_DEFINITION_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.TASK_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BpmProcessInstanceCopyServiceImpl} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmProcessInstanceCopyServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmProcessInstanceCopyServiceImpl copyService;

    @Mock
    private BpmProcessInstanceCopyMapper copyMapper;
    @Mock
    private BpmTaskService taskService;
    @Mock
    private BpmProcessInstanceService processInstanceService;
    @Mock
    private BpmProcessDefinitionService processDefinitionService;
    @Mock
    private Task task;
    @Mock
    private ProcessInstance processInstance;
    @Mock
    private ProcessDefinition processDefinition;

    @Test
    public void testCreateProcessInstanceCopy_task_success() {
        // 准备参数
        when(taskService.getTask("task-1")).thenReturn(task);
        when(task.getProcessInstanceId()).thenReturn("process-instance-1");
        when(task.getTaskDefinitionKey()).thenReturn("approve");
        when(task.getName()).thenReturn("审批");
        when(task.getId()).thenReturn("task-1");
        prepareProcessInstance();

        // 调用
        copyService.createProcessInstanceCopy(List.of(2L, 3L), "请知悉", "task-1");
        // 断言
        ArgumentCaptor<List<BpmProcessInstanceCopyDO>> captor = ArgumentCaptor.forClass(List.class);
        verify(copyMapper).insertBatch(captor.capture());
        assertEquals(List.of(2L, 3L), captor.getValue().stream().map(BpmProcessInstanceCopyDO::getUserId).toList());
        assertEquals("process-instance-1", captor.getValue().get(0).getProcessInstanceId());
        assertEquals(1L, captor.getValue().get(0).getStartUserId());
    }

    @Test
    public void testCreateProcessInstanceCopy_taskNotExists() {
        // 准备参数
        when(taskService.getTask("task-1")).thenReturn(null);

        // 调用，并断言异常
        assertServiceException(() -> copyService.createProcessInstanceCopy(List.of(2L), "请知悉", "task-1"),
                TASK_NOT_EXISTS);
    }

    @Test
    public void testCreateProcessInstanceCopy_processInstanceNotExists() {
        // 准备参数
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(null);

        // 调用，并断言异常
        assertServiceException(() -> copyService.createProcessInstanceCopy(List.of(2L), "请知悉",
                        "process-instance-1", "approve", "审批", "task-1"), PROCESS_INSTANCE_NOT_EXISTS);
    }

    @Test
    public void testCreateProcessInstanceCopy_processDefinitionNotExists() {
        // 准备参数
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(null);

        // 调用，并断言异常
        assertServiceException(() -> copyService.createProcessInstanceCopy(List.of(2L), "请知悉",
                        "process-instance-1", "approve", "审批", "task-1"), PROCESS_DEFINITION_NOT_EXISTS);
    }

    private void prepareProcessInstance() {
        when(processInstanceService.getProcessInstance("process-instance-1")).thenReturn(processInstance);
        when(processInstance.getStartUserId()).thenReturn("1");
        when(processInstance.getName()).thenReturn("请假申请");
        when(processInstance.getProcessDefinitionId()).thenReturn("definition-1");
        when(processDefinitionService.getProcessDefinition("definition-1")).thenReturn(processDefinition);
        when(processDefinition.getCategory()).thenReturn("oa");
    }

}
