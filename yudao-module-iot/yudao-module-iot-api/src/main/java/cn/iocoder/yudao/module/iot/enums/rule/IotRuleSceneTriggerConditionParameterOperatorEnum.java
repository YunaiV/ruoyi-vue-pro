package cn.iocoder.yudao.module.iot.enums.rule;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Iot 场景触发条件参数的操作符枚举
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Getter
public enum IotRuleSceneTriggerConditionParameterOperatorEnum implements ArrayValuable<String> {

    EQUALS("=", "%s == %s"),
    NOT_EQUALS("!=", "%s != %s"),

    GREATER_THAN(">", "%s > %s"),
    GREATER_THAN_OR_EQUALS(">=", "%s >= %s"),

    LESS_THAN("<", "%s < %s"),
    LESS_THAN_OR_EQUALS("<=", "%s <= %s"),

    IN("in", "%s in { %s }"),
    NOT_IN("not in", "%s not in { %s }"),

    BETWEEN("between", "(%s >= %s) && (%s <= %s)"),
    NOT_BETWEEN("not between", "!(%s between %s and %s)"),

    LIKE("like", "%s like %s"), // 字符串匹配
    NOT_NULL("not null", ""); // 非空

    private final String operator;
    private final String springExpression;

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotRuleSceneTriggerConditionParameterOperatorEnum::getOperator).toArray(String[]::new);

    public static IotRuleSceneTriggerConditionParameterOperatorEnum operatorOf(String operator) {
        return ArrayUtil.firstMatch(item -> item.getOperator().equals(operator), values());
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
