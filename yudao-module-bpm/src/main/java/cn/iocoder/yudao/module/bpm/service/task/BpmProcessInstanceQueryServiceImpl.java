package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.*;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNodeTask;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.convert.task.BpmProcessInstanceConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmFieldPermissionEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmAttachmentTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceFormFieldQueryTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Attachment;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.BiFunction;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNode;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.parseNodeType;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.flowable.bpmn.constants.BpmnXMLConstants.*;

/**
 * 流程实例【读】Service 实现类
 * <p>
 * ProcessDefinition & ProcessInstance & Execution & Task 的关系：
 * 1. <a href="https://blog.csdn.net/bobozai86/article/details/105210414" />
 * <p>
 * HistoricProcessInstance & ProcessInstance 的关系：
 * 1. <a href=" https://my.oschina.net/843294669/blog/71902" />
 * <p>
 * 简单来说，前者 = 历史 + 运行中的流程实例，后者仅是运行中的流程实例
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class BpmProcessInstanceQueryServiceImpl implements BpmProcessInstanceQueryService {

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;

    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    @Lazy // 避免循环依赖
    private BpmTaskQueryService taskQueryService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;

    // ========== Query 查询相关方法 ==========

    @Override
    public ProcessInstance getProcessInstance(String id) {
        return runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(id)
                .singleResult();
    }

    @Override
    public List<ProcessInstance> getProcessInstances(Set<String> ids) {
        return runtimeService.createProcessInstanceQuery().processInstanceIds(ids).includeProcessVariables().list();
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String id) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(id).includeProcessVariables()
                .singleResult();
    }

    @Override
    public List<HistoricProcessInstance> getHistoricProcessInstances(Set<String> ids) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceIds(ids).includeProcessVariables()
                .list();
    }

    /**
     * 获得当前登录用户的表单字段权限
     *
     * @param loginUserId     当前登录用户编号
     * @param bpmnModel       流程模型
     * @param reqVO           审批详情请求
     * @param processInstance 流程实例；为 null 时，表示流程未发起
     * @param todoTask        当前登录用户的待办任务
     * @param revisionTask    当前登录用户的退回修改中的任务
     * @return 表单字段权限
     */
    private Map<String, String> getFormFieldsPermission(Long loginUserId, BpmnModel bpmnModel,
                                                        BpmApprovalDetailReqVO reqVO,
                                                        HistoricProcessInstance processInstance,
                                                        BpmTaskRespVO todoTask, Task revisionTask) {
        // 情况一：有退回修改中的任务，以退回目标节点的表单权限为准，可编辑。和重新提交时的校验一致：
        // 与工作流保留变量同名的字段不能修改，所以将“可编辑”降级为“只读”，避免前端提交
        if (revisionTask != null) {
            Map<String, String> fieldsPermission = BpmnModelUtils.parseFormFieldsPermission(bpmnModel,
                    (String) revisionTask.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REVISION_TARGET_NODE));
            if (fieldsPermission != null) {
                fieldsPermission.replaceAll((field, permission) -> FlowableUtils.isReservedVariable(field, bpmnModel)
                        ? downgradeWritePermission(permission) : permission);
            }
            return fieldsPermission;
        }
        // 情况二：有待办任务，以待办任务所在节点的表单权限为准，可编辑
        if (todoTask != null) {
            return BpmnModelUtils.parseFormFieldsPermission(bpmnModel, todoTask.getTaskDefinitionKey());
        }
        // 情况三：流程未发起，使用传递的流程活动（例如说，发起人节点）的表单权限，可编辑
        if (processInstance == null) {
            return BpmnModelUtils.parseFormFieldsPermission(bpmnModel, reqVO.getActivityId());
        }

        // 情况四：流程已发起，但没有待办任务，只能查看
        // 3.1 优先，使用自己办理过的任务（已办）所在节点
        String activityId = getMyHistoricTaskDefinitionKey(loginUserId, reqVO.getTaskId(), processInstance.getId());
        // 3.2 其次，使用传递的流程活动（例如说，抄送节点）
        if (StrUtil.isEmpty(activityId)) {
            activityId = reqVO.getActivityId();
        }
        // 3.3 最后，如果是流程发起人（例如说，从“我的流程”进入），使用发起人节点
        if (StrUtil.isEmpty(activityId) && ObjUtil.equal(String.valueOf(loginUserId), processInstance.getStartUserId())) {
            activityId = START_USER_NODE_ID;
        }
        // 3.4 其它参与者未传递 taskId 时，使用自己最近审批通过的任务所在节点，避免看到该节点隐藏的字段
        // 补充说明：传递了其它流程实例、或者其它人的 taskId 时，不使用，与原逻辑一致
        if (StrUtil.isEmpty(activityId) && StrUtil.isEmpty(reqVO.getTaskId())) {
            activityId = getMyApprovedTaskDefinitionKey(loginUserId, processInstance.getId());
        }
        // 没有待办任务，无法提交表单，所以将“可编辑”降级为“只读”，避免误导
        return downgradeWritePermission(BpmnModelUtils.parseFormFieldsPermission(bpmnModel, activityId));
    }

    /**
     * 获得用户在指定流程实例中，办理过的任务所在的节点编号
     * <p>
     * 1. 优先，传递的 taskId 任务
     * 2. 其次，传递的 taskId 是该流程实例中退回修改过的任务（即【待我修改】的链接），但不是该用户办理的任务时，
     *    使用该用户最近审批通过的任务，见 {@link #getMyApprovedTaskDefinitionKey(Long, String)}。
     *    例如说：修改链接过期、或者重新提交后，避免看到之前隐藏的字段
     *
     * @param userId            用户编号
     * @param taskId            任务编号
     * @param processInstanceId 流程实例编号
     * @return 节点编号；任务不存在、不属于该流程实例，或者找不到该用户办理的任务时，返回 null
     */
    private String getMyHistoricTaskDefinitionKey(Long userId, String taskId, String processInstanceId) {
        if (StrUtil.isEmpty(taskId)) {
            return null;
        }
        HistoricTaskInstance task = taskQueryService.getHistoricTask(taskId);
        if (task == null || ObjUtil.notEqual(task.getProcessInstanceId(), processInstanceId)) {
            return null;
        }
        // 1. 优先，传递的 taskId 任务，是该用户办理（审批人或拥有人）的任务
        String userIdStr = String.valueOf(userId);
        if (ObjUtil.equal(userIdStr, task.getAssignee()) || ObjUtil.equal(userIdStr, task.getOwner())) {
            return task.getTaskDefinitionKey();
        }
        // 2. 其次，退回修改过的任务的链接，使用最近审批通过的任务
        if (!task.getTaskLocalVariables().containsKey(BpmnVariableConstants.TASK_VARIABLE_REVISION_ID)) {
            return null;
        }
        return getMyApprovedTaskDefinitionKey(userId, processInstanceId);
    }

    /**
     * 获得用户在指定流程实例中，最近审批通过的任务所在的节点编号
     * <p>
     * 只使用审批通过的任务：被取消、退回的任务，没有提交过表单；排除加签的子任务，它们与父任务共用节点
     *
     * @param userId            用户编号
     * @param processInstanceId 流程实例编号
     * @return 节点编号；没有时，返回 null
     */
    private String getMyApprovedTaskDefinitionKey(Long userId, String processInstanceId) {
        // 最近：按审批完成时间（endTime）最大；时间相同时，按编号排序，保证稳定。不按创建时间，因为先创建的任务可能后审批
        HistoricTaskInstance task = taskQueryService.getTaskListByProcessInstanceId(processInstanceId, false).stream()
                .filter(item -> item.getParentTaskId() == null && ObjUtil.equal(String.valueOf(userId), item.getAssignee())
                        && BpmTaskStatusEnum.APPROVE.getStatus().equals(FlowableUtils.getTaskStatus(item)))
                .max(Comparator.comparing(HistoricTaskInstance::getEndTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(HistoricTaskInstance::getId))
                .orElse(null);
        return task != null ? task.getTaskDefinitionKey() : null;
    }

    /**
     * 将表单字段权限中的“可编辑”降级为“只读”
     *
     * @param fieldsPermission 表单字段权限
     * @return 表单字段权限
     */
    private static Map<String, String> downgradeWritePermission(Map<String, String> fieldsPermission) {
        if (CollUtil.isEmpty(fieldsPermission)) {
            return fieldsPermission;
        }
        fieldsPermission.replaceAll((field, permission) -> downgradeWritePermission(permission));
        return fieldsPermission;
    }

    private static String downgradeWritePermission(String permission) {
        return String.valueOf(BpmFieldPermissionEnum.WRITE.getPermission()).equals(permission)
                ? String.valueOf(BpmFieldPermissionEnum.READ.getPermission()) : permission;
    }

    @Override
    public BpmApprovalDetailRespVO getApprovalDetail(Long loginUserId, BpmApprovalDetailReqVO reqVO) {
        // 1.1 从 reqVO 中，读取公共变量
        Long startUserId = loginUserId; // 流程发起人
        HistoricProcessInstance historicProcessInstance = null; // 流程实例
        Integer processInstanceStatus = BpmProcessInstanceStatusEnum.NOT_START.getStatus(); // 流程状态
        Map<String, Object> processVariables = new HashMap<>(); // 流程变量
        // 1.2 先读取当前登录用户的待办任务、退回修改中的任务，再读取流程变量
        // 为什么？重新提交会同时修改流程变量、任务的批次，先读任务再读变量，避免读到“修改前的表单 + 新的批次”。
        // 反之，读到“修改后的表单 + 旧的批次”时，提交会因为批次不一致被拒绝
        BpmTaskRespVO todoTask = null; // 当前登录用户的待办任务
        Task revisionTask = null; // 当前登录用户的退回修改中的任务：传递 taskId 时（例如说，从【待我修改】进入），只使用该任务
        if (reqVO.getProcessInstanceId() != null) {
            todoTask = taskQueryService.getTodoTask(loginUserId, reqVO.getTaskId(), reqVO.getProcessInstanceId());
            revisionTask = taskQueryService.getRevisionTask(loginUserId, reqVO.getProcessInstanceId(), reqVO.getTaskId());
        }
        // 1.3 如果是流程已发起的场景，则使用流程实例的数据
        if (reqVO.getProcessInstanceId() != null) {
            historicProcessInstance = getHistoricProcessInstance(reqVO.getProcessInstanceId());
            if (historicProcessInstance == null) {
                throw exception(ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS);
            }
            startUserId = Long.valueOf(historicProcessInstance.getStartUserId());
            processInstanceStatus = FlowableUtils.getProcessInstanceStatus(historicProcessInstance);
            // 合并 DB 和前端传递的流量变量，以前端的为主
            if (CollUtil.isNotEmpty(historicProcessInstance.getProcessVariables())) {
                processVariables.putAll(historicProcessInstance.getProcessVariables());
            }
        }
        if (CollUtil.isNotEmpty(reqVO.getProcessVariables())) {
            processVariables.putAll(reqVO.getProcessVariables());
        }
        // 特殊：如果是未发起的场景，则设置发起用户，解决“发起流程”时，需要使用到该变量的问题。例如说：https://t.zsxq.com/fMw5g
        if (historicProcessInstance == null) {
            processVariables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, loginUserId);
        }
        // 1.4 读取其它相关数据
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(
                historicProcessInstance != null ? historicProcessInstance.getProcessDefinitionId()
                        : reqVO.getProcessDefinitionId());
        if (processDefinition == null) {
            throw exception(ErrorCodeConstants.PROCESS_DEFINITION_NOT_EXISTS);
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
                .getProcessDefinitionInfo(processDefinition.getId());
        BpmnModel bpmnModel = processDefinitionService.getProcessDefinitionBpmnModel(processDefinition.getId());

        // 2.1 已结束 + 进行中的活动节点
        List<ActivityNode> endActivityNodes = null; // 已结束的审批信息
        List<ActivityNode> runActivityNodes = null; // 进行中的审批信息
        List<HistoricActivityInstance> activities = null; // 流程实例列表
        if (reqVO.getProcessInstanceId() != null) {
            activities = taskQueryService.getActivityListByProcessInstanceId(reqVO.getProcessInstanceId());
            List<HistoricTaskInstance> tasks = taskQueryService.getTaskListByProcessInstanceId(reqVO.getProcessInstanceId(),
                    true);
            endActivityNodes = getEndActivityNodeList(startUserId, bpmnModel, processDefinitionInfo,
                    historicProcessInstance, processInstanceStatus, activities, tasks);
            runActivityNodes = getRunApproveNodeList(startUserId, bpmnModel, processDefinition, processVariables,
                    activities, tasks);
        }

        // 2.2 流程已经结束，直接 return，无需预测
        if (BpmProcessInstanceStatusEnum.isProcessEndStatus(processInstanceStatus)) {
            return buildApprovalDetail(loginUserId, reqVO, bpmnModel, processDefinition, processDefinitionInfo,
                    historicProcessInstance,
                    processInstanceStatus, endActivityNodes, runActivityNodes, null, null, null);
        }

        // 3.1 当前登录用户的待办任务、退回修改中的任务，已经在 1.2 中读取

        // 3.2 获取由于退回操作，需要预测的节点。从流程变量中获取，回退操作会设置这些变量
        Set<String> needSimulateTaskDefKeysByReturn = new HashSet<>();
        // 是否从进行中的节点开始预测：实际发生过手工退回（会设置需要预测的节点的变量，即使之后被审批移除为空）、审批超时【自动跳转】。
        // 从流程实例本身读取，不使用前端传递的流程变量，避免预览时误用
        boolean simulateAfterRunningNodes = false;
        if (StrUtil.isNotEmpty(reqVO.getProcessInstanceId())) {
            Object needSimulateTaskIds = runtimeService.getVariable(reqVO.getProcessInstanceId(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_NEED_SIMULATE_TASK_IDS);
            needSimulateTaskDefKeysByReturn.addAll(Convert.toSet(String.class, needSimulateTaskIds));
            simulateAfterRunningNodes = needSimulateTaskIds != null || BooleanUtil.isTrue(Convert.toBool(runtimeService.getVariable(
                    reqVO.getProcessInstanceId(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_TIMEOUT_JUMPED)));
        }
        // 移除运行中的节点，运行中的节点无需预测
        if (CollUtil.isNotEmpty(runActivityNodes)) {
            runActivityNodes.forEach( activityNode -> needSimulateTaskDefKeysByReturn.remove(activityNode.getId()));
        }

        // 3.3 预测未运行节点的审批信息
        List<ActivityNode> simulateActivityNodes = getSimulateApproveNodeList(startUserId, bpmnModel,
                processDefinitionInfo,
                processVariables, activities, needSimulateTaskDefKeysByReturn, simulateAfterRunningNodes);

        // 4. 拼接最终数据
        return buildApprovalDetail(loginUserId, reqVO, bpmnModel, processDefinition, processDefinitionInfo, historicProcessInstance,
                processInstanceStatus, endActivityNodes, runActivityNodes, simulateActivityNodes, todoTask, revisionTask);
    }

    @Override
    public List<ActivityNode> getNextApprovalNodes(Long loginUserId, BpmApprovalDetailReqVO reqVO) {
        // 1.1 校验任务存在，且是当前用户的
        Task task = taskQueryService.validateTask(loginUserId, reqVO.getTaskId());
        // 1.2 校验流程实例存在
        ProcessInstance instance = getProcessInstance(task.getProcessInstanceId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }
        HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(task.getProcessInstanceId());
        if (historicProcessInstance == null) {
            throw exception(ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS);
        }
        // 1.3 校验BpmnModel
        BpmnModel bpmnModel = processDefinitionService.getProcessDefinitionBpmnModel(task.getProcessDefinitionId());
        if (bpmnModel == null) {
            return null;
        }

        // 2. 设置流程变量
        Map<String, Object> processVariables = new HashMap<>();
        // 2.1 获取历史中流程变量
        if (CollUtil.isNotEmpty(historicProcessInstance.getProcessVariables())) {
            processVariables.putAll(historicProcessInstance.getProcessVariables());
        }
        // 2.2 合并前端传递的流程变量，以前端为准
        if (CollUtil.isNotEmpty(reqVO.getProcessVariables())) {
            processVariables.putAll(reqVO.getProcessVariables());
        }

        // 3.1 获取下一个将要执行的节点集合
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        // 3.2 获取 UserTask 节点
        List<UserTask> nextUserTaskList = BpmnModelUtils.getNextUserTasks(flowElement, bpmnModel, processVariables);
        List<ActivityNode> nextActivityNodes = convertList(nextUserTaskList, node -> new ActivityNode().setId(node.getId())
                .setName(node.getName()).setNodeType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                .setStatus(BpmTaskStatusEnum.RUNNING.getStatus())
                .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(node))
                .setCandidateUserIds(getTaskCandidateUserList(bpmnModel, node.getId(),
                        loginUserId, historicProcessInstance.getProcessDefinitionId(), processVariables)));
        if (CollUtil.isEmpty(nextActivityNodes)) {
            return nextActivityNodes;
        }

        // 4. 拼接基础信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(nextActivityNodes, ActivityNode::getCandidateUserIds, Collection::stream));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        nextActivityNodes.forEach(node -> node.setCandidateUsers(convertList(node.getCandidateUserIds(), userId -> {
            AdminUserRespDTO user = userMap.get(userId);
            if (user != null) {
                return BpmProcessInstanceConvert.INSTANCE.buildUser(userId, userMap, deptMap);
            }
            return null;
        })));
        return nextActivityNodes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PageResult<HistoricProcessInstance> getProcessInstancePage(Long userId,
                                                                      BpmProcessInstancePageReqVO pageReqVO) {
        // 1. 构建查询条件
        HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceTenantId(FlowableUtils.getTenantId())
                .orderByProcessInstanceStartTime().desc();
        if (userId != null) { // 【我的流程】菜单时，需要传递该字段
            processInstanceQuery.startedBy(String.valueOf(userId));
        } else if (pageReqVO.getStartUserId() != null) { // 【管理流程】菜单时，才会传递该字段
            processInstanceQuery.startedBy(String.valueOf(pageReqVO.getStartUserId()));
        }
        if (StrUtil.isNotEmpty(pageReqVO.getName())) {
            processInstanceQuery.processInstanceNameLike("%" + pageReqVO.getName() + "%");
        }
        if (StrUtil.isNotEmpty(pageReqVO.getProcessDefinitionKey())) {
            processInstanceQuery.processDefinitionKey(pageReqVO.getProcessDefinitionKey());
        }
        if (StrUtil.isNotEmpty(pageReqVO.getCategory())) {
            processInstanceQuery.processDefinitionCategory(pageReqVO.getCategory());
        }
        if (pageReqVO.getStatus() != null) {
            processInstanceQuery.variableValueEquals(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                    pageReqVO.getStatus());
        }
        if (ArrayUtil.isNotEmpty(pageReqVO.getCreateTime())) {
            processInstanceQuery.startedAfter(DateUtils.of(pageReqVO.getCreateTime()[0]));
            processInstanceQuery.startedBefore(DateUtils.of(pageReqVO.getCreateTime()[1]));
        }
        if (ArrayUtil.isNotEmpty(pageReqVO.getEndTime())) {
            processInstanceQuery.finishedAfter(DateUtils.of(pageReqVO.getEndTime()[0]));
            processInstanceQuery.finishedBefore(DateUtils.of(pageReqVO.getEndTime()[1]));
        }
        // 表单字段查询
        Map<String, Object> formFieldsParams = JsonUtils.parseObject(pageReqVO.getFormFieldsParams(), Map.class);
        if (CollUtil.isNotEmpty(formFieldsParams)) {
            formFieldsParams.forEach((key, value) -> buildFormFieldQuery(processInstanceQuery, key, value));
        }

        // 2.1 查询数量
        long processInstanceCount = processInstanceQuery.count();
        if (processInstanceCount == 0) {
            return PageResult.empty(processInstanceCount);
        }
        // 2.2 查询列表
        List<HistoricProcessInstance> processInstanceList = processInstanceQuery.listPage(PageUtils.getStart(pageReqVO),
                pageReqVO.getPageSize());
        return new PageResult<>(processInstanceList, processInstanceCount);
    }

    /**
     * 构建表单字段的查询条件，均通过 Flowable 的流程变量条件，在数据库中查询
     * <p>
     * 1. 值为标量时，按全等匹配
     * 2. 值为 {op, value} 时，按 op 查询，见 {@link BpmProcessInstanceFormFieldQueryTypeEnum}。范围的两端均包含，其中一端为空时只限制另一端
     * 补充说明：值、范围的两端都为空时，不进行筛选
     *
     * @param query 流程实例查询
     * @param key   表单字段
     * @param value 查询值
     */
    @SuppressWarnings("unchecked")
    private static void buildFormFieldQuery(HistoricProcessInstanceQuery query, String key, Object value) {
        // 情况一：全等匹配
        if (!(value instanceof Map)) {
            if (value != null && StrUtil.isNotEmpty(String.valueOf(value))) {
                query.variableValueEquals(key, value);
            }
            return;
        }
        Map<String, Object> condition = (Map<String, Object>) value;
        Object op = condition.get("op");
        Object conditionValue = condition.get("value");
        BpmProcessInstanceFormFieldQueryTypeEnum queryType = BpmProcessInstanceFormFieldQueryTypeEnum.of(Convert.toStr(op));
        if (queryType == null) {
            throw invalidParamException("表单字段({}) 的查询方式({}) 不支持", key, op);
        }
        // 情况二：模糊匹配，只匹配字符串类型的变量
        // 注意：查询值中的 %、_ 作为 LIKE 通配符，不进行转义（与流程名称的模糊查询一致）
        if (queryType == BpmProcessInstanceFormFieldQueryTypeEnum.LIKE) {
            if (conditionValue != null && !(conditionValue instanceof String)) {
                throw invalidParamException("表单字段({}) 的模糊查询值必须是字符串", key);
            }
            if (StrUtil.isNotEmpty((String) conditionValue)) {
                query.variableValueLike(key, "%" + conditionValue + "%");
            }
            return;
        }
        // 数字范围、日期区间：参数形状均为 [开始, 结束]
        List<?> range = parseFormFieldRange(key, conditionValue);
        if (range == null) {
            return;
        }
        // 情况三：数字范围
        if (queryType == BpmProcessInstanceFormFieldQueryTypeEnum.NUMBER_RANGE) {
            for (int i = 0; i < range.size(); i++) {
                Object number = range.get(i);
                if (number == null) {
                    continue;
                }
                if (!(number instanceof Number)) {
                    throw invalidParamException("表单字段({}) 的数字范围必须是数字", key);
                }
                addFormFieldNumberBound(query, key, new BigDecimal(number.toString()), i == 0);
            }
            return;
        }
        // 情况四：日期区间。日期组件（date、datetime 类型）的值，存储为 yyyy-MM-dd、yyyy-MM-dd HH:mm:ss（可能带毫秒）格式的字符串，
        // 按字符串比较即为时间顺序；上界使用 < 结束日期的下一天，从而包含结束日期当天的所有日期时间（包括毫秒），并排除下一天的零点
        LocalDate startDate = parseFormFieldDate(key, range.get(0));
        LocalDate endDate = parseFormFieldDate(key, range.get(1));
        if (startDate != null) {
            query.variableValueGreaterThanOrEqual(key, startDate.toString());
        }
        if (endDate != null) {
            query.variableValueLessThan(key, endDate.plusDays(1).toString());
        }
    }

    /**
     * 解析表单字段的范围查询值：必须是 [开始, 结束]；为空时，返回 null，表示不进行筛选
     */
    private static List<?> parseFormFieldRange(String key, Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> range) || range.size() != 2) {
            throw invalidParamException("表单字段({}) 的范围查询值必须是 [开始, 结束]", key);
        }
        return range;
    }

    /**
     * 添加表单字段的数字边界条件
     * <p>
     * 表单的数字，经 JSON 反序列化后为 Integer、Long、Double，Flowable 分别存储为 integer、long、double 类型的变量，
     * 并且只比较相同类型的变量。所以，通过 or 匹配任一类型；每个流程实例的同一字段只有一个变量，所以上、下边界的 or 组合后，即为该变量在范围内
     *
     * @param query 流程实例查询
     * @param key   表单字段
     * @param value 边界值
     * @param lower 是否为下边界
     */
    private static void addFormFieldNumberBound(HistoricProcessInstanceQuery query, String key,
                                                BigDecimal value, boolean lower) {
        // 1. 下边界使用 >=，上边界使用 <=，两端均包含
        BiFunction<String, Object, HistoricProcessInstanceQuery> condition = lower
                ? query::variableValueGreaterThanOrEqual : query::variableValueLessThanOrEqual;
        // 2. 整数类型的边界：小数向范围内取整，例如说 >= 1.5 即 >= 2，<= 1.5 即 <= 1
        BigDecimal integerValue = value.setScale(0, lower ? RoundingMode.CEILING : RoundingMode.FLOOR);
        query.or();
        // 2.1 integer 类型：超出 int 范围时钳制到极值；该类型没有满足的值时，不添加条件
        Long intBound = NumberUtils.getIntegerBound(integerValue, lower, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (intBound != null) {
            condition.apply(key, intBound.intValue());
        }
        // 2.2 long 类型：同上
        Long longBound = NumberUtils.getIntegerBound(integerValue, lower, Long.MIN_VALUE, Long.MAX_VALUE);
        if (longBound != null) {
            condition.apply(key, longBound);
        }
        // 3. double 类型：使用原始边界值，不取整
        condition.apply(key, value.doubleValue());
        query.endOr();
    }

    /**
     * 解析表单字段的日期区间：只支持 yyyy-MM-dd 格式；为空时，返回 null
     */
    private static LocalDate parseFormFieldDate(String key, Object date) {
        if (StrUtil.isBlankIfStr(date)) {
            return null;
        }
        if (date instanceof String dateStr) {
            try {
                return LocalDateTimeUtil.parseDate(dateStr, DatePattern.NORM_DATE_PATTERN);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw invalidParamException("表单字段({}) 的日期({}) 必须是 yyyy-MM-dd 格式", key, date);
    }

    /**
     * 拼接审批详情的最终数据
     * <p>
     * 主要是，拼接审批人的用户信息、部门信息
     */
    private BpmApprovalDetailRespVO buildApprovalDetail(Long loginUserId,
                                                        BpmApprovalDetailReqVO reqVO,
                                                        BpmnModel bpmnModel,
                                                        ProcessDefinition processDefinition,
                                                        BpmProcessDefinitionInfoDO processDefinitionInfo,
                                                        HistoricProcessInstance processInstance,
                                                        Integer processInstanceStatus,
                                                        List<ActivityNode> endApprovalNodeInfos,
                                                        List<ActivityNode> runningApprovalNodeInfos,
                                                        List<ActivityNode> simulateApprovalNodeInfos,
                                                        BpmTaskRespVO todoTask,
                                                        Task revisionTask) {
        // 1. 获取所有需要读取用户信息的 userIds
        List<ActivityNode> approveNodes = newArrayList(
                asList(endApprovalNodeInfos, runningApprovalNodeInfos, simulateApprovalNodeInfos));
        Set<Long> userIds = BpmProcessInstanceConvert.INSTANCE.parseUserIds(processInstance, approveNodes, todoTask);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));

        // 2. 表单权限
        Map<String, String> formFieldsPermission = getFormFieldsPermission(loginUserId, bpmnModel, reqVO,
                processInstance, todoTask, revisionTask);

        // 3. 拼接数据
        BpmApprovalDetailRespVO respVO = BpmProcessInstanceConvert.INSTANCE.buildApprovalDetail(bpmnModel, processDefinition,
                processDefinitionInfo, processInstance,
                processInstanceStatus, approveNodes, todoTask, formFieldsPermission, userMap, deptMap);
        if (revisionTask != null) {
            respVO.setRevisionTaskId(revisionTask.getId()).setRevisionId((String) revisionTask.getTaskLocalVariables()
                    .get(BpmnVariableConstants.TASK_VARIABLE_REVISION_ID));
        }
        return respVO;
    }

    /**
     * 获得【已结束】的活动节点们
     */
    private List<ActivityNode> getEndActivityNodeList(Long startUserId, BpmnModel bpmnModel,
                                                      BpmProcessDefinitionInfoDO processDefinitionInfo,
                                                      HistoricProcessInstance historicProcessInstance, Integer processInstanceStatus,
                                                      List<HistoricActivityInstance> activities, List<HistoricTaskInstance> tasks) {
        // 遍历 tasks 列表，只处理已结束的 UserTask
        // 为什么不通过 activities 呢？因为，加签场景下，它只存在于 tasks，没有 activities，导致如果遍历 activities 的话，它无法成为一个节点
        List<HistoricTaskInstance> endTasks = filterList(tasks, task -> task.getEndTime() != null);
        // 获取已完成节点的附件
        Set<String> endTaskIds = convertSet(endTasks, HistoricTaskInstance::getId);
        List<Attachment> attachments = taskQueryService.getAttachments(historicProcessInstance.getId(), endTaskIds, BpmAttachmentTypeEnum.TASK_ATTACHMENT);
        Map<String, List<Attachment>> taskAttachmentMap = convertMultiMap(attachments, Attachment::getTaskId);
        List<ActivityNode> approvalNodes = convertList(endTasks, task -> {
            FlowElement flowNode = BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
            List<Attachment> taskAttachments = taskAttachmentMap.get(task.getId());
            ActivityNode activityNode = new ActivityNode().setId(task.getTaskDefinitionKey()).setName(task.getName())
                    .setNodeType(START_USER_NODE_ID.equals(task.getTaskDefinitionKey())
                            ? BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType()
                            : ObjUtil.defaultIfNull(parseNodeType(flowNode), // 目的：解决“办理节点”的识别
                            BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType()))
                    .setStatus(getEndActivityNodeStatus(task))
                    .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(flowNode))
                    .setStartTime(DateUtils.of(task.getCreateTime())).setEndTime(DateUtils.of(task.getEndTime()))
                    .setTasks(singletonList(BpmProcessInstanceConvert.INSTANCE.buildApprovalTaskInfo(task, taskAttachments)));
            // 如果是取消状态，则跳过
            if (BpmTaskStatusEnum.isCancelStatus(activityNode.getStatus())) {
                return null;
            }
            return activityNode;
        });

        // 遍历 activities，只处理已结束的 StartEvent、EndEvent
        List<HistoricActivityInstance> endActivities = filterList(activities, activity -> activity.getEndTime() != null
                && (StrUtil.equalsAny(activity.getActivityType(), ELEMENT_EVENT_START, ELEMENT_CALL_ACTIVITY, ELEMENT_EVENT_END)));
        endActivities.forEach(activity -> {
            // StartEvent：只处理 BPMN 的场景。因为，SIMPLE 情况下，已经有 START_USER_NODE 节点
            if (ELEMENT_EVENT_START.equals(activity.getActivityType())
                    && BpmModelTypeEnum.BPMN.getType().equals(processDefinitionInfo.getModelType())
                    && !CollUtil.contains(activities, // 特殊：如果已经存在用户手动创建的 START_USER_NODE_ID 节点，则忽略 StartEvent
                    historicActivity -> historicActivity.getActivityId().equals(START_USER_NODE_ID))) {
                ActivityNodeTask startTask = new ActivityNodeTask().setId(BpmnModelConstants.START_USER_NODE_ID)
                        .setAssignee(startUserId).setStatus(BpmTaskStatusEnum.APPROVE.getStatus());
                ActivityNode startNode = new ActivityNode().setId(startTask.getId())
                        .setName(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getName())
                        .setNodeType(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType())
                        .setStatus(startTask.getStatus()).setTasks(ListUtil.of(startTask))
                        .setStartTime(DateUtils.of(activity.getStartTime()))
                        .setEndTime(DateUtils.of(activity.getEndTime()));
                approvalNodes.add(0, startNode);
                return;
            }
            // EndEvent
            if (ELEMENT_EVENT_END.equals(activity.getActivityType())) {
                if (BpmProcessInstanceStatusEnum.isRejectStatus(processInstanceStatus)) {
                    // 拒绝情况下，不需要展示 EndEvent 结束节点。原因是：前端已经展示 x 效果，无需重复展示
                    return;
                }
                ActivityNode endNode = new ActivityNode().setId(activity.getId())
                        .setName(BpmSimpleModelNodeTypeEnum.END_NODE.getName())
                        .setNodeType(BpmSimpleModelNodeTypeEnum.END_NODE.getType()).setStatus(processInstanceStatus)
                        .setStartTime(DateUtils.of(activity.getStartTime()))
                        .setEndTime(DateUtils.of(activity.getEndTime()));
                String reason = FlowableUtils.getProcessInstanceReason(historicProcessInstance);
                if (StrUtil.isNotEmpty(reason)) {
                    endNode.setTasks(singletonList(new ActivityNodeTask().setId(endNode.getId())
                            .setStatus(endNode.getStatus()).setReason(reason)));
                }
                approvalNodes.add(endNode);
            }
            // CallActivity
            if (ELEMENT_CALL_ACTIVITY.equals(activity.getActivityType())) {
                ActivityNode callActivity = new ActivityNode().setId(activity.getId())
                        .setName(BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getName())
                        .setNodeType(BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getType()).setStatus(processInstanceStatus)
                        .setStartTime(DateUtils.of(activity.getStartTime()))
                        .setEndTime(DateUtils.of(activity.getEndTime()))
                        .setProcessInstanceId(activity.getCalledProcessInstanceId());
                approvalNodes.add(callActivity);
            }
        });

        // 按照时间排序
        approvalNodes.sort(Comparator.comparing(ActivityNode::getStartTime));
        return approvalNodes;
    }

    /**
     * 获取结束节点的状态
     */
    private Integer getEndActivityNodeStatus(HistoricTaskInstance task) {
        Integer status = FlowableUtils.getTaskStatus(task);
        if (status != null) {
            return status;
        }
        // 结束节点未获取到状态，为跳过状态。可见 bpmn 或者 simple 的 skipExpression
        return BpmTaskStatusEnum.SKIP.getStatus();
    }

    /**
     * 获得【进行中】的活动节点们
     */
    private List<ActivityNode> getRunApproveNodeList(Long startUserId,
                                                     BpmnModel bpmnModel,
                                                     ProcessDefinition processDefinition,
                                                     Map<String, Object> processVariables,
                                                     List<HistoricActivityInstance> activities,
                                                     List<HistoricTaskInstance> tasks) {
        // 构建运行中的任务、子流程，基于 activityId 分组
        List<HistoricActivityInstance> runActivities = filterList(activities, activity -> activity.getEndTime() == null
                && (StrUtil.equalsAny(activity.getActivityType(), ELEMENT_TASK_USER, ELEMENT_CALL_ACTIVITY)));
        Map<String, List<HistoricActivityInstance>> runningTaskMap = convertMultiMap(runActivities,
                HistoricActivityInstance::getActivityId);

        // 按照 activityId 分组，构建 ApprovalNodeInfo 节点
        Map<String, HistoricTaskInstance> taskMap = convertMap(tasks, HistoricTaskInstance::getId);
        return convertList(runningTaskMap.entrySet(), entry -> {
            String activityId = entry.getKey();
            List<HistoricActivityInstance> taskActivities = entry.getValue();
            // 构建活动节点
            FlowElement flowNode = BpmnModelUtils.getFlowElementById(bpmnModel, activityId);
            HistoricActivityInstance firstActivity = CollUtil.getFirst(taskActivities); // 取第一个任务，会签/或签的任务，开始时间相同
            ActivityNode activityNode = new ActivityNode().setId(firstActivity.getActivityId())
                    .setName(firstActivity.getActivityName())
                    .setNodeType(ObjUtil.defaultIfNull(parseNodeType(flowNode), // 目的：解决“办理节点”和"子流程"的识别
                            BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType()))
                    .setStatus(BpmTaskStatusEnum.RUNNING.getStatus())
                    .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(flowNode))
                    .setStartTime(DateUtils.of(CollUtil.getFirst(taskActivities).getStartTime()))
                    .setTasks(new ArrayList<>());
            // 处理每个任务的 tasks 属性
            for (HistoricActivityInstance activity : taskActivities) {
                HistoricTaskInstance task = taskMap.get(activity.getTaskId());
                // 特殊情况：子流程节点 ChildProcess 仅存在于 activity 中，并且没有自身的 task，需要跳过执行
                // 补充说明：此时只展示节点本身，并在下面设置子流程实例编号（processInstanceId），子流程的任务通过它查看
                if (task == null) {
                    continue;
                }
                ActivityNodeTask taskInfo = BpmProcessInstanceConvert.INSTANCE.buildApprovalTaskInfo(task, null);
                // 如果是已退回，等待处理人修改状态，获取回退目标节点和处理人信息用于展示
                if (BpmTaskStatusEnum.WAIT_REVISION.getStatus().equals(taskInfo.getStatus())) {
                    String targetId = (String) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REVISION_TARGET_NODE);
                    String assignee = (String) task.getTaskLocalVariables().get(BpmnVariableConstants.TASK_VARIABLE_REVISION_ASSIGNEE);
                    FlowElement target = BpmnModelUtils.getFlowElementById(bpmnModel, targetId);
                    taskInfo.setRevisionTargetNode(targetId).setRevisionTargetNodeName(target != null ? target.getName() : targetId)
                            .setRevisionAssignee(NumberUtils.parseLong(assignee));
                }
                activityNode.getTasks().add(taskInfo);
                // 加签子任务，需要过滤掉已经完成的加签子任务
                List<HistoricTaskInstance> childrenTasks = filterList(
                        taskQueryService.getAllChildrenTaskListByParentTaskId(activity.getTaskId(), tasks),
                        childTask -> childTask.getEndTime() == null);
                if (CollUtil.isNotEmpty(childrenTasks)) {
                    activityNode.getTasks().addAll(
                            convertList(childrenTasks, item->BpmProcessInstanceConvert.INSTANCE.buildApprovalTaskInfo(item, null)));
                }
            }
            // 处理每个任务的 candidateUsers 属性：如果是依次审批，需要预测它的后续审批人。因为 Task 是审批完一个，创建一个新的 Task
            if (BpmnModelUtils.isSequentialUserTask(flowNode)) {
                List<Long> candidateUserIds = getTaskCandidateUserList(bpmnModel, flowNode.getId(),
                        startUserId, processDefinition.getId(), processVariables);
                // 截取当前审批人位置后面的候选人，不包含当前审批人
                ActivityNodeTask approvalTaskInfo = CollUtil.getFirst(activityNode.getTasks());
                Assert.notNull(approvalTaskInfo, "任务不能为空");
                int index = CollUtil.indexOf(candidateUserIds,
                        userId -> ObjectUtils.equalsAny(userId, approvalTaskInfo.getOwner(),
                                approvalTaskInfo.getAssignee())); // 委派或者向前加签情况，需要先比较 owner
                activityNode.setCandidateUserIds(CollUtil.sub(candidateUserIds, index + 1, candidateUserIds.size()));
            }
            if (BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getType().equals(activityNode.getNodeType())) {
                activityNode.setProcessInstanceId(firstActivity.getCalledProcessInstanceId());
            }
            return activityNode;
        });
    }

    /**
     * 获得【预测（未来）】的活动节点们
     */
    private List<ActivityNode> getSimulateApproveNodeList(Long startUserId, BpmnModel bpmnModel,
                                                          BpmProcessDefinitionInfoDO processDefinitionInfo,
                                                          Map<String, Object> processVariables,
                                                          List<HistoricActivityInstance> activities,
                                                          Set<String> needSimulateTaskDefKeysByReturn,
                                                          boolean simulateAfterRunningNodes) {
        Set<String> runActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId);
        // 情况一：BPMN 设计器
        if (Objects.equals(BpmModelTypeEnum.BPMN.getType(), processDefinitionInfo.getModelType())) {
            // 特殊：手工退回、审批超时【自动跳转】过的流程实例，从进行中的节点开始预测，见 BpmnModelUtils#simulateProcessAfterElements
            // 原因：HistoricActivityInstance 包括了历史的操作，进行中的节点之后的节点即使执行过（例如说，多次退回后，走了之前的分支），
            // 也会再次执行，需要预测；退回时记录的节点，只包括最近一次退回目标之后执行过的节点，不够
            List<FlowElement> afterElements = simulateAfterRunningNodes ? BpmnModelUtils.simulateProcessAfterElements(bpmnModel,
                    convertList(filterList(activities, activity -> activity.getEndTime() == null),
                            HistoricActivityInstance::getActivityId), processVariables) : null;
            // 正常情况（或者进行中的节点在流程模型中都不存在时），从 StartEvent 开始预测
            List<FlowElement> flowElements = afterElements != null ? afterElements
                    : BpmnModelUtils.simulateProcess(bpmnModel, processVariables);
            // 从进行中的节点开始预测时，预测的节点都会再次执行，即使历史中执行过，也需要预测
            Set<String> needSimulateTaskDefKeys = afterElements != null ? convertSet(afterElements, FlowElement::getId)
                    : needSimulateTaskDefKeysByReturn;
            return convertList(flowElements, flowElement -> buildNotRunApproveNodeForBpmn(
                    startUserId, bpmnModel, flowElements,
                    processDefinitionInfo, processVariables, flowElement, runActivityIds, needSimulateTaskDefKeys));
        }
        // 情况二：SIMPLE 设计器
        if (Objects.equals(BpmModelTypeEnum.SIMPLE.getType(), processDefinitionInfo.getModelType())) {
            BpmSimpleModelNodeVO simpleModel = JsonUtils.parseObject(processDefinitionInfo.getSimpleModel(),
                    BpmSimpleModelNodeVO.class);
            // 特殊：手工退回、审批超时【自动跳转】过的流程实例，不再按流程变量从头执行，从当前进行中的节点开始预测
            if (simulateAfterRunningNodes) {
                return getSimulateApproveNodeListAfterRunningNodes(startUserId, bpmnModel, processDefinitionInfo,
                        processVariables, activities, simpleModel);
            }
            List<BpmSimpleModelNodeVO> simpleNodes = SimpleModelUtils.simulateProcess(simpleModel, processVariables);
            return convertList(simpleNodes, simpleNode -> buildNotRunApproveNodeForSimple(
                    startUserId, bpmnModel,
                    processDefinitionInfo, processVariables, simpleNode, runActivityIds, needSimulateTaskDefKeysByReturn));
        }
        throw new IllegalArgumentException("未知设计器类型：" + processDefinitionInfo.getModelType());
    }

    /**
     * 获得手工退回、审批超时【自动跳转】过的 SIMPLE 流程实例，【预测（未来）】的活动节点们
     * <p>
     * 跨条件分支跳转后，流程实例不再按流程变量从头执行：从头预测会展示已经绕过的节点，漏掉跳转目标之后的节点；
     * 退回、往回跳过已经执行的节点（包括并行分支）时，它们会因为执行过而不展示。所以，只按实际进行中的节点预测它们之后的节点，
     * 见 {@link SimpleModelUtils#simulateProcessAfterNodes}。这些节点都会再次执行，所以即使执行过，也需要展示
     * <p>
     * 进行中的节点都不在模型中时（例如说，数据异常），无法可靠预测，不展示未来的节点，避免展示错误的分支
     */
    private List<ActivityNode> getSimulateApproveNodeListAfterRunningNodes(Long startUserId, BpmnModel bpmnModel,
                                                                          BpmProcessDefinitionInfoDO processDefinitionInfo,
                                                                          Map<String, Object> processVariables,
                                                                          List<HistoricActivityInstance> activities,
                                                                          BpmSimpleModelNodeVO simpleModel) {
        // 1. 获得进行中（未结束）的节点
        Set<String> runningActivityIds = convertSet(filterList(activities, activity -> activity.getEndTime() == null),
                HistoricActivityInstance::getActivityId);
        // 2. 从进行中的节点开始，预测之后的节点；无法预测时，不展示未来的节点
        List<BpmSimpleModelNodeVO> simpleNodes = SimpleModelUtils.simulateProcessAfterNodes(simpleModel,
                runningActivityIds, processVariables);
        if (CollUtil.isEmpty(simpleNodes)) {
            log.warn("[getSimulateApproveNodeListAfterRunningNodes][定义({}) 进行中的节点({}) 无法预测，不展示未来的节点]",
                    processDefinitionInfo.getProcessDefinitionId(), runningActivityIds);
            return Collections.emptyList();
        }
        // 3. 构建预测的活动节点：预测的节点都会再次执行，所以作为需要预测的节点，即使执行过，也展示
        Set<String> needSimulateTaskDefKeys = convertSet(simpleNodes, BpmSimpleModelNodeVO::getId);
        return convertList(simpleNodes, simpleNode -> buildNotRunApproveNodeForSimple(
                startUserId, bpmnModel, processDefinitionInfo, processVariables, simpleNode,
                runningActivityIds, needSimulateTaskDefKeys));
    }

    private ActivityNode buildNotRunApproveNodeForSimple(Long startUserId, BpmnModel bpmnModel,
                                                         BpmProcessDefinitionInfoDO processDefinitionInfo, Map<String, Object> processVariables,
                                                         BpmSimpleModelNodeVO node, Set<String> runActivityIds,
                                                         Set<String> needSimulateTaskDefKeysByReturn) {
        // 已经执行过的节点，不需要预测。特殊：需要预测的节点（例如说，从进行中的节点开始预测时，之后的节点会再次执行），即使执行过，也需要预测
        if (runActivityIds.contains(node.getId())
                && !needSimulateTaskDefKeysByReturn.contains(node.getId())) {
            return null;
        }
        Integer status = BpmTaskStatusEnum.NOT_START.getStatus();
        // 如果节点被跳过。设置状态为跳过
        if (SimpleModelUtils.isSkipNode(node, processVariables)) {
            status = BpmTaskStatusEnum.SKIP.getStatus();
        }
        ActivityNode activityNode = new ActivityNode().setId(node.getId()).setName(node.getName())
                .setNodeType(node.getType()).setCandidateStrategy(node.getCandidateStrategy())
                .setStatus(status);

        // 1. 开始节点/审批节点
        if (ObjectUtils.equalsAny(node.getType(),
                BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType(),
                BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType(),
                BpmSimpleModelNodeTypeEnum.TRANSACTOR_NODE.getType())) {
            List<Long> candidateUserIds = getTaskCandidateUserList(bpmnModel, node.getId(),
                    startUserId, processDefinitionInfo.getProcessDefinitionId(), processVariables);
            activityNode.setCandidateUserIds(candidateUserIds);
            return activityNode;
        }

        // 2. 结束节点
        if (BpmSimpleModelNodeTypeEnum.END_NODE.getType().equals(node.getType())) {
            return activityNode;
        }

        // 3. 抄送节点
        if (CollUtil.isEmpty(runActivityIds) && // 流程发起时：需要展示抄送节点，用于选择抄送人
                BpmSimpleModelNodeTypeEnum.COPY_NODE.getType().equals(node.getType())) {
            List<Long> candidateUserIds = getTaskCandidateUserList(bpmnModel, node.getId(),
                    startUserId, processDefinitionInfo.getProcessDefinitionId(), processVariables);
            activityNode.setCandidateUserIds(candidateUserIds);
            return activityNode;
        }

        // 4. 子流程节点
        if (BpmSimpleModelNodeTypeEnum.CHILD_PROCESS.getType().equals(node.getType())) {
            return activityNode;
        }
        return null;
    }

    private ActivityNode buildNotRunApproveNodeForBpmn(Long startUserId, BpmnModel bpmnModel, List<FlowElement> flowElements,
                                                       BpmProcessDefinitionInfoDO processDefinitionInfo,
                                                       Map<String, Object> processVariables,
                                                       FlowElement node, Set<String> runActivityIds,
                                                       Set<String> needSimulateTaskDefKeysByReturn) {
        // 回退操作时候，会记录需要预测的节点到流程变量中。即使节点在历史操作中，也需要预测。
        if (!needSimulateTaskDefKeysByReturn.contains(node.getId()) && runActivityIds.contains(node.getId())) {
            return null;
        }

        Integer status = BpmTaskStatusEnum.NOT_START.getStatus();
        // 如果节点被跳过，状态设置为跳过
        if (BpmnModelUtils.isSkipNode(node, processVariables)) {
            status = BpmTaskStatusEnum.SKIP.getStatus();
        }
        ActivityNode activityNode = new ActivityNode().setId(node.getId())
                .setStatus(status);

        // 1. 开始节点
        if (node instanceof StartEvent) {
            if (CollUtil.contains(flowElements, // 特殊：如果已经存在用户手动创建的 START_USER_NODE_ID 节点，则忽略 StartEvent
                    flowElement -> flowElement.getId().equals(START_USER_NODE_ID))) {
                return null;
            }
            return activityNode.setName(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getName())
                    .setNodeType(BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType());
        }

        // 2. 审批节点
        if (node instanceof UserTask) {
            List<Long> candidateUserIds = getTaskCandidateUserList(bpmnModel, node.getId(),
                    startUserId, processDefinitionInfo.getProcessDefinitionId(), processVariables);
            return activityNode.setName(node.getName()).setNodeType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                    .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(node))
                    .setCandidateUserIds(candidateUserIds);
        }

        // 3. 结束节点
        if (node instanceof EndEvent) {
            return activityNode.setName(BpmSimpleModelNodeTypeEnum.END_NODE.getName())
                    .setNodeType(BpmSimpleModelNodeTypeEnum.END_NODE.getType());
        }
        return null;
    }

    private List<Long> getTaskCandidateUserList(BpmnModel bpmnModel, String activityId,
                                                Long startUserId, String processDefinitionId, Map<String, Object> processVariables) {
        Set<Long> userIds = taskCandidateInvoker.calculateUsersByActivity(bpmnModel, activityId,
                startUserId, processDefinitionId, processVariables);
        return new ArrayList<>(userIds);
    }

    @Override
    public BpmProcessInstanceBpmnModelViewRespVO getProcessInstanceBpmnModelView(String id) {
        // 1.1 获得流程实例
        HistoricProcessInstance processInstance = getHistoricProcessInstance(id);
        if (processInstance == null) {
            return null;
        }
        // 1.2 获得流程定义
        BpmnModel bpmnModel = processDefinitionService
                .getProcessDefinitionBpmnModel(processInstance.getProcessDefinitionId());
        if (bpmnModel == null) {
            return null;
        }
        BpmSimpleModelNodeVO simpleModel = null;
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.getProcessDefinitionInfo(
                processInstance.getProcessDefinitionId());
        if (processDefinitionInfo != null
                && BpmModelTypeEnum.SIMPLE.getType().equals(processDefinitionInfo.getModelType())) {
            simpleModel = JsonUtils.parseObject(processDefinitionInfo.getSimpleModel(), BpmSimpleModelNodeVO.class);
        }
        // 1.3 获得流程实例对应的活动实例列表 + 任务列表
        List<HistoricActivityInstance> activities = taskQueryService.getActivityListByProcessInstanceId(id);
        List<HistoricTaskInstance> tasks = taskQueryService.getTaskListByProcessInstanceId(id, true);

        // 2.1 拼接进度信息
        Set<String> unfinishedTaskActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId,
                activityInstance -> activityInstance.getEndTime() == null);
        Set<String> finishedTaskActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId,
                activityInstance -> activityInstance.getEndTime() != null
                        && ObjectUtil.notEqual(activityInstance.getActivityType(),
                        BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW));
        Set<String> finishedSequenceFlowActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId,
                activityInstance -> activityInstance.getEndTime() != null
                        && ObjectUtil.equals(activityInstance.getActivityType(),
                        BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW));
        // 特殊：会签情况下，会有部分已完成（审批）、部分未完成（待审批），此时需要 finishedTaskActivityIds 移除掉
        finishedTaskActivityIds.removeAll(unfinishedTaskActivityIds);
        // 特殊：如果流程实例被拒绝，则需要计算是哪个活动节点。
        // 注意，只取最后一个。因为会存在多次拒绝的情况，拒绝驳回到指定节点
        Set<String> rejectTaskActivityIds = CollUtil.newHashSet();
        if (BpmProcessInstanceStatusEnum.isRejectStatus(FlowableUtils.getProcessInstanceStatus(processInstance))) {
            tasks.stream()
                    .filter(task -> BpmTaskStatusEnum.isRejectStatus(FlowableUtils.getTaskStatus(task)))
                    .max(Comparator.comparing(HistoricTaskInstance::getEndTime))
                    .ifPresent(reject -> rejectTaskActivityIds.add(reject.getTaskDefinitionKey()));
            finishedTaskActivityIds.removeAll(rejectTaskActivityIds);
        }

        // 2.2 拼接基础信息
        Set<Long> userIds = BpmProcessInstanceConvert.INSTANCE.parseUserIds02(processInstance, tasks);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceBpmnModelView(processInstance, tasks, bpmnModel,
                simpleModel,
                unfinishedTaskActivityIds, finishedTaskActivityIds, finishedSequenceFlowActivityIds,
                rejectTaskActivityIds,
                userMap, deptMap);
    }

}
