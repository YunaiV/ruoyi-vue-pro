package cn.iocoder.yudao.module.bpm.framework.flowable.core.simple;

import lombok.Data;

import java.util.List;

/**
 * 仿钉钉流程设计器条件节点的条件组 Model
 *
 * @author jason
 */
@Data
public class SimpleModelConditionGroups {

    /**
     * 条件组的逻辑关系是否为与的关系
     */
    private Boolean and;

    /**
     * 条件组下的条件
     */
    private List<SimpleModelCondition> conditions;

    @Data
    public static class SimpleModelCondition {

        /**
         * 条件下面多个规则的逻辑关系是否为与的关系
         */
        private Boolean and;


        /**
         * 条件下的规则
         */
        private List<ConditionRule> rules;
    }

    @Data
    public static class ConditionRule {

        /**
         * 类型. TODO  暂时未定义, 未想好
         */
        private Integer type;

        /**
         * 运行符号. 例如 == <
         */
        private String opCode;

        /**
         * 运算符左边的值
         */
        private String leftSide;

        /**
         * 运算符右边的值
         */
        private String rightSide;
    }
}
