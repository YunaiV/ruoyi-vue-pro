package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO.ConditionGroups;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnVariableConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmCopyTaskDelegate;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.listener.BpmTriggerTaskDelegate;
import cn.iocoder.yudao.module.bpm.service.task.listener.BpmCallActivityListener;
import cn.iocoder.yudao.module.bpm.service.task.listener.BpmUserTaskListener;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.*;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils.*;
import static java.util.Arrays.asList;

/**
 * 仿钉钉/飞书的模型相关的工具方法
 * <p>
 * 1. 核心的逻辑实现，可见 {@link #buildBpmnModel(String, String, BpmSimpleModelNodeVO)} 方法
 * 2. 所有的 BpmSimpleModelNodeVO 转换成 BPMN FlowNode 元素，可见 {@link NodeConvert} 实现类
 *
 * @author jason
 */
public class SimpleModelUtils {

    private static final Map<BpmSimpleModelNodeTypeEnum, NodeConvert> NODE_CONVERTS = MapUtil.newHashMap();

    static {
        List<NodeConvert> converts = asList(new StartNodeConvert(), new EndNodeConvert(),
                new StartUserNodeConvert(), new ApproveNodeConvert(), new CopyNodeConvert(), new TransactorNodeConvert(),
                new DelayTimerNodeConvert(), new TriggerNodeConvert(),
                new ConditionBranchNodeConvert(), new ParallelBranchNodeConvert(), new InclusiveBranchNodeConvert(), new RouteBranchNodeConvert(),
                new ChildProcessConvert());
        converts.forEach(convert -> NODE_CONVERTS.put(convert.getType(), convert));
    }

    /**
     * 仿钉钉流程设计模型数据结构（json）转换成 Bpmn Model
     * <p>
     * 整体逻辑如下：
     * 1. 创建：BpmnModel、Process 对象
     * 2. 转换：将 BpmSimpleModelNodeVO 转换成 BPMN FlowNode 元素
     * 3. 连接：构建并添加节点之间的连线 Sequence Flow
     *
     * @param processId       流程标识
     * @param processName     流程名称
     * @param simpleModelNode 仿钉钉流程设计模型数据结构
     * @return Bpmn Model
     */
    public static BpmnModel buildBpmnModel(String processId, String processName, BpmSimpleModelNodeVO simpleModelNode) {
        // 1. 创建 BpmnModel
        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.setTargetNamespace(BpmnXMLConstants.BPMN2_NAMESPACE); // 设置命名空间。不加这个，解析 Message 会报 NPE 异常
        // 创建 Process 对象
        Process process = new Process();
        process.setId(processId);
        process.setName(processName);
        process.setExecutable(Boolean.TRUE);
        bpmnModel.addProcess(process);

        // 2.1 创建 StartNode 节点
        // 原因是：目前前端的第一个节点是“发起人节点”，所以这里构建一个 StartNode，用于创建 Bpmn 的 StartEvent 节点
        BpmSimpleModelNodeVO startNode = buildStartNode();
        startNode.setChildNode(simpleModelNode);
        // 2.2 将前端传递的 simpleModelNode 数据结构（json），转换成从 BPMN FlowNode 元素，并添加到 Main Process 中
        traverseNodeToBuildFlowNode(startNode, process);

        // 3. 构建并添加节点之间的连线 Sequence Flow
        EndEvent endEvent = getEndEvent(bpmnModel);
        traverseNodeToBuildSequenceFlow(process, startNode, endEvent.getId());

        // 4. 自动布局
        new BpmnAutoLayout(bpmnModel).execute();
        return bpmnModel;
    }

    private static BpmSimpleModelNodeVO buildStartNode() {
        return new BpmSimpleModelNodeVO().setId(START_EVENT_NODE_ID)
                .setName(BpmSimpleModelNodeTypeEnum.START_NODE.getName())
                .setType(BpmSimpleModelNodeTypeEnum.START_NODE.getType());
    }

    /**
     * 遍历节点，构建 FlowNode 元素
     *
     * @param node    SIMPLE 节点
     * @param process BPMN 流程
     */
    private static void traverseNodeToBuildFlowNode(BpmSimpleModelNodeVO node, Process process) {
        // 1. 判断是否有效节点
        if (!isValidNode(node)) {
            return;
        }
        BpmSimpleModelNodeTypeEnum nodeType = BpmSimpleModelNodeTypeEnum.valueOf(node.getType());
        Assert.notNull(nodeType, "模型节点类型({})不支持", node.getType());

        // 2. 处理当前节点
        NodeConvert nodeConvert = NODE_CONVERTS.get(nodeType);
        Assert.notNull(nodeConvert, "模型节点类型的转换器({})不存在", node.getType());
        List<? extends FlowElement> flowElements = nodeConvert.convertList(node);
        flowElements.forEach(process::addFlowElement);

        // 3.1 情况一：如果当前是分支节点，并且存在条件节点，则处理每个条件的子节点
        if (BpmSimpleModelNodeTypeEnum.isBranchNode(node.getType())
                && CollUtil.isNotEmpty(node.getConditionNodes())) {
            // 注意：这里的 item.getChildNode() 处理的是每个条件的子节点，不是处理条件
            node.getConditionNodes().forEach(item -> traverseNodeToBuildFlowNode(item.getChildNode(), process));
        }

        // 3.2 情况二：如果有“子”节点，则递归处理子节点
        traverseNodeToBuildFlowNode(node.getChildNode(), process);
    }

    /**
     * 遍历节点，构建 SequenceFlow 元素
     *
     * @param process      Bpmn 流程
     * @param node         当前节点
     * @param targetNodeId 目标节点 ID
     */
    private static void traverseNodeToBuildSequenceFlow(Process process, BpmSimpleModelNodeVO node, String targetNodeId) {
        // 1.1 无效节点返回
        if (!isValidNode(node)) {
            return;
        }
        // 1.2 END_NODE 直接返回
        BpmSimpleModelNodeTypeEnum nodeType = BpmSimpleModelNodeTypeEnum.valueOf(node.getType());
        Assert.notNull(nodeType, "模型节点类型不支持");
        if (nodeType == BpmSimpleModelNodeTypeEnum.END_NODE) {
            return;
        }

        // 2.1 情况一：普通节点
        if (!BpmSimpleModelNodeTypeEnum.isBranchNode(node.getType())) {
            traverseNormalNodeToBuildSequenceFlow(process, node, targetNodeId);
        } else {
            // 2.2 情况二：分支节点
            traverseBranchNodeToBuildSequenceFlow(process, node, targetNodeId);
        }
    }

