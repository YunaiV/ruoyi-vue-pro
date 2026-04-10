package cn.iocoder.yudao.module.iot.enums.rule;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 场景触发条件的操作符枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotSceneRuleConditionOperatorEnum implements ArrayValuable<String> {

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
    NOT_NULL("not null", "#source != null && #source.length() > 0"), // 非空

    // ========== 特殊：不放在字典里 ==========

    // TODO @puhui999：@芋艿：需要测试下
    DATE_TIME_GREATER_THAN("date_time_>", "#source > #value"), // 在时间之后：时间戳
    DATE_TIME_LESS_THAN("date_time_<", "#source < #value"), // 在时间之前：时间戳
    DATE_TIME_BETWEEN("date_time_between", // 在时间之间：时间戳
            "(#source >= #values.get(0)) && (#source <= #values.get(1))"),

    // TODO @puhui999：@芋艿：需要测试下
    TIME_GREATER_THAN("time_>", "#source.isAfter(#value)"), // 在当日时间之后：HH:mm:ss
    TIME_LESS_THAN("time_<", "#source.isBefore(#value)"), // 在当日时间之前：HH:mm:ss
    TIME_BETWEEN("time_between", // 在当日时间之间：HH:mm:ss
            "(#source >= #values.get(0)) && (#source <= #values.get(1))"),

    ;

    private final String operator;
    private final String springExpression;

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotSceneRuleConditionOperatorEnum::getOperator).toArray(String[]::new);

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
    public static final String SPRING_EXPRESSION_VALUE_LIST = "values";

    public static IotSceneRuleConditionOperatorEnum operatorOf(String operator) {
        return ArrayUtil.firstMatch(item -> item.getOperator().equals(operator), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
