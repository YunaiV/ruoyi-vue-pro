package cn.iocoder.yudao.module.bpm.framework.flowable.core.el;

import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.common.engine.impl.el.function.AbstractFlowableVariableExpressionFunction;
import org.springframework.stereotype.Component;

/**
 * 根据流程变量 variable 的类型，转换参数的值
 *
 * 目前用于 ConditionNodeConvert 的 buildConditionExpression 方法中
 *
 * @author jason
 */
@Component
public class VariableConvertByTypeExpressionFunction extends AbstractFlowableVariableExpressionFunction {

    public VariableConvertByTypeExpressionFunction() {
        super("convertByType");
    }

    public static Object convertByType(VariableContainer variableContainer, String variableName, Object parmaValue) {
        Object variable = variableContainer.getVariable(variableName);
        if (variable != null && parmaValue != null) {
            // 如果值不是字符串类型，流程变量的类型是字符串，把值转成字符串
            if (!(parmaValue instanceof String) && variable instanceof String ) {
                return parmaValue.toString();
            }
        }
        return parmaValue;
    }

}