    /**
     * 遍历普通（非条件）节点，构建 SequenceFlow 元素
     *
     * @param process      Bpmn 流程
     * @param node         当前节点
     * @param targetNodeId 目标节点 ID
     */
    private static void traverseNormalNodeToBuildSequenceFlow(Process process, BpmSimpleModelNodeVO node, String targetNodeId) {
        BpmSimpleModelNodeVO childNode = node.getChildNode();
        boolean isChildNodeValid = isValidNode(childNode);
        // 情况一：有“子”节点，则建立连线
        // 情况二：没有“子节点”，则直接跟 targetNodeId 建立连线。例如说，结束节点、条件分支（分支节点的孩子节点或聚合节点）的最后一个节点
        String finalTargetNodeId = isChildNodeValid ? childNode.getId() : targetNodeId;

        // 如果没有附加节点：则直接建立连线
        if (StrUtil.isEmpty(node.getAttachNodeId())) {
            SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), finalTargetNodeId);
            process.addFlowElement(sequenceFlow);
        } else {
            // 如果有附加节点：需要先建立和附加节点的连线，再建立附加节点和目标节点的连线。例如说，触发器节点（HTTP 回调）
            List<SequenceFlow> sequenceFlows = buildAttachNodeSequenceFlow(node.getId(), node.getAttachNodeId(), finalTargetNodeId);
            sequenceFlows.forEach(process::addFlowElement);
        }

        // 因为有子节点，递归调用后续子节点
        if (isChildNodeValid) {
            traverseNodeToBuildSequenceFlow(process, childNode, targetNodeId);
        }
    }

    /**
     * 构建有附加节点的连线
     *
     * @param nodeId       当前节点 ID
     * @param attachNodeId 附属节点 ID
     * @param targetNodeId 目标节点 ID
     */
    private static List<SequenceFlow> buildAttachNodeSequenceFlow(String nodeId, String attachNodeId, String targetNodeId) {
        SequenceFlow sequenceFlow = buildBpmnSequenceFlow(nodeId, attachNodeId, null, null, null);
        SequenceFlow attachSequenceFlow = buildBpmnSequenceFlow(attachNodeId, targetNodeId, null, null, null);
        return CollUtil.newArrayList(sequenceFlow, attachSequenceFlow);
    }

    /**
     * 遍历条件节点，构建 SequenceFlow 元素
     *
     * @param process      Bpmn 流程
     * @param node         当前节点
     * @param targetNodeId 目标节点 ID
     */
    private static void traverseBranchNodeToBuildSequenceFlow(Process process, BpmSimpleModelNodeVO node, String targetNodeId) {
        BpmSimpleModelNodeTypeEnum nodeType = BpmSimpleModelNodeTypeEnum.valueOf(node.getType());
        BpmSimpleModelNodeVO childNode = node.getChildNode();
        List<BpmSimpleModelNodeVO> conditionNodes = node.getConditionNodes();
        // TODO @芋艿 路由分支没有conditionNodes 这里注释会影响吗？@jason：一起帮忙瞅瞅！
//        Assert.notEmpty(conditionNodes, "分支节点的条件节点不能为空");
        // 分支终点节点 ID
        String branchEndNodeId = null;
        if (nodeType == BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE
                || nodeType == BpmSimpleModelNodeTypeEnum.ROUTER_BRANCH_NODE) { // 条件分支或路由分支
            // 分两种情况 1. 分支节点有孩子节点为孩子节点 Id 2. 分支节点孩子为无效节点时 (分支嵌套且为分支最后一个节点) 为分支终点节点 ID
            branchEndNodeId = isValidNode(childNode) ? childNode.getId() : targetNodeId;
        } else if (nodeType == BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE
                || nodeType == BpmSimpleModelNodeTypeEnum.INCLUSIVE_BRANCH_NODE) {  // 并行分支或包容分支
            // 分支节点：分支终点节点 Id 为程序创建的网关集合节点。目前不会从前端传入。
            branchEndNodeId = buildGatewayJoinId(node.getId());
        }
        Assert.notEmpty(branchEndNodeId, "分支终点节点 Id 不能为空");

        // 3. 遍历分支节点
        if (nodeType == BpmSimpleModelNodeTypeEnum.ROUTER_BRANCH_NODE) {
            // 路由分支遍历
            for (BpmSimpleModelNodeVO.RouterSetting router : node.getRouterGroups()) {
                SequenceFlow sequenceFlow = RouteBranchNodeConvert.buildSequenceFlow(node.getId(), router);
                process.addFlowElement(sequenceFlow);
            }
        } else {
            // 下面的注释，以如下情况举例子。分支 1：A->B->C->D->E，分支 2：A->D->E。其中，A 为分支节点, D 为 A 孩子节点
            for (BpmSimpleModelNodeVO item : conditionNodes) {
                Assert.isTrue(Objects.equals(item.getType(), BpmSimpleModelNodeTypeEnum.CONDITION_NODE.getType()),
                        "条件节点类型({})不符合", item.getType());
                BpmSimpleModelNodeVO conditionChildNode = item.getChildNode();
                // 3.1 分支有后续节点。即分支 1: A->B->C->D 的情况
                if (isValidNode(conditionChildNode)) {
                    // 3.1.1 建立与后续的节点的连线。例如说，建立 A->B 的连线
                    SequenceFlow sequenceFlow = ConditionNodeConvert.buildSequenceFlow(node.getId(), conditionChildNode.getId(), nodeType, item);
                    process.addFlowElement(sequenceFlow);
                    // 3.1.2 递归调用后续节点连线。例如说，建立 B->C->D 的连线
                    traverseNodeToBuildSequenceFlow(process, conditionChildNode, branchEndNodeId);
                } else {
                    // 3.2 分支没有后续节点。例如说，建立 A->D 的连线
                    SequenceFlow sequenceFlow = ConditionNodeConvert.buildSequenceFlow(node.getId(), branchEndNodeId, nodeType, item);
                    process.addFlowElement(sequenceFlow);
                }
            }
        }

        // 4.1 如果是并行分支、包容分支，由于是程序创建的聚合网关，需要手工创建聚合网关和下一个节点的连线
        if (nodeType == BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE
                || nodeType == BpmSimpleModelNodeTypeEnum.INCLUSIVE_BRANCH_NODE) {
            String nextNodeId = isValidNode(childNode) ? childNode.getId() : targetNodeId;
            SequenceFlow sequenceFlow = buildBpmnSequenceFlow(branchEndNodeId, nextNodeId);
            process.addFlowElement(sequenceFlow);
            // 4.2 如果是路由分支，需要连接后续节点为默认路由
        } else if (nodeType == BpmSimpleModelNodeTypeEnum.ROUTER_BRANCH_NODE) {
            SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), branchEndNodeId, node.getRouterDefaultFlowId(),
                    null, null);
            process.addFlowElement(sequenceFlow);
        }

        // 5. 递归调用后续节点 继续递归。例如说，建立 D->E 的连线
        traverseNodeToBuildSequenceFlow(process, childNode, targetNodeId);
    }

    private static SequenceFlow buildBpmnSequenceFlow(String sourceId, String targetId) {
        return buildBpmnSequenceFlow(sourceId, targetId, null, null, null);
    }

    private static SequenceFlow buildBpmnSequenceFlow(String sourceId, String targetId,
                                                      String sequenceFlowId, String sequenceFlowName,
                                                      String conditionExpression) {
        Assert.notEmpty(sourceId, "sourceId 不能为空");
        Assert.notEmpty(targetId, "targetId 不能为空");
        // TODO @jason：如果 sequenceFlowId 不存在的时候，是不是要生成一个默认的 sequenceFlowId？ @芋艿： 貌似不需要,Flowable 会默认生成；TODO @jason：建议还是搞一个，主要是后续好排查问题。
        // TODO @jason：如果 name 不存在的时候，是不是要生成一个默认的 name？ @芋艿： 不需要生成默认的吧？ 这个会在流程图展示的， 一般用户填写的。不好生成默认的吧；TODO @jason：建议还是搞一个，主要是后续好排查问题。
        SequenceFlow sequenceFlow = new SequenceFlow(sourceId, targetId);
        if (StrUtil.isNotEmpty(sequenceFlowId)) {
            sequenceFlow.setId(sequenceFlowId);
        }
        if (StrUtil.isNotEmpty(sequenceFlowName)) {
            sequenceFlow.setName(sequenceFlowName);
        }
        if (StrUtil.isNotEmpty(conditionExpression)) {
            sequenceFlow.setConditionExpression(conditionExpression);
        }
        return sequenceFlow;
    }

    public static boolean isValidNode(BpmSimpleModelNodeVO node) {
        return node != null && node.getId() != null;
    }

    public static boolean isSequentialApproveNode(BpmSimpleModelNodeVO node) {
        return BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType().equals(node.getType())
                && BpmUserTaskApproveMethodEnum.SEQUENTIAL.getMethod().equals(node.getApproveMethod());
    }

    // ========== 各种 convert 节点的方法: BpmSimpleModelNodeVO => BPMN FlowElement ==========

    private interface NodeConvert {

        default List<? extends FlowElement> convertList(BpmSimpleModelNodeVO node) {
            return Collections.singletonList(convert(node));
        }

        default FlowElement convert(BpmSimpleModelNodeVO node) {
            throw new UnsupportedOperationException("请实现该方法");
        }

        BpmSimpleModelNodeTypeEnum getType();

    }

    private static class StartNodeConvert implements NodeConvert {

        @Override
        public StartEvent convert(BpmSimpleModelNodeVO node) {
            StartEvent startEvent = new StartEvent();
            startEvent.setId(node.getId());
            startEvent.setName(node.getName());
            return startEvent;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.START_NODE;
        }

    }

    private static class EndNodeConvert implements NodeConvert {

        @Override
        public EndEvent convert(BpmSimpleModelNodeVO node) {
            EndEvent endEvent = new EndEvent();
            endEvent.setId(node.getId());
            endEvent.setName(node.getName());
            // TODO @芋艿 + jason：要不要加一个终止定义？
            return endEvent;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.END_NODE;
        }

    }

    private static class StartUserNodeConvert implements NodeConvert {

        @Override
        public UserTask convert(BpmSimpleModelNodeVO node) {
            UserTask userTask = new UserTask();
            userTask.setId(node.getId());
            userTask.setName(node.getName());

            // 人工审批
            addExtensionElement(userTask, USER_TASK_APPROVE_TYPE, BpmUserTaskApproveTypeEnum.USER.getType());
            // 候选人策略为发起人自己
            addCandidateElements(BpmTaskCandidateStrategyEnum.START_USER.getStrategy(), null, userTask);
            // 添加表单字段权限属性元素
            addFormFieldsPermission(node.getFieldsPermission(), userTask);
            // 添加操作按钮配置属性元素
            addButtonsSetting(node.getButtonsSetting(), userTask);
            // 使用自动通过策略
            // TODO @芋艿 复用了SKIP， 是否需要新加一个策略；TODO @芋艿：【回复】是不是应该类似飞书，搞个草稿状态。待定；还有一种策略，不标记自动通过，而是首次发起后，第一个节点，自动通过；
            addAssignStartUserHandlerType(BpmUserTaskAssignStartUserHandlerTypeEnum.SKIP.getType(), userTask);
            return userTask;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.START_USER_NODE;
        }

    }

    private static class ApproveNodeConvert implements NodeConvert {

        @Override
        public List<FlowElement> convertList(BpmSimpleModelNodeVO node) {
            List<FlowElement> flowElements = new ArrayList<>(2);
            // 1. 构建用户任务
            UserTask userTask = buildBpmnUserTask(node);
            flowElements.add(userTask);

            // 2. 添加用户任务的 Timer Boundary Event, 用于任务的审批超时处理
            if (node.getTimeoutHandler() != null && node.getTimeoutHandler().getEnable()) {
                BoundaryEvent boundaryEvent = buildUserTaskTimeoutBoundaryEvent(userTask, node.getTimeoutHandler());
                flowElements.add(boundaryEvent);
            }
            return flowElements;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.APPROVE_NODE;
        }

        /**
         * 添加 UserTask 用户的审批超时 BoundaryEvent 事件
         *
         * @param userTask       审批任务
         * @param timeoutHandler 超时处理器
         * @return BoundaryEvent 超时事件
         */
        private BoundaryEvent buildUserTaskTimeoutBoundaryEvent(UserTask userTask,
                                                                BpmSimpleModelNodeVO.TimeoutHandler timeoutHandler) {
            // 1. 创建 Timeout Boundary Event
            String timeCycle = null;
            if (Objects.equals(BpmUserTaskTimeoutHandlerTypeEnum.REMINDER.getType(), timeoutHandler.getType()) &&
                    timeoutHandler.getMaxRemindCount() != null && timeoutHandler.getMaxRemindCount() > 1) {
                timeCycle = String.format("R%d/%s",
                        timeoutHandler.getMaxRemindCount(), timeoutHandler.getTimeDuration());
            }
            BoundaryEvent boundaryEvent = buildTimeoutBoundaryEvent(userTask, BpmBoundaryEventTypeEnum.USER_TASK_TIMEOUT.getType(),
                    timeoutHandler.getTimeDuration(), timeCycle, null);

            // 2 添加超时执行动作元素
            addExtensionElement(boundaryEvent, USER_TASK_TIMEOUT_HANDLER_TYPE, timeoutHandler.getType());
            return boundaryEvent;
        }

        private UserTask buildBpmnUserTask(BpmSimpleModelNodeVO node) {
            UserTask userTask = new UserTask();
            userTask.setId(node.getId());
            userTask.setName(node.getName());

            // 如果不是审批人节点，则直接返回
            addExtensionElement(userTask, USER_TASK_APPROVE_TYPE, node.getApproveType());
            if (ObjectUtil.notEqual(node.getApproveType(), BpmUserTaskApproveTypeEnum.USER.getType())) {
                return userTask;
            }

            // 添加候选人元素
            addCandidateElements(node.getCandidateStrategy(), node.getCandidateParam(), userTask);
            // 添加表单字段权限属性元素
            addFormFieldsPermission(node.getFieldsPermission(), userTask);
            // 添加操作按钮配置属性元素
            addButtonsSetting(node.getButtonsSetting(), userTask);
            // 处理多实例（审批方式）
            processMultiInstanceLoopCharacteristics(node.getApproveMethod(), node.getApproveRatio(), userTask);
            // 添加任务被拒绝的处理元素
            addTaskRejectElements(node.getRejectHandler(), userTask);
            // 添加用户任务的审批人与发起人相同时的处理元素
            addAssignStartUserHandlerType(node.getAssignStartUserHandlerType(), userTask);
            // 添加用户任务的空处理元素
            addAssignEmptyHandlerType(node.getAssignEmptyHandler(), userTask);
            //  设置审批任务的截止时间
            if (node.getTimeoutHandler() != null && node.getTimeoutHandler().getEnable()) {
                userTask.setDueDate(node.getTimeoutHandler().getTimeDuration());
            }
            // 设置监听器
            addUserTaskListener(node, userTask);
            // 添加是否需要签名
            addSignEnable(node.getSignEnable(), userTask);
            // 审批意见
            addReasonRequire(node.getReasonRequire(), userTask);
            // 节点类型
            addNodeType(node.getType(), userTask);
            // 添加跳过表达式
            if (StrUtil.isNotEmpty(node.getSkipExpression())) {
                userTask.setSkipExpression(node.getSkipExpression());
            }
            return userTask;
        }

        private void addUserTaskListener(BpmSimpleModelNodeVO node, UserTask userTask) {
            List<FlowableListener> flowableListeners = new ArrayList<>(3);
            if (node.getTaskCreateListener() != null
                    && Boolean.TRUE.equals(node.getTaskCreateListener().getEnable())) {
                FlowableListener flowableListener = new FlowableListener();
                flowableListener.setEvent(TaskListener.EVENTNAME_CREATE);
                flowableListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
                flowableListener.setImplementation(BpmUserTaskListener.DELEGATE_EXPRESSION);
                addListenerConfig(flowableListener, node.getTaskCreateListener());
                flowableListeners.add(flowableListener);
            }
            if (node.getTaskAssignListener() != null
                    && Boolean.TRUE.equals(node.getTaskAssignListener().getEnable())) {
                FlowableListener flowableListener = new FlowableListener();
                flowableListener.setEvent(TaskListener.EVENTNAME_ASSIGNMENT);
                flowableListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
                flowableListener.setImplementation(BpmUserTaskListener.DELEGATE_EXPRESSION);
                addListenerConfig(flowableListener, node.getTaskAssignListener());
                flowableListeners.add(flowableListener);
            }
            if (node.getTaskCompleteListener() != null
                    && Boolean.TRUE.equals(node.getTaskCompleteListener().getEnable())) {
                FlowableListener flowableListener = new FlowableListener();
                flowableListener.setEvent(TaskListener.EVENTNAME_COMPLETE);
                flowableListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
                flowableListener.setImplementation(BpmUserTaskListener.DELEGATE_EXPRESSION);
                addListenerConfig(flowableListener, node.getTaskCompleteListener());
                flowableListeners.add(flowableListener);
            }
            if (CollUtil.isNotEmpty(flowableListeners)) {
                userTask.setTaskListeners(flowableListeners);
            }
        }

        private void processMultiInstanceLoopCharacteristics(Integer approveMethod, Integer approveRatio, UserTask userTask) {
            BpmUserTaskApproveMethodEnum approveMethodEnum = BpmUserTaskApproveMethodEnum.valueOf(approveMethod);
            Assert.notNull(approveMethodEnum, "审批方式({})不能为空", approveMethodEnum);
            // 添加审批方式的扩展属性
            addExtensionElement(userTask, USER_TASK_APPROVE_METHOD, approveMethod);
            if (approveMethodEnum == BpmUserTaskApproveMethodEnum.RANDOM) {
                // 随机审批，不需要设置多实例属性
                return;
            }

            // 处理多实例审批方式
            MultiInstanceLoopCharacteristics multiInstanceCharacteristics = new MultiInstanceLoopCharacteristics();
            // 设置 collectionVariable。本系统用不到，仅仅为了 Flowable 校验不报错
            multiInstanceCharacteristics.setInputDataItem("${coll_userList}");
            if (approveMethodEnum == BpmUserTaskApproveMethodEnum.ANY) {
                multiInstanceCharacteristics.setCompletionCondition(approveMethodEnum.getCompletionCondition());
                multiInstanceCharacteristics.setSequential(false);
            } else if (approveMethodEnum == BpmUserTaskApproveMethodEnum.SEQUENTIAL) {
                multiInstanceCharacteristics.setCompletionCondition(approveMethodEnum.getCompletionCondition());
                multiInstanceCharacteristics.setSequential(true);
                multiInstanceCharacteristics.setLoopCardinality("1");
            } else if (approveMethodEnum == BpmUserTaskApproveMethodEnum.RATIO) {
                Assert.notNull(approveRatio, "通过比例不能为空");
                multiInstanceCharacteristics.setCompletionCondition(
                        String.format(approveMethodEnum.getCompletionCondition(), String.format("%.2f", approveRatio / 100D)));
                multiInstanceCharacteristics.setSequential(false);
            }
            userTask.setLoopCharacteristics(multiInstanceCharacteristics);
        }

    }

    private static class TransactorNodeConvert extends ApproveNodeConvert {

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.TRANSACTOR_NODE;
        }

    }

    private static class CopyNodeConvert implements NodeConvert {

        @Override
        public ServiceTask convert(BpmSimpleModelNodeVO node) {
            ServiceTask serviceTask = new ServiceTask();
            serviceTask.setId(node.getId());
            serviceTask.setName(node.getName());
            serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
            serviceTask.setImplementation("${" + BpmCopyTaskDelegate.BEAN_NAME + "}");

            // 添加抄送候选人元素
            addCandidateElements(node.getCandidateStrategy(), node.getCandidateParam(), serviceTask);
            // 添加表单字段权限属性元素
            addFormFieldsPermission(node.getFieldsPermission(), serviceTask);
            return serviceTask;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.COPY_NODE;
        }

    }

    private static class ConditionBranchNodeConvert implements NodeConvert {

        @Override
        public ExclusiveGateway convert(BpmSimpleModelNodeVO node) {
            ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
            exclusiveGateway.setId(node.getId());
            // TODO @jason：setName

            // 设置默认的序列流（条件）
            BpmSimpleModelNodeVO defaultSeqFlow = CollUtil.findOne(node.getConditionNodes(),
                    item -> BooleanUtil.isTrue(item.getConditionSetting().getDefaultFlow()));
            Assert.notNull(defaultSeqFlow, "条件分支节点({})的默认序列流不能为空", node.getId());
            exclusiveGateway.setDefaultFlow(defaultSeqFlow.getId());
            return exclusiveGateway;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE;
        }

    }

    private static class ParallelBranchNodeConvert implements NodeConvert {

        /**
         * 并行分支使用包容网关。需要设置所有出口条件表达式的值为 true 。原因是，解决 https://t.zsxq.com/m6GXh 反馈问题
         *
         * @see {@link ConditionNodeConvert#buildSequenceFlow}
         */
        @Override
        public List<InclusiveGateway> convertList(BpmSimpleModelNodeVO node) {

            InclusiveGateway inclusiveGateway = new InclusiveGateway();
            inclusiveGateway.setId(node.getId());
            // TODO @jason：setName

            // 合并网关 由程序创建，前端不需要传入
            InclusiveGateway joinParallelGateway = new InclusiveGateway();
            joinParallelGateway.setId(buildGatewayJoinId(node.getId()));
            // TODO @jason：setName
            return CollUtil.newArrayList(inclusiveGateway, joinParallelGateway);
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE;
        }

    }

    private static class InclusiveBranchNodeConvert implements NodeConvert {

        @Override
        public List<InclusiveGateway> convertList(BpmSimpleModelNodeVO node) {
            InclusiveGateway inclusiveGateway = new InclusiveGateway();
            inclusiveGateway.setId(node.getId());
            // 设置默认的序列流（条件）
            BpmSimpleModelNodeVO defaultSeqFlow = CollUtil.findOne(node.getConditionNodes(),
                    item -> BooleanUtil.isTrue(item.getConditionSetting().getDefaultFlow()));
            Assert.notNull(defaultSeqFlow, "包容分支节点({})的默认序列流不能为空", node.getId());
            inclusiveGateway.setDefaultFlow(defaultSeqFlow.getId());
            // TODO @jason：setName

            // 并行聚合网关由程序创建，前端不需要传入
            InclusiveGateway joinInclusiveGateway = new InclusiveGateway();
            joinInclusiveGateway.setId(buildGatewayJoinId(node.getId()));
            // TODO @jason：setName
            return CollUtil.newArrayList(inclusiveGateway, joinInclusiveGateway);
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.INCLUSIVE_BRANCH_NODE;
        }

    }

    public static class ConditionNodeConvert implements NodeConvert {

        @Override
        public List<? extends FlowElement> convertList(BpmSimpleModelNodeVO node) {
            // 原因是：正常情况下，它不会被调用到
            throw new UnsupportedOperationException("条件节点不支持转换");
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.CONDITION_NODE;
        }

        public static SequenceFlow buildSequenceFlow(String sourceId, String targetId,
                                                     BpmSimpleModelNodeTypeEnum nodeType, BpmSimpleModelNodeVO node) {
            String conditionExpression;
            // 并行分支，使用包容网关实现，强制设置条件表达式为 true
            if (BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE == nodeType) {
                conditionExpression ="${true}";
            } else {
                conditionExpression = buildConditionExpression(node.getConditionSetting());
            }
            return buildBpmnSequenceFlow(sourceId, targetId, node.getId(), node.getName(), conditionExpression);
        }
    }

    /**
     * 构造条件表达式
     */
    public static String buildConditionExpression(BpmSimpleModelNodeVO.ConditionSetting conditionSetting) {
        if (conditionSetting == null) {
            return null;
        }
        return buildConditionExpression(conditionSetting.getConditionType(), conditionSetting.getConditionExpression(),
                conditionSetting.getConditionGroups());
    }

    public static String buildConditionExpression(BpmSimpleModelNodeVO.RouterSetting routerSetting) {
        return buildConditionExpression(routerSetting.getConditionType(), routerSetting.getConditionExpression(),
                routerSetting.getConditionGroups());
    }

    public static String buildConditionExpression(Integer conditionType, String conditionExpression, ConditionGroups conditionGroups) {
        BpmSimpleModeConditionTypeEnum conditionTypeEnum = BpmSimpleModeConditionTypeEnum.valueOf(conditionType);
        if (conditionTypeEnum == BpmSimpleModeConditionTypeEnum.EXPRESSION) {
            return conditionExpression;
        }
        if (conditionTypeEnum == BpmSimpleModeConditionTypeEnum.RULE) {
            if (conditionGroups == null || CollUtil.isEmpty(conditionGroups.getConditions())) {
                return null;
            }
            List<String> strConditionGroups = convertList(conditionGroups.getConditions(), item -> {
                if (CollUtil.isEmpty(item.getRules())) {
                    return "";
                }
                // 构造规则表达式
                List<String> list = convertList(item.getRules(), (rule) -> {
                    String rightSide = NumberUtil.isNumber(rule.getRightSide()) ? rule.getRightSide()
                            : "\"" + rule.getRightSide() + "\""; // 如果非数值类型加引号
                    return String.format(" vars:getOrDefault(%s, null) %s var:convertByType(%s,%s) ",
                            rule.getLeftSide(), // 左侧：读取变量
                            rule.getOpCode(), // 中间：操作符，比较
                            rule.getLeftSide(), rightSide); // 右侧：转换变量，VariableConvertByTypeExpressionFunction
                });
                // 构造条件组的表达式
                Boolean and = item.getAnd();
                return "(" + CollUtil.join(list, and ? " && " : " || ") + ")";
            });
            return String.format("${%s}", CollUtil.join(strConditionGroups, conditionGroups.getAnd() ? " && " : " || "));
        }
        return null;
    }

    public static class DelayTimerNodeConvert implements NodeConvert {

        @Override
        public List<FlowElement> convertList(BpmSimpleModelNodeVO node) {
            List<FlowElement> flowElements = new ArrayList<>(2);
            // 1. 构建接收任务，通过接收任务可卡住节点
            ReceiveTask receiveTask = new ReceiveTask();
            receiveTask.setId(node.getId());
            receiveTask.setName(node.getName());
            flowElements.add(receiveTask);

            // 2. 添加接收任务的 Timer Boundary Event
            if (node.getDelaySetting() != null) {
                BoundaryEvent boundaryEvent = null;
                if (node.getDelaySetting().getDelayType().equals(BpmDelayTimerTypeEnum.FIXED_DATE_TIME.getType())) {
                    boundaryEvent = buildTimeoutBoundaryEvent(receiveTask, BpmBoundaryEventTypeEnum.DELAY_TIMER_TIMEOUT.getType(),
                            null, null, node.getDelaySetting().getDelayTime());
                } else if (node.getDelaySetting().getDelayType().equals(BpmDelayTimerTypeEnum.FIXED_TIME_DURATION.getType())) {
                    boundaryEvent = buildTimeoutBoundaryEvent(receiveTask, BpmBoundaryEventTypeEnum.DELAY_TIMER_TIMEOUT.getType(),
                            node.getDelaySetting().getDelayTime(), null, null);
                } else {
                    throw new UnsupportedOperationException("不支持的延迟类型：" + node.getDelaySetting());
                }
                flowElements.add(boundaryEvent);
            }
            return flowElements;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.DELAY_TIMER_NODE;
        }
    }

    public static class TriggerNodeConvert implements NodeConvert {

        @Override
        public List<? extends FlowElement> convertList(BpmSimpleModelNodeVO node) {
            Assert.notNull(node.getTriggerSetting(), "触发器节点设置不能为空");
            List<FlowElement> flowElements = new ArrayList<>(2);
            // HTTP 回调请求。需要附加一个 ReceiveTask、发起请求后、等待回调执行
            if (BpmTriggerTypeEnum.HTTP_CALLBACK.getType().equals(node.getTriggerSetting().getType())) {
                Assert.notNull(node.getTriggerSetting().getHttpRequestSetting(), "触发器 HTTP 回调请求设置不能为空");
                ReceiveTask receiveTask = new ReceiveTask();
                receiveTask.setId("Activity_" + IdUtil.fastUUID());
                receiveTask.setName("HTTP 回调");
                node.setAttachNodeId(receiveTask.getId());
                flowElements.add(receiveTask);
                // 重要：设置 callbackTaskDefineKey，用于 HTTP 回调
                node.getTriggerSetting().getHttpRequestSetting().setCallbackTaskDefineKey(receiveTask.getId());
            }

            // 触发器使用 ServiceTask 来实现
            ServiceTask serviceTask = new ServiceTask();
            serviceTask.setId(node.getId());
            serviceTask.setName(node.getName());
            serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
            serviceTask.setImplementation("${" + BpmTriggerTaskDelegate.BEAN_NAME + "}");
            addExtensionElement(serviceTask, TRIGGER_TYPE, node.getTriggerSetting().getType());
            if (node.getTriggerSetting().getHttpRequestSetting() != null) {
                addExtensionElementJson(serviceTask, TRIGGER_PARAM, node.getTriggerSetting().getHttpRequestSetting());
            }
            if (node.getTriggerSetting().getFormSettings() != null) {
                addExtensionElementJson(serviceTask, TRIGGER_PARAM, node.getTriggerSetting().getFormSettings());
            }
            flowElements.add(serviceTask);
            return flowElements;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.TRIGGER_NODE;
        }
    }

    public static class RouteBranchNodeConvert implements NodeConvert {

        @Override
        public ExclusiveGateway convert(BpmSimpleModelNodeVO node) {
            ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
            exclusiveGateway.setId(node.getId());

            // 设置默认的序列流（条件）
            node.setRouterDefaultFlowId("Flow_" + IdUtil.fastUUID());
            exclusiveGateway.setDefaultFlow(node.getRouterDefaultFlowId());
            return exclusiveGateway;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.ROUTER_BRANCH_NODE;
        }

        public static SequenceFlow buildSequenceFlow(String nodeId, BpmSimpleModelNodeVO.RouterSetting router) {
            String conditionExpression = SimpleModelUtils.buildConditionExpression(router);
            return buildBpmnSequenceFlow(nodeId, router.getNodeId(), null, null, conditionExpression);
        }

    }

    private static class ChildProcessConvert implements NodeConvert {

        @Override
        public List<FlowElement> convertList(BpmSimpleModelNodeVO node) {
            List<FlowElement> flowElements = new ArrayList<>(2);
            BpmSimpleModelNodeVO.ChildProcessSetting childProcessSetting = node.getChildProcessSetting();
            List<IOParameter> inVariables = childProcessSetting.getInVariables() == null ?
                    new ArrayList<>() : new ArrayList<>(childProcessSetting.getInVariables());
            CallActivity callActivity = new CallActivity();
            callActivity.setId(node.getId());
            callActivity.setName(node.getName());
            callActivity.setCalledElementType("key");
            // 1. 是否异步
            if (node.getChildProcessSetting().getAsync()) {
                callActivity.setAsynchronous(true);
            }

            // 2. 调用的子流程
            callActivity.setCalledElement(childProcessSetting.getCalledProcessDefinitionKey());
            callActivity.setProcessInstanceName(childProcessSetting.getCalledProcessDefinitionName());

            // 3. 是否自动跳过子流程发起节点
            IOParameter ioParameter = new IOParameter();
            ioParameter.setSourceExpression(childProcessSetting.getSkipStartUserNode().toString());
            ioParameter.setTarget(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_SKIP_START_USER_NODE);
            inVariables.add(ioParameter);

            // 4. 【默认需要传递的一些变量】流程状态
            ioParameter = new IOParameter();
            ioParameter.setSource(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS);
            ioParameter.setTarget(BpmnVariableConstants.PROCESS_INSTANCE_VARIABLE_STATUS);
            inVariables.add(ioParameter);

            // 5. 主→子变量传递、子->主变量传递
            callActivity.setInParameters(inVariables);
            if (ArrayUtil.isNotEmpty(childProcessSetting.getOutVariables()) && ObjUtil.notEqual(childProcessSetting.getAsync(), Boolean.TRUE)) {
                callActivity.setOutParameters(childProcessSetting.getOutVariables());
            }

            // 6. 子流程发起人配置
            List<FlowableListener> executionListeners = new ArrayList<>();
            FlowableListener flowableListener = new FlowableListener();
            flowableListener.setEvent(ExecutionListener.EVENTNAME_START);
            flowableListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
            flowableListener.setImplementation(BpmCallActivityListener.DELEGATE_EXPRESSION);
            FieldExtension fieldExtension = new FieldExtension();
            fieldExtension.setFieldName("listenerConfig");
            fieldExtension.setStringValue(JsonUtils.toJsonString(childProcessSetting.getStartUserSetting()));
            flowableListener.getFieldExtensions().add(fieldExtension);
            executionListeners.add(flowableListener);
            callActivity.setExecutionListeners(executionListeners);

            // 7. 超时设置
            if (childProcessSetting.getTimeoutSetting() != null && Boolean.TRUE.equals(childProcessSetting.getTimeoutSetting().getEnable())) {
                BoundaryEvent boundaryEvent = null;
                if (childProcessSetting.getTimeoutSetting().getType().equals(BpmDelayTimerTypeEnum.FIXED_DATE_TIME.getType())) {
                    boundaryEvent = buildTimeoutBoundaryEvent(callActivity, BpmBoundaryEventTypeEnum.DELAY_TIMER_TIMEOUT.getType(),
                            childProcessSetting.getTimeoutSetting().getTimeExpression(), null, null);
                } else if (childProcessSetting.getTimeoutSetting().getType().equals(BpmDelayTimerTypeEnum.FIXED_TIME_DURATION.getType())) {
                    boundaryEvent = buildTimeoutBoundaryEvent(callActivity, BpmBoundaryEventTypeEnum.CHILD_PROCESS_TIMEOUT.getType(),
                            null, null, childProcessSetting.getTimeoutSetting().getTimeExpression());
                }
                flowElements.add(boundaryEvent);
            }

            // 8. 多实例
            if (childProcessSetting.getMultiInstanceSetting() != null && Boolean.TRUE.equals(childProcessSetting.getMultiInstanceSetting().getEnable())) {
                MultiInstanceLoopCharacteristics multiInstanceCharacteristics = new MultiInstanceLoopCharacteristics();
                multiInstanceCharacteristics.setSequential(childProcessSetting.getMultiInstanceSetting().getSequential());
                if (childProcessSetting.getMultiInstanceSetting().getSourceType().equals(BpmChildProcessMultiInstanceSourceTypeEnum.FIXED_QUANTITY.getType())) {
                    multiInstanceCharacteristics.setLoopCardinality(childProcessSetting.getMultiInstanceSetting().getSource());
                }
                if (childProcessSetting.getMultiInstanceSetting().getSourceType().equals(BpmChildProcessMultiInstanceSourceTypeEnum.NUMBER_FORM.getType()) ||
                        childProcessSetting.getMultiInstanceSetting().getSourceType().equals(BpmChildProcessMultiInstanceSourceTypeEnum.MULTIPLE_FORM.getType())) {
                    multiInstanceCharacteristics.setInputDataItem(childProcessSetting.getMultiInstanceSetting().getSource());
                }
                multiInstanceCharacteristics.setCompletionCondition(String.format(BpmUserTaskApproveMethodEnum.RATIO.getCompletionCondition(),
                        String.format("%.2f", childProcessSetting.getMultiInstanceSetting().getApproveRatio() / 100D)));
                callActivity.setLoopCharacteristics(multiInstanceCharacteristics);
                addExtensionElement(callActivity, CHILD_PROCESS_MULTI_INSTANCE_SOURCE_TYPE, childProcessSetting.getMultiInstanceSetting().getSourceType());
            }

            // 添加节点类型
            addNodeType(node.getType(), callActivity);
            flowElements.add(callActivity);
            return flowElements;
        }

        @Override
        public BpmSimpleModelNodeTypeEnum getType() {
            return BpmSimpleModelNodeTypeEnum.CHILD_PROCESS;
        }

    }

    private static String buildGatewayJoinId(String id) {
        return id + "_join";
    }

    private static BoundaryEvent buildTimeoutBoundaryEvent(Activity attachedToRef, Integer type,
                                                           String timeDuration, String timeCycle, String timeDate) {
        // 1.1 定时器边界事件
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setId("Event-" + IdUtil.fastUUID());
        boundaryEvent.setCancelActivity(false); // 设置关联的任务为不会被中断
        boundaryEvent.setAttachedToRef(attachedToRef);
        // 1.2 定义超时时间表达式
        TimerEventDefinition eventDefinition = new TimerEventDefinition();
        if (ObjUtil.isNotNull(timeDuration)) {
            eventDefinition.setTimeDuration(timeDuration);
        }
        if (ObjUtil.isNotNull(timeDuration)) {
            eventDefinition.setTimeCycle(timeCycle);
        }
        if (ObjUtil.isNotNull(timeDate)) {
            eventDefinition.setTimeDate(timeDate);
        }
        boundaryEvent.addEventDefinition(eventDefinition);

        // 2. 添加定时器边界事件类型
        addExtensionElement(boundaryEvent, BOUNDARY_EVENT_TYPE, type);
        return boundaryEvent;
    }

    // ========== SIMPLE 流程预测相关的方法 ==========

    public static List<BpmSimpleModelNodeVO> simulateProcess(BpmSimpleModelNodeVO rootNode, Map<String, Object> variables) {
        List<BpmSimpleModelNodeVO> resultNodes = new ArrayList<>();

        // 从头开始遍历
        simulateNextNode(rootNode, variables, resultNodes);
        return resultNodes;
    }

    private static void simulateNextNode(BpmSimpleModelNodeVO currentNode, Map<String, Object> variables,
                                         List<BpmSimpleModelNodeVO> resultNodes) {
        // 如果不合法（包括为空），则直接结束
        if (!isValidNode(currentNode)) {
            return;
        }
        BpmSimpleModelNodeTypeEnum nodeType = BpmSimpleModelNodeTypeEnum.valueOf(currentNode.getType());
        Assert.notNull(nodeType, "模型节点类型不支持");

        // 情况：START_NODE/START_USER_NODE/APPROVE_NODE/COPY_NODE/END_NODE/TRANSACTOR_NODE
        if (nodeType == BpmSimpleModelNodeTypeEnum.START_NODE
                || nodeType == BpmSimpleModelNodeTypeEnum.START_USER_NODE
                || nodeType == BpmSimpleModelNodeTypeEnum.APPROVE_NODE
                || nodeType == BpmSimpleModelNodeTypeEnum.TRANSACTOR_NODE
                || nodeType == BpmSimpleModelNodeTypeEnum.COPY_NODE
                || nodeType == BpmSimpleModelNodeTypeEnum.CHILD_PROCESS
                || nodeType == BpmSimpleModelNodeTypeEnum.END_NODE) {
            // 添加此节点
            resultNodes.add(currentNode);
        }

        // 情况：CONDITION_BRANCH_NODE 排它，只有一个满足条件的。如果没有，就走默认的
        if (nodeType == BpmSimpleModelNodeTypeEnum.CONDITION_BRANCH_NODE) {
            // 查找满足条件的 BpmSimpleModelNodeVO 节点
            BpmSimpleModelNodeVO matchConditionNode = CollUtil.findOne(currentNode.getConditionNodes(),
                    conditionNode -> !BooleanUtil.isTrue(conditionNode.getConditionSetting().getDefaultFlow())
                            && evalConditionExpress(variables, conditionNode.getConditionSetting()));
            if (matchConditionNode == null) {
                matchConditionNode = CollUtil.findOne(currentNode.getConditionNodes(),
                        conditionNode -> BooleanUtil.isTrue(conditionNode.getConditionSetting().getDefaultFlow()));
            }
            Assert.notNull(matchConditionNode, "找不到条件节点({})", currentNode);
            // 遍历满足条件的 BpmSimpleModelNodeVO 节点
            simulateNextNode(matchConditionNode.getChildNode(), variables, resultNodes);
        }

        // 情况：INCLUSIVE_BRANCH_NODE 包容，多个满足条件的。如果没有，就走默认的
        if (nodeType == BpmSimpleModelNodeTypeEnum.INCLUSIVE_BRANCH_NODE) {
            // 查找满足条件的 BpmSimpleModelNodeVO 节点
            Collection<BpmSimpleModelNodeVO> matchConditionNodes = CollUtil.filterNew(currentNode.getConditionNodes(),
                    conditionNode -> !BooleanUtil.isTrue(conditionNode.getConditionSetting().getDefaultFlow())
                            && evalConditionExpress(variables, conditionNode.getConditionSetting()));
            if (CollUtil.isEmpty(matchConditionNodes)) {
                matchConditionNodes = CollUtil.filterNew(currentNode.getConditionNodes(),
                        conditionNode -> BooleanUtil.isTrue(conditionNode.getConditionSetting().getDefaultFlow()));
            }
            Assert.isTrue(!matchConditionNodes.isEmpty(), "找不到条件节点({})", currentNode);
            // 遍历满足条件的 BpmSimpleModelNodeVO 节点
            matchConditionNodes.forEach(matchConditionNode ->
                    simulateNextNode(matchConditionNode.getChildNode(), variables, resultNodes));
        }

        // 情况：PARALLEL_BRANCH_NODE 并行，都满足，都走
        if (nodeType == BpmSimpleModelNodeTypeEnum.PARALLEL_BRANCH_NODE) {
            // 遍历所有 BpmSimpleModelNodeVO 节点
            currentNode.getConditionNodes().forEach(matchConditionNode ->
                    simulateNextNode(matchConditionNode.getChildNode(), variables, resultNodes));
        }

        // 遍历子节点
        simulateNextNode(currentNode.getChildNode(), variables, resultNodes);
    }

    /**
     * 根据跳过表达式，判断是否跳过此节点
     */
    public static boolean isSkipNode(BpmSimpleModelNodeVO currentNode, Map<String, Object> variables) {
        if (StrUtil.isEmpty(currentNode.getSkipExpression())) {
            return false;
        }
        return BpmnModelUtils.evalConditionExpress(variables, currentNode.getSkipExpression());
    }

    public static boolean evalConditionExpress(Map<String, Object> variables, BpmSimpleModelNodeVO.ConditionSetting conditionSetting) {
        return BpmnModelUtils.evalConditionExpress(variables, buildConditionExpression(conditionSetting));
    }

}
