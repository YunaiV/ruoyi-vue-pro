package cn.iocoder.yudao.module.iot.enums.rule;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 场景触发条件参数的操作符枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotRuleSceneTriggerConditionParameterOperatorEnum implements ArrayValuable<String> {

    EQUALS("=", "#source == #value"),
    NOT_EQUALS("!=", "!(#source == #value)"),

    GREATER_THAN(">", "#source > #value"),
    GREATER_THAN_OR_EQUALS(">=", "#source >= #value"),

    LESS_THAN("<", "#source < #value"),
    LESS_THAN_OR_EQUALS("<=", "#source <= #value"),

    IN("in", "#values.contains(#source)"),
    NOT_IN("not in", "!(#values.contains(#source))"),

    BETWEEN("between", "(#source >= #values.get(0)) && (#source <= #values.get(1))"),
    NOT_BETWEEN("not between", "(#source < #values.get(0)) || (#source > #values.get(1))"),

    LIKE("like", "#source.contains(#value)"), // 字符串匹配
    NOT_NULL("not null", "#source != null && #source.length() > 0"); // 非空

    private final String operator;
    private final String springExpression;

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotRuleSceneTriggerConditionParameterOperatorEnum::getOperator).toArray(String[]::new);

    /**
     * Spring 表达式 - 原始值
     */
    public static final String SPRING_EXPRESSION_SOURCE = "source";
    /**
     * Spring 表达式 - 目标值
     */
    public static final String SPRING_EXPRESSION_VALUE = "value";
    /**
     * Spring 表达式 - 目标值数组
     */
    public static final String SPRING_EXPRESSION_VALUE_List = "values";

    public static IotRuleSceneTriggerConditionParameterOperatorEnum operatorOf(String operator) {
        return ArrayUtil.firstMatch(item -> item.getOperator().equals(operator), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
