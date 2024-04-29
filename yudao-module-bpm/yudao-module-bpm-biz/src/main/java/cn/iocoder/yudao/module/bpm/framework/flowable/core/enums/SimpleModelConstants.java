package cn.iocoder.yudao.module.bpm.framework.flowable.core.enums;

/**
 * 仿钉钉快搭 JSON 常量信息
 *
 * @author jason
 */
public interface SimpleModelConstants {

    // TODO @芋艿：审批方式的名字，可能要看下；
    /**
     * 审批方式属性
     */
    String APPROVE_METHOD_ATTRIBUTE = "approveMethod";

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
}
