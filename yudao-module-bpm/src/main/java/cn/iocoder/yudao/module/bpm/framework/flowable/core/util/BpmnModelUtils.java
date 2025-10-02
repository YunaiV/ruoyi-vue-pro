package cn.iocoder.yudao.module.bpm.framework.flowable.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRespVO;
import cn.iocoder.yudao.module.bpm.enums.definition.*;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;
import org.flowable.engine.impl.el.FixedValue;

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

    public static void addExtensionElementJson(FlowElement element, String name, Object value) {
        if (value == null) {
            return;
        }
        addExtensionElement(element, name, JsonUtils.toJsonString(value));
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
     * 解析子流程多实例来源类型
     *
     * @see BpmChildProcessMultiInstanceSourceTypeEnum
     * @param element 任务节点
     * @return 多实例来源类型
     */
    public static Integer parseMultiInstanceSourceType(FlowElement element) {
        return NumberUtils.parseInt(parseExtensionElement(element, BpmnModelConstants.CHILD_PROCESS_MULTI_INSTANCE_SOURCE_TYPE));
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
    public static BpmUserTaskRejectHandlerTypeEnum parseRejectHandlerType(FlowElement userTask) {
        Integer rejectHandlerType = NumberUtils.parseInt(parseExtensionElement(userTask, USER_TASK_REJECT_HANDLER_TYPE));
        return BpmUserTaskRejectHandlerTypeEnum.typeOf(rejectHandlerType);
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

    public static void addSignEnable(Boolean signEnable, FlowElement userTask) {
        addExtensionElement(userTask, SIGN_ENABLE,
                ObjUtil.isNotNull(signEnable) ? signEnable.toString() : Boolean.FALSE.toString());
    }

    public static Boolean parseSignEnable(BpmnModel bpmnModel, String flowElementId) {
        FlowElement flowElement = getFlowElementById(bpmnModel, flowElementId);
        if (flowElement == null) {
            return false;
        }
        List<ExtensionElement> extensionElements = flowElement.getExtensionElements().get(SIGN_ENABLE);
        if (CollUtil.isEmpty(extensionElements)) {
            return false;
        }
        return Convert.toBool(extensionElements.get(0).getElementText(), false);
    }

    public static void addReasonRequire(Boolean reasonRequire, FlowElement userTask) {
        addExtensionElement(userTask, REASON_REQUIRE,
                ObjUtil.isNotNull(reasonRequire) ? reasonRequire.toString() : Boolean.FALSE.toString());
    }

    public static Boolean parseReasonRequire(BpmnModel bpmnModel, String flowElementId) {
        FlowElement flowElement = getFlowElementById(bpmnModel, flowElementId);
        if (flowElement == null) {
            return false;
        }
        List<ExtensionElement> extensionElements = flowElement.getExtensionElements().get(REASON_REQUIRE);
        if (CollUtil.isEmpty(extensionElements)) {
            return false;
        }
        return Convert.toBool(extensionElements.get(0).getElementText(), false);
    }

    public static void addListenerConfig(FlowableListener flowableListener, BpmSimpleModelNodeVO.ListenerHandler handler) {
        FieldExtension fieldExtension = new FieldExtension();
        fieldExtension.setFieldName("listenerConfig");
        fieldExtension.setStringValue(JsonUtils.toJsonString(handler));
        flowableListener.getFieldExtensions().add(fieldExtension);
    }

    public static BpmSimpleModelNodeVO.ListenerHandler parseListenerConfig(FixedValue fixedValue) {
        String expressionText = fixedValue.getExpressionText();
        Assert.notNull(expressionText, "监听器扩展字段({})不能为空", expressionText);
        return JsonUtils.parseObject(expressionText, BpmSimpleModelNodeVO.ListenerHandler.class);
    }

    public static BpmTriggerTypeEnum parserTriggerType(FlowElement flowElement) {
        Integer triggerType = NumberUtils.parseInt(parseExtensionElement(flowElement, TRIGGER_TYPE));
        return BpmTriggerTypeEnum.typeOf(triggerType);
    }

    public static String parserTriggerParam(FlowElement flowElement) {
        return parseExtensionElement(flowElement, TRIGGER_PARAM);
    }

    /**
     * 给节点添加节点类型
     *
     * @param nodeType 节点类型
     * @param flowElement 节点
     */
    public static void addNodeType(Integer nodeType, FlowElement flowElement) {
        addExtensionElement(flowElement, BpmnModelConstants.NODE_TYPE, nodeType);
    }

    /**
     * 解析节点类型
     *
     * @param flowElement 节点
     * @return 节点类型
     */
    public static Integer parseNodeType(FlowElement flowElement) {
        return NumberUtils.parseInt(parseExtensionElement(flowElement, BpmnModelConstants.NODE_TYPE));
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
        FlowElement flowElement = process.getFlowElement(flowElementId);
        if (flowElement != null) {
            return flowElement;
        }
        return model.getFlowElement(flowElementId);
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
        // 1. 没有入口连线，则返回 false
        if (CollUtil.isEmpty(sequenceFlows)) {
            return false;
        }
        // 2. 循环找目标元素, 找到目标节点
        for (SequenceFlow sequenceFlow : sequenceFlows) {
            // 如果发现连线重复，说明循环了，跳过这个循环
            if (visitedElements.contains(sequenceFlow.getId())) {
                continue;
            }
            // 添加已经走过的连线
            visitedElements.add(sequenceFlow.getId());
            // 这条线路存在目标节点，直接返回 true
            FlowElement sourceFlowElement = sequenceFlow.getSourceFlowElement();
            if (target.getId().equals(sourceFlowElement.getId())) {
               return true;
            }
            // 如果目标节点为并行网关，跳过这个循环 (TODO 疑问：这个判断作用是防止回退到并行网关分支上的节点吗？）
            if (sourceFlowElement instanceof ParallelGateway) {
                continue;
            }
            // 继续迭代，如果找到目标节点直接返回 true
            if (isSequentialReachable(sourceFlowElement, target, visitedElements)) {
                return true;
            }
        }
        // 未找到返回 false
        return false;
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
            // 添加节点
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
            SequenceFlow matchSequenceFlow = findMatchSequenceFlowByExclusiveGateway((Gateway) currentElement, variables);
            // 遍历满足条件的 SequenceFlow 路径
            if (matchSequenceFlow != null) {
                simulateNextFlowElements(matchSequenceFlow.getTargetFlowElement(), variables, resultElements, visitElements);
            }
        }
        // 情况：InclusiveGateway 包容，多个满足条件的。如果没有，就走默认的
        else if (currentElement instanceof InclusiveGateway) {
            // 查找满足条件的 SequenceFlow 路径
            Collection<SequenceFlow> matchSequenceFlows = findMatchSequenceFlowsByInclusiveGateway((Gateway) currentElement, variables);
            // 遍历满足条件的 SequenceFlow 路径
            matchSequenceFlows.forEach(
                    flow -> simulateNextFlowElements(flow.getTargetFlowElement(), variables, resultElements, visitElements));
        }
        // 情况：ParallelGateway 并行，都满足，都走
        else if (currentElement instanceof ParallelGateway) {
            Gateway gateway = (Gateway) currentElement;
            // 遍历子节点
            gateway.getOutgoingFlows().forEach(
                    nextElement -> simulateNextFlowElements(nextElement.getTargetFlowElement(), variables, resultElements, visitElements));
        }
    }

    /**
     * 判断是否跳过此节点
     *
     * @param flowNode 节点
     * @param variables 流程变量
     */
    public static boolean isSkipNode(FlowElement flowNode, Map<String, Object> variables) {
        // 1. 检查节点是否有跳过表达式（支持多种任务节点类型）
        String skipExpression = null;
        if (flowNode instanceof UserTask) {
            skipExpression = ((UserTask) flowNode).getSkipExpression();
        } else if (flowNode instanceof ServiceTask) {
            skipExpression = ((ServiceTask) flowNode).getSkipExpression();
        } else if (flowNode instanceof ScriptTask) {
            skipExpression = ((ScriptTask) flowNode).getSkipExpression();
        }

        if (StrUtil.isEmpty(skipExpression)) {
            return false;
        }

        // 2. 计算跳过表达式的值
        return evalConditionExpress(variables, skipExpression);
    }

    /**
     * 根据当前节点，获取下一个节点
     *
     * @param currentElement 当前节点
     * @param bpmnModel  BPMN模型
     * @param variables 流程变量
     */
    @SuppressWarnings("PatternVariableCanBeUsed")
    public static List<FlowNode> getNextFlowNodes(FlowElement currentElement, BpmnModel bpmnModel,
                                                  Map<String, Object> variables){
        List<FlowNode> nextFlowNodes = new ArrayList<>(); // 下一个执行的流程节点集合
        FlowNode currentNode = (FlowNode) currentElement;  // 当前执行节点的基本属性
        List<SequenceFlow> outgoingFlows = currentNode.getOutgoingFlows();  // 当前节点的关联节点
        if (CollUtil.isEmpty(outgoingFlows)) {
            log.warn("[getNextFlowNodes][当前节点({}) 的 outgoingFlows 为空]", currentNode.getId());
            return nextFlowNodes;
        }

        // 遍历每个出口流
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            // 获取目标节点的基本属性
            FlowElement targetElement = bpmnModel.getFlowElement(outgoingFlow.getTargetRef());
            if (targetElement == null) {
                continue;
            }
            // 如果是结束节点，直接返回
            if (targetElement instanceof EndEvent) {
                break;
            }
            // 情况一：处理不同类型的网关
            if (targetElement instanceof Gateway) {
                Gateway gateway = (Gateway) targetElement;
                if (gateway instanceof ExclusiveGateway) {
                    handleExclusiveGateway(gateway, bpmnModel, variables, nextFlowNodes);
                } else if (gateway instanceof InclusiveGateway) {
                    handleInclusiveGateway(gateway, bpmnModel, variables, nextFlowNodes);
                } else if (gateway instanceof ParallelGateway) {
                    handleParallelGateway(gateway, bpmnModel, variables, nextFlowNodes);
                }
            } else {
                // 情况二：如果不是网关，直接添加到下一个节点列表
                nextFlowNodes.add((FlowNode) targetElement);
            }
        }
        return nextFlowNodes;
    }

    /**
     * 查找起始节点下一个用户任务列表列表
     *
     * @param source 起始节点
     * @return 结果
     */
    public static List<UserTask> getNextUserTasks(FlowElement source) {
        return getNextUserTasks(source, null, null);
    }

    /**
     * 查找起始节点下一个用户任务列表列表
     * @param source 起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList 用户任务列表
     * @return 结果
     */
    public static List<UserTask> getNextUserTasks(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        hasSequenceFlow = Optional.ofNullable(hasSequenceFlow).orElse(new HashSet<>());
        userTaskList = Optional.ofNullable(userTaskList).orElse(new ArrayList<>());
        // 获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (!sequenceFlows.isEmpty()) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
                if (targetFlowElement instanceof UserTask) {
                    // 若节点为用户任务，加入到结果列表中
                    userTaskList.add((UserTask) targetFlowElement);
                } else {
                    // 若节点非用户任务，继续递归查找下一个节点
                    getNextUserTasks(targetFlowElement, hasSequenceFlow, userTaskList);
                }
            }
        }
        return userTaskList;
    }

    /**
     * 处理排它网关
     *
     * @param gateway 排他网关
     * @param bpmnModel BPMN模型
     * @param variables 流程变量
     * @param nextFlowNodes 下一个执行的流程节点集合
     */
    private static void handleExclusiveGateway(Gateway gateway, BpmnModel bpmnModel,
                                               Map<String, Object> variables, List<FlowNode> nextFlowNodes) {
        // 查找满足条件的 SequenceFlow 路径
        SequenceFlow matchSequenceFlow = findMatchSequenceFlowByExclusiveGateway(gateway, variables);
        // 遍历满足条件的 SequenceFlow 路径
        if (matchSequenceFlow != null) {
            FlowElement targetElement = bpmnModel.getFlowElement(matchSequenceFlow.getTargetRef());
            if (targetElement instanceof FlowNode) {
                nextFlowNodes.add((FlowNode) targetElement);
            }
        }
    }

    /**
     * 处理排它网关（Exclusive Gateway），选择符合条件的路径
     *
     * @param gateway 排他网关
     * @param variables 流程变量
     * @return 符合条件的路径
     */
    private static SequenceFlow findMatchSequenceFlowByExclusiveGateway(Gateway gateway, Map<String, Object> variables) {
        SequenceFlow matchSequenceFlow = CollUtil.findOne(gateway.getOutgoingFlows(),
                    flow -> ObjUtil.notEqual(gateway.getDefaultFlow(), flow.getId())
                            && (evalConditionExpress(variables, flow.getConditionExpression())));
        if (matchSequenceFlow == null) {
            matchSequenceFlow = CollUtil.findOne(gateway.getOutgoingFlows(),
                    flow -> ObjUtil.equal(gateway.getDefaultFlow(), flow.getId()));
            // 特殊：没有默认的情况下，并且只有 1 个条件，则认为它是默认的
            if (matchSequenceFlow == null && gateway.getOutgoingFlows().size() == 1) {
                matchSequenceFlow = gateway.getOutgoingFlows().get(0);
            }
        }
        return matchSequenceFlow;
    }

    /**
     * 处理包容网关
     *
     * @param gateway 排他网关
     * @param bpmnModel BPMN模型
     * @param variables 流程变量
     * @param nextFlowNodes 下一个执行的流程节点集合
     */
    private static void handleInclusiveGateway(Gateway gateway, BpmnModel bpmnModel,
                                               Map<String, Object> variables, List<FlowNode> nextFlowNodes) {
        // 查找满足条件的 SequenceFlow 路径集合
        Collection<SequenceFlow> matchSequenceFlows = findMatchSequenceFlowsByInclusiveGateway(gateway, variables);
        // 遍历满足条件的 SequenceFlow 路径，获取目标节点
        matchSequenceFlows.forEach(flow -> {
            FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
            if (targetElement instanceof FlowNode) {
                nextFlowNodes.add((FlowNode) targetElement);
            }
        });
    }

    /**
     * 处理排它网关（Inclusive Gateway），选择符合条件的路径
     *
     * @param gateway 排他网关
     * @param variables 流程变量
     * @return 符合条件的路径
     */
    private static Collection<SequenceFlow> findMatchSequenceFlowsByInclusiveGateway(Gateway gateway, Map<String, Object> variables) {
        // 查找满足条件的 SequenceFlow 路径
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
        return matchSequenceFlows;
    }


    /**
     * 处理并行网关
     *
     * @param gateway 排他网关
     * @param bpmnModel BPMN模型
     * @param variables 流程变量
     * @param nextFlowNodes 下一个执行的流程节点集合
     */
    private static void handleParallelGateway(Gateway gateway, BpmnModel bpmnModel,
                                              Map<String, Object> variables, List<FlowNode> nextFlowNodes) {
        // 并行网关，遍历所有出口路径，获取目标节点
        gateway.getOutgoingFlows().forEach(flow -> {
            FlowElement targetElement = bpmnModel.getFlowElement(flow.getTargetRef());
            if (targetElement instanceof FlowNode) {
                nextFlowNodes.add((FlowNode) targetElement);
            }
        });
    }

    /**
     * 计算条件表达式是否为 true 满足条件
     *
     * @param variables 流程实例
     * @param expression 条件表达式
     * @return 是否满足条件
     */
    public static boolean evalConditionExpress(Map<String, Object> variables, String expression) {
        if (StrUtil.isEmpty(expression)) {
            return Boolean.FALSE;
        }
        // 如果 variables 为空，则创建一个的原因？可能 expression 的计算，不依赖于 variables
        if (variables == null) {
            variables = new HashMap<>();
        }

        // 执行计算
        try {
            Object result = FlowableUtils.getExpressionValue(variables, expression);
            return Boolean.TRUE.equals(result);
        } catch (FlowableException ex) {
            // 为什么使用 info 日志？原因是，expression 如果从 variables 取不到值，会报错。实际这种情况下，可以忽略
            log.info("[evalConditionExpress][条件表达式({}) 变量({}) 解析报错]", expression, variables, ex);
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
