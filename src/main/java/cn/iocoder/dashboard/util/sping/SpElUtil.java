package cn.iocoder.dashboard.util.sping;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class SpElUtil {

    private SpElUtil() {
    }

    public static String analysisSpEl(String spElString, ProceedingJoinPoint joinPoint) {
        SpelExpressionParser parser = new SpelExpressionParser();
        DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
        // 通过joinPoint获取被注解方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 使用spring的DefaultParameterNameDiscoverer获取方法形参名数组
        String[] paramNames = nameDiscoverer.getParameterNames(method);
        // 解析过后的Spring表达式对象
        Expression expression = parser.parseExpression(spElString);
        // spring的表达式上下文对象
        EvaluationContext context = new StandardEvaluationContext();
        // 通过joinPoint获取被注解方法的形参
        Object[] args = joinPoint.getArgs();
        // 给上下文赋值
        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        Object value = expression.getValue(context);
        return value == null ? "null" : value.toString();

    }
}
