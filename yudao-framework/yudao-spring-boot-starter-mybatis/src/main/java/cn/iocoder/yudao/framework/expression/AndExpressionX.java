package cn.iocoder.yudao.framework.expression;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;

/**
 * AndExpression 的扩展类(会在原有表达式两端加上括号)
 */
public class AndExpressionX extends AndExpression {

    public AndExpressionX() {
    }

    public AndExpressionX(Expression leftExpression, Expression rightExpression) {
        this.setLeftExpression(leftExpression);
        this.setRightExpression(rightExpression);
    }

    @Override
    public String toString() {
        return "(" + super.toString() + ")";
    }

}
