package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmApproveMethodEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModeConditionType;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeType;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.simple.SimpleModelConditionGroups;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.simple.SimpleModelUserTaskConfig;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.simple.SimpleModelUserTaskConfig.RejectHandler;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.module.bpm.enums.definition.BpmBoundaryEventType.USER_TASK_TIMEOUT;
import static cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeType.*;
import static cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskTimeoutActionEnum.AUTO_REMINDER;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.*;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.SimpleModelConstants.*;
import static org.flowable.bpmn.constants.BpmnXMLConstants.*;

/**
 * 仿钉钉快搭模型相关的工具方法
 *
 * @author jason
 */
public class SimpleModelUtils {

    public static final String BPMN_SIMPLE_COPY_EXECUTION_SCRIPT = "#{bpmSimpleNodeService.copy(execution)}";

    /**
     * 所有审批人同意的表达式
     */
    public static final String ALL_APPROVE_COMPLETE_EXPRESSION = "${ nrOfCompletedInstances >= nrOfInstances }";

    /**
     * 任一一名审批人同意的表达式
     */
    public static final String ANY_OF_APPROVE_COMPLETE_EXPRESSION = "${ nrOfCompletedInstances > 0 }";

    // TODO-DONE @jason：建议方法名，改成 buildBpmnModel
    // TODO @yunai：注释需要完善下；

    /**
     * 仿钉钉流程设计模型数据结构(json) 转换成 Bpmn Model (待完善）
     *
     * @param processId       流程标识
     * @param processName     流程名称
     * @param simpleModelNode 仿钉钉流程设计模型数据结构
     * @return Bpmn Model
     */
    public static BpmnModel buildBpmnModel(String processId, String processName, BpmSimpleModelNodeVO simpleModelNode) {
        BpmnModel bpmnModel = new BpmnModel();
        // 不加这个 解析 Message 会报 NPE 异常 .
        bpmnModel.setTargetNamespace(BPMN2_NAMESPACE); // TODO @jason：待定：是不是搞个自定义的 namespace；
        // TODO 芋艿：后续在 review
        // @芋艿 这个 Message 可以去掉 暂时用不上
        Message rejectPostProcessMsg = new Message();
        rejectPostProcessMsg.setName(REJECT_POST_PROCESS_MESSAGE_NAME);
        bpmnModel.addMessage(rejectPostProcessMsg);

        Process process = new Process();
        process.setId(processId);
        process.setName(processName);
        process.setExecutable(Boolean.TRUE); // TODO @jason：这个是必须设置的么？
        bpmnModel.addProcess(process);

        // 前端模型数据结构
        // 从 SimpleModel 构建 FlowNode 并添加到 Main Process
        traverseNodeToBuildFlowNode(simpleModelNode, process);
        // 找到 end event
        EndEvent endEvent = (EndEvent) CollUtil.findOne(process.getFlowElements(), item -> item instanceof EndEvent);

        // 构建并添加节点之间的连线 Sequence Flow
        traverseNodeToBuildSequenceFlow(process, simpleModelNode, endEvent.getId());
        // 自动布局
        new BpmnAutoLayout(bpmnModel).execute();
        return bpmnModel;
    }

