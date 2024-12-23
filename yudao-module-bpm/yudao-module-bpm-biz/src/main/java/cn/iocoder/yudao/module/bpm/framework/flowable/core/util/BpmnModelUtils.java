package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignEmptyHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignStartUserHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskRejectHandlerType;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;

import java.util.*;

import static cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants.*;
import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_NAMESPACE;
import static org.flowable.bpmn.constants.BpmnXMLConstants.FLOWABLE_EXTENSIONS_PREFIX;

/**
 * BPMN Model 操作工具类。目前分成三部分：
 *
 * 1. BPMN 修改 + 解析元素相关的方法
 * 2. BPMN 简单查找相关的方法
 * 3. BPMN 复杂遍历相关的方法
 * 4. BPMN 流程预测相关的方法
 *
 * @author 芋道源码
 */
@Slf4j
public class BpmnModelUtils {

    // ========== BPMN 修改 + 解析元素相关的方法 ==========

    public static void addExtensionElement(FlowElement element, String name, String value) {
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

    public static void addExtensionElement(FlowElement element, String name, Integer value) {
        if (value == null) {
            return;
        }
        addExtensionElement(element, name, String.valueOf(value));
    }

    public static void addExtensionElement(FlowElement element, String name, Map<String, String> attributes) {
        if (attributes == null) {
            return;
        }
        ExtensionElement extensionElement = new ExtensionElement();
        extensionElement.setNamespace(FLOWABLE_EXTENSIONS_NAMESPACE);
        extensionElement.setNamespacePrefix(FLOWABLE_EXTENSIONS_PREFIX);
        extensionElement.setName(name);
        attributes.forEach((key, value) -> {
            ExtensionAttribute extensionAttribute = new ExtensionAttribute(key, value);
            extensionElement.addAttribute(extensionAttribute);
        });
        element.addExtensionElement(extensionElement);
    }

    /**
     * 解析扩展元素
     *
     * @param flowElement 节点
     * @param elementName 元素名称
     * @return 扩展元素
     */
    public static String parseExtensionElement(FlowElement flowElement, String elementName) {
        if (flowElement == null) {
            return null;
        }
        ExtensionElement element = CollUtil.getFirst(flowElement.getExtensionElements().get(elementName));
        return element != null ? element.getElementText() : null;
    }

    /**
     * 给节点添加候选人元素
     *
     * @param candidateStrategy 候选人策略
     * @param candidateParam 候选人参数，允许空
     * @param flowElement 节点
     */
    public static void addCandidateElements(Integer candidateStrategy, String candidateParam, FlowElement flowElement) {
        addExtensionElement(flowElement, BpmnModelConstants.USER_TASK_CANDIDATE_STRATEGY,
                candidateStrategy == null ? null : candidateStrategy.toString());
        addExtensionElement(flowElement, BpmnModelConstants.USER_TASK_CANDIDATE_PARAM, candidateParam);
    }

    /**
     * 解析候选人策略
     *
     * @param userTask 任务节点
     * @return 候选人策略
     */
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

    /**
     * 解析候选人参数
     *
     * @param userTask 任务节点
     * @return 候选人参数
     */
    public static String parseCandidateParam(FlowElement userTask) {
        String candidateParam = userTask.getAttributeValue(
                BpmnModelConstants.NAMESPACE, BpmnModelConstants.USER_TASK_CANDIDATE_PARAM);
        if (candidateParam == null) {
            ExtensionElement element = CollUtil.getFirst(userTask.getExtensionElements().get(BpmnModelConstants.USER_TASK_CANDIDATE_PARAM));
            candidateParam = element != null ? element.getElementText() : null;
        }
        return candidateParam;
    }

    /**
     * 解析审批类型
     *
     * @see BpmUserTaskApproveTypeEnum
     * @param userTask 任务节点
     * @return 审批类型
     */
    public static Integer parseApproveType(FlowElement userTask) {
        return NumberUtils.parseInt(parseExtensionElement(userTask, BpmnModelConstants.USER_TASK_APPROVE_TYPE));
    }

    /**
     * 添加任务拒绝处理元素
     *
     * @param rejectHandler 任务拒绝处理
     * @param userTask 任务节点
     */
    public static void addTaskRejectElements(BpmSimpleModelNodeVO.RejectHandler rejectHandler, UserTask userTask) {
        if (rejectHandler == null) {
            return;
        }
        addExtensionElement(userTask, USER_TASK_REJECT_HANDLER_TYPE, StrUtil.toStringOrNull(rejectHandler.getType()));
        addExtensionElement(userTask, USER_TASK_REJECT_RETURN_TASK_ID, rejectHandler.getReturnNodeId());
    }

    /**
     * 解析任务拒绝处理类型
     *
     * @param userTask 任务节点
     * @return 任务拒绝处理类型
     */
    public static BpmUserTaskRejectHandlerType parseRejectHandlerType(FlowElement userTask) {
        Integer rejectHandlerType = NumberUtils.parseInt(parseExtensionElement(userTask, USER_TASK_REJECT_HANDLER_TYPE));
        return BpmUserTaskRejectHandlerType.typeOf(rejectHandlerType);
    }

    /**
     * 解析任务拒绝返回任务节点 ID
     *
     * @param flowElement 任务节点
     * @return 任务拒绝返回任务节点 ID
     */
    public static String parseReturnTaskId(FlowElement flowElement) {
        return parseExtensionElement(flowElement, USER_TASK_REJECT_RETURN_TASK_ID);
    }

    /**
     * 给节点添加用户任务的审批人与发起人相同时，处理类型枚举
     *
     * @see BpmUserTaskAssignStartUserHandlerTypeEnum
     * @param assignStartUserHandlerType 发起人处理类型
     * @param userTask 任务节点
     */
    public static void addAssignStartUserHandlerType(Integer assignStartUserHandlerType, UserTask userTask) {
        if (assignStartUserHandlerType == null) {
            return;
        }
        addExtensionElement(userTask, USER_TASK_ASSIGN_START_USER_HANDLER_TYPE, assignStartUserHandlerType.toString());
    }

    /**
     * 给节点添加用户任务的审批人为空时，处理类型枚举
     *
     * @see BpmUserTaskAssignEmptyHandlerTypeEnum
     * @param emptyHandler 空处理
     * @param userTask 任务节点
     */
    public static void addAssignEmptyHandlerType(BpmSimpleModelNodeVO.AssignEmptyHandler emptyHandler, UserTask userTask) {
        if (emptyHandler == null) {
            return;
        }
        addExtensionElement(userTask, USER_TASK_ASSIGN_EMPTY_HANDLER_TYPE, StrUtil.toStringOrNull(emptyHandler.getType()));
        addExtensionElement(userTask, USER_TASK_ASSIGN_USER_IDS, StrUtil.join(",", emptyHandler.getUserIds()));
    }

    /**
     * 解析用户任务的审批人与发起人相同时，处理类型枚举
     *
     * @param userTask 任务节点
     * @return 处理类型枚举
     */
    public static Integer parseAssignStartUserHandlerType(FlowElement userTask) {
        return NumberUtils.parseInt(parseExtensionElement(userTask, USER_TASK_ASSIGN_START_USER_HANDLER_TYPE));
    }

    /**
     * 解析用户任务的审批人为空时，处理类型枚举
     *
     * @param userTask 任务节点
     * @return 处理类型枚举
     */
    public static Integer parseAssignEmptyHandlerType(FlowElement userTask) {
        return NumberUtils.parseInt(parseExtensionElement(userTask, USER_TASK_ASSIGN_EMPTY_HANDLER_TYPE));
    }

    /**
     * 解析用户任务的审批人为空时，处理用户 ID 数组
     *
     * @param userTask 任务节点
     * @return 处理用户 ID 数组
     */
    public static List<Long> parseAssignEmptyHandlerUserIds(FlowElement userTask) {
        return StrUtils.splitToLong(parseExtensionElement(userTask, USER_TASK_ASSIGN_USER_IDS), ",");
    }

    /**
     * 给节点添加表单字段权限元素
     *
     * @param fieldsPermissions 表单字段权限
     * @param flowElement 节点
     */
    public static void addFormFieldsPermission(List<Map<String, String>> fieldsPermissions, FlowElement flowElement) {
        if (CollUtil.isNotEmpty(fieldsPermissions)) {
            fieldsPermissions.forEach(item -> addExtensionElement(flowElement, FORM_FIELD_PERMISSION_ELEMENT, item));
        }
    }

    /**
     * 解析表单字段权限
     *
     * @param bpmnModel bpmnModel 对象
     * @param flowElementId 元素 ID
     * @return 表单字段权限
     */
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
            String field = element.getAttributeValue(null, FORM_FIELD_PERMISSION_ELEMENT_FIELD_ATTRIBUTE);
            String permission = element.getAttributeValue(null, FORM_FIELD_PERMISSION_ELEMENT_PERMISSION_ATTRIBUTE);
            if (StrUtil.isNotEmpty(field) && StrUtil.isNotEmpty(permission)) {
                fieldsPermission.put(field, permission);
            }
        });
        return fieldsPermission;
    }

    /**
     * 给节点添加操作按钮设置元素
     */
    public static void addButtonsSetting(List<BpmSimpleModelNodeVO.OperationButtonSetting> buttonsSetting, UserTask userTask) {
        if (CollUtil.isNotEmpty(buttonsSetting)) {
            List<Map<String, String>> list = CollectionUtils.convertList(buttonsSetting, item -> {
                Map<String, String> settingMap = Maps.newHashMapWithExpectedSize(3);
                settingMap.put(BUTTON_SETTING_ELEMENT_ID_ATTRIBUTE, String.valueOf(item.getId()));
                settingMap.put(BUTTON_SETTING_ELEMENT_DISPLAY_NAME_ATTRIBUTE, item.getDisplayName());
                settingMap.put(BUTTON_SETTING_ELEMENT_ENABLE_ATTRIBUTE, String.valueOf(item.getEnable()));
                return settingMap;
            });
            list.forEach(item -> addExtensionElement(userTask, BUTTON_SETTING_ELEMENT, item));
        }
    }

    /**
     * 解析操作按钮设置
     *
     * @param bpmnModel bpmnModel 对象
     * @param flowElementId 元素 ID
     * @return 操作按钮设置
     */
    public static Map<Integer, BpmTaskRespVO.OperationButtonSetting> parseButtonsSetting(BpmnModel bpmnModel, String flowElementId) {
        FlowElement flowElement = getFlowElementById(bpmnModel, flowElementId);
        if (flowElement == null) {
            return null;
        }
        List<ExtensionElement> extensionElements = flowElement.getExtensionElements().get(BUTTON_SETTING_ELEMENT);
        if (CollUtil.isEmpty(extensionElements)) {
            return null;
        }
        Map<Integer, BpmTaskRespVO.OperationButtonSetting> buttonSettings = Maps.newHashMapWithExpectedSize(extensionElements.size());
        extensionElements.forEach(element -> {
            String id = element.getAttributeValue(null, BUTTON_SETTING_ELEMENT_ID_ATTRIBUTE);
            String displayName = element.getAttributeValue(null, BUTTON_SETTING_ELEMENT_DISPLAY_NAME_ATTRIBUTE);
            String enable = element.getAttributeValue(null, BUTTON_SETTING_ELEMENT_ENABLE_ATTRIBUTE);
            if (StrUtil.isNotEmpty(id)) {
                BpmTaskRespVO.OperationButtonSetting setting = new BpmTaskRespVO.OperationButtonSetting();
                buttonSettings.put(Integer.valueOf(id), setting.setDisplayName(displayName).setEnable(Boolean.parseBoolean(enable)));
            }
        });
        return buttonSettings;
    }

    /**
     * 解析边界事件扩展元素
     *
     * @param boundaryEvent 边界事件
     * @param customElement 元素
     * @return 扩展元素
     */
    public static String parseBoundaryEventExtensionElement(BoundaryEvent boundaryEvent, String customElement) {
        if (boundaryEvent == null) {
            return null;
        }
        ExtensionElement extensionElement = CollUtil.getFirst(boundaryEvent.getExtensionElements().get(customElement));
        return Optional.ofNullable(extensionElement).map(ExtensionElement::getElementText).orElse(null);
    }

    // ========== BPM 简单查找相关的方法 ==========

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
    @SuppressWarnings("unchecked")
    public static <T extends FlowElement> List<T> getBpmnModelElements(BpmnModel model, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        model.getProcesses().forEach(process -> process.getFlowElements().forEach(flowElement -> {
            if (flowElement.getClass().isAssignableFrom(clazz)) {
                result.add((T) flowElement);
            }
        }));
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
        // 从 flowElementList 找 endEvent
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
        return StrUtil.utf8Str(converter.convertToXML(model));
    }

    public static String getBpmnXml(byte[] bpmnBytes) {
        if (ArrayUtil.isEmpty(bpmnBytes)) {
            return null;
        }
        return StrUtil.utf8Str(bpmnBytes);
    }

    // ========== BPMN 复杂遍历相关的方法 ==========

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
     * 不存在直接退回到子流程中的情况，但存在从子流程出去到父流程情况
     *
     * @param source          起始节点
     * @param target          目标节点
     * @param visitedElements 已经经过的连线的 ID，用于判断线路是否重复
     * @return 结果
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    // ========== BPMN 流程预测相关的方法 ==========

    /**
     * 流程预测，返回 StartEvent、UserTask、ServiceTask、EndEvent 节点元素，最终是 List 串行结果
     *
     * @param bpmnModel BPMN 图
     * @param variables 变量
     * @return 节点元素数组
     */
    public static List<FlowElement> simulateProcess(BpmnModel bpmnModel, Map<String, Object> variables) {
        List<FlowElement> resultElements = new ArrayList<>();
        Set<FlowElement> visitElements = new HashSet<>();

        // 从 StartEvent 开始遍历
        StartEvent startEvent = getStartEvent(bpmnModel);
        simulateNextFlowElements(startEvent, variables, resultElements, visitElements);

        // 将 EndEvent 放在末尾。原因是，DFS 遍历，可能 EndEvent 在 resultElements 中
        List<FlowElement> endEvents = CollUtil.removeWithAddIf(resultElements,
                flowElement -> flowElement instanceof EndEvent);
        resultElements.addAll(endEvents);
        return resultElements;
    }

    @SuppressWarnings("PatternVariableCanBeUsed")
    private static void simulateNextFlowElements(FlowElement currentElement, Map<String, Object> variables,
                                                 List<FlowElement> resultElements, Set<FlowElement> visitElements) {
        // 如果为空，或者已经遍历过，则直接结束
        if (currentElement == null) {
            return;
        }
        if (visitElements.contains(currentElement)) {
            return;
        }
        visitElements.add(currentElement);

        // 情况：StartEvent/EndEvent/UserTask/ServiceTask
        if (currentElement instanceof StartEvent
            || currentElement instanceof EndEvent
            || currentElement instanceof UserTask
            || currentElement instanceof ServiceTask) {
            // 添加元素
            FlowNode flowNode = (FlowNode) currentElement;
            resultElements.add(flowNode);
            // 遍历子节点
            flowNode.getOutgoingFlows().forEach(
                    nextElement -> simulateNextFlowElements(nextElement.getTargetFlowElement(), variables, resultElements, visitElements));
            return;
        }

        // 情况：ExclusiveGateway 排它，只有一个满足条件的。如果没有，就走默认的
        if (currentElement instanceof ExclusiveGateway) {
            // 查找满足条件的 SequenceFlow 路径
            Gateway gateway = (Gateway) currentElement;
            SequenceFlow matchSequenceFlow = CollUtil.findOne(gateway.getOutgoingFlows(),
                    flow -> ObjUtil.notEqual(gateway.getDefaultFlow(), flow.getId())
                            && evalConditionExpress(variables, flow.getConditionExpression()));
            if (matchSequenceFlow == null) {
                matchSequenceFlow = CollUtil.findOne(gateway.getOutgoingFlows(),
                        flow -> ObjUtil.equal(gateway.getDefaultFlow(), flow.getId()));
                // 特殊：没有默认的情况下，并且只有 1 个条件，则认为它是默认的
                if (matchSequenceFlow == null && gateway.getOutgoingFlows().size() == 1) {
                    matchSequenceFlow = gateway.getOutgoingFlows().get(0);
                }
            }
            // 遍历满足条件的 SequenceFlow 路径
            if (matchSequenceFlow != null) {
                simulateNextFlowElements(matchSequenceFlow.getTargetFlowElement(), variables, resultElements, visitElements);
            }
            return;
        }

        // 情况：InclusiveGateway 包容，多个满足条件的。如果没有，就走默认的
        if (currentElement instanceof InclusiveGateway) {
            // 查找满足条件的 SequenceFlow 路径
            Gateway gateway = (Gateway) currentElement;
            Collection<SequenceFlow> matchSequenceFlows = CollUtil.filterNew(gateway.getOutgoingFlows(),
                    flow -> ObjUtil.notEqual(gateway.getDefaultFlow(), flow.getId())
                            && evalConditionExpress(variables, flow.getConditionExpression()));
            if (CollUtil.isEmpty(matchSequenceFlows)) {
                matchSequenceFlows = CollUtil.filterNew(gateway.getOutgoingFlows(),
                        flow -> ObjUtil.equal(gateway.getDefaultFlow(), flow.getId()));
                // 特殊：没有默认的情况下，并且只有 1 个条件，则认为它是默认的
                if (CollUtil.isEmpty(matchSequenceFlows) && gateway.getOutgoingFlows().size() == 1) {
                    matchSequenceFlows = gateway.getOutgoingFlows();
                }
            }
            // 遍历满足条件的 SequenceFlow 路径
            matchSequenceFlows.forEach(
                    flow -> simulateNextFlowElements(flow.getTargetFlowElement(), variables, resultElements, visitElements));
        }

        // 情况：ParallelGateway 并行，都满足，都走
        if (currentElement instanceof ParallelGateway) {
            Gateway gateway = (Gateway) currentElement;
            // 遍历子节点
            gateway.getOutgoingFlows().forEach(
                    nextElement -> simulateNextFlowElements(nextElement.getTargetFlowElement(), variables, resultElements, visitElements));
            return;
        }
    }

    /**
     * 计算条件表达式是否为 true 满足条件
     *
     * @param variables 流程实例
     * @param express 条件表达式
     * @return 是否满足条件
     */
    public static boolean evalConditionExpress(Map<String, Object> variables, String express) {
        if (express == null) {
            return Boolean.FALSE;
        }
        try {
            Object result = FlowableUtils.getExpressionValue(variables, express);
            return Boolean.TRUE.equals(result);
        } catch (FlowableException ex) {
            log.error("[evalConditionExpress][条件表达式({}) 变量({}) 解析报错", express, variables, ex);
            return Boolean.FALSE;
        }
    }

    @SuppressWarnings("PatternVariableCanBeUsed")
    public static boolean isSequentialUserTask(FlowElement flowElement) {
        if (!(flowElement instanceof UserTask)) {
            return false;
        }
        UserTask userTask = (UserTask) flowElement;
        MultiInstanceLoopCharacteristics loopCharacteristics = userTask.getLoopCharacteristics();
        return loopCharacteristics != null && loopCharacteristics.isSequential();
    }

}
