package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.*;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNodeTask;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.convert.task.BpmProcessInstanceConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.dal.redis.BpmProcessIdRedisDAO;
import cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmReasonEnum;
import cn.iocoder.yudao.module.bpm.enums.task.BpmTaskStatusEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.event.BpmProcessInstanceEventPublisher;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.SimpleModelUtils;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.bpm.service.message.BpmMessageService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmApprovalDetailRespVO.ActivityNode;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.START_USER_NODE_ID;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.parseNodeType;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.flowable.bpmn.constants.BpmnXMLConstants.*;

/**
 * 流程实例 Service 实现类
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
public class BpmProcessInstanceServiceImpl implements BpmProcessInstanceService {

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private HistoryService historyService;

    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    @Lazy // 避免循环依赖
    private BpmTaskService taskService;
    @Resource
    private BpmMessageService messageService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Resource
    private BpmProcessInstanceEventPublisher processInstanceEventPublisher;

    @Resource
    private BpmTaskCandidateInvoker taskCandidateInvoker;

    @Resource
    private BpmProcessIdRedisDAO processIdRedisDAO;

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

    private Map<String, String> getFormFieldsPermission(BpmnModel bpmnModel,
                                                        String activityId, String taskId) {
        // 1. 获取流程活动编号。流程活动 Id 为空事，从流程任务中获取流程活动 Id
        if (StrUtil.isEmpty(activityId) && StrUtil.isNotEmpty(taskId)) {
            activityId = Optional.ofNullable(taskService.getHistoricTask(taskId))
                    .map(HistoricTaskInstance::getTaskDefinitionKey).orElse(null);
        }
        if (StrUtil.isEmpty(activityId)) {
            return null;
        }

        // 2. 从 BpmnModel 中解析表单字段权限
        return BpmnModelUtils.parseFormFieldsPermission(bpmnModel, activityId);
    }

    @Override
    public BpmApprovalDetailRespVO getApprovalDetail(Long loginUserId, BpmApprovalDetailReqVO reqVO) {
        // 1.1 从 reqVO 中，读取公共变量
        Long startUserId = loginUserId; // 流程发起人
        HistoricProcessInstance historicProcessInstance = null; // 流程实例
        Integer processInstanceStatus = BpmProcessInstanceStatusEnum.NOT_START.getStatus(); // 流程状态
        Map<String, Object> processVariables = new HashMap<>(); // 流程变量
        // 1.2 如果是流程已发起的场景，则使用流程实例的数据
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
        // 1.3 读取其它相关数据
        ProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(
                historicProcessInstance != null ? historicProcessInstance.getProcessDefinitionId()
                        : reqVO.getProcessDefinitionId());
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
                .getProcessDefinitionInfo(processDefinition.getId());
        BpmnModel bpmnModel = processDefinitionService.getProcessDefinitionBpmnModel(processDefinition.getId());

        // 2.1 已结束 + 进行中的活动节点
        List<ActivityNode> endActivityNodes = null; // 已结束的审批信息
        List<ActivityNode> runActivityNodes = null; // 进行中的审批信息
        List<HistoricActivityInstance> activities = null; // 流程实例列表
        if (reqVO.getProcessInstanceId() != null) {
            activities = taskService.getActivityListByProcessInstanceId(reqVO.getProcessInstanceId());
            List<HistoricTaskInstance> tasks = taskService.getTaskListByProcessInstanceId(reqVO.getProcessInstanceId(),
                    true);
            endActivityNodes = getEndActivityNodeList(startUserId, bpmnModel, processDefinitionInfo,
                    historicProcessInstance, processInstanceStatus, activities, tasks);
            runActivityNodes = getRunApproveNodeList(startUserId, bpmnModel, processDefinition, processVariables,
                    activities, tasks);
        }

        // 2.2 流程已经结束，直接 return，无需预测
        if (BpmProcessInstanceStatusEnum.isProcessEndStatus(processInstanceStatus)) {
            return buildApprovalDetail(reqVO, bpmnModel, processDefinition, processDefinitionInfo,
                    historicProcessInstance,
                    processInstanceStatus, endActivityNodes, runActivityNodes, null, null);
        }

        // 3.1 计算当前登录用户的待办任务
        BpmTaskRespVO todoTask = taskService.getTodoTask(loginUserId, reqVO.getTaskId(), reqVO.getProcessInstanceId());
        // 3.2 预测未运行节点的审批信息
        List<ActivityNode> simulateActivityNodes = getSimulateApproveNodeList(startUserId, bpmnModel,
                processDefinitionInfo,
                processVariables, activities);
        // 3.3 如果是发起动作，activityId 为开始节点，不校验审批人自选节点
        if (ObjUtil.equals(reqVO.getActivityId(), BpmnModelConstants.START_USER_NODE_ID)) {
            simulateActivityNodes.removeIf(node ->
                    BpmTaskCandidateStrategyEnum.APPROVE_USER_SELECT.getStrategy().equals(node.getCandidateStrategy()));
        }

        // 4. 拼接最终数据
        return buildApprovalDetail(reqVO, bpmnModel, processDefinition, processDefinitionInfo, historicProcessInstance,
                processInstanceStatus, endActivityNodes, runActivityNodes, simulateActivityNodes, todoTask);
    }

    @Override
    public List<ActivityNode> getNextApprovalNodes(Long loginUserId, BpmApprovalDetailReqVO reqVO) {
        // 1.1 校验任务存在，且是当前用户的
        Task task = taskService.validateTask(loginUserId, reqVO.getTaskId());
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

        // 3. 获取下一个将要执行的节点集合
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        List<FlowNode> nextFlowNodes = BpmnModelUtils.getNextFlowNodes(flowElement, bpmnModel, processVariables);
        List<ActivityNode> nextActivityNodes = convertList(nextFlowNodes, node -> new ActivityNode().setId(node.getId())
                .setName(node.getName()).setNodeType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType())
                .setStatus(BpmTaskStatusEnum.RUNNING.getStatus())
                .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(node))
                .setCandidateUserIds(getTaskCandidateUserList(bpmnModel, node.getId(),
                        loginUserId, historicProcessInstance.getProcessDefinitionId(), processVariables)));
        if (CollUtil.isNotEmpty(nextActivityNodes)) {
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
            formFieldsParams.forEach((key, value) -> {
                if (StrUtil.isEmpty(String.valueOf(value))) {
                    return;
                }
                // TODO @lesan：应支持多种类型的查询方式，目前只有字符串全等
                processInstanceQuery.variableValueEquals(key, value);
            });
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
     * 拼接审批详情的最终数据
     * <p>
     * 主要是，拼接审批人的用户信息、部门信息
     */
    private BpmApprovalDetailRespVO buildApprovalDetail(BpmApprovalDetailReqVO reqVO,
                                                        BpmnModel bpmnModel,
                                                        ProcessDefinition processDefinition,
                                                        BpmProcessDefinitionInfoDO processDefinitionInfo,
                                                        HistoricProcessInstance processInstance,
                                                        Integer processInstanceStatus,
                                                        List<ActivityNode> endApprovalNodeInfos,
                                                        List<ActivityNode> runningApprovalNodeInfos,
                                                        List<ActivityNode> simulateApprovalNodeInfos,
                                                        BpmTaskRespVO todoTask) {
        // 1. 获取所有需要读取用户信息的 userIds
        List<ActivityNode> approveNodes = newArrayList(
                asList(endApprovalNodeInfos, runningApprovalNodeInfos, simulateApprovalNodeInfos));
        Set<Long> userIds = BpmProcessInstanceConvert.INSTANCE.parseUserIds(processInstance, approveNodes, todoTask);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));

        // 2. 表单权限
        String taskId = reqVO.getTaskId() == null && todoTask != null ? todoTask.getId() : reqVO.getTaskId();
        Map<String, String> formFieldsPermission = getFormFieldsPermission(bpmnModel, reqVO.getActivityId(), taskId);

        // 3. 拼接数据
        return BpmProcessInstanceConvert.INSTANCE.buildApprovalDetail(bpmnModel, processDefinition,
                processDefinitionInfo, processInstance,
                processInstanceStatus, approveNodes, todoTask, formFieldsPermission, userMap, deptMap);
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
        List<ActivityNode> approvalNodes = convertList(endTasks, task -> {
            FlowElement flowNode = BpmnModelUtils.getFlowElementById(bpmnModel, task.getTaskDefinitionKey());
            ActivityNode activityNode = new ActivityNode().setId(task.getTaskDefinitionKey()).setName(task.getName())
                    .setNodeType(START_USER_NODE_ID.equals(task.getTaskDefinitionKey())
                            ? BpmSimpleModelNodeTypeEnum.START_USER_NODE.getType()
                            : ObjUtil.defaultIfNull(parseNodeType(flowNode), // 目的：解决“办理节点”的识别
                            BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType()))
                    .setStatus(FlowableUtils.getTaskStatus(task))
                    .setCandidateStrategy(BpmnModelUtils.parseCandidateStrategy(flowNode))
                    .setStartTime(DateUtils.of(task.getCreateTime())).setEndTime(DateUtils.of(task.getEndTime()))
                    .setTasks(singletonList(BpmProcessInstanceConvert.INSTANCE.buildApprovalTaskInfo(task)));
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
                    && BpmModelTypeEnum.BPMN.getType().equals(processDefinitionInfo.getModelType())) {
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
                        .setProcessInstanceId(activity.getProcessInstanceId());
                approvalNodes.add(callActivity);
            }
        });

        // 按照时间排序
        approvalNodes.sort(Comparator.comparing(ActivityNode::getStartTime));
        return approvalNodes;
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
                // TODO @芋艿：后续看看怎么优化！
                if (task == null) {
                    continue;
                }
                activityNode.getTasks().add(BpmProcessInstanceConvert.INSTANCE.buildApprovalTaskInfo(task));
                // 加签子任务，需要过滤掉已经完成的加签子任务
                List<HistoricTaskInstance> childrenTasks = filterList(
                        taskService.getAllChildrenTaskListByParentTaskId(activity.getTaskId(), tasks),
                        childTask -> childTask.getEndTime() == null);
                if (CollUtil.isNotEmpty(childrenTasks)) {
                    activityNode.getTasks().addAll(
                            convertList(childrenTasks, BpmProcessInstanceConvert.INSTANCE::buildApprovalTaskInfo));
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
                activityNode.setProcessInstanceId(firstActivity.getProcessInstanceId());
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
                                                          List<HistoricActivityInstance> activities) {
        // TODO @芋艿：【可优化】在驳回场景下，未来的预测准确性不高。原因是，驳回后，HistoricActivityInstance
        // 包括了历史的操作，不是只有 startEvent 到当前节点的记录
        Set<String> runActivityIds = convertSet(activities, HistoricActivityInstance::getActivityId);
        // 情况一：BPMN 设计器
        if (Objects.equals(BpmModelTypeEnum.BPMN.getType(), processDefinitionInfo.getModelType())) {
            List<FlowElement> flowElements = BpmnModelUtils.simulateProcess(bpmnModel, processVariables);
            return convertList(flowElements, flowElement -> buildNotRunApproveNodeForBpmn(startUserId, bpmnModel,
                    processDefinitionInfo, processVariables, flowElement, runActivityIds));
        }
        // 情况二：SIMPLE 设计器
        if (Objects.equals(BpmModelTypeEnum.SIMPLE.getType(), processDefinitionInfo.getModelType())) {
            BpmSimpleModelNodeVO simpleModel = JsonUtils.parseObject(processDefinitionInfo.getSimpleModel(),
                    BpmSimpleModelNodeVO.class);
            List<BpmSimpleModelNodeVO> simpleNodes = SimpleModelUtils.simulateProcess(simpleModel, processVariables);
            return convertList(simpleNodes, simpleNode -> buildNotRunApproveNodeForSimple(startUserId, bpmnModel,
                    processDefinitionInfo, processVariables, simpleNode, runActivityIds));
        }
        throw new IllegalArgumentException("未知设计器类型：" + processDefinitionInfo.getModelType());
    }

    private ActivityNode buildNotRunApproveNodeForSimple(Long startUserId, BpmnModel bpmnModel,
                                                         BpmProcessDefinitionInfoDO processDefinitionInfo, Map<String, Object> processVariables,
                                                         BpmSimpleModelNodeVO node, Set<String> runActivityIds) {
        // TODO @芋艿：【可优化】在驳回场景下，未来的预测准确性不高。原因是，驳回后，HistoricActivityInstance
        // 包括了历史的操作，不是只有 startEvent 到当前节点的记录
        if (runActivityIds.contains(node.getId())) {
            return null;
        }

        ActivityNode activityNode = new ActivityNode().setId(node.getId()).setName(node.getName())
                .setNodeType(node.getType()).setCandidateStrategy(node.getCandidateStrategy())
                .setStatus(BpmTaskStatusEnum.NOT_START.getStatus());

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

    private ActivityNode buildNotRunApproveNodeForBpmn(Long startUserId, BpmnModel bpmnModel,
                                                       BpmProcessDefinitionInfoDO processDefinitionInfo, Map<String, Object> processVariables,
                                                       FlowElement node, Set<String> runActivityIds) {
        if (runActivityIds.contains(node.getId())) {
            return null;
        }
        ActivityNode activityNode = new ActivityNode().setId(node.getId())
                .setStatus(BpmTaskStatusEnum.NOT_START.getStatus());

        // 1. 开始节点
        if (node instanceof StartEvent) {
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
        List<HistoricActivityInstance> activities = taskService.getActivityListByProcessInstanceId(id);
        List<HistoricTaskInstance> tasks = taskService.getTaskListByProcessInstanceId(id, true);

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

    // ========== Update 写入相关方法 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqVO createReqVO) {
        // 获得流程定义
        ProcessDefinition definition = processDefinitionService
                .getProcessDefinition(createReqVO.getProcessDefinitionId());
        // 发起流程
        return createProcessInstance0(userId, definition, createReqVO.getVariables(), null,
                createReqVO.getStartUserSelectAssignees());
    }

    @Override
    public String createProcessInstance(Long userId, @Valid BpmProcessInstanceCreateReqDTO createReqDTO) {
        return FlowableUtils.executeAuthenticatedUserId(userId, () -> {
            // 获得流程定义
            ProcessDefinition definition = processDefinitionService
                    .getActiveProcessDefinition(createReqDTO.getProcessDefinitionKey());
            // 发起流程
            return createProcessInstance0(userId, definition, createReqDTO.getVariables(),
                    createReqDTO.getBusinessKey(),
                    createReqDTO.getStartUserSelectAssignees());
        });
    }

    private String createProcessInstance0(Long userId, ProcessDefinition definition,
                                          Map<String, Object> variables, String businessKey,
                                          Map<String, List<Long>> startUserSelectAssignees) {
        // 1.1 校验流程定义
        if (definition == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }
        if (definition.isSuspended()) {
            throw exception(PROCESS_DEFINITION_IS_SUSPENDED);
        }
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
                .getProcessDefinitionInfo(definition.getId());
        if (processDefinitionInfo == null) {
            throw exception(PROCESS_DEFINITION_NOT_EXISTS);
        }
        // 1.2 校验是否能够发起
        if (!processDefinitionService.canUserStartProcessDefinition(processDefinitionInfo, userId)) {
            throw exception(PROCESS_INSTANCE_START_USER_CAN_START);
        }
        // 1.3 校验发起人自选审批人
        validateStartUserSelectAssignees(userId, definition, startUserSelectAssignees, variables);

        // 2. 创建流程实例
        if (variables == null) {
            variables = new HashMap<>();
        }
        FlowableUtils.filterProcessInstanceFormVariable(variables); // 过滤一下，避免 ProcessInstance 系统级的变量被占用
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, userId); // 设置流程变量，发起人 ID
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS, // 流程实例状态：审批中
                BpmProcessInstanceStatusEnum.RUNNING.getStatus());
        variables.put(BpmnVariableConstants.PROCESS_INSTANCE_SKIP_EXPRESSION_ENABLED, true); // 跳过表达式需要添加此变量为 true，不影响没配置 skipExpression 的节点
        if (CollUtil.isNotEmpty(startUserSelectAssignees)) {
            // 设置流程变量，发起人自选审批人
            variables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_SELECT_ASSIGNEES,
                    startUserSelectAssignees);
        }

        // 3. 创建流程
        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(definition.getId())
                .businessKey(businessKey)
                .variables(variables);
        // 3.1 创建流程 ID
        BpmModelMetaInfoVO.ProcessIdRule processIdRule = processDefinitionInfo.getProcessIdRule();
        if (processIdRule != null && Boolean.TRUE.equals(processIdRule.getEnable())) {
            processInstanceBuilder.predefineProcessInstanceId(processIdRedisDAO.generate(processIdRule));
        }
        // 3.2 流程名称
        BpmModelMetaInfoVO.TitleSetting titleSetting = processDefinitionInfo.getTitleSetting();
        if (titleSetting != null && Boolean.TRUE.equals(titleSetting.getEnable())) {
            AdminUserRespDTO user = adminUserApi.getUser(userId);
            Map<String, Object> cloneVariables = new HashMap<>(variables);
            cloneVariables.put(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_START_USER_ID, user.getNickname());
            cloneVariables.put(BpmnVariableConstants.PROCESS_START_TIME, DateUtil.now());
            cloneVariables.put(BpmnVariableConstants.PROCESS_DEFINITION_NAME, definition.getName().trim());
            processInstanceBuilder.name(StrUtil.format(titleSetting.getTitle(), cloneVariables));
        } else {
            processInstanceBuilder.name(definition.getName().trim());
        }
        // 3.3 发起流程实例
        ProcessInstance instance = processInstanceBuilder.start();
        return instance.getId();
    }

    private void validateStartUserSelectAssignees(Long userId, ProcessDefinition definition,
                                                  Map<String, List<Long>> startUserSelectAssignees,
                                                  Map<String, Object> variables) {
        // 1. 获取预测的节点信息
        BpmApprovalDetailRespVO detailRespVO = getApprovalDetail(userId, new BpmApprovalDetailReqVO()
                .setProcessDefinitionId(definition.getId())
                .setProcessVariables(variables));
        List<ActivityNode> activityNodes = detailRespVO.getActivityNodes();
        if (CollUtil.isEmpty(activityNodes)) {
            return;
        }

        // 2.1 移除掉不是发起人自选审批人节点
        activityNodes.removeIf(task ->
                ObjectUtil.notEqual(BpmTaskCandidateStrategyEnum.START_USER_SELECT.getStrategy(), task.getCandidateStrategy()));
        // 2.2 流程发起时要先获取当前流程的预测走向节点，发起时只校验预测的节点发起人自选审批人的审批人和抄送人是否都配置了
        activityNodes.forEach(task -> {
            List<Long> assignees = startUserSelectAssignees != null ? startUserSelectAssignees.get(task.getId()) : null;
            if (CollUtil.isEmpty(assignees)) {
                throw exception(PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG, task.getName());
            }
            Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(assignees);
            assignees.forEach(assignee -> {
                if (userMap.get(assignee) == null) {
                    throw exception(PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_EXISTS, task.getName(), assignee);
                }
            });
        });
    }

    @Override
    public void cancelProcessInstanceByStartUser(Long userId, @Valid BpmProcessInstanceCancelReqVO cancelReqVO) {
        // 1.1 校验流程实例存在
        ProcessInstance instance = getProcessInstance(cancelReqVO.getId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
        }
        // 1.2 只能取消自己的
        if (!Objects.equals(instance.getStartUserId(), String.valueOf(userId))) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF);
        }
        // 1.3 校验允许撤销审批中的申请
        BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService
                .getProcessDefinitionInfo(instance.getProcessDefinitionId());
        Assert.notNull(processDefinitionInfo, "流程定义({})不存在", processDefinitionInfo);
        if (processDefinitionInfo.getAllowCancelRunningProcess() != null // 防止未配置 AllowCancelRunningProcess , 默认为可取消
                && Boolean.FALSE.equals(processDefinitionInfo.getAllowCancelRunningProcess())) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_ALLOW);
        }
        // 1.4 子流程不允许取消
        if (StrUtil.isNotBlank(instance.getSuperExecutionId())) {
            throw exception(PROCESS_INSTANCE_CANCEL_CHILD_FAIL_NOT_ALLOW);
        }

        // 2. 取消流程
        updateProcessInstanceCancel(cancelReqVO.getId(),
                BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_START_USER.format(cancelReqVO.getReason()));
    }

    @Override
    public void cancelProcessInstanceByAdmin(Long userId, BpmProcessInstanceCancelReqVO cancelReqVO) {
        // 1.1 校验流程实例存在
        ProcessInstance instance = getProcessInstance(cancelReqVO.getId());
        if (instance == null) {
            throw exception(PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS);
        }

        // 2. 取消流程
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        updateProcessInstanceCancel(cancelReqVO.getId(),
                BpmReasonEnum.CANCEL_PROCESS_INSTANCE_BY_ADMIN.format(user.getNickname(), cancelReqVO.getReason()));
    }

    private void updateProcessInstanceCancel(String id, String reason) {
        // 1. 更新流程实例 status
        runtimeService.setVariable(id, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                BpmProcessInstanceStatusEnum.CANCEL.getStatus());
        runtimeService.setVariable(id, BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON, reason);

        // 2. 取消所有子流程
        List<ProcessInstance> childProcessInstances = runtimeService.createProcessInstanceQuery()
                .superProcessInstanceId(id).list();
        childProcessInstances.forEach(processInstance -> updateProcessInstanceCancel(
                processInstance.getProcessInstanceId(), BpmReasonEnum.CANCEL_CHILD_PROCESS_INSTANCE_BY_MAIN_PROCESS.getReason()));

        // 3. 结束流程
        taskService.moveTaskToEnd(id, reason);
    }

    @Override
    public void updateProcessInstanceReject(ProcessInstance processInstance, String reason) {
        runtimeService.setVariable(processInstance.getProcessInstanceId(),
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                BpmProcessInstanceStatusEnum.REJECT.getStatus());
        runtimeService.setVariable(processInstance.getProcessInstanceId(),
                BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON,
                BpmReasonEnum.REJECT_TASK.format(reason));
    }

    @Override
    public void updateProcessInstanceVariables(String id, Map<String, Object> variables) {
        runtimeService.setVariables(id, variables);
    }

    @Override
    public void removeProcessInstanceVariables(String id, Collection<String> variableNames) {
        runtimeService.removeVariables(id, variableNames);
    }

    // ========== Event 事件相关方法 ==========

    @Override
    public void processProcessInstanceCompleted(ProcessInstance instance) {
        // 注意：需要基于 instance 设置租户编号，避免 Flowable 内部异步时，丢失租户编号
        FlowableUtils.execute(instance.getTenantId(), () -> {
            // 1.1 获取当前状态
            Integer status = (Integer) instance.getProcessVariables()
                    .get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS);
            String reason = (String) instance.getProcessVariables()
                    .get(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_REASON);
            // 1.2 当流程状态还是审批状态中，说明审批通过了，则变更下它的状态
            // 为什么这么处理？因为流程完成，并且完成了，说明审批通过了
            if (Objects.equals(status, BpmProcessInstanceStatusEnum.RUNNING.getStatus())) {
                status = BpmProcessInstanceStatusEnum.APPROVE.getStatus();
                runtimeService.setVariable(instance.getId(), BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS,
                        status);
            }

            // 2. 发送对应的消息通知
            if (Objects.equals(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
                messageService.sendMessageWhenProcessInstanceApprove(
                        BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceApproveMessage(instance));
            } else if (Objects.equals(status, BpmProcessInstanceStatusEnum.REJECT.getStatus())) {
                messageService.sendMessageWhenProcessInstanceReject(
                        BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceRejectMessage(instance, reason));
            }

            // 3. 发送流程实例的状态事件
            processInstanceEventPublisher.sendProcessInstanceResultEvent(
                    BpmProcessInstanceConvert.INSTANCE.buildProcessInstanceStatusEvent(this, instance, status));

            // 4. 流程后置通知
            if (Objects.equals(status, BpmProcessInstanceStatusEnum.APPROVE.getStatus())) {
                BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.
                        getProcessDefinitionInfo(instance.getProcessDefinitionId());
                if (ObjUtil.isNotNull(processDefinitionInfo) &&
                        ObjUtil.isNotNull(processDefinitionInfo.getProcessAfterTriggerSetting())) {
                    BpmModelMetaInfoVO.HttpRequestSetting setting = processDefinitionInfo.getProcessAfterTriggerSetting();

                    BpmHttpRequestUtils.executeBpmHttpRequest(instance,
                            setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
                }
            }
        });
    }

    @Override
    public void processProcessInstanceCreated(ProcessInstance instance) {
        // 注意：需要基于 instance 设置租户编号，避免 Flowable 内部异步时，丢失租户编号
        FlowableUtils.execute(instance.getTenantId(), () -> {
            // 流程前置通知
            BpmProcessDefinitionInfoDO processDefinitionInfo = processDefinitionService.
                    getProcessDefinitionInfo(instance.getProcessDefinitionId());
            if (ObjUtil.isNull(processDefinitionInfo) ||
                    ObjUtil.isNull(processDefinitionInfo.getProcessBeforeTriggerSetting())) {
                return;
            }
            BpmModelMetaInfoVO.HttpRequestSetting setting = processDefinitionInfo.getProcessBeforeTriggerSetting();
            BpmHttpRequestUtils.executeBpmHttpRequest(instance,
                    setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
        });
    }

}
