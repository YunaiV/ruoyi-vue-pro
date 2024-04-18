package cn.iocoder.yudao.module.bpm.framework.flowable.core.enums;

import com.google.common.collect.ImmutableSet;
import org.flowable.bpmn.model.*;

import java.util.Set;

/**
 * BPMN XML 常量信息
 *
 * @author 芋道源码
 */
public interface BpmnModelConstants {

    String BPMN_FILE_SUFFIX = ".bpmn";

    /**
     * BPMN 中的命名空间
     */
    String NAMESPACE = "http://flowable.org/bpmn";

    /**
     * BPMN UserTask 的扩展属性，用于标记候选人策略
     */
    String USER_TASK_CANDIDATE_STRATEGY = "candidateStrategy";
    /**
     * BPMN UserTask 的扩展属性，用于标记候选人参数
     */
    String USER_TASK_CANDIDATE_PARAM = "candidateParam";

    /**
     * BPMN ExtensionElement 流程表单字段权限元素, 用于标记字段权限
     */
    String FORM_FIELD_PERMISSION_ELEMENT = "fieldsPermission";

    /**
     * BPMN ExtensionElement Attribute, 用于标记表单字段
     */
    String FORM_FIELD_PERMISSION_ELEMENT_FIELD_ATTRIBUTE = "field";

    /**
     * BPMN ExtensionElement Attribute, 用于标记表单权限
     */
    String FORM_FIELD_PERMISSION_ELEMENT_PERMISSION_ATTRIBUTE = "permission";

    // TODO @芋艿：这里后面得关注下；
    /**
     * BPMN End Event 节点 Id， 用于后端生成 End Event 节点
     */
    String END_EVENT_ID = "EndEvent_1";

    /**
     * 支持转仿钉钉设计模型的 Bpmn 节点
     */
    Set<Class<? extends FlowNode>> SUPPORT_CONVERT_SIMPLE_FlOW_NODES = ImmutableSet.of(UserTask.class, EndEvent.class);

}
