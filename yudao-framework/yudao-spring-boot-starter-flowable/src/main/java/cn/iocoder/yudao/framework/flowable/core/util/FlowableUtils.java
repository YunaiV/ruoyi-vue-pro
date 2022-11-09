package cn.iocoder.yudao.framework.flowable.core.util;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.impl.identity.Authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Flowable 相关的工具方法
 *
 * @author 芋道源码
 */
public class FlowableUtils {

    // ========== User 相关的工具方法 ==========

    public static void setAuthenticatedUserId(Long userId) {
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
    }

    public static void clearAuthenticatedUserId() {
        Authentication.setAuthenticatedUserId(null);
    }

    // ========== BPMN 相关的工具方法 ==========

    /**
     * 获得 BPMN 流程中，指定的元素们
     *
     * @param model
     * @param clazz 指定元素。例如说，{@link org.flowable.bpmn.model.UserTask}、{@link org.flowable.bpmn.model.Gateway} 等等
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
    public  static byte[] getBpmnBytes(BpmnModel model) {
        if (model == null) {
            return new byte[0];
        }
        BpmnXMLConverter converter = new BpmnXMLConverter();
        return converter.convertToXML(model);
    }

    // ========== Execution 相关的工具方法 ==========

    public static String formatCollectionVariable(String activityId) {
        return activityId + "_assignees";
    }

    public static String formatCollectionElementVariable(String activityId) {
        return activityId + "_assignee";
    }

}
