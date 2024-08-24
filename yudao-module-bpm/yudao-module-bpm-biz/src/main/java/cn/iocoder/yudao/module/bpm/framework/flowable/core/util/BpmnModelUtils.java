package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskRejectHandlerType;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;

import java.util.*;

import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.*;
import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_NAMESPACE;

/**
 * 流程模型转操作工具类
 */
public class BpmnModelUtils {

    public static Integer parseCandidateStrategy(FlowElement userTask) {
        Integer candidateStrategy = NumberUtils.parseInt(userTask.getAttributeValue(
                BpmnModelConstants.NAMESPACE, BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY));
        // TODO @芋艿 尝试从 ExtensionElement 取. 后续相关扩展是否都可以 存 extensionElement。 如表单权限。 按钮权限
        if (candidateStrategy == null) {
            ExtensionElement element = CollUtil.getFirst(userTask.getExtensionElements().get(BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY));
            candidateStrategy = element != null ? NumberUtils.parseInt(element.getElementText()) : null;
        }
        return candidateStrategy;
    }

    public static String parseCandidateParam(FlowElement userTask) {
        String candidateParam = userTask.getAttributeValue(
                BpmnModelConstants.NAMESPACE, BpmnModelConstants.USER_TASK_CANDIDATE_PARAM);
        if (candidateParam == null) {
            ExtensionElement element = CollUtil.getFirst(userTask.getExtensionElements().get(BpmnModelConstants.USER_TASK_CANDIDATE_PARAM));
            candidateParam = element != null ? element.getElementText() : null;
        }
        return candidateParam;
    }

    public static Integer parseApproveType(FlowElement userTask) {
        return NumberUtils.parseInt(parseExtensionElement(userTask, BpmnModelConstants.USER_TASK_APPROVE_TYPE));
    }

    public static BpmUserTaskRejectHandlerType parseRejectHandlerType(FlowElement userTask) {
        Integer rejectHandlerType = NumberUtils.parseInt(parseExtensionElement(userTask, USER_TASK_REJECT_HANDLER_TYPE));
        return BpmUserTaskRejectHandlerType.typeOf(rejectHandlerType);
    }

    public static String parseReturnTaskId(FlowElement flowElement) {
        return parseExtensionElement(flowElement, USER_TASK_REJECT_RETURN_TASK_ID);
    }

    public static Integer parseAssignStartUserHandlerType(FlowElement userTask) {
        return NumberUtils.parseInt(parseExtensionElement(userTask, USER_TASK_ASSIGN_START_USER_HANDLER_TYPE));
    }

    public static Integer parseAssignEmptyHandlerType(FlowElement userTask) {
        return NumberUtils.parseInt(parseExtensionElement(userTask, USER_TASK_ASSIGN_EMPTY_HANDLER_TYPE));
    }

    public static List<Long> parseAssignEmptyHandlerUserIds(FlowElement userTask) {
        return StrUtils.splitToLong(parseExtensionElement(userTask, USER_TASK_ASSIGN_USER_IDS), ",");
    }

    public static String parseExtensionElement(FlowElement flowElement, String elementName) {
        if (flowElement == null) {
            return null;
        }
        ExtensionElement element = CollUtil.getFirst(flowElement.getExtensionElements().get(elementName));
        return element != null ? element.getElementText() : null;
    }

    public static Map<String, String> parseFormFieldsPermission(BpmnModel bpmnModel, String flowElementId) {
        if (bpmnModel == null || StrUtil.isEmpty(flowElementId)) {
            return null;
        }
        FlowElement flowElement = getFlowElementById(bpmnModel, flowElementId);
        if (flowElement == null) {
            return null;
        }
        List<ExtensionElement> extensionElements = flowElement.getExtensionElements().get(FORM_FIELD_PERMISSION_ELEMENT);
        if (CollUtil.isEmpty(extensionElements)) {
            return null;
        }
        Map<String, String> fieldsPermission = MapUtil.newHashMap();
        extensionElements.forEach(element -> {
            String field = element.getAttributeValue(FLOWABLE_EXTENSIONS_NAMESPACE, FORM_FIELD_PERMISSION_ELEMENT_FIELD_ATTRIBUTE);
            String permission = element.getAttributeValue(FLOWABLE_EXTENSIONS_NAMESPACE, FORM_FIELD_PERMISSION_ELEMENT_PERMISSION_ATTRIBUTE);
            if (StrUtil.isNotEmpty(field) && StrUtil.isNotEmpty(permission)) {
                fieldsPermission.put(field, permission);
            }
        });
        return fieldsPermission;
    }