    private static void traverseNodeToBuildSequenceFlow(Process process, BpmSimpleModelNodeVO node, String targetNodeId) {
        // 1.无效节点返回
        if (!isValidNode(node)) {
            return;
        }

        // 如果是网关分支节点. 后续节点可能为 null。但不是 END_EVENT 节点
        // TODO-DONE @芋艿：这个要不要挪到 START_NODE - INCLUSIVE_BRANCH_JOIN_NODE 待定；感觉 switch 那最终是分三个情况；branch、子节点、结束了；（每种情况的注释，需要写的更完整）
//        if (!BpmSimpleModelNodeType.isBranchNode(node.getType()) && (childNode == null || childNode.getId() == null)) {
//            SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), targetNodeId, null, null, null);
//            process.addFlowElement(sequenceFlow);
//            return;
//        }
        BpmSimpleModelNodeType nodeType = BpmSimpleModelNodeType.valueOf(node.getType());
        Assert.notNull(nodeType, "模型节点类型不支持");
        BpmSimpleModelNodeVO childNode = node.getChildNode();
        // 2.1 普通节点
        if (!BpmSimpleModelNodeType.isBranchNode(node.getType())) {
            // 2.1.1 结束节点退出递归
            if (nodeType == END_NODE) {
                return;
            }
            if (!isValidNode(childNode)) {
                // 2.1.2 普通节点且无孩子节点。分两种情况
                // a.结束节点  b. 条件分支的最后一个节点.与分支节点的孩子节点或聚合节点建立连线。
                SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), targetNodeId, null, null, null);
                process.addFlowElement(sequenceFlow);
            } else {
                // 2.1.3 普通节点且有孩子节点。建立连线
                SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), childNode.getId(), null, null, null);
                process.addFlowElement(sequenceFlow);
                // 递归调用后续节点
                traverseNodeToBuildSequenceFlow(process, childNode, targetNodeId);
            }
        } else {
            // 2.2 分支节点
            List<BpmSimpleModelNodeVO> conditionNodes = node.getConditionNodes();
            Assert.notEmpty(conditionNodes, "分支节点的条件节点不能为空");
            // 4.1 分支节点，遍历分支节点. 如下情况:
            // 分支1、A->B->C->D->E 和 分支2、A->D->E。 A为分支节点, D为A孩子节点
            // 分支终点节点， 1. 分支节点有孩子节点时为孩子节点  2. 当分支节点孩子为无效节点时。分支嵌套时并且为分支最后一个节点
            String branchEndNodeId = isValidNode(childNode) ? childNode.getId() : targetNodeId ;
            for (BpmSimpleModelNodeVO item : conditionNodes) {
                // TODO @jason：条件分支的情况下，需要分 item 搞的条件，和 conditionNodes 搞的条件
                // 构建表达式
                String conditionExpression = buildConditionExpression(item);
                BpmSimpleModelNodeVO nextNodeOnCondition = item.getChildNode();
                // 4.2 分支有后续节点, 分支1: A->B->C->D
                if (isValidNode(nextNodeOnCondition)) {
                    // 4.2.1 建立 A->B
                    SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), nextNodeOnCondition.getId(),
                            item.getId(), item.getName(), conditionExpression);
                    process.addFlowElement(sequenceFlow);
                    // 4.2.2 递归调用后续节点连线。 建立 B->C->D 的连线
                    traverseNodeToBuildSequenceFlow(process, nextNodeOnCondition, branchEndNodeId);
                } else {
                    // 4.3 分支无后续节点 建立 A->D
                    SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), branchEndNodeId,
                            item.getId(), item.getName(), conditionExpression);
                    process.addFlowElement(sequenceFlow);
                }
            }
            // 递归调用后续节点 继续递归建立 D->E 的连线
            traverseNodeToBuildSequenceFlow(process, childNode, targetNodeId);
        }

        // TODO @jason：下面的 PARALLEL_BRANCH_FORK_NODE、CONDITION_BRANCH_NODE、INCLUSIVE_BRANCH_FORK_NODE 是不是就是 isBranchNode？如果是的话，貌似不用 swtich，而是 if else 分类处理呢。
