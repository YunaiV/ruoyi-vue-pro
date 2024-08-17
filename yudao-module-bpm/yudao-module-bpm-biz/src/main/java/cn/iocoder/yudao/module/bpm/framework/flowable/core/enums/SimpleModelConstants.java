package cn.iocoder.yudao.module.bpm.framework.flowable.core.enums;

// TODO @jason：要不合并到 BpmnModelConstants 那
/**
 * 仿钉钉快搭 JSON 常量信息
 *
 * @author jason
 */
public interface SimpleModelConstants {

    // TODO @芋艿：条件表达式的字段名

    /**
     * 网关节点默认序列流属性
     */
    String DEFAULT_FLOW_ATTRIBUTE = "defaultFlow";

    /**
     * 条件节点的条件类型属性
     */
    String CONDITION_TYPE_ATTRIBUTE = "conditionType";

    /**
     * 条件节点条件表达式属性
     */
    String CONDITION_EXPRESSION_ATTRIBUTE = "conditionExpression";

    /**
     * 条件规则的条件组属性
     */
    String CONDITION_GROUPS_ATTRIBUTE = "conditionGroups";

}
