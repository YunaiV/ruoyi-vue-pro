package cn.iocoder.yudao.module.bpm.framework.flowable;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskPageReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceUpdateServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskQueryServiceImpl;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskUpdateServiceImpl;
import jakarta.annotation.Resource;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link BpmTaskQueryServiceImpl} 的【待办任务】、【已办任务】、【任务管理】分页的 Flowable 集成测试，覆盖创建时间的筛选
 *
 * <p>通过 Flowable 的时钟，控制任务的创建时间、审批时间；审批人使用独立的用户，避免其它测试的数据干扰。</p>
 * <p>测试代码兼容 JDK 8，与 JDK 8 分支仅 Resource 注解的包名不同。</p>
 *
 * @author 芋道源码
 */
public class BpmTaskPageFlowableTest extends BaseFlowableUnitTest {

    private static final Long USER_ID = 9527L;
    private static final LocalDateTime START = LocalDateTime.of(2020, 1, 1, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2020, 1, 1, 12, 0);

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private TaskService flowableTaskService;
    @Resource
    private ProcessEngineConfiguration processEngineConfiguration;

    @Resource
    private BpmTaskQueryServiceImpl taskQueryService;
    @Resource
    private BpmTaskUpdateServiceImpl taskUpdateService;
    @Resource
    private BpmProcessInstanceUpdateServiceImpl processInstanceUpdateService;

    private String deploymentId;
    /**
     * 任务编号，key 为任务的标识
     */
    private final Map<String, String> taskIds = new HashMap<>();

    @BeforeEach
    public void setUp() {
        // 简单审批流程：审批人为 USER_ID
        deploymentId = repositoryService.createDeployment()
                .addClasspathResource("bpmn/simple-approve.bpmn20.xml").deploy().getId();
        mockFlowableBackedServices(repositoryService);
        when(taskCandidateInvoker.calculateUsersByTask(any())).thenReturn(Collections.singleton(USER_ID));

        // 准备数据：每个任务的创建时间、审批时间相同，按时间从旧到新创建
        createApprovedTask("older", START.minusNanos(1_000_000)); // 区间外较旧：早 1 毫秒
        createApprovedTask("start", START); // 恰好开始
        createApprovedTask("in1", START.plusMinutes(30));
        createApprovedTask("in2", START.plusMinutes(60));
        createApprovedTask("in3", START.plusMinutes(90));
        createApprovedTask("end", END); // 恰好结束
        createApprovedTask("newer", END.plusNanos(1_000_000)); // 区间外较新：晚 1 毫秒
    }

    @AfterEach
    public void tearDown() {
        processEngineConfiguration.getClock().reset();
        repositoryService.deleteDeployment(deploymentId, true);
    }

    @Test
    public void testGetTaskDonePage_createTime() {
        assertCreateTimePages(reqVO -> taskQueryService.getTaskDonePage(USER_ID, reqVO));
    }

    @Test
    public void testGetTaskPage_createTime() {
        assertCreateTimePages(reqVO -> taskQueryService.getTaskPage(USER_ID, reqVO));
    }

    @Test
    public void testGetTaskTodoPage_createTime() {
        // 准备参数：待办任务，按时间从旧到新创建
        createTodoTask("todoOlder", START.minusNanos(1_000_000)); // 区间外较旧：早 1 毫秒
        createTodoTask("todoStart", START); // 恰好开始
        createTodoTask("todoEnd", END); // 恰好结束
        createTodoTask("todoNewer", END.plusNanos(1_000_000)); // 区间外较新：晚 1 毫秒

        // 调用
        PageResult<Task> pageResult = taskQueryService.getTaskTodoPage(USER_ID,
                buildPageReqVO(1, 10, new LocalDateTime[]{START, END}));
        // 断言：包含区间两端，按创建时间倒序
        assertEquals(2, pageResult.getTotal().longValue());
        assertEquals(Arrays.asList(taskIds.get("todoEnd"), taskIds.get("todoStart")),
                convertList(pageResult.getList(), Task::getId));
    }

    @Test
    public void testGetTaskDonePage_noCreateTime() {
        // 调用 + 断言：不筛选创建时间时，保持原有行为，返回全部
        assertPage(taskQueryService.getTaskDonePage(USER_ID, buildPageReqVO(1, 10, null)), 7,
                "newer", "end", "in3", "in2", "in1", "start", "older");
    }

    /**
     * 断言创建时间筛选：包含区间两端，总数为筛选后的数量，并且按审批时间倒序跨页
     */
    private void assertCreateTimePages(Function<BpmTaskPageReqVO, PageResult<HistoricTaskInstance>> pageFunction) {
        LocalDateTime[] createTime = new LocalDateTime[]{START, END};
        // 调用 + 断言：区间内跨页，不包含区间外较新、较旧的数据
        assertPage(pageFunction.apply(buildPageReqVO(1, 2, createTime)), 5, "end", "in3");
        assertPage(pageFunction.apply(buildPageReqVO(2, 2, createTime)), 5, "in2", "in1");
        assertPage(pageFunction.apply(buildPageReqVO(3, 2, createTime)), 5, "start");
        // 调用 + 断言：超出最后一页时，返回空页，总数不变
        assertPage(pageFunction.apply(buildPageReqVO(4, 2, createTime)), 5);
        // 调用 + 断言：区间内没有数据
        assertPage(pageFunction.apply(buildPageReqVO(1, 2, new LocalDateTime[]{
                START.minusDays(1), END.minusDays(1)})), 0);
    }

    private void createApprovedTask(String key, LocalDateTime time) {
        Task task = createTodoTask(key, time);
        taskUpdateService.approveTask(USER_ID, new BpmTaskApproveReqVO().setId(task.getId()).setReason("同意"));
    }

    private Task createTodoTask(String key, LocalDateTime time) {
        processEngineConfiguration.getClock().setCurrentTime(DateUtils.of(time));
        String processInstanceId = processInstanceUpdateService.createProcessInstance(1L,
                new BpmProcessInstanceCreateReqDTO().setProcessDefinitionKey("simpleApprove"));
        Task task = flowableTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        assertEquals(String.valueOf(USER_ID), task.getAssignee());
        taskIds.put(key, task.getId());
        return task;
    }

    private static BpmTaskPageReqVO buildPageReqVO(int pageNo, int pageSize, LocalDateTime[] createTime) {
        BpmTaskPageReqVO reqVO = new BpmTaskPageReqVO();
        reqVO.setPageNo(pageNo);
        reqVO.setPageSize(pageSize);
        reqVO.setCreateTime(createTime);
        return reqVO;
    }

    private void assertPage(PageResult<HistoricTaskInstance> pageResult, long total, String... keys) {
        assertEquals(total, pageResult.getTotal().longValue());
        assertEquals(convertList(Arrays.asList(keys), taskIds::get),
                convertList(pageResult.getList(), HistoricTaskInstance::getId));
    }

}