//        switch (nodeType) {
//            case START_NODE:
//            case APPROVE_NODE:
//            case COPY_NODE:
//            case PARALLEL_BRANCH_JOIN_NODE:
//            case INCLUSIVE_BRANCH_JOIN_NODE: {
//                SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), childNode.getId(), null, null, null);
//                process.addFlowElement(sequenceFlow);
//                // 递归调用后续节点
//                buildAndAddBpmnSequenceFlow(process, childNode, targetNodeId);
//                break;
//            }
//            case PARALLEL_BRANCH_FORK_NODE:
//            case CONDITION_BRANCH_NODE:
//            case INCLUSIVE_BRANCH_FORK_NODE: {
//                // TODO @jason：这里 sequenceFlowTargetId 不建议做这样的 default。万一可能有 bug 哈；直接弄到对应的 136- 146 会更安全一点。
//                String sequenceFlowTargetId = (childNode == null || childNode.getId() == null) ? targetNodeId : childNode.getId();
//                List<BpmSimpleModelNodeVO> conditionNodes = node.getConditionNodes();
//                Assert.notEmpty(conditionNodes, "网关节点的条件节点不能为空");
//                for (BpmSimpleModelNodeVO item : conditionNodes) {
//                    // 构建表达式
//                    // TODO @jason：条件分支的情况下，需要分 item 搞的条件，和 conditionNodes 搞的条件
//                    String conditionExpression = buildConditionExpression(item);
//
//                    BpmSimpleModelNodeVO nextNodeOnCondition = item.getChildNode();
//                    // TODO @jason：isValidNode
//                    if (nextNodeOnCondition != null && nextNodeOnCondition.getId() != null) {
//                        // TODO @jason：会存在 item.name 未空的情况么？这个时候，要不要兜底处理拼接
//                        SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), nextNodeOnCondition.getId(),
//                                item.getId(), item.getName(), conditionExpression);
//                        process.addFlowElement(sequenceFlow);
//                        // 递归调用后续节点
//                        // TODO @jason：最好也有个例子，嘿嘿；S4
//                        buildAndAddBpmnSequenceFlow(process, nextNodeOnCondition, sequenceFlowTargetId);
//                    } else {
//                        SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), sequenceFlowTargetId,
//                                item.getId(), item.getName(), conditionExpression);
//                        process.addFlowElement(sequenceFlow);
//                    }
//                }
//                // 递归调用后续节点 TODO @jason：最好有个例子哈
//                buildAndAddBpmnSequenceFlow(process, childNode, targetNodeId);
//                break;
//            }
//            default: {
//                // TODO 其它节点类型的实现
//            }
//        }

    }

    /**
     * 构造条件表达式
     *
     * @param conditionNode 条件节点
     */
    private static String buildConditionExpression(BpmSimpleModelNodeVO conditionNode) {
        Integer conditionType = MapUtil.getInt(conditionNode.getAttributes(), CONDITION_TYPE_ATTRIBUTE);
        BpmSimpleModeConditionType conditionTypeEnum = BpmSimpleModeConditionType.valueOf(conditionType);
        String conditionExpression = null;
        if (conditionTypeEnum == BpmSimpleModeConditionType.EXPRESSION) {
            conditionExpression = MapUtil.getStr(conditionNode.getAttributes(), CONDITION_EXPRESSION_ATTRIBUTE);
        }
        if (conditionTypeEnum == BpmSimpleModeConditionType.RULE) {
            SimpleModelConditionGroups conditionGroups = BeanUtil.toBean(MapUtil.get(conditionNode.getAttributes(),
                            CONDITION_GROUPS_ATTRIBUTE, new TypeReference<Map<String, Object>>() {
                            }),
                    SimpleModelConditionGroups.class);
            if (conditionGroups != null && CollUtil.isNotEmpty(conditionGroups.getConditions())) {
                List<String> strConditionGroups = conditionGroups.getConditions().stream().map(item -> {
                    if (CollUtil.isNotEmpty(item.getRules())) {
                        Boolean and = item.getAnd();
                        List<String> list = CollectionUtils.convertList(item.getRules(), (rule) -> {
                            // 如果非数值类型加引号
                            String rightSide = NumberUtil.isNumber(rule.getRightSide()) ? rule.getRightSide() : "\"" + rule.getRightSide() + "\"";
                            return String.format(" %s %s var:convertByType(%s,%s)", rule.getLeftSide(), rule.getOpCode(), rule.getLeftSide(), rightSide);
                        });
                        return "(" + CollUtil.join(list, and ? " && " : " || ") + ")";
                    } else {
                        return "";
                    }
                }).toList();
                conditionExpression = String.format("${%s}", CollUtil.join(strConditionGroups, conditionGroups.getAnd() ? " && " : " || "));
            }

        }
        // TODO 待增加其它类型
        return conditionExpression;
    }

    private static SequenceFlow buildBpmnSequenceFlow(String sourceId, String targetId, String seqFlowId, String seqName, String conditionExpression) {
        Assert.notEmpty(sourceId, "sourceId 不能为空");
        Assert.notEmpty(targetId, "targetId 不能为空");
        // TODO @jason：如果 seqFlowId 不存在的时候，是不是要生成一个默认的 seqFlowId？ @芋艿： 貌似不需要,Flowable 会默认生成
        // TODO @jason：如果 name 不存在的时候，是不是要生成一个默认的 name？ @芋艿： 不需要生成默认的吧？ 这个会在流程图展示的， 一般用户填写的。不好生成默认的吧
        SequenceFlow sequenceFlow = new SequenceFlow(sourceId, targetId);
        if (StrUtil.isNotEmpty(conditionExpression)) {
            sequenceFlow.setConditionExpression(conditionExpression);
        }
        if (StrUtil.isNotEmpty(seqFlowId)) {
            sequenceFlow.setId(seqFlowId);
        }
        if (StrUtil.isNotEmpty(seqName)) {
            sequenceFlow.setName(seqName);
        }
        return sequenceFlow;
    }

    // TODO-DONE @jason：要不改成 recursionNode 递归节点，然后把 build 名字让出来，专门用于构建各种 Node
    // @芋艿 改成了 traverseNodeToBuildFlowNode， 连线的叫 traverseNodeToBuildSequenceFlow
    // TODO-DONE @jason：node 改成 node，process 改成 process；更符合递归的感觉哈，处理当前节点
    private static void traverseNodeToBuildFlowNode(BpmSimpleModelNodeVO node, Process process) {
        // 判断是否有效节点
        // TODO-DONE @jason：是不是写个 isValidNode 方法：判断是否为有效节点；
        if (!isValidNode(node)) {
            return;
        }
        BpmSimpleModelNodeType nodeType = BpmSimpleModelNodeType.valueOf(node.getType());
        Assert.notNull(nodeType, "模型节点类型不支持");

        // TODO-DONE @jason：要不抽个 buildNode 方法，然后返回一个 List<FlowElement>，之后这个方法 addFlowElement；原因是，让当前这个方法，有主干逻辑；不然现在太长了；
        List<FlowElement> flowElements = buildFlowNode(node, nodeType);
        flowElements.forEach(process::addFlowElement);

        // 如果不是网关类型的接口， 并且chileNode为空退出
        // TODO-DONE @jason：建议这个判断去掉，可以更简洁一点；因为往下走；如果不成功，本身也就会结束哈；主要是，这里多了一个这样的判断，增加了理解成本；
        // 如果是“分支”节点，则递归处理条件
        if (BpmSimpleModelNodeType.isBranchNode(node.getType())
                && ArrayUtil.isNotEmpty(node.getConditionNodes())) {
            // TODO-DONE @jason：可以搞成 stream 写成一行哈
            node.getConditionNodes().forEach(item -> traverseNodeToBuildFlowNode(item.getChildNode(), process));
        }

        // 如果有“子”节点，则递归处理子节点
        // TODO-DONE @jason：这个，是不是不写判断，直接继续调用；因为本身 buildAndAddBpmnFlowNode 就会最开始判断了哈，就不重复判断了；
        traverseNodeToBuildFlowNode(node.getChildNode(), process);
    }

    private static boolean isValidNode(BpmSimpleModelNodeVO node) {
        return node != null && node.getId() != null;
    }

    private static List<FlowElement> buildFlowNode(BpmSimpleModelNodeVO node, BpmSimpleModelNodeType nodeType) {
        List<FlowElement> list = new ArrayList<>();
        switch (nodeType) {
            case START_NODE: {
                // TODO-DONE @jason：每个 nodeType，buildXXX 方法要不更明确，并且去掉 Bpmn；
                // @芋艿 改成 convert 是不是好理解一点
                StartEvent startEvent = convertStartNode(node);
                list.add(startEvent);
                break;
            }
            case APPROVE_NODE: {
                // TODO-DONE @jason：这个，搞成一个 buildUserTask，然后把下面这 2 种节点，搞在一起实现类；这样 buildNode 里面可以更简洁；
                // TODO-DONE @jason：这里还有个想法，是不是可以所有的都叫 buildXXXNode，然后里面有一些是 bpmn 相关的构建，叫做 buildBpmnUserTask，用于区分；
                // @芋艿 改成 convertXXXNode, ， 方面里面使用 buildBpmnXXXNode. 是否更好理解
                // 转换审批节点
                List<FlowElement> flowElements = convertApproveNode(node);
                list.addAll(flowElements);
                break;
            }
            case COPY_NODE: {
                ServiceTask serviceTask = convertCopyNode(node);
                list.add(serviceTask);
                break;
            }
            case CONDITION_BRANCH_NODE: {
                ExclusiveGateway exclusiveGateway = convertConditionBranchNode(node);
                list.add(exclusiveGateway);
                break;
            }
            case PARALLEL_BRANCH_FORK_NODE:
            case PARALLEL_BRANCH_JOIN_NODE: {
                ParallelGateway parallelGateway = convertParallelBranchNode(node);
                list.add(parallelGateway);
                break;
            }

            case INCLUSIVE_BRANCH_FORK_NODE: {
                InclusiveGateway inclusiveGateway = convertInclusiveBranchNode(node, Boolean.TRUE);
                list.add(inclusiveGateway);
                break;
            }
            case INCLUSIVE_BRANCH_JOIN_NODE: {
                InclusiveGateway inclusiveGateway = convertInclusiveBranchNode(node, Boolean.FALSE);
                list.add(inclusiveGateway);
                break;
            }
            case END_NODE: {
                EndEvent endEvent = convertEndNode(node);
                list.add(endEvent);
                break;
            }
            default: {
                // TODO 其它节点类型的实现
            }
        }
        return list;
    }

    private static List<FlowElement> convertApproveNode(BpmSimpleModelNodeVO node) {
        List<FlowElement> flowElements = new ArrayList<>();
        SimpleModelUserTaskConfig userTaskConfig = BeanUtil.toBean(node.getAttributes(), SimpleModelUserTaskConfig.class);
        UserTask userTask = buildBpmnUserTask(node, userTaskConfig);
        flowElements.add(userTask);
        if (userTaskConfig.getTimeoutHandler() != null && userTaskConfig.getTimeoutHandler().getEnable()) {
            // 添加用户任务的 Timer Boundary Event, 用于任务的超时处理
            BoundaryEvent boundaryEvent = buildUserTaskTimerBoundaryEvent(userTask, userTaskConfig.getTimeoutHandler());
            flowElements.add(boundaryEvent);
        }
        return flowElements;
    }

    private static BoundaryEvent buildUserTaskTimerBoundaryEvent(UserTask userTask, SimpleModelUserTaskConfig.TimeoutHandler timeoutHandler) {
        // 定时器边界事件
        BoundaryEvent boundaryEvent = new BoundaryEvent();
        boundaryEvent.setId("Event-" + IdUtil.fastUUID());
        // 设置关联的任务为不会被中断
        boundaryEvent.setCancelActivity(false);
        boundaryEvent.setAttachedToRef(userTask);
        TimerEventDefinition eventDefinition = new TimerEventDefinition();
        eventDefinition.setTimeDuration(timeoutHandler.getTimeDuration());
        if (Objects.equals(AUTO_REMINDER.getAction(), timeoutHandler.getAction()) &&
                timeoutHandler.getMaxRemindCount() != null && timeoutHandler.getMaxRemindCount() > 1) {
            // 最大提醒次数
            eventDefinition.setTimeCycle(String.format("R%d/%s", timeoutHandler.getMaxRemindCount(), timeoutHandler.getTimeDuration()));
        }
        boundaryEvent.addEventDefinition(eventDefinition);
        // 添加定时器边界事件类型
        addExtensionElement(boundaryEvent, BOUNDARY_EVENT_TYPE, USER_TASK_TIMEOUT.getType().toString());
        // 添加超时执行动作元素
        addExtensionElement(boundaryEvent, USER_TASK_TIMEOUT_HANDLER_ACTION, StrUtil.toStringOrNull(timeoutHandler.getAction()));
        return boundaryEvent;
    }

    private static ParallelGateway convertParallelBranchNode(BpmSimpleModelNodeVO node) {
        ParallelGateway parallelGateway = new ParallelGateway();
        parallelGateway.setId(node.getId());
        // TODO @jason：setName

        // TODO @芋艿 + jason：合并网关；是不是要有条件啥的。微信讨论
        // @芋艿 貌似并行网关没有条件的
        return parallelGateway;
    }

    private static ServiceTask convertCopyNode(BpmSimpleModelNodeVO node) {
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setId(node.getId());
        serviceTask.setName(node.getName());
        // TODO @jason：建议用 delegateExpression；原因是，直接走 bpmSimpleNodeService.copy(execution) 的话，万一后续抄送改实现，可能比较麻烦。最好是搞个独立的 bean，然后它去调用抄 bpmSimpleNodeService；
        serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
        serviceTask.setImplementation(BPMN_SIMPLE_COPY_EXECUTION_SCRIPT);

        // 添加抄送候选人元素
        addCandidateElements(MapUtil.getInt(node.getAttributes(), BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY),
                MapUtil.getStr(node.getAttributes(), BpmnModelConstants.USER_TASK_CANDIDATE_PARAM),
                serviceTask);

        // 添加表单字段权限属性元素
        // TODO @芋艿：这块关注下哈；
        List<Map<String, String>> fieldsPermissions = MapUtil.get(node.getAttributes(),
                FORM_FIELD_PERMISSION_ELEMENT, new TypeReference<>() {
                });
        addFormFieldsPermission(fieldsPermissions, serviceTask);
        return serviceTask;
    }

    /**
     * 给节点添加候选人元素
     */
    private static void addCandidateElements(Integer candidateStrategy, String candidateParam, FlowElement flowElement) {
        addExtensionElement(flowElement, BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY,
                candidateStrategy == null ? null : candidateStrategy.toString());
        addExtensionElement(flowElement, BpmnModelConstants.USER_TASK_CANDIDATE_PARAM, candidateParam);
    }

    private static ExclusiveGateway convertConditionBranchNode(BpmSimpleModelNodeVO node) {
        Assert.notEmpty(node.getConditionNodes(), "条件分支节点不能为空");
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(node.getId());
        // 寻找默认的序列流
        BpmSimpleModelNodeVO defaultSeqFlow = CollUtil.findOne(node.getConditionNodes(),
                item -> BooleanUtil.isTrue(MapUtil.getBool(item.getAttributes(), DEFAULT_FLOW_ATTRIBUTE)));
        if (defaultSeqFlow != null) {
            exclusiveGateway.setDefaultFlow(defaultSeqFlow.getId());
        }
        return exclusiveGateway;
    }

    private static InclusiveGateway convertInclusiveBranchNode(BpmSimpleModelNodeVO node, Boolean isFork) {
        InclusiveGateway inclusiveGateway = new InclusiveGateway();
        inclusiveGateway.setId(node.getId());
        // TODO @jason：这里是不是 setName 哈；

        // TODO @芋艿 + jason：是不是搞个合并网关；这里微信讨论下，有点奇怪；
        // @芋艿 isFork 为 false 就是合并网关。由前端传入。这个前端暂时还未实现
        if (isFork) {
            Assert.notEmpty(node.getConditionNodes(), "条件节点不能为空");
            // 寻找默认的序列流
            BpmSimpleModelNodeVO defaultSeqFlow = CollUtil.findOne(node.getConditionNodes(),
                    item -> BooleanUtil.isTrue(MapUtil.getBool(item.getAttributes(), DEFAULT_FLOW_ATTRIBUTE)));
            if (defaultSeqFlow != null) {
                inclusiveGateway.setDefaultFlow(defaultSeqFlow.getId());
            }
        }
        return inclusiveGateway;
    }

    private static UserTask buildBpmnUserTask(BpmSimpleModelNodeVO node, SimpleModelUserTaskConfig userTaskConfig) {
        UserTask userTask = new UserTask();
        userTask.setId(node.getId());
        userTask.setName(node.getName());
        //  设置审批任务的截止时间
        if (userTaskConfig.getTimeoutHandler() != null && userTaskConfig.getTimeoutHandler().getEnable()) {
            userTask.setDueDate(userTaskConfig.getTimeoutHandler().getTimeDuration());
        }

        // TODO 芋艿 + jason：要不要基于服务任务，实现或签下的审批不通过？或者说，按比例审批

        // TODO @jason：addCandidateElements、processMultiInstanceLoopCharacteristics 建议一起搞哈？
        // 添加候选人元素
        addCandidateElements(userTaskConfig.getCandidateStrategy(), userTaskConfig.getCandidateParam(), userTask);
        // 添加表单字段权限属性元素
        addFormFieldsPermission(userTaskConfig.getFieldsPermission(), userTask);
        // 处理多实例
        processMultiInstanceLoopCharacteristics(userTaskConfig.getApproveMethod(), userTask);
        // 添加任务被拒绝的处理元素
        addTaskRejectElements(userTaskConfig.getRejectHandler(), userTask);
        return userTask;
    }

    private static void addTaskRejectElements(RejectHandler rejectHandler, UserTask userTask) {
        if (rejectHandler == null) {
            return;
        }
        addExtensionElement(userTask, USER_TASK_REJECT_HANDLER_TYPE, StrUtil.toStringOrNull(rejectHandler.getType()));
        addExtensionElement(userTask, USER_TASK_REJECT_RETURN_TASK_ID, rejectHandler.getReturnNodeId());
    }

    private static void processMultiInstanceLoopCharacteristics(Integer approveMethod, UserTask userTask) {
        BpmApproveMethodEnum bpmApproveMethodEnum = BpmApproveMethodEnum.valueOf(approveMethod);
        if (bpmApproveMethodEnum == null || bpmApproveMethodEnum == BpmApproveMethodEnum.SINGLE_PERSON_APPROVE) {
            return;
        }
        MultiInstanceLoopCharacteristics multiInstanceCharacteristics = new MultiInstanceLoopCharacteristics();
        //  设置 collectionVariable。本系统用不到。会在 仅仅为了校验。
        multiInstanceCharacteristics.setInputDataItem("${coll_userList}");
        if (bpmApproveMethodEnum == BpmApproveMethodEnum.ALL_APPROVE) {
            multiInstanceCharacteristics.setCompletionCondition(ALL_APPROVE_COMPLETE_EXPRESSION);
            multiInstanceCharacteristics.setSequential(false);
        } else if (bpmApproveMethodEnum == BpmApproveMethodEnum.ANY_OF_APPROVE) {
            multiInstanceCharacteristics.setCompletionCondition(ANY_OF_APPROVE_COMPLETE_EXPRESSION);
            multiInstanceCharacteristics.setSequential(false);
            userTask.setLoopCharacteristics(multiInstanceCharacteristics);
        } else if (bpmApproveMethodEnum == BpmApproveMethodEnum.SEQUENTIAL_APPROVE) {
            multiInstanceCharacteristics.setCompletionCondition(ALL_APPROVE_COMPLETE_EXPRESSION);
            multiInstanceCharacteristics.setSequential(true);
            multiInstanceCharacteristics.setLoopCardinality("1");
            userTask.setLoopCharacteristics(multiInstanceCharacteristics);
        }
        userTask.setLoopCharacteristics(multiInstanceCharacteristics);
    }

    /**
     * 给节点添加表单字段权限元素
     */
    private static void addFormFieldsPermission(List<Map<String, String>> fieldsPermissions, FlowElement flowElement) {
        if (CollUtil.isNotEmpty(fieldsPermissions)) {
            fieldsPermissions.forEach(item -> addExtensionElement(flowElement, FORM_FIELD_PERMISSION_ELEMENT, item));
        }
    }

    private static void addExtensionElement(FlowElement element, String name, Map<String, String> attributes) {
        if (attributes == null) {
            return;
        }
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setNamespace(FLOWABLE_EXTENSIONS_NAMESPACE);
        extensionElement.setNamespacePrefix(FLOWABLE_EXTENSIONS_PREFIX);
        extensionElement.setName(name);
        attributes.forEach((key, value) -> {
            ExtensionAttribute extensionAttribute = new ExtensionAttribute(key, value);
            extensionAttribute.setNamespace(FLOWABLE_EXTENSIONS_NAMESPACE);
            extensionElement.addAttribute(extensionAttribute);
        });
        element.addExtensionElement(extensionElement);
    }

    private static void addExtensionElement(FlowElement element, String name, String value) {
        if (value == null) {
            return;
        }
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setNamespace(FLOWABLE_EXTENSIONS_NAMESPACE);
        extensionElement.setNamespacePrefix(FLOWABLE_EXTENSIONS_PREFIX);
        extensionElement.setElementText(value);
        extensionElement.setName(name);
        element.addExtensionElement(extensionElement);
    }

    // ========== 各种 build 节点的方法 ==========

    private static StartEvent convertStartNode(BpmSimpleModelNodeVO node) {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(node.getId());
        startEvent.setName(node.getName());

        // TODO 芋艿 + jason：要不要在开启节点后面，加一个“发起人”任务节点，然后自动审批通过
        // @芋艿 这个是不是由前端来实现。 默认开始节点后面跟一个 “发起人”的审批节点(审批人是发起人自己）。
        // 我看有些平台这个审批节点允许删除，有些不允许。由用户决定
        return startEvent;
    }

    private static EndEvent convertEndNode(BpmSimpleModelNodeVO node) {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(node.getId());
        endEvent.setName(node.getName());

        // TODO @芋艿 + jason：要不要加一个终止定义？
        return endEvent;
    }

}