    public static Map<Integer, BpmTaskRespVO.OperationButtonSetting> parseButtonsSetting(BpmnModel bpmnModel, String flowElementId) {
        FlowElement flowElement = getFlowElementById(bpmnModel, flowElementId);
        if (flowElement == null) {
            return null;
        }
        List<ExtensionElement> extensionElements = flowElement.getExtensionElements().get(BUTTON_SETTING_ELEMENT);
        if (CollUtil.isEmpty(extensionElements)) {
            return null;
        }
        Map<Integer, BpmTaskRespVO.OperationButtonSetting> buttonSettings = MapUtil.newHashMap(16);
        extensionElements.forEach(element -> {
            String id = element.getAttributeValue(FLOWABLE_EXTENSIONS_NAMESPACE, BUTTON_SETTING_ELEMENT_ID_ATTRIBUTE);
            String displayName = element.getAttributeValue(FLOWABLE_EXTENSIONS_NAMESPACE, BUTTON_SETTING_ELEMENT_DISPLAY_NAME_ATTRIBUTE);
            String enable = element.getAttributeValue(FLOWABLE_EXTENSIONS_NAMESPACE, BUTTON_SETTING_ELEMENT_ENABLE_ATTRIBUTE);
            if (StrUtil.isNotEmpty(id)) {
                BpmTaskRespVO.OperationButtonSetting setting = new BpmTaskRespVO.OperationButtonSetting();
                buttonSettings.put(Integer.valueOf(id), setting.setDisplayName(displayName).setEnable(Boolean.parseBoolean(enable)));
            }
        });
        return buttonSettings;
    }

    /**
     * 根据节点，获取入口连线
     *
     * @param source 起始节点
     * @return 入口连线列表
     */
    public static List<SequenceFlow> getElementIncomingFlows(FlowElement source) {
        if (source instanceof FlowNode) {
            return ((FlowNode) source).getIncomingFlows();
        }
        return new ArrayList<>();
    }

    /**
     * 根据节点，获取出口连线
     *
     * @param source 起始节点
     * @return 出口连线列表
     */
    public static List<SequenceFlow> getElementOutgoingFlows(FlowElement source) {
        if (source instanceof FlowNode) {
            return ((FlowNode) source).getOutgoingFlows();
        }
        return new ArrayList<>();
    }

    /**
     * 获取流程元素信息
     *
     * @param model         bpmnModel 对象
     * @param flowElementId 元素 ID
     * @return 元素信息
     */
    public static FlowElement getFlowElementById(BpmnModel model, String flowElementId) {
        Process process = model.getMainProcess();
        return process.getFlowElement(flowElementId);
    }

    /**
     * 获得 BPMN 流程中，指定的元素们
     *
     * @param model 模型
     * @param clazz 指定元素。例如说，{@link UserTask}、{@link Gateway} 等等
     * @return 元素们
     */
    public static <T extends FlowElement> List<T> getBpmnModelElements(BpmnModel model, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        model.getProcesses().forEach(process -> {
            process.getFlowElements().forEach(flowElement -> {
                if (flowElement.getClass().isAssignableFrom(clazz)) {
                    result.add((T) flowElement);
                }
            });
        });
        return result;
    }

    public static StartEvent getStartEvent(BpmnModel model) {
        Process process = model.getMainProcess();
        // 从 initialFlowElement 找
        FlowElement startElement = process.getInitialFlowElement();
        if (startElement instanceof StartEvent) {
            return (StartEvent) startElement;
        }
        // 从 flowElementList 找
        return (StartEvent) CollUtil.findOne(process.getFlowElements(), flowElement -> flowElement instanceof StartEvent);
    }

