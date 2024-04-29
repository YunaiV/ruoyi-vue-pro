package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmApproveMethodEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModeConditionType;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeType;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.SimpleModelConstants;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeType.END_EVENT;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.FORM_FIELD_PERMISSION_ELEMENT;
import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.SimpleModelConstants.*;
import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_NAMESPACE;
import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_PREFIX;

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
    public static final String ALL_APPROVE_COMPLETE_EXPRESSION = "${ nrOfCompletedInstances >= 0 }";

    /**
     * 任一一名审批人同意的表达式
     */
    public static final String ANY_OF_APPROVE_COMPLETE_EXPRESSION = "${ nrOfCompletedInstances >= nrOfInstances }";

    /**
     * 仿钉钉流程设计模型数据结构(json) 转换成 Bpmn Model (待完善）
     *
     * @param processId       流程标识
     * @param processName     流程名称
     * @param simpleModelNode 仿钉钉流程设计模型数据结构
     * @return Bpmn Model
     */
    public static BpmnModel convertSimpleModelToBpmnModel(String processId, String processName, BpmSimpleModelNodeVO simpleModelNode) {
        BpmnModel bpmnModel = new BpmnModel();
        Process mainProcess = new Process();
        mainProcess.setId(processId);
        mainProcess.setName(processName);
        mainProcess.setExecutable(Boolean.TRUE);
        bpmnModel.addProcess(mainProcess);
        // 前端模型数据结构。
        // 从 SimpleModel 构建 FlowNode 并添加到 Main Process
        buildAndAddBpmnFlowNode(simpleModelNode, mainProcess);
        // 找到 end event
        EndEvent endEvent = (EndEvent) CollUtil.findOne(mainProcess.getFlowElements(), item -> item instanceof EndEvent);
        if (endEvent == null) {
            // TODO 暂时为了兼容 单独构建 end event 节点. 后面去掉
            endEvent = buildAndAddBpmnEndEvent(mainProcess);
        }
        // 构建并添加节点之间的连线 Sequence Flow
        buildAndAddBpmnSequenceFlow(mainProcess, simpleModelNode, endEvent.getId());
        // 自动布局
        new BpmnAutoLayout(bpmnModel).execute();
        return bpmnModel;
    }

    private static void buildAndAddBpmnSequenceFlow(Process mainProcess, BpmSimpleModelNodeVO node, String targetId) {
        // 节点为 null 或者 为END_EVENT 退出
        if (node == null || node.getId() == null || END_EVENT.getType().equals(node.getType())) {
            return;
        }
        BpmSimpleModelNodeVO childNode = node.getChildNode();
        // 如果是网关分支节点. 后续节点可能为 null。但不是 END_EVENT 节点
        if (!BpmSimpleModelNodeType.isBranchNode(node.getType()) && (childNode == null || childNode.getId() == null)) {
            SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), targetId, null, null, null);
            mainProcess.addFlowElement(sequenceFlow);
            return;
        }
        BpmSimpleModelNodeType nodeType = BpmSimpleModelNodeType.valueOf(node.getType());
        Assert.notNull(nodeType, "模型节点类型不支持");
        switch (nodeType) {
            case START_EVENT:
            case USER_TASK:
            case COPY_TASK:
            case PARALLEL_GATEWAY_JOIN:
            case INCLUSIVE_GATEWAY_JOIN: {
                SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), childNode.getId(), null, null, null);
                mainProcess.addFlowElement(sequenceFlow);
                // 递归调用后续节点
                buildAndAddBpmnSequenceFlow(mainProcess, childNode, targetId);
                break;
            }
            case PARALLEL_GATEWAY_FORK:
            case EXCLUSIVE_GATEWAY:
            case INCLUSIVE_GATEWAY_FORK: {
                String sequenceFlowTargetId = (childNode == null || childNode.getId() == null) ? targetId : childNode.getId();
                List<BpmSimpleModelNodeVO> conditionNodes = node.getConditionNodes();
                Assert.notEmpty(conditionNodes, "网关节点的条件节点不能为空");
                for (BpmSimpleModelNodeVO item : conditionNodes) {
                    String conditionExpression = buildConditionExpression(item);
                    BpmSimpleModelNodeVO nextNodeOnCondition = item.getChildNode();
                    if (nextNodeOnCondition != null && nextNodeOnCondition.getId() != null) {
                        SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), nextNodeOnCondition.getId(),
                                item.getId(), item.getName(), conditionExpression);
                        mainProcess.addFlowElement(sequenceFlow);
                        // 递归调用后续节点
                        buildAndAddBpmnSequenceFlow(mainProcess, nextNodeOnCondition, sequenceFlowTargetId);
                    } else {
                        SequenceFlow sequenceFlow = buildBpmnSequenceFlow(node.getId(), sequenceFlowTargetId,
                                item.getId(), item.getName(), conditionExpression);
                        mainProcess.addFlowElement(sequenceFlow);
                    }
                }
                // 递归调用后续节点
                buildAndAddBpmnSequenceFlow(mainProcess, childNode, targetId);
                break;
            }
            default: {
                // TODO 其它节点类型的实现
            }
        }

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
        if (conditionTypeEnum == BpmSimpleModeConditionType.CUSTOM_EXPRESSION) {
            conditionExpression = MapUtil.getStr(conditionNode.getAttributes(), CONDITION_EXPRESSION_ATTRIBUTE);
        }
        // TODO 待增加其它类型
        return conditionExpression;

    }

    private static SequenceFlow buildBpmnSequenceFlow(String sourceId, String targetId, String seqFlowId, String seqName, String conditionExpression) {
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

    private static void buildAndAddBpmnFlowNode(BpmSimpleModelNodeVO simpleModelNode, Process mainProcess) {
        // 节点为 null 退出
        if (simpleModelNode == null || simpleModelNode.getId() == null) {
            return;
        }
        BpmSimpleModelNodeType nodeType = BpmSimpleModelNodeType.valueOf(simpleModelNode.getType());
        Assert.notNull(nodeType, "模型节点类型不支持");
        switch (nodeType) {
            case START_EVENT: {
                StartEvent startEvent = buildBpmnStartEvent(simpleModelNode);
                mainProcess.addFlowElement(startEvent);
                break;
            }
            case USER_TASK: {
                UserTask userTask = buildBpmnUserTask(simpleModelNode);
                mainProcess.addFlowElement(userTask);
                break;
            }
            case COPY_TASK: {
                ServiceTask serviceTask = buildBpmnServiceTask(simpleModelNode);
                mainProcess.addFlowElement(serviceTask);
                break;
            }
            case EXCLUSIVE_GATEWAY: {
                ExclusiveGateway exclusiveGateway = buildBpmnExclusiveGateway(simpleModelNode);
                mainProcess.addFlowElement(exclusiveGateway);
                break;
            }
            case PARALLEL_GATEWAY_FORK:
            case PARALLEL_GATEWAY_JOIN: {
                ParallelGateway parallelGateway = buildBpmnParallelGateway(simpleModelNode);
                mainProcess.addFlowElement(parallelGateway);
                break;
            }
            case INCLUSIVE_GATEWAY_FORK: {
                InclusiveGateway inclusiveGateway = buildBpmnInclusiveGateway(simpleModelNode, Boolean.TRUE);
                mainProcess.addFlowElement(inclusiveGateway);
                break;
            }
            case INCLUSIVE_GATEWAY_JOIN: {
                InclusiveGateway inclusiveGateway = buildBpmnInclusiveGateway(simpleModelNode, Boolean.FALSE);
                mainProcess.addFlowElement(inclusiveGateway);
                break;
            }
            case END_EVENT: {
                EndEvent endEvent = buildBpmnEndEvent(simpleModelNode);
                mainProcess.addFlowElement(endEvent);
                break;
            }
            default: {
                // TODO 其它节点类型的实现
            }
        }

        // 如果不是网关类型的接口， 并且chileNode为空退出
        if (!BpmSimpleModelNodeType.isBranchNode(simpleModelNode.getType()) && simpleModelNode.getChildNode() == null) {
            return;
        }

        // 如果是网关类型接口. 递归添加条件节点
        if (BpmSimpleModelNodeType.isBranchNode(simpleModelNode.getType()) && ArrayUtil.isNotEmpty(simpleModelNode.getConditionNodes())) {
            for (BpmSimpleModelNodeVO node : simpleModelNode.getConditionNodes()) {
                buildAndAddBpmnFlowNode(node.getChildNode(), mainProcess);
            }
        }

        // chileNode不为空，递归添加子节点
        if (simpleModelNode.getChildNode() != null) {
            buildAndAddBpmnFlowNode(simpleModelNode.getChildNode(), mainProcess);
        }
    }

    private static ParallelGateway buildBpmnParallelGateway(BpmSimpleModelNodeVO node) {
        ParallelGateway parallelGateway = new ParallelGateway();
        parallelGateway.setId(node.getId());
        return parallelGateway;
    }

    private static ServiceTask buildBpmnServiceTask(BpmSimpleModelNodeVO node) {
        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
        serviceTask.setImplementation(BPMN_SIMPLE_COPY_EXECUTION_SCRIPT);
        serviceTask.setId(node.getId());
        serviceTask.setName(node.getName());
        // TODO @jason：建议使用 ServiceTask，通过 executionListeners 实现；
        // @芋艿 ServiceTask 就可以了吧。 不需要 executionListeners
        addCandidateElements(node, serviceTask);
        return serviceTask;
    }


    /**
     * 给节点添加候选人元素
     */
    private static void addCandidateElements(BpmSimpleModelNodeVO node, FlowElement flowElement) {
        Integer candidateStrategy = MapUtil.getInt(node.getAttributes(), BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY);
        addExtensionElement(flowElement, BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY,
                candidateStrategy == null ? null : String.valueOf(candidateStrategy));
        addExtensionElement(flowElement, BpmnModelConstants.USER_TASK_CANDIDATE_PARAM,
                MapUtil.getStr(node.getAttributes(), BpmnModelConstants.USER_TASK_CANDIDATE_PARAM));
    }

    private static ExclusiveGateway buildBpmnExclusiveGateway(BpmSimpleModelNodeVO node) {
        Assert.notEmpty(node.getConditionNodes(), "网关节点的条件节点不能为空");
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

    private static InclusiveGateway buildBpmnInclusiveGateway(BpmSimpleModelNodeVO node, Boolean isFork) {
        InclusiveGateway inclusiveGateway = new InclusiveGateway();
        inclusiveGateway.setId(node.getId());
        if (isFork) {
            Assert.notEmpty(node.getConditionNodes(), "网关节点的条件节点不能为空");
            // 网关的最后一个条件为 网关的 default sequence flow
            inclusiveGateway.setDefaultFlow(String.format("%s_SequenceFlow_%d", node.getId(), node.getConditionNodes().size()));
        }
        return inclusiveGateway;
    }

    private static EndEvent buildAndAddBpmnEndEvent(Process mainProcess) {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(BpmnModelConstants.END_EVENT_ID);
        endEvent.setName("结束");
        mainProcess.addFlowElement(endEvent);
        return endEvent;
    }

    private static UserTask buildBpmnUserTask(BpmSimpleModelNodeVO node) {
        UserTask userTask = new UserTask();
        userTask.setId(node.getId());
        userTask.setName(node.getName());
        // 添加候选人元素
        addCandidateElements(node, userTask);
        // 添加表单字段权限属性元素
        addFormFieldsPermission(node, userTask);
        // 处理多实例
        processMultiInstanceLoopCharacteristics(node, userTask);
        return userTask;
    }

    private static void processMultiInstanceLoopCharacteristics(BpmSimpleModelNodeVO node, UserTask userTask) {
        Integer approveMethod = MapUtil.getInt(node.getAttributes(), SimpleModelConstants.APPROVE_METHOD_ATTRIBUTE);
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
    private static void addFormFieldsPermission(BpmSimpleModelNodeVO node, FlowElement flowElement) {
        List<Map<String, String>> fieldsPermissions = MapUtil.get(node.getAttributes(),
                FORM_FIELD_PERMISSION_ELEMENT, new TypeReference<>() {
                });
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

    private static StartEvent buildBpmnStartEvent(BpmSimpleModelNodeVO node) {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(node.getId());
        startEvent.setName(node.getName());
        return startEvent;
    }

    private static EndEvent buildBpmnEndEvent(BpmSimpleModelNodeVO node) {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(node.getId());
        endEvent.setName(node.getName());
        return endEvent;
    }
}
