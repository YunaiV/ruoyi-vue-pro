package cn.iocoder.yudao.framework.flowable.core.util;

import cn.hutool.core.collection.CollUtil;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;

import java.util.*;

/**
 * 流程模型转操作工具类
 */
public class BpmnModelUtils {

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
     * @param model
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

    /**
     * 比较 两个bpmnModel 是否相同
     * @param oldModel  老的bpmn model
     * @param newModel 新的bpmn model
     */
    public static boolean equals(BpmnModel oldModel, BpmnModel newModel) {
        // 由于 BpmnModel 未提供 equals 方法，所以只能转成字节数组，进行比较
        return Arrays.equals(getBpmnBytes(oldModel), getBpmnBytes(newModel));
    }

    /**
     * 把 bpmnModel 转换成 byte[]
     * @param model  bpmnModel
     */
    public static byte[] getBpmnBytes(BpmnModel model) {
        if (model == null) {
            return new byte[0];
        }
        BpmnXMLConverter converter = new BpmnXMLConverter();
        return converter.convertToXML(model);
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

}
