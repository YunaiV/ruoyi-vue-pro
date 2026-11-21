package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.convert.task.BpmTaskConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.enums.task.*;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmFormService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.task.Attachment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.*;

/**
 * 流程任务实例【读】Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class BpmTaskQueryServiceImpl implements BpmTaskQueryService {

    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;
    @Resource
    private ManagementService managementService;

    @Resource
    private BpmProcessDefinitionService bpmProcessDefinitionService;
    @Resource
    private BpmModelService modelService;
    @Resource
    private BpmFormService formService;

    // ========== Query 查询相关方法 ==========

    @Override
    public PageResult<Task> getTaskTodoPage(Long userId, BpmTaskPageReqVO pageVO) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId)) // 分配给自己
                .active()
                // 排除退回修改中的任务：等待修改人重新提交，审批人暂时不能处理。在查询条件中排除，保证分页准确
                .taskVariableValueNotEquals(BpmnVariableConstants.TASK_VARIABLE_STATUS, BpmTaskStatusEnum.WAIT_REVISION.getStatus())
                .includeProcessVariables()
                .taskTenantId(FlowableUtils.getTenantId())
                .orderByTaskCreateTime().desc(); // 创建时间倒序
        if (StrUtil.isNotBlank(pageVO.getName())) {
            taskQuery.taskNameLike("%" + pageVO.getName() + "%");
        }
        if (StrUtil.isNotEmpty(pageVO.getCategory())) {
            taskQuery.taskCategory(pageVO.getCategory());
        }
        if (StrUtil.isNotEmpty(pageVO.getProcessDefinitionKey())) {
            taskQuery.processDefinitionKey(pageVO.getProcessDefinitionKey());
        }
        if (ArrayUtil.isNotEmpty(pageVO.getCreateTime())) {
            // 包含区间两端：taskCreatedAfter、taskCreatedBefore 不包含边界，所以前后各扩展 1 毫秒
            taskQuery.taskCreatedAfter(new Date(DateUtils.of(pageVO.getCreateTime()[0]).getTime() - 1));
            taskQuery.taskCreatedBefore(new Date(DateUtils.of(pageVO.getCreateTime()[1]).getTime() + 1));
        }
        long count = taskQuery.count();
        if (count == 0) {
            return PageResult.empty();
        }
        List<Task> tasks = taskQuery.listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());
        return new PageResult<>(tasks, count);
    }

    @Override
    public BpmTaskRespVO getTodoTask(Long userId, String taskId, String processInstanceId) {
        // 1.1 获取指定的用户待办任务
        Task todoTask = getMyTodoTask(userId, taskId, processInstanceId);
        // 1.2 获取不到，则获取该流程实例下，第一个用户的待办任务
        if (todoTask == null) {
            todoTask = getMyFirstTodoTask(userId, processInstanceId);
        }
        if (todoTask == null) {
            return null;
        }

        // 2. 查询该任务的子任务
        List<Task> childrenTasks = getAllChildrenTaskListByParentTaskId(todoTask.getId(), CollUtil.newArrayList(todoTask));

        // 3. 转换返回
        BpmnModel bpmnModel = bpmProcessDefinitionService.getProcessDefinitionBpmnModel(todoTask.getProcessDefinitionId());
        Map<Integer, BpmTaskRespVO.OperationButtonSetting> buttonsSetting = BpmnModelUtils.parseButtonsSetting(
                bpmnModel, todoTask.getTaskDefinitionKey());
        Boolean signEnable = parseSignEnable(bpmnModel, todoTask.getTaskDefinitionKey());
        Boolean reasonRequire = parseReasonRequire(bpmnModel, todoTask.getTaskDefinitionKey());
        Integer nodeType = parseNodeType(BpmnModelUtils.getFlowElementById(bpmnModel, todoTask.getTaskDefinitionKey()));

        // 4. 任务表单
        BpmFormDO taskForm = null;
        if (StrUtil.isNotBlank(todoTask.getFormKey())) {
            taskForm = formService.getForm(NumberUtils.parseLong(todoTask.getFormKey()));
        }

        return BpmTaskConvert.INSTANCE.buildTodoTask(todoTask, childrenTasks, buttonsSetting, taskForm)
                .setNodeType(nodeType).setSignEnable(signEnable).setReasonRequire(reasonRequire)
                .setRevisionId((String) todoTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REVISION_ID));
    }

    /**
     * 获得用户指定 taskId 任务编号的“待办”（未审批、且可审核）的任务
     *
     * @param userId            用户编号
     * @param taskId            任务编号
     * @param processInstanceId 流程编号
     * @return 任务
     */
    private Task getMyTodoTask(Long userId, String taskId, String processInstanceId) {
        if (StrUtil.isEmpty(taskId)) {
            return null;
        }
        Task task = getTask(taskId);
        if (task == null) {
            return null;
        }
        // 校验任务属于该流程实例，避免使用其它流程实例的任务
        if (ObjectUtil.notEqual(task.getProcessInstanceId(), processInstanceId)) {
            return null;
        }
        if (!isAssignUserTask(userId, task) && !isAddSignUserTask(userId, task)) {
            return null;
        }
        // 退回修改中的任务，审批人暂时不能处理，所以不是待办
        if (isWaitRevisionTask(task)) {
            return null;
        }
        return task;
    }

    /**
     * 获得用户指定 processInstanceId 流程编号下的首个“待办”（未审批、且可审核）的任务
     *
     * @param userId            用户编号
     * @param processInstanceId 流程编号
     * @return 任务
     */
    private Task getMyFirstTodoTask(Long userId, String processInstanceId) {
        if (processInstanceId == null) {
            return null;
        }
        // 1. 查询所有任务
        List<Task> tasks = taskService.createTaskQuery()
                .active()
                .processInstanceId(processInstanceId)
                .includeTaskLocalVariables()
                .includeProcessVariables()
                .orderByTaskCreateTime().asc() // 按创建时间升序
                .list();

        // 2. 查询我的首个任务
        return CollUtil.findOne(tasks, task -> {
            if (isWaitRevisionTask(task)) { // 退回修改中的任务，不是待办
                return false;
            }
            return isAssignUserTask(userId, task) // 当前用户为审批人
                    || isAddSignUserTask(userId, task); // 当前用户为加签人（为了减签）
        });
    }

    @Override
    public Task getRevisionTask(Long userId, String processInstanceId, String taskId) {
        if (StrUtil.isAllEmpty(processInstanceId, taskId)) {
            return null;
        }
        TaskQuery taskQuery = buildRevisionTaskQuery(userId);
        if (StrUtil.isNotEmpty(processInstanceId)) {
            taskQuery.processInstanceId(processInstanceId);
        }
        // 传递 taskId 时，只查询该任务，避免误用该流程实例中的其它任务
        if (StrUtil.isNotEmpty(taskId)) {
            taskQuery.taskId(taskId);
        }
        return CollUtil.getFirst(taskQuery.orderByTaskCreateTime().asc().orderByTaskId().asc().list());
    }

    @Override
    public PageResult<Task> getRevisionTaskPage(Long userId, BpmTaskPageReqVO pageVO) {
        TaskQuery taskQuery = buildRevisionTaskQuery(userId)
                .orderByTaskCreateTime().desc() // 创建时间倒序
                .orderByTaskId().desc(); // 创建时间相同时，按编号排序，保证分页稳定
        long count = taskQuery.count();
        if (count == 0) {
            return PageResult.empty();
        }
        List<Task> tasks = taskQuery.listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());
        return new PageResult<>(tasks, count);
    }

    /**
     * 构建指定用户的退回修改中（WAIT_REVISION）的任务查询
     * <p>
     * 修改人记录在任务变量中，而不是 assignee（仍然是审批人），所以按任务变量查询，并限定当前租户
     *
     * @param userId 用户编号，即修改人
     * @return 任务查询
     */
    private TaskQuery buildRevisionTaskQuery(Long userId) {
        return taskService.createTaskQuery()
                .taskTenantId(FlowableUtils.getTenantId())
                .active()
                .taskVariableValueEquals(BpmnVariableConstants.TASK_VARIABLE_REVISION_ASSIGNEE, String.valueOf(userId))
                .taskVariableValueEquals(BpmnVariableConstants.TASK_VARIABLE_STATUS, BpmTaskStatusEnum.WAIT_REVISION.getStatus())
                .includeTaskLocalVariables();
    }

    @Override
    public PageResult<HistoricTaskInstance> getTaskDonePage(Long userId, BpmTaskPageReqVO pageVO) {
        HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery()
                .finished() // 已完成
                .taskAssignee(String.valueOf(userId)) // 分配给自己
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime().desc(); // 审批时间倒序
        if (StrUtil.isNotBlank(pageVO.getName())) {
            taskQuery.taskNameLike("%" + pageVO.getName() + "%");
        }
        if (pageVO.getStatus() != null) {
            taskQuery.taskVariableValueEquals(BpmnVariableConstants.TASK_VARIABLE_STATUS, pageVO.getStatus());
        }
        if (ArrayUtil.isNotEmpty(pageVO.getCreateTime())) {
            // 包含区间两端：taskCreatedAfter、taskCreatedBefore 不包含边界，所以前后各扩展 1 毫秒
            taskQuery.taskCreatedAfter(new Date(DateUtils.of(pageVO.getCreateTime()[0]).getTime() - 1));
            taskQuery.taskCreatedBefore(new Date(DateUtils.of(pageVO.getCreateTime()[1]).getTime() + 1));
        }
        // 执行查询
        long count = taskQuery.count();
        if (count == 0) {
            return PageResult.empty();
        }
        List<HistoricTaskInstance> tasks = taskQuery.listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());

        // 特殊：强制移除自动完成的“发起人”节点
        // 补充说明：由于 taskQuery 无法方面的过滤，所以暂时通过内存过滤
        tasks.removeIf(task -> task.getTaskDefinitionKey().equals(START_USER_NODE_ID));
        return new PageResult<>(tasks, count);
    }

    @Override
    public PageResult<HistoricTaskInstance> getTaskPage(Long userId, BpmTaskPageReqVO pageVO) {
        HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery()
                .includeTaskLocalVariables()
                .taskTenantId(FlowableUtils.getTenantId())
                .orderByHistoricTaskInstanceEndTime().desc(); // 审批时间倒序
        if (StrUtil.isNotBlank(pageVO.getName())) {
            taskQuery.taskNameLike("%" + pageVO.getName() + "%");
        }
        if (StrUtil.isNotEmpty(pageVO.getCategory())) {
            taskQuery.taskCategory(pageVO.getCategory());
        }
        if (ArrayUtil.isNotEmpty(pageVO.getCreateTime())) {
            // 包含区间两端：taskCreatedAfter、taskCreatedBefore 不包含边界，所以前后各扩展 1 毫秒
            taskQuery.taskCreatedAfter(new Date(DateUtils.of(pageVO.getCreateTime()[0]).getTime() - 1));
            taskQuery.taskCreatedBefore(new Date(DateUtils.of(pageVO.getCreateTime()[1]).getTime() + 1));
        }
        // 执行查询
        long count = taskQuery.count();
        if (count == 0) {
            return PageResult.empty();
        }
        List<HistoricTaskInstance> tasks = taskQuery.listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());
        return new PageResult<>(tasks, count);
    }

    @Override
    public List<Task> getTasksByProcessInstanceIds(List<String> processInstanceIds) {
        if (CollUtil.isEmpty(processInstanceIds)) {
            return Collections.emptyList();
        }
        return taskService.createTaskQuery().processInstanceIdIn(processInstanceIds).list();
    }

    @Override
    public List<HistoricTaskInstance> getTaskListByProcessInstanceId(String processInstanceId, Boolean asc) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .includeTaskLocalVariables()
                .processInstanceId(processInstanceId);
        if (Boolean.TRUE.equals(asc)) {
            query.orderByHistoricTaskInstanceStartTime().asc();
        } else {
            query.orderByHistoricTaskInstanceStartTime().desc();
        }
        return query.list();
    }

    @Override
    public Task validateTask(Long userId, String taskId) {
        Task task = validateTaskExists(taskId);
        // 为什么判断 assignee 非空的情况下？
        // 例如说：在审批人为空时，我们会有“自动审批通过”的策略，此时 userId 为 null，允许通过
        if (StrUtil.isNotBlank(task.getAssignee())
                && ObjectUtil.notEqual(userId, NumberUtils.parseLong(task.getAssignee()))) {
            throw exception(TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        }
        // 为什么等待中（WAIT），或者 assignee 为空、owner 非空时，不允许操作？
        // 例如说：【加签】等待中（WAIT）的任务（向前加签的父任务、向后加签的子任务），没有 assignee，
        // 需要等待其它任务审批后，才能恢复 assignee 继续审批。此时，任何人都不能直接审批、拒绝它
        // 补充说明：被委派的任务向前加签时，等待人记录在任务变量中，owner 仍是委派人，所以需要按 WAIT 状态判断
        Integer status = (Integer) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
        if (BpmTaskStatusEnum.WAIT.getStatus().equals(status)
                || (StrUtil.isBlank(task.getAssignee()) && StrUtil.isNotBlank(task.getOwner()))) {
            throw exception(TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        }
        // 为什么审批通过中（APPROVING）时，不允许操作？
        // 例如说：【向后加签】的父任务已经审批通过，需要等待加签出来的子任务审批完成后自动完成。此时，不能重复审批、拒绝、继续加签等
        if (BpmTaskStatusEnum.APPROVING.getStatus().equals(status)) {
            throw exception(TASK_OPERATE_FAIL_APPROVING);
        }
        // 为什么退回修改中（WAIT_REVISION）时，不允许操作？
        // 例如说：节点配置【从当前节点审批】时，退回后任务不跳转，需要等待修改人重新提交后，才能继续审批、退回、委派、加签等
        if (BpmTaskStatusEnum.WAIT_REVISION.getStatus().equals(status)) {
            throw exception(TASK_WAIT_REVISION);
        }
        return task;
    }

    @Override
    public Task validateTaskExists(String id) {
        Task task = getTask(id);
        if (task == null) {
            throw exception(TASK_NOT_EXISTS);
        }
        return task;
    }

    @Override
    public Task getTask(String id) {
        return taskService.createTaskQuery().taskId(id).includeTaskLocalVariables().singleResult();
    }

    @Override
    public HistoricTaskInstance getHistoricTask(String id) {
        return historyService.createHistoricTaskInstanceQuery().taskId(id).includeTaskLocalVariables().singleResult();
    }

    @Override
    public List<HistoricTaskInstance> getHistoricTasks(Collection<String> taskIds) {
        if (CollUtil.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        return historyService.createHistoricTaskInstanceQuery().taskIds(taskIds).includeTaskLocalVariables().list();
    }

    @Override
    public List<Task> getRunningTaskListByProcessInstanceId(String processInstanceId, Boolean assigned, String defineKey) {
        Assert.notNull(processInstanceId, "processInstanceId 不能为空");
        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstanceId).active()
                .includeTaskLocalVariables();
        if (BooleanUtil.isTrue(assigned)) {
            taskQuery.taskAssigned();
        } else if (BooleanUtil.isFalse(assigned)) {
            taskQuery.taskUnassigned();
        }
        if (StrUtil.isNotEmpty(defineKey)) {
            taskQuery.taskDefinitionKey(defineKey);
        }
        return taskQuery.list();
    }

    @Override
    public List<UserTask> getUserTaskListByReturn(String id) {
        // 1.1 校验当前任务 task 存在
        Task task = validateTaskExists(id);
        // 1.2 根据流程定义获取流程模型信息
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(task.getProcessDefinitionId());
        FlowElement source = BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
        if (source == null) {
            throw exception(TASK_NOT_EXISTS);
        }

        // 2.1 查询该任务的前置任务节点的 key 集合
        List<UserTask> previousUserList = BpmnModelUtils.getPreviousUserTaskList(source, null, null);
        if (CollUtil.isEmpty(previousUserList)) {
            return Collections.emptyList();
        }
        // 2.2 过滤：只有串行可到达的节点，才可以退回。类似非串行、子流程无法退回
        previousUserList.removeIf(userTask -> !BpmnModelUtils.isSequentialReachable(source, userTask, null));

        // 2.3 过滤：只能退回到已经处理过的节点（排除审批未经过的节点）。相关 issue：https://github.com/YunaiV/ruoyi-vue-pro/issues/982
        List<HistoricTaskInstance> finishedTasks = getFinishedTaskListByProcessInstanceIdWithoutCancel(task.getProcessInstanceId());
        Set<String> finishedTaskDefinitionKeys = convertSet(finishedTasks, HistoricTaskInstance::getTaskDefinitionKey);
        previousUserList.removeIf(userTask -> !finishedTaskDefinitionKeys.contains(userTask.getId()));
        return previousUserList;
    }

    @Override
    public <T extends TaskInfo> List<T> getAllChildrenTaskListByParentTaskId(String parentTaskId, List<T> tasks) {
        if (CollUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        Map<String, List<T>> parentTaskMap = convertMultiMap(
                filterList(tasks, task -> StrUtil.isNotEmpty(task.getParentTaskId())), TaskInfo::getParentTaskId);
        if (CollUtil.isEmpty(parentTaskMap)) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();
        // 1. 递归获取子级
        Stack<String> stack = new Stack<>();
        stack.push(parentTaskId);
        // 2. 递归遍历
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            if (stack.isEmpty()) {
                break;
            }
            // 2.1 获取子任务们
            String taskId = stack.pop();
            List<T> childTaskList = filterList(tasks, task -> StrUtil.equals(task.getParentTaskId(), taskId));
            // 2.2 如果非空，则添加到 stack 进一步递归
            if (CollUtil.isNotEmpty(childTaskList)) {
                stack.addAll(convertList(childTaskList, TaskInfo::getId));
                result.addAll(childTaskList);
            }
        }
        return result;
    }

    @Override
    public List<Task> getTaskListByParentTaskId(String parentTaskId) {
        String tableName = managementService.getTableName(TaskEntity.class);
        // taskService.createTaskQuery() 没有 parentId 参数，所以写 sql 查询
        String sql = "select ID_,NAME_,OWNER_,ASSIGNEE_ from " + tableName + " where PARENT_TASK_ID_=#{parentTaskId}";
        return taskService.createNativeTaskQuery().sql(sql).parameter("parentTaskId", parentTaskId).list();
    }

    @Override
    public List<HistoricActivityInstance> getActivityListByProcessInstanceId(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();
    }

    @Override
    public List<HistoricActivityInstance> getHistoricActivityListByExecutionId(String executionId) {
        return historyService.createHistoricActivityInstanceQuery().executionId(executionId).list();
    }

    @Override
    public List<HistoricTaskInstance> getFinishedTaskListByProcessInstanceIdWithoutCancel(String processInstanceId) {
        return historyService.createHistoricTaskInstanceQuery()
                .finished()
                .includeTaskLocalVariables()
                .processInstanceId(processInstanceId)
                .taskVariableValueNotEquals(BpmnVariableConstants.TASK_VARIABLE_STATUS,
                        BpmTaskStatusEnum.CANCEL.getStatus())
                .orderByHistoricTaskInstanceStartTime().asc().list();
    }

    @Override
    public List<Attachment> getAttachments(String processInstanceId, Set<String> taskIds, BpmAttachmentTypeEnum attachmentType) {
        List<Attachment> result = taskService.getProcessInstanceAttachments(processInstanceId);
        if (CollUtil.isNotEmpty(taskIds)) {
            result = filterList(result, attachment -> taskIds.contains(attachment.getTaskId()));
        }
        if (attachmentType != null) {
            result = filterList(result, attachment -> attachmentType.getType().equals(attachment.getType()));
        }
        return result;
    }

    /**
     * 判断指定用户，是否是当前任务的审批人
     *
     * @param userId 用户编号
     * @param task   任务
     * @return 是否
     */
    static boolean isAssignUserTask(Long userId, Task task) {
        Long assignee = NumberUtil.parseLong(task.getAssignee(), null);
        return ObjectUtil.equals(userId, assignee);
    }

    /**
     * 获得【加签】场景下，任务的拥有人：即等待中（WAIT）恢复审批的用户
     * <p>
     * 1. 被委派的任务向前加签时，等待人记录在 {@link BpmnVariableConstants#TASK_SIGN_WAIT_ASSIGNEE} 任务变量中，
     *    此时 owner 是委派人（仅用于归还），由等待人替代 owner
     * 2. 其它情况，沿用 owner。例如说：先向后加签、再委派的任务，owner 就是实际的加签人
     *
     * @param task 任务，需要包含任务变量
     * @return 用户编号
     */
    static String getSignTaskOwner(Task task) {
        String waitAssignee = (String) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_SIGN_WAIT_ASSIGNEE);
        return StrUtil.isNotBlank(waitAssignee) ? waitAssignee : task.getOwner();
    }

    /**
     * 判断指定用户，是否是当前任务的审批人，或者加签等待中的拥有人
     *
     * @param userId 用户编号
     * @param task   任务
     * @return 是否
     */
    static boolean isSignTaskUser(Long userId, Task task) {
        return isAssignUserTask(userId, task)
                || ObjectUtil.equal(userId, NumberUtil.parseLong(getSignTaskOwner(task), null));
    }

    /**
     * 判断指定用户，是否是当前任务的加签人
     *
     * @param userId 用户 Id
     * @param task   任务
     * @return 是否
     */
    private boolean isAddSignUserTask(Long userId, Task task) {
        return BpmTaskSignTypeEnum.of(task.getScopeType()) != null
                && isSignTaskUser(userId, task);
    }

    /**
     * 判断任务是否退回修改中（WAIT_REVISION）：等待修改人重新提交，审批人暂时不能处理
     *
     * @param task 任务，需要包含任务变量
     * @return 是否
     */
    static boolean isWaitRevisionTask(TaskInfo task) {
        return BpmTaskStatusEnum.WAIT_REVISION.getStatus().equals(FlowableUtils.getTaskStatus(task));
    }

}