    public static EndEvent getEndEvent(BpmnModel model) {
        Process process = model.getMainProcess();
        // 从 flowElementList 找 endEvent. TODO 多个 EndEvent 会有问题
        return (EndEvent) CollUtil.findOne(process.getFlowElements(), flowElement -> flowElement instanceof EndEvent);
    }

    public static BpmnModel getBpmnModel(byte[] bpmnBytes) {
        if (ArrayUtil.isEmpty(bpmnBytes)) {
            return null;
        }
        BpmnXMLConverter converter = new BpmnXMLConverter();
        // 补充说明：由于在 Flowable 中自定义了属性，所以 validateSchema 传递 false
        return converter.convertToBpmnModel(new BytesStreamSource(bpmnBytes), false, false);
    }

    public static String getBpmnXml(BpmnModel model) {
        if (model == null) {
            return null;
        }
        BpmnXMLConverter converter = new BpmnXMLConverter();
        return new String(converter.convertToXML(model));
    }

    // ========== 遍历相关的方法 ==========

    /**
     * 找到 source 节点之前的所有用户任务节点
     *
     * @param source          起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList    已找到的用户任务节点
     * @return 用户任务节点 数组
     */
    public static List<UserTask> getPreviousUserTaskList(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            userTaskList = getPreviousUserTaskList(source.getSubProcess(), hasSequenceFlow, userTaskList);
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (sequenceFlows == null) {
            return userTaskList;
        }
        // 循环找到目标元素
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            // 如果发现连线重复，说明循环了，跳过这个循环
            if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                continue;
            }
            // 添加已经走过的连线
            hasSequenceFlow.add(sequenceFlow.getId());
            // 类型为用户节点，则新增父级节点
            if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                userTaskList.add((UserTask) sequenceFlow.getSourceFlowElement());
            }
            // 类型为子流程，则添加子流程开始节点出口处相连的节点
            if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                // 获取子流程用户任务节点
                List<UserTask> childUserTaskList = findChildProcessUserTaskList((StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, null);
                // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                if (CollUtil.isNotEmpty(childUserTaskList)) {
                    userTaskList.addAll(childUserTaskList);
                }
            }
            // 继续迭代
            userTaskList = getPreviousUserTaskList(sequenceFlow.getSourceFlowElement(), hasSequenceFlow, userTaskList);
        }
        return userTaskList;
    }

    /**
     * 迭代获取子流程用户任务节点
     *
     * @param source          起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList    需要撤回的用户任务列表
     * @return 用户任务节点
     */
    public static List<UserTask> findChildProcessUserTaskList(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (sequenceFlows == null) {
            return userTaskList;
        }
        // 循环找到目标元素
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            // 如果发现连线重复，说明循环了，跳过这个循环
            if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                continue;
            }
            // 添加已经走过的连线
            hasSequenceFlow.add(sequenceFlow.getId());
            // 如果为用户任务类型，且任务节点的 Key 正在运行的任务中存在，添加
            if (sequenceFlow.getTargetFlowElement() instanceof UserTask) {
                userTaskList.add((UserTask) sequenceFlow.getTargetFlowElement());
                continue;
            }
            // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
            if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                List<UserTask> childUserTaskList = findChildProcessUserTaskList((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, null);
                // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                if (CollUtil.isNotEmpty(childUserTaskList)) {
                    userTaskList.addAll(childUserTaskList);
                    continue;
                }
            }
            // 继续迭代
            userTaskList = findChildProcessUserTaskList(sequenceFlow.getTargetFlowElement(), hasSequenceFlow, userTaskList);
        }
        return userTaskList;
    }


    /**
     * 迭代从后向前扫描，判断目标节点相对于当前节点是否是串行
     * 不存在直接回退到子流程中的情况，但存在从子流程出去到父流程情况
     *
     * @param source          起始节点
     * @param target          目标节点
     * @param visitedElements 已经经过的连线的 ID，用于判断线路是否重复
     * @return 结果
     */
    public static boolean isSequentialReachable(FlowElement source, FlowElement target, Set<String> visitedElements) {
        visitedElements = visitedElements == null ? new HashSet<>() : visitedElements;
        // 不能是开始事件和子流程
        if (source instanceof StartEvent && isInEventSubprocess(source)) {
            return false;
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (CollUtil.isEmpty(sequenceFlows)) {
            return true;
        }
        // 循环找到目标元素
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            // 如果发现连线重复，说明循环了，跳过这个循环
            if (visitedElements.contains(sequenceFlow.getId())) {
                continue;
            }
            // 添加已经走过的连线
            visitedElements.add(sequenceFlow.getId());
            // 这条线路存在目标节点，这条线路完成，进入下个线路
            FlowElement sourceFlowElement = sequenceFlow.getSourceFlowElement();
            if (target.getId().equals(sourceFlowElement.getId())) {
                continue;
            }
            // 如果目标节点为并行网关，则不继续
            if (sourceFlowElement instanceof ParallelGateway) {
                return false;
            }
            // 否则就继续迭代
            if (!isSequentialReachable(sourceFlowElement, target, visitedElements)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断当前节点是否属于不同的子流程
     *
     * @param flowElement 被判断的节点
     * @return true 表示属于子流程
     */
    private static boolean isInEventSubprocess(FlowElement flowElement) {
        FlowElementsContainer flowElementsContainer = flowElement.getParentContainer();
        while (flowElementsContainer != null) {
            if (flowElementsContainer instanceof EventSubProcess) {
                return true;
            }

            if (flowElementsContainer instanceof FlowElement) {
                flowElementsContainer = ((FlowElement) flowElementsContainer).getParentContainer();
            } else {
                flowElementsContainer = null;
            }
        }
        return false;
    }

    /**
     * 根据正在运行的任务节点，迭代获取子级任务节点列表，向后找
     *
     * @param source          起始节点
     * @param runTaskKeyList  正在运行的任务 Key，用于校验任务节点是否是正在运行的节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList    需要撤回的用户任务列表
     * @return 子级任务节点列表
     */
    public static List<UserTask> iteratorFindChildUserTasks(FlowElement source, List<String> runTaskKeyList,
                                                            Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;
        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            userTaskList = iteratorFindChildUserTasks(source.getSubProcess(), runTaskKeyList, hasSequenceFlow, userTaskList);
        }

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (sequenceFlows == null) {
            return userTaskList;
        }
        // 循环找到目标元素
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            // 如果发现连线重复，说明循环了，跳过这个循环
            if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                continue;
            }
            // 添加已经走过的连线
            hasSequenceFlow.add(sequenceFlow.getId());
            // 如果为用户任务类型，且任务节点的 Key 正在运行的任务中存在，添加
            if (sequenceFlow.getTargetFlowElement() instanceof UserTask && runTaskKeyList.contains((sequenceFlow.getTargetFlowElement()).getId())) {
                userTaskList.add((UserTask) sequenceFlow.getTargetFlowElement());
                continue;
            }
            // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
            if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                List<UserTask> childUserTaskList = iteratorFindChildUserTasks((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), runTaskKeyList, hasSequenceFlow, null);
                // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                if (CollUtil.isNotEmpty(childUserTaskList)) {
                    userTaskList.addAll(childUserTaskList);
                    continue;
                }
            }
            // 继续迭代
            userTaskList = iteratorFindChildUserTasks(sequenceFlow.getTargetFlowElement(), runTaskKeyList, hasSequenceFlow, userTaskList);
        }
        return userTaskList;
    }

    public static String parseBoundaryEventExtensionElement(BoundaryEvent boundaryEvent, String customElement) {
        if (boundaryEvent == null) {
            return null;
        }
        ExtensionElement extensionElement = CollUtil.getFirst(boundaryEvent.getExtensionElements().get(customElement));
        return Optional.ofNullable(extensionElement).map(ExtensionElement::getElementText).orElse(null);
    }
}
