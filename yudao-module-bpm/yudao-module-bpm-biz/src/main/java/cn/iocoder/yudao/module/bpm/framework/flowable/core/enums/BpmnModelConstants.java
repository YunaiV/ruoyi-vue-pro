package cn.iocoder.yudao.module.bpm.framework.flowable.core.enums;

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
     * BPMN Start Event 节点 Id, 用于后端生成 Start Event 节点
     */
    String START_EVENT_ID = "StartEvent_1";

    /**
     * BPMN End Event 节点 Id， 用于后端生成 End Event 节点
     */
    String END_EVENT_ID = "EndEvent_1";

}
