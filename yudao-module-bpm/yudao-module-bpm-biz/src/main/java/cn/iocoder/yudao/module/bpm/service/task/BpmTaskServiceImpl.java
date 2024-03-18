package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.convert.task.BpmTaskConvert;
import cn.iocoder.yudao.module.bpm.enums.task.BpmCommentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatustEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskAddSignTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmConstants;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * 流程任务实例 Service 实现类
 *
 * @author 芋道源码
 * @author jason
 */
@Slf4j
@Service
public class BpmTaskServiceImpl implements BpmTaskService {

    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private ManagementService managementService;

    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;
    @Resource
    private BpmModelService bpmModelService;
    @Resource
    private BpmMessageService messageService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public PageResult<Task> getTodoTaskPage(Long userId, BpmTaskPageReqVO pageVO) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId)) // 分配给自己
                .active()
                .includeProcessVariables()
                .orderByTaskCreateTime().desc(); // 创建时间倒序
        if (StrUtil.isNotBlank(pageVO.getName())) {
            taskQuery.taskNameLike("%" + pageVO.getName() + "%");
        }
        if (ArrayUtil.isNotEmpty(pageVO.getCreateTime())) {
            taskQuery.taskCreatedAfter(DateUtils.of(pageVO.getCreateTime()[0]));
            taskQuery.taskCreatedAfter(DateUtils.of(pageVO.getCreateTime()[1]));
        }
        long count = taskQuery.count();
        if (count == 0) {
            return PageResult.empty();
        }
        List<Task> tasks = taskQuery.listPage(PageUtils.getStart(pageVO), pageVO.getPageSize());
        return new PageResult<>(tasks, count);
    }

    @Override
    public PageResult<HistoricTaskInstance> getDoneTaskPage(Long userId, BpmTaskPageReqVO pageVO) {
        HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery()
                .finished() // 已完成
                .taskAssignee(String.valueOf(userId)) // 分配给自己
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceEndTime().desc(); // 审批时间倒序
        if (StrUtil.isNotBlank(pageVO.getName())) {
            taskQuery.taskNameLike("%" + pageVO.getName() + "%");
        }
        if (ArrayUtil.isNotEmpty(pageVO.getCreateTime())) {
            taskQuery.taskCreatedAfter(DateUtils.of(pageVO.getCreateTime()[0]));
            taskQuery.taskCreatedAfter(DateUtils.of(pageVO.getCreateTime()[1]));
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
    public List<HistoricTaskInstance> getTaskListByProcessInstanceId(String processInstanceId) {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .includeTaskLocalVariables()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().desc() // 创建时间倒序
                .list();
        if (CollUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        return tasks;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveTask(Long userId, @Valid BpmTaskApproveReqVO reqVO) {
        // 1.1 校验任务存在
        Task task = validateTask(userId, reqVO.getId());
        // 1.2 校验流程实例存在
        ProcessInstance instance = processInstanceService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 2. 抄送用户
        if (CollUtil.isNotEmpty(reqVO.getCopyUserIds())) {
            processInstanceCopyService.createProcessInstanceCopy(reqVO.getCopyUserIds(), reqVO.getId());
        }

        // 情况一：被委派的任务，不调用 complete 去完成任务
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            approveDelegateTask(reqVO, task);
            return;
        }

        // 情况二：审批有【后】加签的任务
        if (BpmTaskAddSignTypeEnum.AFTER.getType().equals(task.getScopeType())) {
            approveAfterSignTask(task, reqVO);
            return;
        }

        // 情况三：审批普通的任务。大多数情况下，都是这样
        // 3.1 更新 task 状态、原因
        updateTaskStatus(task.getId(), BpmTaskStatustEnum.APPROVE.getStatus());
        // 3.2 添加评论
        taskService.addComment(task.getId(), task.getProcessInstanceId(),
                BpmCommentTypeEnum.APPROVE.getType(), reqVO.getReason());
        // 3.3 调用 BPM complete 去完成任务
        taskService.complete(task.getId(), instance.getProcessVariables());

        // 【加签专属】处理加签任务
        handleParentTaskIfSign(task.getParentTaskId());
    }

    /**
     * 审批通过存在“后加签”的任务。
     * <p>
     * 注意：该任务不能马上完成，需要一个中间状态（SIGN_AFTER），并激活剩余所有子任务（PROCESS）为可审批处理
     *
     * @param task  当前任务
     * @param reqVO 前端请求参数
     */
    private void approveAfterSignTask(Task task, BpmTaskApproveReqVO reqVO) {
        // 1. 有向后加签，则该任务状态临时设置为 ADD_SIGN_AFTER 状态
//        taskExtMapper.updateByTaskId(
//                new BpmTaskExtDO().setTaskId(task.getId()).setResult(BpmProcessInstanceResultEnum.SIGN_AFTER.getResult())
//                        .setReason(reqVO.getReason()).setEndTime(LocalDateTime.now()));
        // TODO @芋艿：reqVO.reason？？？
        updateTaskStatus(task.getId(), BpmTaskStatustEnum.APPROVING.getStatus());

        // 2. 激活子任务
        List<String> childrenTaskIdList = getChildrenTaskIdList(task.getId());
        for (String childrenTaskId : childrenTaskIdList) {
            taskService.resolveTask(childrenTaskId);
            // 更新任务扩展表中子任务为进行中
            updateTaskStatus(childrenTaskId, BpmTaskStatustEnum.RUNNING.getStatus());
        }
        // 2.1 更新任务扩展表中子任务为进行中
//        taskExtMapper.updateBatchByTaskIdList(childrenTaskIdList,
//                new BpmTaskExtDO().setResult(BpmProcessInstanceResultEnum.PROCESS.getResult()));
    }

    /**
     * 如果父任务是有前后【加签】的任务，如果它【加签】出来的子任务都被处理，需要处理父任务：
     *
     * 1. 如果是【向前】加签，则需要重新激活父任务，让它可以被审批
     * 2. 如果是【向后】加签，则需要完成父任务，让它完成审批
     *
     * @param parentTaskId 父任务编号
     */
    private void handleParentTaskIfSign(String parentTaskId) {
        if (StrUtil.isBlank(parentTaskId)) {
            return;
        }
        // 1.1 判断是否还有子任务。如果没有，就不处理
        Long childrenTaskCount = getChildrenTaskCount(parentTaskId);
        if (childrenTaskCount > 0) {
            return;
        }
        // 1.2 只处理加签的父任务
        Task parentTask = validateTaskExist(parentTaskId);
        String scopeType = parentTask.getScopeType();
        if (!validateSignType(scopeType)){
            return;
        }

        // 2. 子任务已处理完成，清空 scopeType 字段，修改 parentTask 信息，方便后续可以继续向前后向后加签
        TaskEntityImpl parentTaskImpl = (TaskEntityImpl) parentTask;
        parentTaskImpl.setScopeType(null);
        taskService.saveTask(parentTaskImpl);

        // 3.1 情况一：处理向【向前】加签
        if (BpmTaskAddSignTypeEnum.BEFORE.getType().equals(scopeType)) {
            // 3.1.1 owner 重新赋值给父任务的 assignee，这样它就可以被审批
            taskService.resolveTask(parentTaskId);
            // 3.1.2 更新流程任务 status
            updateTaskStatus(parentTaskId, BpmTaskStatustEnum.RUNNING.getStatus());
        // 3.2 情况二：处理向【向后】加签
        } else if (BpmTaskAddSignTypeEnum.AFTER.getType().equals(scopeType)) {
            // 3.2.1 完成自己（因为它已经没有子任务，所以也可以完成）
            updateTaskStatus(parentTaskId, BpmTaskStatustEnum.APPROVE.getStatus());
            taskService.complete(parentTaskId);
        }

        // 4. 递归处理父任务
        handleParentTaskIfSign(parentTask.getParentTaskId());
    }

    /**
     * 获取子任务个数
     *
     * @param parentTaskId 父任务 ID
     * @return 剩余子任务个数
     */
    private Long getChildrenTaskCount(String parentTaskId) {
        String tableName = managementService.getTableName(TaskEntity.class);
        String sql = "SELECT COUNT(1) from " + tableName + " WHERE PARENT_TASK_ID_=#{parentTaskId}";
        return taskService.createNativeTaskQuery().sql(sql).parameter("parentTaskId", parentTaskId).count();
    }

    /**
     * 审批被委派的任务
     *
     * @param reqVO 前端请求参数，包含当前任务ID，审批意见等
     * @param task  当前被审批的任务
     */
    private void approveDelegateTask(BpmTaskApproveReqVO reqVO, Task task) {
        // 1. 添加审批意见
        AdminUserRespDTO currentUser = adminUserApi.getUser(WebFrameworkUtils.getLoginUserId());
        AdminUserRespDTO sourceApproveUser = adminUserApi.getUser(NumberUtils.parseLong(task.getOwner()));
        Assert.notNull(sourceApproveUser, "委派任务找不到原审批人，需要检查数据");
        String comment = StrUtil.format("[{}]完成委派任务，任务重新回到[{}]手中，审批意见为:{}", currentUser.getNickname(),
                sourceApproveUser.getNickname(), reqVO.getReason());
        taskService.addComment(reqVO.getId(), task.getProcessInstanceId(),
                BpmCommentTypeEnum.DELEGATE.getType().toString(), comment);

        // 2.1 调用 resolveTask 完成任务。
        // 底层调用 TaskHelper.changeTaskAssignee(task, task.getOwner())：将 owner 设置为 assignee
        taskService.resolveTask(task.getId());
        // 2.2 更新任务拓展表为【处理中】
//        taskExtMapper.updateByTaskId(
//                new BpmTaskExtDO().setTaskId(task.getId()).setResult(BpmProcessInstanceResultEnum.RUNNING.getResult())
//                        .setReason(reqVO.getReason()));
        updateTaskStatus(task.getId(), BpmTaskStatustEnum.RUNNING.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectTask(Long userId, @Valid BpmTaskRejectReqVO reqVO) {
        Task task = validateTask(userId, reqVO.getId());
        // 校验流程实例存在
        ProcessInstance instance = processInstanceService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 更新流程实例为不通过
        updateTaskStatus(task.getId(), BpmTaskStatustEnum.REJECT.getStatus());
        processInstanceService.updateProcessInstanceExtReject(instance.getProcessInstanceId(), reqVO.getReason());

//        // 更新任务拓展表为不通过
//        taskExtMapper.updateByTaskId(
//                new BpmTaskExtDO().setTaskId(task.getId()).setResult(BpmProcessInstanceResultEnum.REJECT.getResult())
//                        .setEndTime(LocalDateTime.now()).setReason(reqVO.getReason()));
    }

    @Override
    public void updateTaskAssignee(Long userId, BpmTaskUpdateAssigneeReqVO reqVO) {
        // 校验任务存在
        Task task = validateTask(userId, reqVO.getId());
        // 更新负责人
        updateTaskAssignee(task.getId(), reqVO.getAssigneeUserId());
    }

    @Override
    public void updateTaskAssignee(String id, Long userId) {
        taskService.setAssignee(id, String.valueOf(userId));
    }

    /**
     * 更新流程任务的 status 状态
     *
     * @param id    任务编号
     * @param status 状态
     */
    private void updateTaskStatus(String id, Integer status) {
        taskService.setVariableLocal(id, BpmConstants.TASK_VARIABLE_STATUS, status);
    }

    /**
     * 更新流程任务的 status 状态、reason 理由
     *
     * @param id 任务编号
     * @param status 状态
     * @param reason 理由（审批通过、审批不通过的理由）
     */
    private void updateTaskStatus(String id, Integer status, String reason) {
        taskService.setVariableLocal(id, BpmConstants.TASK_VARIABLE_STATUS, status);
        taskService.setVariableLocal(id, BpmConstants.TASK_VARIABLE_REASON, reason);
    }

    /**
     * 校验任务是否存在， 并且是否是分配给自己的任务
     *
     * @param userId 用户 id
     * @param taskId task id
     */
    private Task validateTask(Long userId, String taskId) {
        Task task = validateTaskExist(taskId);
        if (!Objects.equals(userId, NumberUtils.parseLong(task.getAssignee()))) {
            throw exception(TASK_OPERATE_FAIL_ASSIGN_NOT_SELF);
        }
        return task;
    }

    @Override
    public void createTaskExt(Task task) {
//        BpmTaskExtDO taskExtDO = BpmTaskConvert.INSTANCE.convert2TaskExt(task)
//                .setResult(BpmProcessInstanceResultEnum.PROCESS.getResult());
//        // 向后加签生成的任务，状态不能为进行中，需要等前面父任务完成
//        if (BpmTaskAddSignTypeEnum.AFTER_CHILDREN_TASK.getType().equals(task.getScopeType())) {
//            taskExtDO.setResult(BpmProcessInstanceResultEnum.WAIT_BEFORE_TASK.getResult());
//        }
//        taskExtMapper.insert(taskExtDO);
//        Integer status = (Integer) task.getTaskLocalVariables().get(BpmConstants.TASK_VARIABLE_STATUS);
//        if (status != null) {
//            return;
//        }
//
//        status = BpmProcessInstanceResultEnum.RUNNING.getResult();
        // 向后加签生成的任务，状态不能为进行中，需要等前面父任务完成
//        if (BpmTaskAddSignTypeEnum.AFTER_CHILDREN_TASK.getType().equals(task.getScopeType())) {
//            status = BpmProcessInstanceResultEnum.WAIT_BEFORE_TASK.getResult();
//        }
        updateTaskStatus(task.getId(), BpmTaskStatustEnum.RUNNING.getStatus());
    }

    @Override
    public void updateTaskExtComplete(Task task) {
//        BpmTaskExtDO taskExtDO = BpmTaskConvert.INSTANCE.convert2TaskExt(task)
//                .setResult(BpmProcessInstanceResultEnum.APPROVE.getResult()) // 不设置也问题不大，因为 Complete 一般是审核通过，已经设置
//                .setEndTime(LocalDateTime.now());
//        taskExtMapper.updateByTaskId(taskExtDO);

        updateTaskStatus(task.getId(), BpmTaskStatustEnum.APPROVE.getStatus());
    }

    @Override
    public void updateTaskExtCancel(String taskId) {
        Task task = getTask(taskId);
        // 可能只是活动，不是任务，所以查询不到
        if (task == null) {
            log.error("[updateTaskExtCancel][taskId({}) 任务不存在]", taskId);
            return;
        }

        Integer status = (Integer) task.getTaskLocalVariables().get(BpmConstants.TASK_VARIABLE_STATUS);
        if (BpmTaskStatustEnum.isEndStatus(status)) {
            log.error("[updateTaskExtCancel][taskId({}) 处于结果({})，无需进行更新]", taskId, status);
            return;
        }
        updateTaskStatus(taskId, BpmTaskStatustEnum.CANCEL.getStatus());

        if (true) {
            return;
        }

        // 需要在事务提交后，才进行查询。不然查询不到历史的原因
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                // 可能只是活动，不是任务，所以查询不到
                HistoricTaskInstance task = getHistoricTask(taskId);
                if (task == null) {
                    return;
                }

//                // 如果任务拓展表已经是完成的状态，则跳过
//                BpmTaskExtDO taskExt = taskExtMapper.selectByTaskId(taskId);
//                if (taskExt == null) {
//                    log.error("[updateTaskExtCancel][taskId({}) 查找不到对应的记录，可能存在问题]", taskId);
//                    return;
//                }
//                // 如果已经是最终的结果，则跳过
//                if (BpmProcessInstanceResultEnum.isEndResult(taskExt.getResult())) {
//                    log.error("[updateTaskExtCancel][taskId({}) 处于结果({})，无需进行更新]", taskId, taskExt.getResult());
//                    return;
//                }
                Integer status = (Integer) task.getTaskLocalVariables().get(BpmConstants.TASK_VARIABLE_STATUS);
                if (BpmTaskStatustEnum.isEndStatus(status)) {
                    log.error("[updateTaskExtCancel][taskId({}) 处于结果({})，无需进行更新]", taskId, status);
                    return;
                }

                // 更新任务
//                taskExtMapper.updateById(new BpmTaskExtDO().setId(taskExt.getId()).setResult(BpmProcessInstanceResultEnum.CANCEL.getResult())
//                        .setEndTime(LocalDateTime.now()).setReason(BpmProcessInstanceDeleteReasonEnum.translateReason(task.getDeleteReason())));
                updateTaskStatus(taskId, BpmTaskStatustEnum.CANCEL.getStatus());
            }

        });
    }

    @Override
    public void updateTaskExtAssign(Task task) {
//        BpmTaskExtDO taskExtDO =
//                new BpmTaskExtDO().setAssigneeUserId(NumberUtils.parseLong(task.getAssignee())).setTaskId(task.getId());
//        taskExtMapper.updateByTaskId(taskExtDO);

        // 发送通知。在事务提交时，批量执行操作，所以直接查询会无法查询到 ProcessInstance，所以这里是通过监听事务的提交来实现。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (StrUtil.isNotEmpty(task.getAssignee())) {
                    ProcessInstance processInstance =
                            processInstanceService.getProcessInstance(task.getProcessInstanceId());
                    AdminUserRespDTO startUser = adminUserApi.getUser(Long.valueOf(processInstance.getStartUserId()));
                    messageService.sendMessageWhenTaskAssigned(
                            BpmTaskConvert.INSTANCE.convert(processInstance, startUser, task));
                }
            }
        });
    }

    private Task validateTaskExist(String id) {
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

    private HistoricTaskInstance getHistoricTask(String id) {
        return historyService.createHistoricTaskInstanceQuery().taskId(id).includeTaskLocalVariables().singleResult();
    }

    @Override
    public List<UserTask> getUserTaskListByReturn(String id) {
        // 1.1 校验当前任务 task 存在
        Task task = validateTaskExist(id);
        // 1.2 根据流程定义获取流程模型信息
        BpmnModel bpmnModel = bpmModelService.getBpmnModelByDefinitionId(task.getProcessDefinitionId());
        FlowElement source = BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
        if (source == null) {
            throw exception(TASK_NOT_EXISTS);
        }

        // 2.1 查询该任务的前置任务节点的 key 集合
        List<UserTask> previousUserList = BpmnModelUtils.getPreviousUserTaskList(source, null, null);
        if (CollUtil.isEmpty(previousUserList)) {
            return Collections.emptyList();
        }
        // 2.2 过滤：只有串行可到达的节点，才可以回退。类似非串行、子流程无法退回
        previousUserList.removeIf(userTask -> !BpmnModelUtils.isSequentialReachable(source, userTask, null));
        return previousUserList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnTask(Long userId, BpmTaskReturnReqVO reqVO) {
        // 1.1 当前任务 task
        Task task = validateTask(userId, reqVO.getId());
        if (task.isSuspended()) {
            throw exception(TASK_IS_PENDING);
        }
        // 1.2 校验源头和目标节点的关系，并返回目标元素
        FlowElement targetElement = validateTargetTaskCanReturn(task.getTaskDefinitionKey(),
                reqVO.getTargetTaskDefinitionKey(), task.getProcessDefinitionId());

        // 2. 调用 Flowable 框架的回退逻辑
        returnTask(task, targetElement, reqVO);
    }

    /**
     * 回退流程节点时，校验目标任务节点是否可回退
     *
     * @param sourceKey           当前任务节点 Key
     * @param targetKey           目标任务节点 key
     * @param processDefinitionId 当前流程定义 ID
     * @return 目标任务节点元素
     */
    private FlowElement validateTargetTaskCanReturn(String sourceKey, String targetKey, String processDefinitionId) {
        // 1.1 获取流程模型信息
        BpmnModel bpmnModel = bpmModelService.getBpmnModelByDefinitionId(processDefinitionId);
        // 1.3 获取当前任务节点元素
        FlowElement source = BpmnModelUtils.getFlowElementById(bpmnModel, sourceKey);
        // 1.3 获取跳转的节点元素
        FlowElement target = BpmnModelUtils.getFlowElementById(bpmnModel, targetKey);
        if (target == null) {
            throw exception(TASK_TARGET_NODE_NOT_EXISTS);
        }

        // 2.2 只有串行可到达的节点，才可以回退。类似非串行、子流程无法退回
        if (!BpmnModelUtils.isSequentialReachable(source, target, null)) {
            throw exception(TASK_RETURN_FAIL_SOURCE_TARGET_ERROR);
        }
        return target;
    }

    /**
     * 执行回退逻辑
     *
     * @param currentTask   当前回退的任务
     * @param targetElement 需要回退到的目标任务
     * @param reqVO         前端参数封装
     */
    public void returnTask(Task currentTask, FlowElement targetElement, BpmTaskReturnReqVO reqVO) {
        // 1. 获得所有需要回撤的任务 taskDefinitionKey，用于稍后的 moveActivityIdsToSingleActivityId 回撤
        // 1.1 获取所有正常进行的任务节点 Key
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(currentTask.getProcessInstanceId()).list();
        List<String> runTaskKeyList = convertList(taskList, Task::getTaskDefinitionKey);
        // 1.2 通过 targetElement 的出口连线，计算在 runTaskKeyList 有哪些 key 需要被撤回
        // 为什么不直接使用 runTaskKeyList 呢？因为可能存在多个审批分支，例如说：A -> B -> C 和 D -> F，而只要 C 撤回到 A，需要排除掉 F
        List<UserTask> returnUserTaskList = BpmnModelUtils.iteratorFindChildUserTasks(targetElement, runTaskKeyList, null, null);
        List<String> returnTaskKeyList = convertList(returnUserTaskList, UserTask::getId);

        // 2. 给当前要被回退的 task 数组，设置回退意见
        taskList.forEach(task -> {
            // 需要排除掉，不需要设置回退意见的任务
            if (!returnTaskKeyList.contains(task.getTaskDefinitionKey())) {
                return;
            }
            // 2.1 添加评论
            taskService.addComment(task.getId(), currentTask.getProcessInstanceId(),
                    BpmCommentTypeEnum.BACK.getType(), reqVO.getReason());
            // 2.2 更新 task 状态 + 原因
            updateTaskStatus(task.getId(), BpmTaskStatustEnum.BACK.getStatus(), reqVO.getReason());
        });

        // 3. 执行驳回
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(currentTask.getProcessInstanceId())
                .moveActivityIdsToSingleActivityId(returnTaskKeyList, // 当前要跳转的节点列表( 1 或多)
                        reqVO.getTargetTaskDefinitionKey()) // targetKey 跳转到的节点(1)
                .changeState();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(Long userId, BpmTaskDelegateReqVO reqVO) {
        // 1.1 校验任务
        Task task = validateTaskCanDelegate(userId, reqVO);
        // 1.2 校验目标用户存在
        AdminUserRespDTO delegateUser = adminUserApi.getUser(reqVO.getDelegateUserId());
        if (delegateUser == null) {
            throw exception(TASK_DELEGATE_FAIL_USER_NOT_EXISTS);
        }

        // 2. 添加审批意见
        AdminUserRespDTO currentUser = adminUserApi.getUser(userId);
        String comment = StrUtil.format("[{}]将任务委派给[{}]，委派理由为:{}", currentUser.getNickname(),
                delegateUser.getNickname(), reqVO.getReason());
        String taskId = reqVO.getId();
        taskService.addComment(taskId, task.getProcessInstanceId(),
                BpmCommentTypeEnum.DELEGATE.getType().toString(), comment);

        // 3.1 设置任务所有人 (owner) 为原任务的处理人 (assignee)
        taskService.setOwner(taskId, task.getAssignee());
        // 3.2 执行委派，将任务委派给 receiveId
        taskService.delegateTask(taskId, reqVO.getDelegateUserId().toString());
        // 3.3 更新任务拓展表为【委派】
//        taskExtMapper.updateByTaskId(
//                new BpmTaskExtDO().setTaskId(task.getId()).setResult(BpmProcessInstanceResultEnum.DELEGATE.getResult())
//                        .setReason(reqVO.getReason()));
        updateTaskStatus(taskId, BpmTaskStatustEnum.DELEGATE.getStatus());
    }

    /**
     * 校验任务委派参数
     *
     * @param userId 用户编号
     * @param reqVO  任务编号，接收人ID
     * @return 当前任务信息
     */
    private Task validateTaskCanDelegate(Long userId, BpmTaskDelegateReqVO reqVO) {
        // 校验任务
        Task task = validateTask(userId, reqVO.getId());
        // 校验当前审批人和被委派人不是同一人
        if (task.getAssignee().equals(reqVO.getDelegateUserId().toString())) {
            throw exception(TASK_DELEGATE_FAIL_USER_REPEAT);
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSignTask(Long userId, BpmTaskAddSignReqVO reqVO) {
        // 1. 获取和校验任务
        TaskEntityImpl taskEntity = validateAddSign(userId, reqVO);
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(reqVO.getUserIdList());
        if (CollUtil.isEmpty(userList)) {
            throw exception(TASK_ADD_SIGN_USER_NOT_EXIST);
        }

        // 2. 处理当前任务
        // 2.1 开启计数功能，主要用于为了让表 ACT_RU_TASK 中的 SUB_TASK_COUNT_ 字段记录下总共有多少子任务，后续可能有用
        taskEntity.setCountEnabled(true);
        if (reqVO.getType().equals(BpmTaskAddSignTypeEnum.BEFORE.getType())) {
            // 2.2 向前加签，设置 owner，置空 assign。等子任务都完成后，再调用 resolveTask 重新将 owner 设置为 assign
            // 原因是：不能和向前加签的子任务一起审批，需要等前面的子任务都完成才能审批
            taskEntity.setOwner(taskEntity.getAssignee());
            taskEntity.setAssignee(null);
            // 2.3 更新扩展表状态
//            taskExtMapper.updateByTaskId(
//                    new BpmTaskExtDO().setTaskId(taskEntity.getId())
//                            .setResult(BpmProcessInstanceResultEnum.SIGN_BEFORE.getResult())
//                            .setReason(reqVO.getReason()));
//            taskEntity.setTransientVariableLocal(BpmConstants.TASK_VARIABLE_STATUS, BpmProcessInstanceResultEnum.SIGN_BEFORE.getResult());
//            updateTaskStatus(taskEntity.getId(), BpmProcessInstanceResultEnum.SIGN_BEFORE.getResult()); // TODO 芋艿：貌似会有实物并发的问题；所以不能用这个调用，只能 set
        }
        // 2.4 记录加签方式，完成任务时需要用到判断
        taskEntity.setScopeType(reqVO.getType());
        // 2.5 保存当前任务修改后的值
        taskService.saveTask(taskEntity);
        if (reqVO.getType().equals(BpmTaskAddSignTypeEnum.BEFORE.getType())) {
            updateTaskStatus(taskEntity.getId(), BpmTaskStatustEnum.WAIT.getStatus()); // TODO 芋艿：貌似只能放在这个地方，不然会有并发修改的报错
        }

        // 3. 创建加签任务
        createSignTask(convertList(reqVO.getUserIdList(), String::valueOf), taskEntity);

        // 4. 记录加签 comment，拼接结果为： [当前用户]向前加签/向后加签给了[多个用户]，理由为：reason
        AdminUserRespDTO currentUser = adminUserApi.getUser(userId);
        String comment = StrUtil.format(BpmCommentTypeEnum.ADD_SIGN.getComment(), currentUser.getNickname(),
                BpmTaskAddSignTypeEnum.formatDesc(reqVO.getType()), String.join(",", convertList(userList, AdminUserRespDTO::getNickname)), reqVO.getReason());
        taskService.addComment(reqVO.getId(), taskEntity.getProcessInstanceId(),
                BpmCommentTypeEnum.ADD_SIGN.getType().toString(), comment);
    }


    /**
     * 校验任务的加签是否一致
     * <p>
     * 1. 如果存在“向前加签”的任务，则不能“向后加签”
     * 2. 如果存在“向后加签”的任务，则不能“向前加签”
     *
     * @param userId 当前用户 ID
     * @param reqVO  请求参数，包含任务 ID 和加签类型
     * @return 当前任务
     */
    private TaskEntityImpl validateAddSign(Long userId, BpmTaskAddSignReqVO reqVO) {
        TaskEntityImpl taskEntity = (TaskEntityImpl) validateTask(userId, reqVO.getId());
        // 向前加签和向后加签不能同时存在
        if (taskEntity.getScopeType() != null
                && ObjectUtil.notEqual(taskEntity.getScopeType(), reqVO.getType())) {
            throw exception(TASK_ADD_SIGN_TYPE_ERROR,
                    BpmTaskAddSignTypeEnum.formatDesc(taskEntity.getScopeType()), BpmTaskAddSignTypeEnum.formatDesc(reqVO.getType()));
        }

        // 同一个 key 的任务，审批人不重复
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(taskEntity.getProcessInstanceId())
                .taskDefinitionKey(taskEntity.getTaskDefinitionKey()).list();
        List<Long> currentAssigneeList = convertList(taskList, task -> NumberUtils.parseLong(task.getAssignee()));
        // 保留交集在 currentAssigneeList 中
        currentAssigneeList.retainAll(reqVO.getUserIdList());
        if (CollUtil.isNotEmpty(currentAssigneeList)) {
            List<AdminUserRespDTO> userList = adminUserApi.getUserList(currentAssigneeList);
            throw exception(TASK_ADD_SIGN_USER_REPEAT, String.join(",", convertList(userList, AdminUserRespDTO::getNickname)));
        }
        return taskEntity;
    }

    /**
     * 创建加签子任务
     *
     * @param addSingUserIdList 被加签的用户 ID
     * @param taskEntity        被加签的任务
     */
    private void createSignTask(List<String> addSingUserIdList, TaskEntityImpl taskEntity) {
        if (CollUtil.isEmpty(addSingUserIdList)) {
            return;
        }
        // 创建加签人的新任务，全部基于 taskEntity 为父任务来创建
        for (String addSignId : addSingUserIdList) {
            if (StrUtil.isBlank(addSignId)) {
                continue;
            }
            createSignTask(taskEntity, addSignId);
        }
    }

    /**
     * 创建加签子任务
     *
     * @param parentTask 父任务
     * @param assignee   子任务的执行人
     */
    private void createSignTask(TaskEntityImpl parentTask, String assignee) {
        // 1. 生成子任务
        TaskEntityImpl task = (TaskEntityImpl) taskService.newTask(IdUtil.fastSimpleUUID());
        task = BpmTaskConvert.INSTANCE.convert(task, parentTask);
        if (BpmTaskAddSignTypeEnum.BEFORE.getType().equals(parentTask.getScopeType())) {
            // 2.1 前加签，设置审批人
            task.setAssignee(assignee);
        } else {
            // 2.2.1 设置 owner 不设置 assignee 是因为不能同时审批，需要等父任务完成
            task.setOwner(assignee);
            // 2.2.2 设置向后加签任务的 scopeType 为 afterChildrenTask，用于设置任务扩展表的状态
//            task.setScopeType(BpmTaskAddSignTypeEnum.AFTER_CHILDREN_TASK.getType());
//            task.setVariableLocal(BpmConstants.TASK_VARIABLE_STATUS, BpmProcessInstanceResultEnum.WAIT.getResult());
        }
        // 2. 保存子任务
        taskService.saveTask(task);
        // 3. TODO
        if (BpmTaskAddSignTypeEnum.AFTER.getType().equals(parentTask.getScopeType())) {
            updateTaskStatus(task.getId(), BpmTaskStatustEnum.WAIT.getStatus());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSignTask(Long userId, BpmTaskSubSignReqVO reqVO) {
        // 1.1 校验 task 可以被减签
//        Task task = validateSubSign(reqVO.getId()); // TODO 芋艿：这个判断，暂时有点问题！在前置加签的时候
        Task task = validateTaskExist(reqVO.getId());
        // 1.2 校验取消人存在
        AdminUserRespDTO cancelUser = null;
        if (StrUtil.isNotBlank(task.getAssignee())) {
            cancelUser = adminUserApi.getUser(NumberUtils.parseLong(task.getAssignee()));
        }
        if (cancelUser == null && StrUtil.isNotBlank(task.getOwner())) {
            cancelUser = adminUserApi.getUser(NumberUtils.parseLong(task.getOwner()));
        }
        Assert.notNull(cancelUser, "任务中没有所有者和审批人，数据错误");

        // 2. 删除任务和对应子任务
        // 2.1 获取所有需要删除的任务 ID ，包含当前任务和所有子任务
        List<String> allTaskIdList = getAllChildTaskIds(task.getId());
        // 2.2 删除任务和所有子任务
        taskService.deleteTasks(allTaskIdList);
        // 2.3 修改扩展表状态为取消
//        taskExtMapper.updateBatchByTaskIdList(allTaskIdList, new BpmTaskExtDO().setResult(BpmProcessInstanceResultEnum.CANCEL.getResult())
//                .setReason(StrUtil.format("由于{}操作[减签]，任务被取消", user.getNickname())));
//        allTaskIdList.forEach(taskId -> updateTaskStatus(taskId, BpmProcessInstanceResultEnum.CANCEL.getResult())); // TODO @芋艿：交给取消；考虑到理由，可能不能直接给；

        // 3. 记录日志到父任务中。先记录日志是因为，通过 handleParentTask 方法之后，任务可能被完成了，并且不存在了，会报异常，所以先记录
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        String comment = StrUtil.format(BpmCommentTypeEnum.SUB_SIGN.getComment(), user.getNickname(), cancelUser.getNickname());
        taskService.addComment(task.getParentTaskId(), task.getProcessInstanceId(),
                BpmCommentTypeEnum.SUB_SIGN.getType().toString(), comment);

        // 4. 处理当前任务的父任务
        handleParentTaskIfSign(task.getParentTaskId());
    }

    /**
     * 校验任务是否能被减签
     *
     * @param id 任务ID
     * @return 任务信息
     */
    private Task validateSubSign(String id) {
        Task task = validateTaskExist(id);

        // 必须有 scopeType
        String scopeType = task.getScopeType();
        if (StrUtil.isEmpty(scopeType)) {
            throw exception(TASK_SUB_SIGN_NO_PARENT);
        }
        // 并且值为 向前和向后加签
        if (!validateSignType(scopeType)) {
            throw exception(TASK_SUB_SIGN_NO_PARENT);
        }
        return task;
    }

    /**
     * 判断当前类型是否为加签
     * @param scopeType 任务的 scopeType
     * @return 当前 scopeType 为加签则返回 true
     */
    private boolean validateSignType(String scopeType){
        return StrUtil.equalsAny(scopeType,BpmTaskAddSignTypeEnum.BEFORE.getType(),scopeType, BpmTaskAddSignTypeEnum.AFTER.getType());
    }

//    TODO @海：BpmProcessInstanceResultEnum.CAN_SUB_SIGN_STATUS_LIST) 应该作为条件，mapper 不要有业务
    /**
     * 获取所有要被取消的删除的任务 ID 集合
     *
     * @param parentTaskId 父级任务ID
     * @return 所有任务ID
     */
    public List<String> getAllChildTaskIds(String parentTaskId) {
        List<String> allChildTaskIds = new ArrayList<>();
        // 1. 递归获取子级
        Stack<String> stack = new Stack<>();
        // 1.1 将根任务ID入栈
        stack.push(parentTaskId);
        //控制遍历的次数不超过 Byte.MAX_VALUE，避免脏数据造成死循环
        int count = 0;
        // TODO @海：< 的前后空格，要注意哈；
        while (!stack.isEmpty() && count<Byte.MAX_VALUE) {
            // 1.2 弹出栈顶任务ID
            String taskId = stack.pop();
            // 1.3 将任务ID添加到结果集合中
            allChildTaskIds.add(taskId);
            // 1.4 获取该任务的子任务列表
            // TODO @海：有个更高效的写法；一次性去 in 一层；不然每个节点，都去查询一次 db， 太浪费了；每次 in，最终就是 O(h) 查询，而不是 O(n) 查询；
            List<String> childrenTaskIdList = getChildrenTaskIdList(taskId);
            if (CollUtil.isNotEmpty(childrenTaskIdList)) {
                for (String childTaskId : childrenTaskIdList) {
                    // 1.5 将子任务ID入栈，以便后续处理
                    stack.push(childTaskId);
                }
            }
            count++;
        }
        return allChildTaskIds;
    }

    /**
     * 获取指定父级任务的所有子任务 ID 集合
     *
     * @param parentTaskId 父任务 ID
     * @return 所有子任务的 ID 集合
     */
    private List<String> getChildrenTaskIdList(String parentTaskId) {
        return convertList(getTaskListByParentTaskId(parentTaskId), Task::getId);
    }

    @Override
    public List<Task> getTaskListByParentTaskId(String parentTaskId) {
        String tableName = managementService.getTableName(TaskEntity.class);
        // taskService.createTaskQuery() 没有 parentId 参数，所以写 sql 查询
        String sql = "select ID_,OWNER_,ASSIGNEE_ from " + tableName + " where PARENT_TASK_ID_=#{parentTaskId}";
        return taskService.createNativeTaskQuery().sql(sql).parameter("parentTaskId", parentTaskId).list();
    }

    @Override
    public Map<String, String> getTaskNameByTaskIds(Collection<String> taskIds) {
        if (CollUtil.isEmpty(taskIds)) {
            return Collections.emptyMap();
        }
        List<Task> tasks = taskService.createTaskQuery().taskIds(taskIds).list();
        return convertMap(tasks, Task::getId, Task::getName);
    }

}
