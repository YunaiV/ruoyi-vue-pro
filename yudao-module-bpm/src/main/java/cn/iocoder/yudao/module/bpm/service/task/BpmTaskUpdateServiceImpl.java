package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.yudao.module.bpm.convert.task.BpmTaskConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.enums.task.*;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.comment.BpmCommentService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenTaskTimeoutReqDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.*;
import static cn.iocoder.yudao.module.bpm.service.task.BpmTaskQueryServiceImpl.getSignTaskOwner;
import static cn.iocoder.yudao.module.bpm.service.task.BpmTaskQueryServiceImpl.isAssignUserTask;
import static cn.iocoder.yudao.module.bpm.service.task.BpmTaskQueryServiceImpl.isSignTaskUser;
import static cn.iocoder.yudao.module.bpm.service.task.BpmTaskQueryServiceImpl.isWaitRevisionTask;

/**
 * 流程任务实例【写】Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
public class BpmTaskUpdateServiceImpl implements BpmTaskUpdateService {

    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private ManagementService managementService;

    @Resource
    private BpmTaskQueryService taskQueryService;
    @Resource
    private BpmProcessInstanceQueryService processInstanceQueryService;
    @Resource
    private BpmProcessInstanceUpdateService processInstanceUpdateService;
    @Resource
    private BpmProcessDefinitionService bpmProcessDefinitionService;
    @Resource
    private BpmProcessInstanceCopyService processInstanceCopyService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private BpmCommentService commentService;
    @Resource
    private BpmModelService modelService;
    @Resource
    private BpmMessageService messageService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    // ========== Update 写入相关方法 ==========

    /**
     * 自动审批通过，供事务完成后的回调调用
     *
     * @see <a href="https://wx.zsxq.com/group/88858522214142/topic/22258281454841411">自动审批事务问题</a>
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @DataPermission(enable = false)
    public void approveTaskInternal(Long userId, BpmTaskApproveReqVO reqVO) {
        approveTask(userId, reqVO, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void approveTask(Long userId, @Valid BpmTaskApproveReqVO reqVO) {
        approveTask(userId, reqVO, true);
    }

    /**
     * 审批通过
     *
     * @param userId 用户编号
     * @param reqVO  通过请求
     * @param manual 是否为用户的人工操作：需要校验退回修改的批次。系统自动通过（超时、审批人为空、自动审批）时为 false
     */
    private void approveTask(Long userId, BpmTaskApproveReqVO reqVO, boolean manual) {
        // 1.1 校验任务存在
        Task task = taskQueryService.validateTask(userId, reqVO.getId());
        if (manual) {
            validateRevisionId(task, reqVO.getRevisionId());
        }
        // 1.2 校验流程实例存在
        ProcessInstance instance = processInstanceQueryService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }
        // 1.3 校验签名
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(task.getProcessDefinitionId());
        Boolean signEnable = parseSignEnable(bpmnModel, task.getTaskDefinitionKey());
        if (signEnable && StrUtil.isEmpty(reqVO.getSignPicUrl())) {
            throw exception(TASK_SIGNATURE_NOT_EXISTS);
        }
        // 1.4 校验审批意见
        Boolean reasonRequire = parseReasonRequire(bpmnModel, task.getTaskDefinitionKey());
        if (reasonRequire && StrUtil.isEmpty(reqVO.getReason())) {
            throw exception(TASK_REASON_REQUIRE);
        }

        // 情况一：审批有【后】加签的任务。如果它是被委派的任务，等加签任务都完成后，再归还委派人
        // 补充说明：如果加签任务已经都被减签（例如说：并发减签），则按照情况二、情况三继续审批，覆盖父任务的状态
        if (BpmTaskSignTypeEnum.AFTER.getType().equals(task.getScopeType())
                && approveAfterSignTask(task, reqVO)) {
            return;
        }

        // 情况二：被委派的任务，不调用 complete 去完成任务，而是归还给直接委派人
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            approveDelegateTask(task, reqVO.getReason());
            return;
        }

        // 情况三：审批普通的任务。大多数情况下，都是这样
        // 2.1 更新 task 状态、原因、签字
        updateTaskStatusAndReason(task.getId(), BpmTaskStatusEnum.APPROVE.getStatus(), reqVO.getReason());
        if (signEnable) {
            taskService.setVariableLocal(task.getId(), BpmnVariableConstants.TASK_SIGN_PIC_URL, reqVO.getSignPicUrl());
        }
        // 2.2 添加评论
        commentService.createComment(task.getId(), task.getProcessInstanceId(), BpmCommentTypeEnum.APPROVE,
                reqVO.getReason());
        // 2.3 添加附件
        if (CollUtil.isNotEmpty(reqVO.getAttachments())) {
            reqVO.getAttachments().forEach(attachment -> taskService.createAttachment(BpmAttachmentTypeEnum.TASK_ATTACHMENT.getType(),
                    task.getId(), task.getProcessInstanceId(), FileUtil.getName(URLUtil.getPath(attachment)), null, attachment));
        }

        // 3. 设置流程变量。如果流程变量前端传空，需要从历史实例中获取，原因：前端表单如果在当前节点无可编辑的字段时 variables 一定会为空
        // 场景一：A 节点发起，B 节点表单无可编辑字段，审批通过时，C 节点需要流程变量获取下一个执行节点，但因为 B 节点无可编辑的字段，variables 为空，流程可能出现问题。
        // 场景二：A 节点发起，B 节点只有某一个字段可编辑（比如 day），但 C 节点需要多个节点。
        //       （比如 work + day 变量，在发起时填写，因为 B 节点只有 day 的编辑权限，在审批后，variables 会缺少 work 的值）
        Map<String, Object> processVariables = new HashMap<>();
        if (CollUtil.isNotEmpty(instance.getProcessVariables())) { // 获取历史中流程变量
            processVariables.putAll(instance.getProcessVariables());
        }
        if (CollUtil.isNotEmpty(reqVO.getVariables())) { // 合并前端传递的流程变量，以前端为准
            processVariables.putAll(reqVO.getVariables());
        }

        // 4. 校验并处理 APPROVE_USER_SELECT 当前审批人，选择下一节点审批人的逻辑
        validateAndSetNextAssignees(task.getTaskDefinitionKey(), processVariables,
                bpmnModel, reqVO.getNextAssignees(), instance);
        runtimeService.setVariables(task.getProcessInstanceId(), processVariables);

        // 5. 如果当前节点 Id 存在于需要预测的流程节点中，从中移除。 流程变量在回退操作中设置
        Object needSimulateTaskIds = runtimeService.getVariable(task.getProcessInstanceId(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS);
        Set<String> needSimulateTaskIdsByReturn = Convert.toSet(String.class, needSimulateTaskIds);
        if (needSimulateTaskIdsByReturn.contains(task.getTaskDefinitionKey())) {
            needSimulateTaskIdsByReturn.remove(task.getTaskDefinitionKey());
            runtimeService.setVariable(task.getProcessInstanceId(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS, needSimulateTaskIdsByReturn);
        }

        // 6. 清理退回设置的不自动通过的变量
        clearReturnFlagIfNodeTask(task);

        // 7. 调用 BPM complete 去完成任务
        Map<String,Object> taskVariables = MapUtil.emptyIfNull(reqVO.getVariables()); // task local variables. 一般用于任务内嵌流程表单
        taskService.complete(task.getId(), taskVariables, true);

        // 【加签专属】处理加签任务
        handleParentTaskIfSign(task.getParentTaskId());
    }

    /**
     * 完成流程节点的任务时，清理退回设置的不自动通过的变量
     * <p>
     * 加签的子任务和父任务共用节点 key，子任务完成时节点并未完成，需要保留标记，
     * 避免父任务（例如说：归还委派人）重新分配时，被误判为非退回，从而自动审批通过
     * <p>
     * 仅在该标记存在时才删除，避免每次完成任务都产生无谓的 DB delete
     *
     * @param task 即将完成的任务
     */
    private void clearReturnFlagIfNodeTask(Task task) {
        if (task.getParentTaskId() != null) {
            return;
        }
        String returnFlagKey = String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, task.getTaskDefinitionKey());
        if (runtimeService.hasVariable(task.getProcessInstanceId(), returnFlagKey)) {
            log.info("[clearReturnFlagIfNodeTask][taskId({}) 清理退回标记变量({})]", task.getId(), returnFlagKey);
            runtimeService.removeVariable(task.getProcessInstanceId(), returnFlagKey);
        }
    }

    /**
     * 校验选择的下一个节点的审批人，是否合法
     * <p>
     * 1. 是否有漏选：没有选择审批人
     * 2. 是否有多选：非下一个节点
     *
     * @param taskDefinitionKey 当前任务节点标识
     * @param variables         流程变量
     * @param bpmnModel         流程模型
     * @param nextAssignees     下一个节点审批人集合（参数）
     * @param processInstance   流程实例
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> validateAndSetNextAssignees(String taskDefinitionKey, Map<String, Object> variables, BpmnModel bpmnModel,
                                                            Map<String, List<Long>> nextAssignees, ProcessInstance processInstance) {
        // simple 设计器第一个节点默认为发起人节点，不校验是否存在审批人
        if (Objects.equals(taskDefinitionKey, START_USER_NODE_ID)) {
            return variables;
        }
        // 1. 获取下一个将要执行的节点集合
        FlowElement flowElement = bpmnModel.getFlowElement(taskDefinitionKey);
        List<UserTask> nextFlowNodes = getNextUserTasks(flowElement, bpmnModel, variables);

        // 2. 校验选择的下一个节点的审批人，是否合法
        for (UserTask nextFlowNode : nextFlowNodes) {
            Integer candidateStrategy = parseCandidateStrategy(nextFlowNode);
            // 2.1 情况一：如果节点中的审批人策略为 发起人自选
            if (ObjUtil.equals(candidateStrategy, BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy())) {
                // 特殊：如果当前节点已经存在审批人，则不允许覆盖
                Map<String, List<Long>> startUserSelectAssignees = FlowableUtils.getStartUserSelectAssignees(processInstance.getProcessVariables());
                if (startUserSelectAssignees != null && CollUtil.isNotEmpty(startUserSelectAssignees.get(nextFlowNode.getId()))) {
                    continue;
                }
                // 如果节点存在，但未配置审批人
                List<Long> assignees = nextAssignees != null ? nextAssignees.get(nextFlowNode.getId()) : null;
                if (CollUtil.isEmpty(assignees)) {
                    throw exception(PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG, nextFlowNode.getName());
                }

                // 设置 PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES
                if (startUserSelectAssignees == null) {
                    startUserSelectAssignees = new HashMap<>();
                }
                startUserSelectAssignees.put(nextFlowNode.getId(), assignees);
                variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES, startUserSelectAssignees);
                continue;
            }

            // 2.2 情况二：如果节点中的审批人策略为 审批人，在审批时选择下一个节点的审批人，并且该节点的审批人为空
            if (ObjUtil.equals(candidateStrategy, BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT.getStrategy())) {
                // 特殊：如果当前节点已经存在审批人，则不允许覆盖。 例如并行节点后，设置的审批人自选节点。 https://t.zsxq.com/daxv1
                Map<String, List<Long>> approveUserSelectAssignees = FlowableUtils.getApproveUserSelectAssignees(processInstance.getProcessVariables());
                if (approveUserSelectAssignees != null && CollUtil.isNotEmpty(approveUserSelectAssignees.get(nextFlowNode.getId()))) {
                    continue;
                }
                // 如果节点存在，但未配置审批人
                List<Long> assignees = nextAssignees != null ? nextAssignees.get(nextFlowNode.getId()) : null;
                if (CollUtil.isEmpty(assignees)) {
                    throw exception(PROCESS_INSTANCE_APPROVE_USER_SELECT_ASSIGNEES_NOT_CONFIG, nextFlowNode.getName());
                }

                // 设置 PROCESS_INSTANCE_VARIABLE_APPROVE_USER_SELECT_ASSIGNEES
                if (approveUserSelectAssignees == null) {
                    approveUserSelectAssignees = new HashMap<>();
                }
                approveUserSelectAssignees.put(nextFlowNode.getId(), assignees);
                Map<String, List<Long>> existingApproveUserSelectAssignees = (Map<String, List<Long>>) variables.get(
                        BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_APPROVE_USER_SELECT_ASSIGNEES);
                if (CollUtil.isNotEmpty(existingApproveUserSelectAssignees)) {
                    approveUserSelectAssignees.putAll(existingApproveUserSelectAssignees);
                }
                variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_APPROVE_USER_SELECT_ASSIGNEES, approveUserSelectAssignees);
            }
        }
        return variables;
    }

    /**
     * 审批通过存在“后加签”的任务。
     * <p>
     * 注意：该任务不能马上完成，需要一个中间状态（APPROVING），并激活剩余所有子任务（PROCESS）为可审批处理
     * 如果马上完成，则会触发下一个任务，甚至如果没有下一个任务则流程实例就直接结束了！
     *
     * @param task  当前任务
     * @param reqVO 前端请求参数
     * @return 是否已按后加签处理。没有加签任务时返回 false，由调用方继续按委派、普通任务审批
     */
    private boolean approveAfterSignTask(Task task, BpmTaskApproveReqVO reqVO) {
        // 1. 更新父 task 状态 + 原因
        // 注意：保持先更新父任务、再查询子任务的顺序，避免先读取子任务后，漏掉并发新加签的任务
        updateTaskStatusAndReason(task.getId(), BpmTaskStatusEnum.APPROVING.getStatus(), reqVO.getReason());
        List<Task> childrenTaskList = taskQueryService.getTaskListByParentTaskId(task.getId());
        if (CollUtil.isEmpty(childrenTaskList)) {
            return false;
        }

        // 2. 被委派的任务，记录被委派人的审批意见。因为加签任务不通过时，父任务的原因会被覆盖，需要通过评论保留
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            commentService.createComment(task.getId(), task.getProcessInstanceId(), BpmCommentTypeEnum.APPROVE,
                    reqVO.getReason());
        }

        // 3. 激活子任务
        // 如果子任务在查询后被删除（并发减签），Flowable 会因为任务不存在抛出异常，整体回滚
        childrenTaskList.forEach(childrenTask -> resolveWaitTask(childrenTask.getId()));
        return true;
    }

    /**
     * 如果父任务是有前后【加签】的任务，如果它【加签】出来的子任务都被处理，需要处理父任务：
     * <p>
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
        Long childrenTaskCount = getTaskCountByParentTaskId(parentTaskId);
        if (childrenTaskCount > 0) {
            return;
        }
        // 1.2 只处理加签的父任务
        Task parentTask = taskQueryService.validateTaskExists(parentTaskId);
        BpmTaskSignTypeEnum signType = BpmTaskSignTypeEnum.of(parentTask.getScopeType());
        if (signType == null) {
            return;
        }

        // 2. 子任务已处理完成，清空 scopeType 字段，修改 parentTask 信息，方便后续可以继续向前后向后加签
        TaskEntityImpl parentTaskImpl = (TaskEntityImpl) parentTask;
        parentTaskImpl.setScopeType(null);
        taskService.saveTask(parentTaskImpl);

        // 3.1 情况一：处理向【向前】加签，恢复父任务的 assignee，这样它就可以被审批
        if (signType == BpmTaskSignTypeEnum.BEFORE) {
            resolveWaitTask(parentTaskId);
            // 3.2 情况二：处理向【向后】加签
        } else {
            // 只有 parentTask 处于 APPROVING 的情况下，才可以继续 complete 完成
            // 否则，一个未审批的 parentTask 任务，在加签出来的任务都被减签的情况下，就直接完成审批，这样会存在问题
            Integer status = (Integer) parentTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
            if (ObjectUtil.notEqual(status, BpmTaskStatusEnum.APPROVING.getStatus())) {
                return;
            }
            // 3.2.1 被委派的任务，归还给直接委派人，由委派人继续审批
            if (DelegationState.PENDING.equals(parentTask.getDelegationState())) {
                approveDelegateTask(parentTask, (String) parentTask.getTaskLocalVariables()
                        .get(BpmnVariableConstants.TASK_VARIABLE_REASON));
                return;
            }
            // 3.2.2 完成自己（因为它已经没有子任务，所以也可以完成）
            updateTaskStatus(parentTaskId, BpmTaskStatusEnum.APPROVE.getStatus());
            clearReturnFlagIfNodeTask(parentTask);
            taskService.complete(parentTaskId);
        }

        // 4. 递归处理父任务
        handleParentTaskIfSign(parentTask.getParentTaskId());
    }

    /**
     * 审批被委派的任务：归还给直接委派人
     * <p>
     * 多次委派时（A 委派 B，B 再委派 C），逐层归还（C 归还 B，B 再归还 A），期间任务一直处于委派中，不推进节点
     *
     * @param task   当前被审批的任务，需要包含任务变量
     * @param reason 审批意见
     */
    private void approveDelegateTask(Task task, String reason) {
        // 1. 获得归还人：直接委派人。历史数据没有记录归还顺序时，归还给 owner
        List<String> returnUserIds = getDelegateReturnUserIds(task);
        String returnUserId = returnUserIds.isEmpty() ? task.getOwner() : returnUserIds.remove(returnUserIds.size() - 1);
        Long assigneeUserId = NumberUtils.parseLong(task.getAssignee());
        AdminUserRespDTO assigneeUser = adminUserApi.getUser(assigneeUserId);
        AdminUserRespDTO returnUser = adminUserApi.getUser(NumberUtils.parseLong(returnUserId));
        Assert.notNull(returnUser, "委派任务找不到归还人，需要检查数据");

        // 2. 添加审批意见
        // 为什么使用被委派人作为评论人？向后加签的任务都完成、或者被减签时，会触发归还，此时操作人不是被委派人
        // 所以临时切换 Flowable 认证用户为被委派人，写完后恢复为当前请求的认证用户
        String previousAuthenticatedUserId = Authentication.getAuthenticatedUserId();
        FlowableUtils.setAuthenticatedUserId(assigneeUserId);
        try {
            commentService.createComment(task.getId(), task.getProcessInstanceId(), BpmCommentTypeEnum.DELEGATE_END,
                    assigneeUser.getNickname(), returnUser.getNickname(), reason);
        } finally {
            Authentication.setAuthenticatedUserId(previousAuthenticatedUserId);
        }

        // 3.1 更新 task 状态 + 原因
        updateTaskStatusAndReason(task.getId(), BpmTaskStatusEnum.RUNNING.getStatus(), reason);
        // 3.2 情况一：多次委派，归还给上一层委派人，任务仍然处于委派中
        if (CollUtil.isNotEmpty(returnUserIds)) {
            taskService.setVariableLocal(task.getId(), BpmnVariableConstants.TASK_DELEGATE_RETURN_USERS,
                    String.join(StrUtil.COMMA, returnUserIds));
            taskService.setAssignee(task.getId(), returnUserId);
            return;
        }
        // 3.3 情况二：归还给最初的委派人，结束委派
        taskService.removeVariableLocal(task.getId(), BpmnVariableConstants.TASK_DELEGATE_RETURN_USERS);
        if (StrUtil.equals(task.getOwner(), returnUserId)) {
            // 调用 resolveTask 完成委派。底层调用 TaskHelper.changeTaskAssignee(task, task.getOwner())：将 owner 设置为 assignee
            taskService.resolveTask(task.getId());
            return;
        }
        // 转办后再委派时，直接委派人是转办的接收人，而 owner 需要保留为最初的审批人，所以不能调用 resolveTask
        // 注意：需要重新查询任务，因为上面更新了任务变量，旧的任务实体会因为版本号不一致而保存失败
        TaskEntityImpl taskEntity = (TaskEntityImpl) taskQueryService.validateTaskExists(task.getId());
        taskEntity.setDelegationState(DelegationState.RESOLVED);
        taskService.saveTask(taskEntity);
        taskService.setAssignee(task.getId(), returnUserId);
    }

    /**
     * 获得委派的归还顺序
     *
     * @param task 任务，需要包含任务变量
     * @return 用户编号数组（可修改），最后一个是直接委派人
     */
    private List<String> getDelegateReturnUserIds(Task task) {
        String returnUserIds = (String) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_DELEGATE_RETURN_USERS);
        return new ArrayList<>(StrUtil.splitTrim(returnUserIds, StrUtil.C_COMMA));
    }

    /**
     * 自动审批拒绝，供事务完成后的回调调用
     *
     * @see <a href="https://wx.zsxq.com/group/88858522214142/topic/22258281454841411">自动审批事务问题</a>
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @DataPermission(enable = false)
    public void rejectTaskInternal(Long userId, BpmTaskRejectReqVO reqVO) {
        rejectTask(userId, reqVO, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void rejectTask(Long userId, @Valid BpmTaskRejectReqVO reqVO) {
        rejectTask(userId, reqVO, true);
    }

    /**
     * 审批不通过
     *
     * @param userId 用户编号
     * @param reqVO  不通过请求
     * @param manual 是否为用户的人工操作：需要校验退回修改的批次，并允许退回修改（【从当前节点审批】）。
     *               系统自动不通过（超时、审批人为空、自动审批）时为 false，按常规的退回处理：
     *               否则任务等待修改、重新提交后，仍然没有人处理，或者再次超时又进入等待修改
     */
    private void rejectTask(Long userId, BpmTaskRejectReqVO reqVO, boolean manual) {
        // 1.1 校验任务存在
        Task task = taskQueryService.validateTask(userId, reqVO.getId());
        if (manual) {
            validateRevisionId(task, reqVO.getRevisionId());
        }
        // 1.2 校验流程实例存在
        ProcessInstance instance = processInstanceQueryService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 1.3 获得驳回的处理策略
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(task.getProcessDefinitionId());
        FlowElement userTaskElement = BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
        BpmUserTaskRejectHandlerTypeEnum userTaskRejectHandlerType = BpmnModelUtils.parseRejectHandlerType(userTaskElement);

        // 2.1 添加附件
        if (CollUtil.isNotEmpty(reqVO.getAttachments())) {
            reqVO.getAttachments().forEach(attachment -> taskService.createAttachment(BpmAttachmentTypeEnum.TASK_ATTACHMENT.getType(),
                    task.getId(), task.getProcessInstanceId(), FileUtil.getName(URLUtil.getPath(attachment)), null, attachment));
        }
        // 特殊：驳回到指定的任务节点，并且【从当前节点审批】时，直接退回修改：任务不标记为不通过，等待修改人重新提交后继续审批
        // 不满足退回修改的条件（例如说：多人审批的节点、目标节点没有修改人）时，按下面的常规驳回处理，即【重新依次审批】
        if (manual && userTaskRejectHandlerType == BpmUserTaskRejectHandlerTypeEnum.RETURN_USER_TASK
                && BpmnModelUtils.parseReturnApproveMode(userTaskElement) == BpmReturnApproveModeEnum.RESUME_SOURCE) {
            FlowElement targetElement = validateTargetTaskCanReturn(bpmnModel, task.getTaskDefinitionKey(),
                    BpmnModelUtils.parseReturnTaskId(userTaskElement));
            String revisionAssignee = getRevisionAssignee(task, userTaskElement, targetElement);
            if (revisionAssignee != null) {
                returnTaskToRevision(task, targetElement, revisionAssignee, reqVO.getReason());
                return;
            }
        }

        // 2.2 更新流程任务为不通过
        updateTaskStatusAndReason(task.getId(), BpmTaskStatusEnum.REJECT.getStatus(), reqVO.getReason());
        // 2.3 添加流程评论
        commentService.createComment(task.getId(), task.getProcessInstanceId(), BpmCommentTypeEnum.REJECT,
                reqVO.getReason());

        // 2.4 如果当前任务时被加签的，则加它的根任务也标记成未通过
        // 疑问：为什么要标记未通过呢？
        // 回答：例如说 A 任务被向前加签除 B 任务时，B 任务被审批不通过，此时 A 会被取消。而 yudao-ui-admin-vue3 不展示“已取消”的任务，导致展示不出审批不通过的细节。
        if (task.getParentTaskId() != null) {
            String rootParentId = getTaskRootParentId(task);
            updateTaskStatusAndReason(rootParentId, BpmTaskStatusEnum.REJECT.getStatus(),
                    BpmCommentTypeEnum.REJECT.formatComment("加签任务不通过"));
            commentService.createComment(rootParentId, task.getProcessInstanceId(), BpmCommentTypeEnum.REJECT,
                    "加签任务不通过");
        }

        // 3. 根据不同的 RejectHandler 处理策略
        // 3.1 情况一：驳回到指定的任务节点
        if (userTaskRejectHandlerType == BpmUserTaskRejectHandlerTypeEnum.RETURN_USER_TASK) {
            String returnTaskId = BpmnModelUtils.parseReturnTaskId(userTaskElement);
            Assert.notNull(returnTaskId, "退回的节点不能为空");
            // 不再退回修改：需要退回修改时，已经在上面处理；这里任务已标记为不通过，按常规的退回处理
            returnTask(userId, new BpmTaskReturnReqVO().setId(task.getId())
                    .setTargetTaskDefinitionKey(returnTaskId).setReason(reqVO.getReason()), false);
            return;
        }

        // 3.2 情况二： 标记流程为不通过并结束流程
        processInstanceUpdateService.updateProcessInstanceReject(instance, reqVO.getReason()); // 标记不通过
        moveTaskToEnd(task.getProcessInstanceId(), BpmCommentTypeEnum.REJECT.formatComment(reqVO.getReason())); // 结束流程
    }

    /**
     * 更新流程任务的 status 状态
     *
     * @param id     任务编号
     * @param status 状态
     */
    private void updateTaskStatus(String id, Integer status) {
        taskService.setVariableLocal(id, BpmnVariableConstants.TASK_VARIABLE_STATUS, status);
    }

    /**
     * 更新流程任务的 status 状态、reason 理由
     *
     * @param id     任务编号
     * @param status 状态
     * @param reason 理由（审批通过、审批不通过的理由）
     */
    private void updateTaskStatusAndReason(String id, Integer status, String reason) {
        updateTaskStatus(id, status);
        taskService.setVariableLocal(id, BpmnVariableConstants.TASK_VARIABLE_REASON, reason);
    }

    /**
     * 保存【加签】等待中的任务：记录等待人、置空 assignee，并更新状态为 WAIT
     * <p>
     * 适用于向前加签的父任务、向后加签的子任务，它们需要等待其它任务审批后，再通过 {@link #resolveWaitTask(String)} 恢复审批
     * <p>
     * 特殊：被委派的任务，owner 需要保留给委派归还使用，所以等待人记录到 {@link BpmnVariableConstants#TASK_SIGN_WAIT_ASSIGNEE} 任务变量
     *
     * @param task  任务
     * @param owner 等待恢复审批的用户编号
     */
    private void saveWaitTask(TaskEntityImpl task, String owner) {
        // 1. 普通任务用 owner 记录等待人，委派任务保留 owner 供后续归还
        boolean delegated = DelegationState.PENDING.equals(task.getDelegationState());
        if (!delegated) {
            task.setOwner(owner);
        }
        // 2. 清空 assignee 并先保存任务，暂停当前审批人的操作
        task.setAssignee(null);
        taskService.saveTask(task);

        // 3. 委派任务单独保存等待人，最后将任务设为 WAIT
        // 更新局部变量会改变任务版本号，必须在保存任务实体之后执行，避免乐观锁冲突
        if (delegated) {
            taskService.setVariableLocal(task.getId(), BpmnVariableConstants.TASK_SIGN_WAIT_ASSIGNEE, owner);
        }
        updateTaskStatus(task.getId(), BpmTaskStatusEnum.WAIT.getStatus());
    }

    /**
     * 恢复【加签】等待中的任务：将等待人（owner）重新赋值给 assignee，并更新状态为 RUNNING，这样它就可以被审批
     *
     * @param id 任务编号
     */
    private void resolveWaitTask(String id) {
        // 情况一：被委派的任务，恢复为记录的等待人，并保持委派中，审批后继续归还委派人
        String waitAssignee = (String) taskService.getVariableLocal(id, BpmnVariableConstants.TASK_SIGN_WAIT_ASSIGNEE);
        if (StrUtil.isNotBlank(waitAssignee)) {
            taskService.removeVariableLocal(id, BpmnVariableConstants.TASK_SIGN_WAIT_ASSIGNEE);
            taskService.setAssignee(id, waitAssignee);
        // 情况二：普通的任务，调用 resolveTask 将 owner 重新赋值给 assignee
        } else {
            taskService.resolveTask(id);
        }
        updateTaskStatus(id, BpmTaskStatusEnum.RUNNING.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void returnTask(Long userId, BpmTaskReturnReqVO reqVO) {
        returnTask(userId, reqVO, true);
    }

    /**
     * 退回任务
     *
     * @param userId          用户编号
     * @param reqVO           退回请求
     * @param revisionEnabled 是否允许退回修改（【从当前节点审批】），见 {@link #rejectTask(Long, BpmTaskRejectReqVO, boolean)}
     */
    private void returnTask(Long userId, BpmTaskReturnReqVO reqVO, boolean revisionEnabled) {
        // 1.1 当前任务 task
        Task task = taskQueryService.validateTask(userId, reqVO.getId());
        if (task.isSuspended()) {
            throw exception(TASK_IS_PENDING);
        }
        // 1.2 获取流程模型信息
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(task.getProcessDefinitionId());
        // 1.3 校验源头和目标节点的关系，并返回目标元素
        FlowElement targetElement = validateTargetTaskCanReturn(bpmnModel, task.getTaskDefinitionKey(),
                reqVO.getTargetTaskDefinitionKey());

        // 2.1 情况一：【从当前节点审批】，任务不跳转，等待修改人修改后，由当前审批人继续审批
        if (revisionEnabled) {
            String revisionAssignee = getRevisionAssignee(task,
                    BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey()), targetElement);
            if (revisionAssignee != null) {
                returnTaskToRevision(task, targetElement, revisionAssignee, reqVO.getReason());
                return;
            }
        }
        // 2.2 情况二：【重新依次审批】，调用 Flowable 框架的退回逻辑。包括不满足【从当前节点审批】条件的情况
        returnTask(userId, bpmnModel, task, targetElement, reqVO);
    }

    /**
     * 获得任务退回时，【从当前节点审批】的修改人。人工驳回、退回共用该判断
     * <p>
     * 以下情况，不支持【从当前节点审批】，返回 null，按【重新依次审批】处理：
     * 1. 节点未配置【从当前节点审批】，或者不是流程表单：修改人需要在流程详情中修改流程表单
     * 2. 加签的子任务（parentTaskId 非空）、加签中的父任务（scopeType 非空），与原退回逻辑一致
     * 3. 多人审批的节点（会签、或签、依次审批的实例数大于 1）：其它审批人的任务可能已经完成或者仍在审批，无法只回到当前任务
     * 4. 目标节点没有有效的修改人，例如说：目标节点自动通过、审批人为空时自动通过
     * 补充说明：被委派中的任务支持，重新提交后仍由被委派人审批，再归还委派人
     *
     * @param task          当前任务
     * @param sourceElement 当前任务的节点
     * @param targetElement 退回的目标节点
     * @return 修改人编号；不支持时，返回 null
     */
    private String getRevisionAssignee(Task task, FlowElement sourceElement, FlowElement targetElement) {
        // 1.1 节点配置、加签
        if (BpmnModelUtils.parseReturnApproveMode(sourceElement) != BpmReturnApproveModeEnum.RESUME_SOURCE
                || task.getParentTaskId() != null || task.getScopeType() != null) {
            return null;
        }
        // 1.2 流程表单
        BpmProcessDefinitionInfoDO processDefinitionInfo = bpmProcessDefinitionService.getProcessDefinitionInfo(
                task.getProcessDefinitionId());
        if (processDefinitionInfo == null
                || ObjectUtil.notEqual(BpmModelFormTypeEnum.NORMAL.getType(), processDefinitionInfo.getFormType())) {
            return null;
        }
        // 1.3 多人审批的节点：按多实例的实例数（nrOfInstances，记录在多实例的根 execution 上）判断
        if (sourceElement instanceof Activity activity && activity.getLoopCharacteristics() != null) {
            Integer nrOfInstances = runtimeService.getVariable(task.getExecutionId(), "nrOfInstances", Integer.class);
            if (nrOfInstances != null && nrOfInstances > 1) {
                return null;
            }
        }

        // 2. 修改人：目标节点最近一次审批通过的处理人。目标节点是发起人节点时，就是流程发起人
        // 为什么只取审批通过的任务？或签时其它人的任务被取消，任务被退回、撤回时也会结束，它们的处理人都没有提交过表单
        // 为什么排除加签的子任务？它们和父任务共用节点，但表单由父任务的处理人提交
        // 为什么排除没有处理人的任务？目标节点自动通过时，没有人提交过表单
        List<HistoricTaskInstance> approvedTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .taskDefinitionKey(targetElement.getId())
                .taskVariableValueEquals(BpmnVariableConstants.TASK_VARIABLE_STATUS, BpmTaskStatusEnum.APPROVE.getStatus())
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc().list();
        HistoricTaskInstance approvedTask = findFirst(approvedTasks,
                item -> item.getParentTaskId() == null && StrUtil.isNotBlank(item.getAssignee()));
        return approvedTask != null ? approvedTask.getAssignee() : null;
    }

    /**
     * 退回修改：任务不跳转，标记为退回修改中（WAIT_REVISION），由目标节点的处理人修改表单后重新提交
     *
     * @param task             当前任务
     * @param targetElement    退回的目标节点
     * @param revisionAssignee 修改人编号，见 {@link #getRevisionAssignee(Task, FlowElement, FlowElement)}
     * @param reason           退回意见
     */
    private void returnTaskToRevision(Task task, FlowElement targetElement, String revisionAssignee, String reason) {
        if (task.isSuspended()) {
            throw exception(TASK_IS_PENDING);
        }
        // 1. 记录修改人、目标节点，并生成新的批次：之前批次的页面，不能再重新提交、审批
        taskService.setVariableLocal(task.getId(), BpmnVariableConstants.TASK_VARIABLE_REVISION_ASSIGNEE, revisionAssignee);
        taskService.setVariableLocal(task.getId(), BpmnVariableConstants.TASK_VARIABLE_REVISION_TARGET_NODE, targetElement.getId());
        taskService.setVariableLocal(task.getId(), BpmnVariableConstants.TASK_VARIABLE_REVISION_ID, IdUtil.fastSimpleUUID());
        // 2. 更新 task 状态 + 原因
        updateTaskStatusAndReason(task.getId(), BpmTaskStatusEnum.WAIT_REVISION.getStatus(), reason);
        // 3. 添加评论
        commentService.createComment(task.getId(), task.getProcessInstanceId(), BpmCommentTypeEnum.RETURN, reason);
    }

    /**
     * 结束任务的退回修改：恢复为审批中（RUNNING），由当前审批人继续审批
     * <p>
     * 保留批次编号：审批人需要传递该批次审批，避免使用修改前的页面审批
     *
     * @param taskId 任务编号
     */
    private void resumeRevisionTask(String taskId) {
        taskService.removeVariablesLocal(taskId, List.of(BpmnVariableConstants.TASK_VARIABLE_REASON,
                BpmnVariableConstants.TASK_VARIABLE_REVISION_ASSIGNEE, BpmnVariableConstants.TASK_VARIABLE_REVISION_TARGET_NODE));
        updateTaskStatus(taskId, BpmTaskStatusEnum.RUNNING.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void resubmitTask(Long userId, BpmTaskResubmitReqVO reqVO) {
        // 1.1 校验是该用户待修改的任务，并且是当前批次：避免上一次退回修改的页面，提交到之后的退回修改
        Task task = taskQueryService.getRevisionTask(userId, null, reqVO.getTaskId());
        if (task == null) {
            throw exception(TASK_RESUBMIT_NOT_ALLOWED);
        }
        if (ObjectUtil.notEqual(task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REVISION_ID),
                reqVO.getRevisionId())) {
            throw exception(TASK_REVISION_EXPIRED);
        }
        // 1.2 校验流程实例存在
        ProcessInstance instance = processInstanceQueryService.getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }
        // 1.3 校验修改的表单字段：按退回目标节点的表单字段权限
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(task.getProcessDefinitionId());
        String targetNode = (String) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REVISION_TARGET_NODE);
        Map<String, Object> variables = MapUtil.emptyIfNull(reqVO.getVariables());
        validateRevisionVariables(variables, bpmProcessDefinitionService.getProcessDefinitionInfo(task.getProcessDefinitionId()),
                bpmnModel, BpmnModelUtils.parseFormFieldsPermission(bpmnModel, targetNode), instance.getProcessVariables());

        // 2. 更新流程变量
        if (CollUtil.isNotEmpty(variables)) {
            runtimeService.setVariables(task.getProcessInstanceId(), variables);
        }
        // 3. 恢复任务为审批中，并添加评论
        resumeRevisionTask(task.getId());
        commentService.createComment(task.getId(), task.getProcessInstanceId(), BpmCommentTypeEnum.RESUBMIT);
    }

    /**
     * 校验退回修改后，重新提交的表单变量。和 yudao-ui-admin-vue3 流程详情的表单字段权限保持一致：
     * <p>
     * 1. 不能修改工作流的保留变量，即使表单中配置了同名的字段，见 {@link FlowableUtils#isReservedVariable}
     * 2. 只能修改流程表单的字段，不能修改流程变量、子表单内部的子字段等其它变量
     * 3. 只能修改退回目标节点【可编辑】的字段。未配置权限的字段，前端默认只读，所以也不能修改
     * 4. 子表单（表格表单、子表单）的值，格式与组件生成的一致，并且【只读】、【隐藏】的子字段不能修改，见 {@link #validateRevisionSubFormValue}
     *
     * @param variables             修改的表单变量
     * @param processDefinitionInfo 流程定义信息
     * @param bpmnModel             流程模型
     * @param fieldsPermission      退回目标节点的表单字段权限
     * @param processVariables      修改前的流程变量
     */
    private void validateRevisionVariables(Map<String, Object> variables, BpmProcessDefinitionInfoDO processDefinitionInfo,
                                           BpmnModel bpmnModel, Map<String, String> fieldsPermission,
                                           Map<String, Object> processVariables) {
        if (CollUtil.isEmpty(variables)) {
            return;
        }
        Map<String, JsonNode> fieldRules = new HashMap<>();
        if (processDefinitionInfo != null && BpmModelFormTypeEnum.NORMAL.getType().equals(processDefinitionInfo.getFormType())) {
            CollUtil.emptyIfNull(processDefinitionInfo.getFormFields()).forEach(fieldStr ->
                    FlowableUtils.parseFormFieldRules(JsonUtils.parseTree(fieldStr), fieldRules));
        }
        variables.forEach((field, value) -> {
            JsonNode fieldRule = fieldRules.get(field);
            if (FlowableUtils.isReservedVariable(field, bpmnModel) || fieldRule == null
                    || !isWritePermission(MapUtil.getStr(fieldsPermission, field))) {
                throw exception(TASK_RESUBMIT_FIELD_NOT_WRITABLE, field);
            }
            validateRevisionSubFormValue(field, fieldRule, value, processVariables.get(field), fieldsPermission);
        });
    }

    /**
     * 比较 JSON 值：数字按数值比较，忽略序列化的差异（例如说 10 和 10.0，Integer 和 Long）
     * <p>
     * 补充说明：对象按键比较，本身就忽略键的顺序，见 {@link com.fasterxml.jackson.databind.node.ObjectNode#equals(Comparator, JsonNode)}
     */
    private static final Comparator<JsonNode> JSON_VALUE_COMPARATOR = (o1, o2) -> {
        if (o1.isNumber() && o2.isNumber()) {
            return o1.decimalValue().compareTo(o2.decimalValue());
        }
        return o1.equals(o2) ? 0 : 1;
    };

    /**
     * 校验子表单（表格表单、子表单）的值
     * <p>
     * 1. 值的格式，和 form-create 组件生成的一致，否则不能修改：
     *    1.1 表格表单（tableForm）、子表单（group）：每一行是【子字段 - 值】的对象，不能包含未知的子字段
     *    1.2 配置 props.field 的子表单（group）：每一行是该子字段的值，其它子字段不会保存
     * 2. 【只读】、【隐藏】的子字段，不能修改。子字段的权限，和 yudao-ui-admin-vue3 的 getSubFieldPermission 一致：
     *    未单独配置时，继承子表单的权限（这里一定是【可编辑】）。
     *    因为可以新增、删除行，所以按行匹配：每一行的【只读】、【隐藏】子字段，与修改前的某一行一致（已有的行），或者为空、默认值（新增的行）
     *
     * @param field            子表单字段
     * @param fieldRule        子表单的规则
     * @param value            修改后的值
     * @param oldValue         修改前的值
     * @param fieldsPermission 表单字段权限
     */
    private static void validateRevisionSubFormValue(String field, JsonNode fieldRule, Object value, Object oldValue,
                                                     Map<String, String> fieldsPermission) {
        List<JsonNode> subFieldRules = FlowableUtils.getSubFormFieldRules(fieldRule);
        if (subFieldRules == null) {
            return;
        }
        // 1. 校验值的格式：必须是数组，不能为 null
        // 1.1 表格表单、子表单：每一行是对象，不能包含未知的子字段
        // 1.2 配置 props.field 的子表单：每一行是该子字段的值，不能是包含该子字段的对象（组件会拆包，不是组件生成的格式）；
        //     该子字段是普通输入框（input）时，值只能是简单值，不能是对象、数组
        String rowField = StrUtil.emptyToNull(fieldRule.path("props").path("field").asText());
        Set<String> subFields = convertSet(subFieldRules, subFieldRule -> JsonUtils.getText(subFieldRule, "field"));
        boolean inputRowField = rowField != null && anyMatch(subFieldRules, subFieldRule ->
                rowField.equals(JsonUtils.getText(subFieldRule, "field")) && "input".equals(JsonUtils.getText(subFieldRule, "type")));
        if (!(value instanceof List<?> rows) || anyMatch(rows, row -> rowField == null
                ? !(row instanceof Map<?, ?> rowMap) || !subFields.containsAll(rowMap.keySet())
                : (row instanceof Map<?, ?> wrapper && wrapper.containsKey(rowField))
                        || (inputRowField && (row instanceof Map || row instanceof Collection)))) {
            throw exception(TASK_RESUBMIT_FIELD_NOT_WRITABLE, field);
        }

        // 2. 获得不能修改的子字段，以及新增行时它们的默认值：子字段规则的 value，或者子表单（group）的 props.defaultValue
        // 为什么配置 props.field 时，默认值是整个 props.defaultValue？和 group 组件一致，新增行时，该行的值就是 props.defaultValue
        JsonNode rowDefaultValue = fieldRule.path("props").path("defaultValue");
        Map<String, List<JsonNode>> readonlySubFields = new LinkedHashMap<>();
        subFieldRules.forEach(subFieldRule -> {
            String subField = JsonUtils.getText(subFieldRule, "field");
            String permission = MapUtil.getStr(fieldsPermission, field + StrUtil.DOT + subField);
            if (permission == null || isWritePermission(permission)
                    || (rowField != null && ObjectUtil.notEqual(rowField, subField))) {
                return;
            }
            JsonNode defaultValue = rowField != null ? rowDefaultValue : rowDefaultValue.get(subField);
            readonlySubFields.put(subField, filterList(Arrays.asList(subFieldRule.get("value"), defaultValue),
                    item -> item != null && !item.isMissingNode()));
        });
        if (readonlySubFields.isEmpty()) {
            return;
        }

        // 3. 逐行校验
        List<Map<String, Object>> oldReadonlyRows = oldValue instanceof List<?> oldRows
                ? convertList(oldRows, oldRow -> getReadonlySubFieldValues(oldRow, rowField, readonlySubFields.keySet()))
                : new ArrayList<>();
        for (Object row : rows) {
            Map<String, Object> readonlyValues = getReadonlySubFieldValues(row, rowField, readonlySubFields.keySet());
            // 3.1 情况一：已有的行，不能修改的子字段与修改前一致。匹配后移除，避免多行匹配同一行
            int oldIndex = CollUtil.indexOf(oldReadonlyRows, oldRow -> isSameJsonValue(oldRow, readonlyValues));
            if (oldIndex >= 0) {
                oldReadonlyRows.remove(oldIndex);
                continue;
            }
            // 3.2 情况二：新增的行，不能修改的子字段为空、或者默认值
            readonlySubFields.forEach((subField, defaultValues) -> {
                Object subValue = readonlyValues.get(subField);
                if (ObjectUtil.isNotEmpty(subValue)
                        && !anyMatch(defaultValues, defaultValue -> isSameJsonValue(defaultValue, subValue))) {
                    throw exception(TASK_RESUBMIT_FIELD_NOT_WRITABLE, field + StrUtil.DOT + subField);
                }
            });
        }
    }

    /**
     * 获得子表单一行中，不能修改的子字段的值
     *
     * @param row       子表单的一行
     * @param rowField  子表单（group）的 props.field。非空时，和 form-create 的 group 组件一致，一行是该子字段的值
     * @param subFields 不能修改的子字段
     * @return 【子字段 - 值】；行的格式不正确时，子字段的值为 null
     */
    private static Map<String, Object> getReadonlySubFieldValues(Object row, String rowField, Collection<String> subFields) {
        Map<?, ?> rowMap = rowField != null ? Collections.singletonMap(rowField, row)
                : row instanceof Map<?, ?> map ? map : Collections.emptyMap();
        Map<String, Object> values = new HashMap<>();
        subFields.forEach(subField -> values.put(subField, rowMap.get(subField)));
        return values;
    }

    /**
     * 判断两个值，转换成 JSON 后是否一致，见 {@link #JSON_VALUE_COMPARATOR}
     */
    private static boolean isSameJsonValue(Object value1, Object value2) {
        JsonNode node1 = ObjectUtil.defaultIfNull(JsonUtils.convertObject(value1, JsonNode.class), NullNode.getInstance());
        JsonNode node2 = ObjectUtil.defaultIfNull(JsonUtils.convertObject(value2, JsonNode.class), NullNode.getInstance());
        return node1.equals(JSON_VALUE_COMPARATOR, node2);
    }

    private static boolean isWritePermission(String permission) {
        return String.valueOf(BpmFieldPermissionEnum.WRITE.getPermission()).equals(permission);
    }

    /**
     * 退回流程节点时，校验目标任务节点是否可退回
     *
     * @param bpmnModel 流程模型
     * @param sourceKey 当前任务节点 Key
     * @param targetKey 目标任务节点 key
     * @return 目标任务节点元素
     */
    private FlowElement validateTargetTaskCanReturn(BpmnModel bpmnModel, String sourceKey, String targetKey) {
        // 1.1 获取当前任务节点元素
        FlowElement source = BpmnModelUtils.getFlowElementById(bpmnModel, sourceKey);
        // 1.2 获取跳转的节点元素
        FlowElement target = BpmnModelUtils.getFlowElementById(bpmnModel, targetKey);
        if (target == null) {
            throw exception(TASK_TARGET_NODE_NOT_EXISTS);
        }

        // 2. 只有串行可到达的节点，才可以退回。类似非串行、子流程无法退回
        if (!BpmnModelUtils.isSequentialReachable(source, target, null)) {
            throw exception(TASK_RETURN_FAIL_SOURCE_TARGET_ERROR);
        }
        return target;
    }

    /**
     * 执行退回逻辑
     *
     * @param userId        用户编号
     * @param bpmnModel     流程模型
     * @param currentTask   当前退回的任务
     * @param targetElement 需要退回到的目标任务
     * @param reqVO         前端参数封装
     */
    public void returnTask(Long userId, BpmnModel bpmnModel, Task currentTask, FlowElement targetElement, BpmTaskReturnReqVO reqVO) {
        // 1. 获得所有需要回撤的任务 taskDefinitionKey，用于稍后的 moveActivityIdsToSingleActivityId 回撤
        // 1.1 获取所有正常进行的任务节点 Key
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(currentTask.getProcessInstanceId()).list();
        List<String> runTaskKeyList = convertList(taskList, Task::getTaskDefinitionKey);
        // 1.2 通过 targetElement 的出口连线，计算在 runTaskKeyList 有哪些 key 需要被撤回
        // 为什么不直接使用 runTaskKeyList 呢？因为可能存在多个审批分支，例如说：A -> B -> C 和 D -> F，而只要 C 撤回到 A，需要排除掉 F
        List<UserTask> returnUserTaskList = BpmnModelUtils.iteratorFindChildUserTasks(targetElement, runTaskKeyList, null, null);
        List<String> returnTaskKeyList = convertList(returnUserTaskList, UserTask::getId);
        // 2. 给当前要被退回的 task 数组，设置退回意见
        taskList.forEach(task -> {
            // 需要排除掉，不需要设置退回意见的任务
            if (!returnTaskKeyList.contains(task.getTaskDefinitionKey())) {
                return;
            }
            // 判断是否分配给自己任务，因为会签任务，一个节点会有多个任务
            if (isAssignUserTask(userId, task)) { // 情况一：自己的任务，进行 RETURN 标记
                // 2.1.1 添加评论
                commentService.createComment(task.getId(), currentTask.getProcessInstanceId(), BpmCommentTypeEnum.RETURN,
                        reqVO.getReason());
                // 2.1.2 更新 task 状态 + 原因
                updateTaskStatusAndReason(task.getId(), BpmTaskStatusEnum.RETURN.getStatus(), reqVO.getReason());
            } else { // 情况二：别人的任务，进行 CANCEL 标记
                processTaskCanceled(task.getId());
            }
        });

        // 3. 构建需要预测的任务流程变量
        Set<String> needSimulateTaskDefinitionKeys = getNeedSimulateTaskDefinitionKeys(bpmnModel, currentTask, targetElement);

        // 4. 执行驳回
        // ① 使用 moveExecutionsToSingleActivityId 替换 moveActivityIdsToSingleActivityId。原因：当多实例任务回退的时候有问题。
        //    相关 issue: https://github.com/flowable/flowable-engine/issues/3944
        // ② flowable 7.2.0 版本后，继续使用 moveActivityIdsToSingleActivityId 方法。原因：flowable 7.2.0 版本修复了该问题。
        //    相关 issue：https://github.com/YunaiV/ruoyi-vue-pro/issues/1018
        // ③ moveActivityIdsToSingleActivityId 使用遇到问题， 相关 issue https://gitee.com/zhijiantianya/yudao-cloud/issues/IJM8MS
        //  改成 moveExecutionsToSingleActivityId 好像并没有遇到 ② 提到的超时提醒失效的问题。暂时先改回 moveExecutionsToSingleActivityId
        // ④ moveExecutionsToSingleActivityId 回退多实例的时候不会去删除多实例根, 应改成 moveActivityIdsToSingleActivityId
        // flowable 8.0.0  修复上面相关问题， 还修复了并行分支回退的问题 https://t.zsxq.com/z4d9i。
        // ⑤ 使用 moveExecutionsToSingleActivityId 方法进行回退操作时，如果是多实例的用户任务【芋道用户任务默认为多实例】，不会删除多实例任务的根数据 ACT_RU_EXECUTION
        // 会导致有一些问题，所以使用 moveActivityIdsToSingleActivityId。 但是该方法在 flowable 6.8.1 ~ 7.1.0 的版本会有 bug 阻塞回退功能
        // 相关 issue: https://github.com/flowable/flowable-engine/issues/3944
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(currentTask.getProcessInstanceId())
                .moveActivityIdsToSingleActivityId(returnTaskKeyList, reqVO.getTargetTaskDefinitionKey())
                // 设置需要预测的任务 ids 的流程变量，用于辅助预测
                .processVariable(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS, needSimulateTaskDefinitionKeys)
                // 设置流程变量节点退回标记, 用于退回到节点，不执行 BpmUserTaskAssignStartUserHandlerTypeEnum 策略，导致自动通过
                .processVariable(String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, reqVO.getTargetTaskDefinitionKey()), Boolean.TRUE)
                .changeState();
    }

    private Set<String> getNeedSimulateTaskDefinitionKeys(BpmnModel bpmnModel, Task currentTask, FlowElement targetElement) {
        // 1. 获取需要预测的任务的 definition key。因为当前任务还没完成，也需要预测
        Set<String> taskDefinitionKeys = CollUtil.newHashSet(currentTask.getTaskDefinitionKey());

        // 2.1 获取已结束任务按时间倒序排序
        List<HistoricTaskInstance> endTaskList = CollectionUtils.filterList(
                taskQueryService.getTaskListByProcessInstanceId(currentTask.getProcessInstanceId(), Boolean.FALSE),
                item -> item.getEndTime() != null);
        // 2.2 从结束任务中找到最近一个的目标任务
        HistoricTaskInstance targetTask = findFirst(endTaskList,
                item -> item.getTaskDefinitionKey().equals(targetElement.getId()));
        if (targetTask == null) {
            return taskDefinitionKeys;
        }
        // 2.3 遍历已结束的任务，找到在 targetTask 之后生成的任务，且串行可达的任务
        endTaskList.forEach(item -> {
            FlowElement element = getFlowElementById(bpmnModel, item.getTaskDefinitionKey());
            // 如果已结束的任务在回退目标节点之后生成，且串行可达，则加到需要预测节点中
            // 补充说明：与 validateTargetTaskCanReturn 使用相同的串行可达判断，保证“可以退回到的节点”与“退回后会再次执行的节点”一致。
            // 跨过并行网关的任务（例如说，其它并行分支上的任务）不会因为本次退回而再次执行，所以不需要预测
            if (item.getCreateTime().compareTo(targetTask.getCreateTime()) > 0
                    && BpmnModelUtils.isSequentialReachable(element, targetElement, null)) {
                taskDefinitionKeys.add(item.getTaskDefinitionKey());
            }
        });
        return taskDefinitionKeys;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void delegateTask(Long userId, BpmTaskDelegateReqVO reqVO) {
        String taskId = reqVO.getId();
        // 1.1 校验任务
        Task task = taskQueryService.validateTask(userId, reqVO.getId());
        if (StrUtil.equals(task.getAssignee(), reqVO.getDelegateUserId().toString())) { // 校验当前审批人和被委派人不是同一人
            throw exception(TASK_DELEGATE_FAIL_USER_REPEAT);
        }
        // 1.2 校验目标用户存在
        AdminUserRespDTO delegateUser = adminUserApi.getUser(reqVO.getDelegateUserId());
        if (delegateUser == null) {
            throw exception(TASK_DELEGATE_FAIL_USER_NOT_EXISTS);
        }

        // 2. 添加委托意见
        AdminUserRespDTO currentUser = adminUserApi.getUser(userId);
        commentService.createComment(taskId, task.getProcessInstanceId(), BpmCommentTypeEnum.DELEGATE_START,
                currentUser.getNickname(), delegateUser.getNickname(), reqVO.getReason());

        // 3.1 设置任务所有人 (owner) 为原任务的处理人 (assignee)
        // 特殊：如果已经被委派（owner 非空），则不需要更新 owner：https://gitee.com/zhijiantianya/yudao-cloud/issues/ICJ153
        if (StrUtil.isEmpty(task.getOwner())) {
            taskService.setOwner(taskId, task.getAssignee());
        }
        // 3.2 记录归还顺序：追加当前审批人为直接委派人，审批后逐层归还
        List<String> returnUserIds = getDelegateReturnUserIds(task);
        if (returnUserIds.isEmpty() && DelegationState.PENDING.equals(task.getDelegationState())
                && StrUtil.isNotBlank(task.getOwner())) {
            returnUserIds.add(task.getOwner()); // 兼容历史数据：委派中、但没有记录归还顺序的任务，最初的委派人是 owner
        }
        returnUserIds.add(StrUtil.blankToDefault(task.getAssignee(), String.valueOf(userId)));
        taskService.setVariableLocal(taskId, BpmnVariableConstants.TASK_DELEGATE_RETURN_USERS,
                String.join(StrUtil.COMMA, returnUserIds));
        // 3.3 执行委派，将任务委派给 delegateUser
        taskService.delegateTask(taskId, reqVO.getDelegateUserId().toString());
        // 补充说明：委托不单独设置状态。如果需要，可通过 Task 的 DelegationState 字段，判断是否为 DelegationState.PENDING 委托中
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void transferTask(Long userId, BpmTaskTransferReqVO reqVO) {
        String taskId = reqVO.getId();
        // 1.1 校验任务
        Task task = taskQueryService.validateTask(userId, reqVO.getId());
        if (StrUtil.equals(task.getAssignee(), reqVO.getAssigneeUserId().toString())) { // 校验当前审批人和被转派人不是同一人
            throw exception(TASK_TRANSFER_FAIL_USER_REPEAT);
        }
        // 1.2 校验目标用户存在
        AdminUserRespDTO assigneeUser = adminUserApi.getUser(reqVO.getAssigneeUserId());
        if (assigneeUser == null) {
            throw exception(TASK_TRANSFER_FAIL_USER_NOT_EXISTS);
        }

        // 2. 添加委托意见
        AdminUserRespDTO currentUser = adminUserApi.getUser(userId);
        commentService.createComment(taskId, task.getProcessInstanceId(), BpmCommentTypeEnum.TRANSFER,
                currentUser.getNickname(), assigneeUser.getNickname(), reqVO.getReason());

        // 3.1 设置任务所有人 (owner) 为原任务的处理人 (assignee)
        // 特殊：如果已经被转派（owner 非空），则不需要更新 owner：https://gitee.com/zhijiantianya/yudao-cloud/issues/ICJ153
        if (StrUtil.isEmpty(task.getOwner())) {
            taskService.setOwner(taskId, task.getAssignee());
        }
        // 3.2 执行转派（审批人），将任务转派给 assigneeUser
        // 委托（ delegate）和转派（transfer）的差别，就在这块的调用！！！！
        taskService.setAssignee(taskId, reqVO.getAssigneeUserId().toString());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void moveTaskToEnd(String processInstanceId, String reason) {
        List<Task> taskList = taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, null, null);
        if (CollUtil.isEmpty(taskList)) {
            return;
        }

        // 1. 其它未结束的任务，直接取消
        // 疑问：为什么不通过 updateTaskStatusWhenCanceled 监听取消，而是直接提前调用呢？
        // 回答：详细见 updateTaskStatusWhenCanceled 的方法，加签的场景
        taskList.forEach(task -> {
            Integer otherTaskStatus = (Integer) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
            if (BpmTaskStatusEnum.isEndStatus(otherTaskStatus)) {
                return;
            }
            processTaskCanceled(task.getId());
        });

        // 2. 终止流程
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(taskList.get(0).getProcessDefinitionId());
        List<String> activityIds = CollUtil.newArrayList(convertSet(taskList, Task::getTaskDefinitionKey));
        EndEvent endEvent = BpmnModelUtils.getEndEvent(bpmnModel);
        Assert.notNull(endEvent, "结束节点不能为空");
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstanceId)
                .moveActivityIdsToSingleActivityId(activityIds, endEvent.getId())
                .changeState();

        // 3. 特殊：如果跳转到 EndEvent 流程还未结束， 执行 deleteProcessInstance 方法
        // 例如说：并行分支下，已经到达汇聚网关、等待其它分支的执行不是任务，不在上面的 activityIds 中，跳转后仍然保留，流程不会结束
        List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list();
        if (CollUtil.isNotEmpty(executions)) {
            log.warn("[moveTaskToEnd][执行跳转到 EndEvent 后, 流程实例未结束，强制执行 deleteProcessInstance 方法]");
            runtimeService.deleteProcessInstance(processInstanceId, reason);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void createSignTask(Long userId, BpmTaskSignCreateReqVO reqVO) {
        // 1. 获取和校验任务
        TaskEntityImpl taskEntity = validateTaskCanCreateSign(userId, reqVO);
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(reqVO.getUserIds());
        if (CollUtil.isEmpty(userList) || userList.size() != reqVO.getUserIds().size()) {
            throw exception(TASK_SIGN_CREATE_USER_NOT_EXIST);
        }

        // 2. 处理当前任务
        // 2.1 开启计数功能，主要用于为了让表 ACT_RU_TASK 中的 SUB_TASK_COUNT_ 字段记录下总共有多少子任务，后续可能有用
        taskEntity.setCountEnabled(true);
        // 2.2 记录加签方式，完成任务时需要用到判断
        taskEntity.setScopeType(reqVO.getType());
        // 2.3 保存当前任务修改后的值
        // 向前加签，设置 owner（被委派的任务记录等待人），置空 assignee，状态为 WAIT。等子任务都完成后，再调用 resolveWaitTask 恢复 assignee
        // 原因是：不能和向前加签的子任务一起审批，需要等前面的子任务都完成才能审批
        if (BpmTaskSignTypeEnum.BEFORE.getType().equals(reqVO.getType())) {
            saveWaitTask(taskEntity, taskEntity.getAssignee());
        } else {
            taskService.saveTask(taskEntity);
        }

        // 3. 创建加签任务
        createSignTaskList(convertList(reqVO.getUserIds(), String::valueOf), taskEntity);

        // 4. 记录加签的评论到 task 任务
        AdminUserRespDTO currentUser = adminUserApi.getUser(userId);
        commentService.createComment(reqVO.getId(), taskEntity.getProcessInstanceId(), BpmCommentTypeEnum.ADD_SIGN,
                currentUser.getNickname(), BpmTaskSignTypeEnum.nameOfType(reqVO.getType()),
                String.join(",", convertList(userList, AdminUserRespDTO::getNickname)), reqVO.getReason());
    }

    /**
     * 校验任务是否可以加签，主要校验加签类型是否一致：
     * <p>
     * 0. 加签类型必须存在
     * 1. 如果存在“向前加签”的任务，则不能“向后加签”
     * 2. 如果存在“向后加签”的任务，则不能“向前加签”
     *
     * @param userId 当前用户 ID
     * @param reqVO  请求参数，包含任务 ID 和加签类型
     * @return 当前任务
     */
    private TaskEntityImpl validateTaskCanCreateSign(Long userId, BpmTaskSignCreateReqVO reqVO) {
        if (BpmTaskSignTypeEnum.of(reqVO.getType()) == null) {
            throw exception(TASK_SIGN_CREATE_TYPE_NOT_EXISTS, reqVO.getType());
        }
        TaskEntityImpl taskEntity = (TaskEntityImpl) taskQueryService.validateTask(userId, reqVO.getId());
        // 向前加签和向后加签不能同时存在
        if (taskEntity.getScopeType() != null
                && ObjectUtil.notEqual(taskEntity.getScopeType(), reqVO.getType())) {
            throw exception(TASK_SIGN_CREATE_TYPE_ERROR,
                    BpmTaskSignTypeEnum.nameOfType(taskEntity.getScopeType()), BpmTaskSignTypeEnum.nameOfType(reqVO.getType()));
        }

        // 同一个 key 的任务，审批人不重复
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(taskEntity.getProcessInstanceId())
                .taskDefinitionKey(taskEntity.getTaskDefinitionKey()).includeTaskLocalVariables().list();
        List<Long> currentAssigneeList = convertListByFlatMap(taskList, task -> // 需要考虑 owner 的情况，因为向后加签时，它暂时没 assignee 而是 owner
                Stream.of(NumberUtils.parseLong(task.getAssignee()), NumberUtils.parseLong(task.getOwner()),
                        NumberUtils.parseLong(getSignTaskOwner(task)))); // 被委派的任务向前加签时，等待人记录在任务变量中
        if (CollUtil.containsAny(currentAssigneeList, reqVO.getUserIds())) {
            List<AdminUserRespDTO> userList = adminUserApi.getUserList(CollUtil.intersection(currentAssigneeList, reqVO.getUserIds()));
            throw exception(TASK_SIGN_CREATE_USER_REPEAT, String.join(",", convertList(userList, AdminUserRespDTO::getNickname)));
        }
        return taskEntity;
    }

    /**
     * 创建加签子任务
     *
     * @param userIds    被加签的用户 ID
     * @param taskEntity 被加签的任务
     */
    private void createSignTaskList(List<String> userIds, TaskEntityImpl taskEntity) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        // 创建加签人的新任务，全部基于 taskEntity 为父任务来创建
        for (String addSignId : userIds) {
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
        BpmTaskConvert.INSTANCE.copyTo(parentTask, task);

        // 2.1 向前加签，设置审批人，子任务可以直接审批
        if (BpmTaskSignTypeEnum.BEFORE.getType().equals(parentTask.getScopeType())) {
            task.setAssignee(assignee);
            taskService.saveTask(task);
            return;
        }
        // 2.2 向后加签，设置 owner 不设置 assignee，状态为 WAIT，是因为不能同时审批，需要等父任务审批完
        saveWaitTask(task, assignee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    @SuppressWarnings("DataFlowIssue")
    public void deleteSignTask(Long userId, BpmTaskSignDeleteReqVO reqVO) {
        // 1.1 校验 task 可以被减签
        Task task = validateTaskCanSignDelete(userId, reqVO.getId());
        // 1.2 校验取消人存在
        AdminUserRespDTO cancelUser = null;
        if (StrUtil.isNotBlank(task.getAssignee())) {
            cancelUser = adminUserApi.getUser(NumberUtils.parseLong(task.getAssignee()));
        }
        String signTaskOwner = getSignTaskOwner(task);
        if (cancelUser == null && StrUtil.isNotBlank(signTaskOwner)) {
            cancelUser = adminUserApi.getUser(NumberUtils.parseLong(signTaskOwner));
        }
        Assert.notNull(cancelUser, "任务中没有所有者和审批人，数据错误");

        // 2.1 获得子任务列表，包括子任务的子任务
        List<Task> childTaskList = getAllChildTaskList(task);
        childTaskList.add(task);
        // 2.2 更新子任务为已取消
        String cancelReason = StrUtil.format("任务被取消，原因：由于[{}]操作[减签]，", cancelUser.getNickname());
        childTaskList.forEach(childTask -> updateTaskStatusAndReason(childTask.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), cancelReason));
        // 2.3 删除任务和所有子任务
        taskService.deleteTasks(convertList(childTaskList, Task::getId));

        // 3. 记录日志到父任务中。先记录日志是因为，通过 handleParentTask 方法之后，任务可能被完成了，并且不存在了，会报异常，所以先记录
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        commentService.createComment(task.getParentTaskId(), task.getProcessInstanceId(), BpmCommentTypeEnum.SUB_SIGN,
                user.getNickname(), cancelUser.getNickname());

        // 4. 处理当前任务的父任务
        handleParentTaskIfSign(task.getParentTaskId());
    }

    @Override
    public void copyTask(Long userId, BpmTaskCopyReqVO reqVO) {
        processInstanceCopyService.createProcessInstanceCopy(reqVO.getCopyUserIds(), reqVO.getReason(), reqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataPermission(enable = false) // 关闭数据权限，避免查询不到用户数据。相关案例：https://gitee.com/zhijiantianya/yudao-cloud/issues/ID1UYA
    public void withdrawTask(Long userId, String taskId) {
        // 1.1 查询本人已办任务
        HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId).taskAssignee(userId.toString()).finished().singleResult();
        if (ObjUtil.isNull(taskInstance)) {
            throw exception(TASK_WITHDRAW_FAIL_TASK_NOT_EXISTS);
        }
        // 1.2 校验流程是否结束
        ProcessInstance processInstance = processInstanceQueryService.getProcessInstance(taskInstance.getProcessInstanceId());
        if (ObjUtil.isNull(processInstance)) {
            throw exception(TASK_WITHDRAW_FAIL_PROCESS_NOT_RUNNING);
        }
        // 1.3 判断此流程是否允许撤回
        BpmProcessDefinitionInfoDO processDefinitionInfo = bpmProcessDefinitionService.getProcessDefinitionInfo(
                processInstance.getProcessDefinitionId());
        if (ObjUtil.isNull(processDefinitionInfo) || !Boolean.TRUE.equals(processDefinitionInfo.getAllowWithdrawTask())) {
            throw exception(TASK_WITHDRAW_FAIL_NOT_ALLOW);
        }
        // 1.4 判断下一个节点是否被审批过，如果是则无法撤回
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(taskInstance.getProcessDefinitionId());
        UserTask userTask = (UserTask) BpmnModelUtils.getFlowElementById(bpmnModel, taskInstance.getTaskDefinitionKey());
        List<String> nextUserTaskKeys = convertList(BpmnModelUtils.getNextUserTasks(userTask), UserTask::getId);
        if (CollUtil.isEmpty(nextUserTaskKeys)) {
            throw exception(TASK_WITHDRAW_FAIL_NEXT_TASK_NOT_ALLOW);
        }
        // 补充说明：taskCreatedAfter 不包含边界，而下一个节点的任务可能与当前任务的结束时间相同（同一毫秒内创建），所以提前 1 毫秒
        long nextUserTaskFinishedCount = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstance.getProcessInstanceId()).taskDefinitionKeys(nextUserTaskKeys)
                .taskCreatedAfter(new Date(taskInstance.getEndTime().getTime() - 1)).finished().count();
        if (nextUserTaskFinishedCount > 0) {
            throw exception(TASK_WITHDRAW_FAIL_NEXT_TASK_NOT_ALLOW);
        }
        // 1.5 获取需要撤回的运行任务
        List<Task> runningTasks = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId())
                .taskDefinitionKeys(nextUserTaskKeys).active().list();
        if (CollUtil.isEmpty(runningTasks)) {
            throw exception(TASK_WITHDRAW_FAIL_NEXT_TASK_NOT_ALLOW);
        }

        // 2.1 取消当前任务
        List<String> withdrawExecutionIds = new ArrayList<>();
        for (Task task : runningTasks) {
            // 标记撤回任务为取消
            commentService.createComment(task.getId(), taskInstance.getProcessInstanceId(), BpmCommentTypeEnum.CANCEL,
                    "前一节点撤回");
            updateTaskStatusAndReason(task.getId(), BpmTaskStatusEnum.CANCEL.getStatus(), BpmReasonEnum.CANCEL_BY_WITHDRAW.getReason());
            withdrawExecutionIds.add(task.getExecutionId());
        }
        // 2.2 执行撤回操作
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getProcessInstanceId())
                .moveExecutionsToSingleActivityId(withdrawExecutionIds, taskInstance.getTaskDefinitionKey())
                .changeState();
    }

    /**
     * 校验任务是否能被减签
     * <p>
     * 1. 被减签的任务，必须是通过加签生成的任务
     * 2. 当前用户，必须是父任务（或祖先任务）的审批人、拥有人，即加签人
     *
     * @param userId 当前用户编号
     * @param id     任务编号
     * @return 任务信息
     */
    private Task validateTaskCanSignDelete(Long userId, String id) {
        Task task = taskQueryService.validateTaskExists(id);
        if (task.getParentTaskId() == null) {
            throw exception(TASK_SIGN_DELETE_NO_PARENT);
        }
        Task parentTask = taskQueryService.getTask(task.getParentTaskId());
        if (parentTask == null) {
            throw exception(TASK_SIGN_DELETE_NO_PARENT);
        }
        if (BpmTaskSignTypeEnum.of(parentTask.getScopeType()) == null) {
            throw exception(TASK_SIGN_DELETE_NO_PARENT);
        }
        if (!isSignCreateUser(userId, parentTask)) {
            throw exception(TASK_SIGN_DELETE_FAIL_NOT_SELF);
        }
        return task;
    }

    /**
     * 判断指定用户，是否为父任务（或祖先任务）的审批人、拥有人
     * <p>
     * 为什么包含祖先任务？多层加签时，最上层的加签人，可以减签它下面所有层级的加签任务
     *
     * @param userId     用户编号
     * @param parentTask 父任务
     * @return 是否
     */
    private boolean isSignCreateUser(Long userId, Task parentTask) {
        if (userId == null) {
            return false;
        }
        Task task = parentTask;
        for (int i = 0; i < Short.MAX_VALUE && task != null; i++) {
            if (isSignTaskUser(userId, task)) {
                return true;
            }
            if (task.getParentTaskId() == null) {
                return false;
            }
            task = taskQueryService.getTask(task.getParentTaskId());
        }
        return false;
    }

    /**
     * 校验退回修改的批次：任务退回修改过时，前端传递的批次需要与任务当前的一致，避免过期的页面（修改前的表单、上一次退回的修改）提交
     * <p>
     * 从未退回修改的任务没有批次，无需校验
     *
     * @param task       任务，需要包含任务变量
     * @param revisionId 前端传递的批次编号
     */
    private static void validateRevisionId(TaskInfo task, String revisionId) {
        Object currentRevisionId = task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REVISION_ID);
        if (currentRevisionId != null && ObjectUtil.notEqual(currentRevisionId, revisionId)) {
            throw exception(TASK_REVISION_EXPIRED);
        }
    }

    /**
     * 获取子任务个数
     *
     * @param parentTaskId 父任务 ID
     * @return 剩余子任务个数
     */
    private Long getTaskCountByParentTaskId(String parentTaskId) {
        String tableName = managementService.getTableName(TaskEntity.class);
        String sql = "SELECT COUNT(1) from " + tableName + " WHERE PARENT_TASK_ID_=#{parentTaskId}";
        return taskService.createNativeTaskQuery().sql(sql).parameter("parentTaskId", parentTaskId).count();
    }

    /**
     * 获得所有子任务列表
     *
     * @param parentTask 父任务
     * @return 所有子任务列表
     */
    private List<Task> getAllChildTaskList(Task parentTask) {
        List<Task> result = new ArrayList<>();
        // 1. 递归获取子级
        Stack<Task> stack = new Stack<>();
        stack.push(parentTask);
        // 2. 递归遍历
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            if (stack.isEmpty()) {
                break;
            }
            // 2.1 获取子任务们
            Task task = stack.pop();
            List<Task> childTaskList = taskQueryService.getTaskListByParentTaskId(task.getId());
            // 2.2 如果非空，则添加到 stack 进一步递归
            if (CollUtil.isNotEmpty(childTaskList)) {
                stack.addAll(childTaskList);
                result.addAll(childTaskList);
            }
        }
        return result;
    }

    /**
     * 获得任务根任务的父任务编号
     *
     * @param task 任务
     * @return 根任务的父任务编号
     */
    private String getTaskRootParentId(Task task) {
        if (task == null || task.getParentTaskId() == null) {
            return null;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            Task parentTask = taskQueryService.getTask(task.getParentTaskId());
            if (parentTask == null) {
                return null;
            }
            if (parentTask.getParentTaskId() == null) {
                return parentTask.getId();
            }
            task = parentTask;
        }
        throw new IllegalArgumentException(String.format("Task(%s) 层级过深，无法获取父节点编号", task.getId()));
    }

    // ========== Event 事件相关方法 ==========

    @Override
    public void processTaskCreated(Task task) {
        // 1. 设置为待办中
        Integer status = (Integer) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
        if (status != null) {
            log.error("[updateTaskStatusWhenCreated][taskId({}) 已经有状态({})]", task.getId(), status);
            return;
        }
        updateTaskStatus(task.getId(), BpmTaskStatusEnum.RUNNING.getStatus());

        ProcessInstance processInstance = processInstanceQueryService.getProcessInstance(task.getProcessInstanceId());
        if (processInstance == null) {
            log.error("[processTaskCreated][taskId({}) 没有找到流程实例]", task.getId());
            return;
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = bpmProcessDefinitionService.
                getProcessDefinitionInfo(processInstance.getProcessDefinitionId());
        if (processDefinitionInfo == null) {
            log.error("[processTaskCreated][processDefinitionId({}) 没有找到流程定义]", processInstance.getProcessDefinitionId());
            return;
        }

        // 2. 任务前置通知
        if (ObjUtil.isNotNull(processDefinitionInfo.getTaskBeforeTriggerSetting())) {
            BpmModelMetaInfoVO.HttpRequestSetting setting = processDefinitionInfo.getTaskBeforeTriggerSetting();
            BpmHttpRequestUtils.executeBpmHttpRequest(processInstance,
                    setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
        }

        // 3. 处理自动通过的情况，例如说：1）无审批人时，是否自动通过、不通过；2）非【人工审核】时，是否自动通过、不通过
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(processInstance.getProcessDefinitionId());
        FlowElement userTaskElement = BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
        Integer approveType = BpmnModelUtils.parseApproveType(userTaskElement);
        Integer assignEmptyHandlerType = BpmnModelUtils.parseAssignEmptyHandlerType(userTaskElement);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            /**
             * 特殊情况：部分情况下，TransactionSynchronizationManager 注册 afterCommit 监听时，不会被调用，但是 afterCompletion 可以
             * 例如说：第一个 task 就是配置【自动通过】或者【自动拒绝】时
             * 参见 <a href="https://gitee.com/zhijiantianya/yudao-cloud/issues/IB7V7Q">issue</a> 反馈
             */
            @Override
            public void afterCompletion(int transactionStatus) {
                // 回滚情况，直接返回
                if (ObjectUtil.equal(transactionStatus, TransactionSynchronization.STATUS_ROLLED_BACK)) {
                    return;
                }
                // 特殊情况：第一个 task 【自动通过】时，第二个任务设置审批人时 transactionStatus 会为 STATUS_UNKNOWN，不知道啥原因
                if (ObjectUtil.equal(transactionStatus, TransactionSynchronization.STATUS_UNKNOWN)
                        && taskQueryService.getTask(task.getId()) == null) {
                    return;
                }
                // 特殊情况一：【人工审核】审批人为空，根据配置是否要自动通过、自动拒绝
                if (ObjectUtil.equal(approveType, BpmUserTaskApproveTypeEnum.USER.getType())) {
                    // 如果有审批人、或者拥有人，则说明不满足情况一，不自动通过、不自动拒绝
                    if (!ObjectUtil.isAllEmpty(task.getAssignee(), task.getOwner())) {
                        return;
                    }
                    if (ObjectUtil.equal(assignEmptyHandlerType, BpmUserTaskAssignEmptyHandlerTypeEnum.APPROVE.getType())) {
                        getSelf().approveTaskInternal(null, new BpmTaskApproveReqVO()
                                .setId(task.getId()).setReason(BpmReasonEnum.ASSIGN_EMPTY_APPROVE.getReason()));
                    } else if (ObjectUtil.equal(assignEmptyHandlerType, BpmUserTaskAssignEmptyHandlerTypeEnum.REJECT.getType())) {
                        getSelf().rejectTaskInternal(null, new BpmTaskRejectReqVO()
                                .setId(task.getId()).setReason(BpmReasonEnum.ASSIGN_EMPTY_REJECT.getReason()));
                    }
                    // 特殊情况二：【自动审核】审批类型为自动通过、不通过
                } else {
                    if (ObjectUtil.equal(approveType, BpmUserTaskApproveTypeEnum.AUTO_APPROVE.getType())) {
                        getSelf().approveTaskInternal(null, new BpmTaskApproveReqVO()
                                .setId(task.getId()).setReason(BpmReasonEnum.APPROVE_TYPE_AUTO_APPROVE.getReason()));
                    } else if (ObjectUtil.equal(approveType, BpmUserTaskApproveTypeEnum.AUTO_REJECT.getType())) {
                        getSelf().rejectTaskInternal(null, new BpmTaskRejectReqVO()
                                .setId(task.getId()).setReason(BpmReasonEnum.APPROVE_TYPE_AUTO_REJECT.getReason()));
                    }
                }
            }

        });
    }

    /**
     * 重要补充说明：该方法目前主要有两个情况会调用到：
     * <p>
     * 1. 或签场景 + 审批通过：一个或签有多个审批时，如果 A 审批通过，其它或签 B、C 等任务会被 Flowable 自动删除，此时需要通过该方法更新状态为已取消
     * 2. 审批不通过：在 {@link #rejectTask(Long, BpmTaskRejectReqVO)} 不通过时，对于加签的任务，不会被 Flowable 删除，此时需要通过该方法更新状态为已取消
     */
    @Override
    public void processTaskCanceled(String taskId) {
        Task task = taskQueryService.getTask(taskId);
        // 1. 可能只是活动，不是任务，所以查询不到
        if (task == null) {
            log.error("[updateTaskStatusWhenCanceled][taskId({}) 任务不存在]", taskId);
            return;
        }

        // 2. 更新 task 状态 + 原因
        Integer status = (Integer) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS);
        if (BpmTaskStatusEnum.isEndStatus(status)) {
            log.error("[updateTaskStatusWhenCanceled][taskId({}) 处于结果({})，无需进行更新]", taskId, status);
            return;
        }
        updateTaskStatusAndReason(taskId, BpmTaskStatusEnum.CANCEL.getStatus(), BpmReasonEnum.CANCEL_BY_SYSTEM.getReason());
        // 补充说明：由于 Task 被删除成 HistoricTask 后，无法通过 Flowable Comment 添加理由，所以无法存储具体的取消理由
    }

    @Override
    @DataPermission(enable = false) // 忽略数据权限，避免因为过滤，导致找不到候选人
    public void processTaskAssigned(Task task) {
        // 发送通知。在事务提交时，批量执行操作，所以直接查询会无法查询到 ProcessInstance，所以这里是通过监听事务的提交来实现。
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            /**
             * 特殊情况：部分情况下，TransactionSynchronizationManager 注册 afterCommit 监听时，不会被调用，但是 afterCompletion 可以
             * 例如说：第一个 task 就是配置【自动通过】或者【自动拒绝】时
             * 参见 <a href="https://gitee.com/zhijiantianya/yudao-cloud/issues/IB7V7Q">issue</a> 反馈
             */
            @Override
            public void afterCompletion(int transactionStatus) {
                // 回滚情况，直接返回
                if (ObjectUtil.equal(transactionStatus, TransactionSynchronization.STATUS_ROLLED_BACK)) {
                    return;
                }
                // 特殊情况：第一个 task 【自动通过】时，第二个任务设置审批人时 transactionStatus 会为 STATUS_UNKNOWN，不知道啥原因
                if (ObjectUtil.equal(transactionStatus, TransactionSynchronization.STATUS_UNKNOWN)
                        && taskQueryService.getTask(task.getId()) == null) {
                    return;
                }
                if (StrUtil.isEmpty(task.getAssignee())) {
                    log.error("[processTaskAssigned][taskId({}) 没有分配到负责人]", task.getId());
                    return;
                }
                ProcessInstance processInstance = processInstanceQueryService.getProcessInstance(task.getProcessInstanceId());
                if (processInstance == null) {
                    log.error("[processTaskAssigned][taskId({}) 没有找到流程实例]", task.getId());
                    return;
                }

                // 需要基于 instance 设置租户编号，避免 Flowable 内部异步执行时【例如：超时自动通过】 丢失租户编号
                FlowableUtils.execute(processInstance.getTenantId(), () -> {
                    // 自动去重，通过自动审批的方式
                    BpmProcessDefinitionInfoDO processDefinitionInfo = bpmProcessDefinitionService.getProcessDefinitionInfo(task.getProcessDefinitionId());
                    if (processDefinitionInfo == null) {
                        log.error("[processTaskAssigned][taskId({}) 没有找到流程定义({})]", task.getId(), task.getProcessDefinitionId());
                        return;
                    }
                    if (processDefinitionInfo.getAutoApprovalType() != null) {
                        HistoricTaskInstanceQuery approvedTaskQuery = historyService.createHistoricTaskInstanceQuery()
                                .processInstanceId(task.getProcessInstanceId())
                                .taskVariableValueEquals(BpmnVariableConstants.TASK_VARIABLE_STATUS, BpmTaskStatusEnum.APPROVE.getStatus())
                                .finished();
                        if (BpmAutoApproveTypeEnum.APPROVE_ALL.getType().equals(processDefinitionInfo.getAutoApprovalType())
                                && approvedTaskQuery.taskAssignee(task.getAssignee()).count() > 0) {
                            getSelf().approveTaskInternal(Long.valueOf(task.getAssignee()), new BpmTaskApproveReqVO().setId(task.getId())
                                    .setReason(BpmAutoApproveTypeEnum.APPROVE_ALL.getName()));
                            return;
                        }
                        // 连续审批的节点自动通过
                        if (BpmAutoApproveTypeEnum.APPROVE_SEQUENT.getType().equals(processDefinitionInfo.getAutoApprovalType())) {
                            BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(processInstance.getProcessDefinitionId());
                            if (bpmnModel == null) {
                                log.error("[processTaskAssigned][taskId({}) 没有找到流程模型({})]", task.getId(), task.getProcessDefinitionId());
                                return;
                            }
                            List<String> sourceTaskIds = convertList(BpmnModelUtils.getElementIncomingUserTaskFlows( // 获取所有的上一个 UserTask 节点连线
                                            BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey())),
                                    SequenceFlow::getSourceRef);
                            approvedTaskQuery.taskDefinitionKeys(sourceTaskIds).orderByTaskCreateTime().desc(); // 设置 taskIds, 并按创建时间倒序排序
                            HistoricTaskInstance firstHisTask = CollUtil.getFirst(approvedTaskQuery.list());
                            if (firstHisTask != null && StrUtil.equals(firstHisTask.getAssignee(), task.getAssignee())) {
                                getSelf().approveTaskInternal(Long.valueOf(task.getAssignee()), new BpmTaskApproveReqVO().setId(task.getId())
                                        .setReason(BpmAutoApproveTypeEnum.APPROVE_SEQUENT.getName()));
                                return;
                            }
                        }
                    }

                    // 获取发起人节点
                    BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(processInstance.getProcessDefinitionId());
                    if (bpmnModel == null) {
                        log.error("[processTaskAssigned][taskId({}) 没有找到流程模型]", task.getId());
                        return;
                    }
                    FlowElement userTaskElement = BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
                    // 判断是否为退回或者驳回：如果是退回或者驳回不走这个策略
                    Boolean returnTaskFlag = runtimeService.getVariable(processInstance.getProcessInstanceId(),
                            String.format(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_RETURN_FLAG, task.getTaskDefinitionKey()), Boolean.class);
                    Boolean skipStartUserNodeFlag = Convert.toBool(runtimeService.getVariable(processInstance.getProcessInstanceId(),
                            BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_SKIP_START_USER_NODE, String.class));
                    if (userTaskElement.getId().equals(START_USER_NODE_ID)
                            && (skipStartUserNodeFlag == null // 目的：一般是“主流程”，发起人节点，自动通过审核
                            || BooleanUtil.isTrue(skipStartUserNodeFlag)) // 目的：一般是“子流程”，发起人节点，按配置自动通过审核
                            && ObjUtil.notEqual(returnTaskFlag, Boolean.TRUE)) {
                        getSelf().approveTaskInternal(Long.valueOf(task.getAssignee()), new BpmTaskApproveReqVO().setId(task.getId())
                                .setReason(BpmReasonEnum.ASSIGN_START_USER_APPROVE_WHEN_SKIP_START_USER_NODE.getReason()));
                        return;
                    }
                    // 当不为发起人节点时，审批人与提交人为同一人时，根据 BpmUserTaskAssignStartUserHandlerTypeEnum 策略进行处理
                    if (ObjectUtil.notEqual(userTaskElement.getId(), START_USER_NODE_ID)
                            && StrUtil.equals(task.getAssignee(), processInstance.getStartUserId())) {
                        if (ObjUtil.notEqual(returnTaskFlag, Boolean.TRUE)) {
                            Integer assignStartUserHandlerType = BpmnModelUtils.parseAssignStartUserHandlerType(userTaskElement);

                            // 情况一：自动跳过
                            if (ObjectUtils.equalsAny(assignStartUserHandlerType,
                                    BpmUserTaskAssignStartUserHandlerTypeEnum.SKIP.getType())) {
                                getSelf().approveTaskInternal(Long.valueOf(task.getAssignee()), new BpmTaskApproveReqVO().setId(task.getId())
                                        .setReason(BpmReasonEnum.ASSIGN_START_USER_APPROVE_WHEN_SKIP.getReason()));
                                return;
                            }
                            // 情况二：转交给部门负责人审批
                            if (ObjectUtils.equalsAny(assignStartUserHandlerType,
                                    BpmUserTaskAssignStartUserHandlerTypeEnum.TRANSFER_DEPT_LEADER.getType())) {
                                AdminUserRespDTO startUser = adminUserApi.getUser(Long.valueOf(processInstance.getStartUserId()));
                                Assert.notNull(startUser, "提交人({})信息为空", processInstance.getStartUserId());
                                DeptRespDTO dept = startUser.getDeptId() != null ? deptApi.getDept(startUser.getDeptId()) : null;
                                Assert.notNull(dept, "提交人({})部门({})信息为空", processInstance.getStartUserId(), startUser.getDeptId());
                                // 找不到部门负责人的情况下，自动审批通过
                                // noinspection DataFlowIssue
                                if (dept.getLeaderUserId() == null) {
                                    getSelf().approveTaskInternal(Long.valueOf(task.getAssignee()), new BpmTaskApproveReqVO().setId(task.getId())
                                            .setReason(BpmReasonEnum.ASSIGN_START_USER_APPROVE_WHEN_DEPT_LEADER_NOT_FOUND.getReason()));
                                    return;
                                }
                                // 找得到部门负责人的情况下，修改负责人
                                if (ObjectUtil.notEqual(dept.getLeaderUserId(), startUser.getId())) {
                                    getSelf().transferTask(Long.valueOf(task.getAssignee()), new BpmTaskTransferReqVO()
                                            .setId(task.getId()).setAssigneeUserId(dept.getLeaderUserId())
                                            .setReason(BpmReasonEnum.ASSIGN_START_USER_TRANSFER_DEPT_LEADER.getReason()));
                                    return;
                                }
                                // 如果部门负责人是自己，还是自己审批吧~
                            }
                        }
                    }

                    // 发送消息
                    AdminUserRespDTO startUser = adminUserApi.getUser(Long.valueOf(processInstance.getStartUserId()));
                    messageService.sendMessageWhenTaskAssigned(BpmTaskConvert.INSTANCE.convert(processInstance, startUser, task));
                });
            }

        });
    }

    @Override
    public void processTaskCompleted(Task task) {
        ProcessInstance processInstance = processInstanceQueryService.getProcessInstance(task.getProcessInstanceId());
        if (processInstance == null) {
            log.error("[processTaskCompleted][taskId({}) 没有找到流程实例]", task.getId());
            return;
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = bpmProcessDefinitionService.
                getProcessDefinitionInfo(processInstance.getProcessDefinitionId());
        if (processDefinitionInfo == null) {
            log.error("[processTaskCompleted][processDefinitionId({}) 没有找到流程定义]", processInstance.getProcessDefinitionId());
            return;
        }

        // 任务后置通知
        if (ObjUtil.isNotNull(processDefinitionInfo.getTaskAfterTriggerSetting())) {
            BpmModelMetaInfoVO.HttpRequestSetting setting = processDefinitionInfo.getTaskAfterTriggerSetting();
            BpmHttpRequestUtils.executeBpmHttpRequest(processInstance,
                    setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTaskTimeout(String processInstanceId, String taskDefineKey, Integer handlerType) {
        ProcessInstance processInstance = processInstanceQueryService.getProcessInstance(processInstanceId);
        if (processInstance == null) {
            log.error("[processTaskTimeout][processInstanceId({}) 没有找到流程实例]", processInstanceId);
            return;
        }
        // 情况四：自动跳转。跳转的是整个节点，需要包括未认领、加签中的任务，所以不使用下面按审批人过滤后的任务，在 jumpTaskWhenTimeout 中校验
        if (Objects.equals(handlerType, BpmUserTaskTimeoutHandlerTypeEnum.AUTO_JUMP.getType())) {
            FlowableUtils.execute(processInstance.getTenantId(), () -> jumpTaskWhenTimeout(processInstance, taskDefineKey));
            return;
        }

        // 过滤【向后加签】审批通过中（APPROVING）的父任务：它已经审批通过，等待加签任务审批后自动完成，无需超时处理
        List<Task> taskList = filterList(taskQueryService.getRunningTaskListByProcessInstanceId(processInstanceId, true, taskDefineKey),
                task -> ObjectUtil.notEqual(task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_STATUS),
                        BpmTaskStatusEnum.APPROVING.getStatus()));
        // 加签说明：加签子任务与父任务的 taskDefinitionKey 相同，有审批人时一起处理；没有审批人的任务不处理，
        // 即向前加签等待中的父任务、向后加签等待父任务审批的子任务。它们在本次自动同意后才分配审批人，由审批人继续审批
        // （例如说：向前加签的子任务都自动同意后，父任务恢复给原审批人）
        if (CollUtil.isEmpty(taskList)) {
            log.error("[processTaskTimeout][processInstanceId({}) 定义Key({}) 没有找到任务]", processInstanceId, taskDefineKey);
            return;
        }

        taskList.forEach(task -> FlowableUtils.execute(task.getTenantId(), () -> {
            // 情况一：自动提醒。退回修改中的任务，审批人暂时不能处理，所以不提醒
            if (Objects.equals(handlerType, BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType())) {
                if (isWaitRevisionTask(task)) {
                    return;
                }
                messageService.sendMessageWhenTaskTimeout(new BpmMessageSendWhenTaskTimeoutReqDTO()
                        .setProcessInstanceId(processInstanceId).setProcessInstanceName(processInstance.getName())
                        .setTaskId(task.getId()).setTaskName(task.getName()).setAssigneeUserId(Long.parseLong(task.getAssignee())));
                return;
            }

            // 特殊：退回修改中的任务，自动同意、拒绝前，先结束退回修改，按节点的超时配置处理。修改人尚未重新提交的修改，不再生效
            if (isWaitRevisionTask(task) && ObjectUtils.equalsAny(handlerType,
                    BpmUserTaskTimeoutHandlerTypeEnum.APPROVE.getType(), BpmUserTaskTimeoutHandlerTypeEnum.REJECT.getType())) {
                resumeRevisionTask(task.getId());
            }
            // 情况二：自动同意
            if (Objects.equals(handlerType, BpmUserTaskTimeoutHandlerTypeEnum.APPROVE.getType())) {
                approveTask(Long.parseLong(task.getAssignee()),
                        new BpmTaskApproveReqVO().setId(task.getId()).setReason(BpmReasonEnum.TIMEOUT_APPROVE.getReason()), false);
                return;
            }

            // 情况三：自动拒绝。按常规的不通过处理，配置驳回到指定节点时直接退回，不进入退回修改，避免超时任务反复等待修改
            if (Objects.equals(handlerType, BpmUserTaskTimeoutHandlerTypeEnum.REJECT.getType())) {
                rejectTask(Long.parseLong(task.getAssignee()),
                        new BpmTaskRejectReqVO().setId(task.getId()).setReason(BpmReasonEnum.REJECT_TASK.getReason()), false);
            }
        }));
    }

    /**
     * 审批超时，自动跳转到配置的审批节点
     * <p>
     * 1. 当前节点未处理的任务（包括加签、退回修改中的任务），标记为跳过，并记录跳转的原因
     * 2. 使用 Flowable 的 ChangeActivityState，在原流程实例中跳转：结束当前节点的执行（包括多实例、超时定时器），在目标节点创建任务。
     *    流程变量、审批历史保持不变
     * <p>
     * 跳转范围见 {@link SimpleModelUtils#isValidTimeoutJump}，保存模型时已经校验。不满足时（例如说：不是 SIMPLE 设计器的流程、存在其它节点的任务），
     * 只记录日志，任务保持审批中，避免跳转后其它任务挂起、或者重复处理
     *
     * @param processInstance 流程实例
     * @param taskDefineKey   超时的任务节点
     */
    private void jumpTaskWhenTimeout(ProcessInstance processInstance, String taskDefineKey) {
        // 1.1 获得跳转的目标节点
        BpmnModel bpmnModel = modelService.getBpmnModelByDefinitionId(processInstance.getProcessDefinitionId());
        FlowElement sourceElement = BpmnModelUtils.getFlowElementById(bpmnModel, taskDefineKey);
        String jumpNodeId = BpmnModelUtils.parseTimeoutJumpNodeId(sourceElement);
        FlowElement targetElement = BpmnModelUtils.getFlowElementById(bpmnModel, jumpNodeId);
        // 1.2 校验跳转范围：仅支持 SIMPLE 设计器，与保存模型时的校验一致
        BpmProcessDefinitionInfoDO processDefinitionInfo = bpmProcessDefinitionService.getProcessDefinitionInfo(
                processInstance.getProcessDefinitionId());
        if (targetElement == null || processDefinitionInfo == null
                || ObjectUtil.notEqual(BpmModelTypeEnum.SIMPLE.getType(), processDefinitionInfo.getModelType())
                || !SimpleModelUtils.isValidTimeoutJump(JsonUtils.parseObject(processDefinitionInfo.getSimpleModel(),
                BpmSimpleModelNodeVO.class), taskDefineKey, jumpNodeId)) {
            log.error("[jumpTaskWhenTimeout][processInstanceId({}) 节点({}) 跳转的节点({})无效，不进行跳转]",
                    processInstance.getId(), taskDefineKey, jumpNodeId);
            return;
        }
        // 1.3 校验当前节点有未处理的任务（包括未认领、加签中的任务）：例如说，定时器到期前，任务刚好审批完成
        List<Task> taskList = taskQueryService.getRunningTaskListByProcessInstanceId(processInstance.getId(), null, null);
        if (!anyMatch(taskList, task -> ObjectUtil.equal(taskDefineKey, task.getTaskDefinitionKey())
                && !BpmTaskStatusEnum.isEndStatus(FlowableUtils.getTaskStatus(task)))) {
            log.error("[jumpTaskWhenTimeout][processInstanceId({}) 节点({}) 没有未处理的任务，不进行跳转]",
                    processInstance.getId(), taskDefineKey);
            return;
        }
        // 1.4 校验没有其它节点的任务：例如说，同时进行的其它分支，跳转后它们的任务会挂起，或者在聚合时重复执行后续节点
        if (anyMatch(taskList, task -> ObjectUtil.notEqual(taskDefineKey, task.getTaskDefinitionKey()))) {
            log.error("[jumpTaskWhenTimeout][processInstanceId({}) 节点({}) 存在其它节点的任务，不进行跳转]",
                    processInstance.getId(), taskDefineKey);
            return;
        }

        // 2. 当前节点未处理的任务，标记为跳过。已经结束的任务（例如说：向后加签的审批通过中的任务）保持不变，与 moveTaskToEnd 一致
        String reason = BpmReasonEnum.TIMEOUT_JUMP.format(targetElement.getName());
        taskList.forEach(task -> {
            if (BpmTaskStatusEnum.isEndStatus(FlowableUtils.getTaskStatus(task))) {
                return;
            }
            // 退回修改中的任务，先结束退回修改。修改人尚未重新提交的修改，不再生效
            if (isWaitRevisionTask(task)) {
                resumeRevisionTask(task.getId());
            }
            updateTaskStatusAndReason(task.getId(), BpmTaskStatusEnum.SKIP.getStatus(), reason);
        });

        // 3. 跳转到目标节点
        // 目标节点按正常到达处理：不设置退回标记 RETURN_FLAG，所以审批人与发起人相同等策略照常生效，与手工退回不同。
        // 同时，标记流程实例发生过跳转，审批详情改为从当前进行中的节点开始预测，见 BpmProcessInstanceQueryServiceImpl#getSimulateApproveNodeList
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstance.getId())
                .moveActivityIdsToSingleActivityId(Collections.singletonList(taskDefineKey), jumpNodeId)
                .processVariable(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_TIMEOUT_JUMPED, Boolean.TRUE)
                .changeState();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processChildProcessTimeout(String processInstanceId, String taskDefineKey) {
        List<ActivityInstance> activityInstances = runtimeService.createActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .activityId(taskDefineKey).list();
        activityInstances.forEach(activityInstance -> FlowableUtils.execute(activityInstance.getTenantId(),
                () -> moveTaskToEnd(activityInstance.getCalledProcessInstanceId(), BpmReasonEnum.TIMEOUT_APPROVE.getReason())));
    }

    @Override
    public void triggerTask(String processInstanceId, String taskDefineKey) {
        Execution execution = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .activityId(taskDefineKey)
                .singleResult();
        if (execution == null) {
            log.error("[triggerTask][processInstanceId({}) activityId({}) 没有找到执行活动]", processInstanceId, taskDefineKey);
            return;
        }

        // 若存在直接触发接收任务，执行后续节点
        FlowableUtils.execute(execution.getTenantId(),
                () -> runtimeService.trigger(execution.getId()));
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private BpmTaskUpdateServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
