package cn.iocoder.dashboard.util.sping;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SpEl解析类
 *
 * @author mashu
 */
public class SpElUtil {

    private static SpelExpressionParser parser = new SpelExpressionParser();
    private static DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private SpElUtil() {
    }

    /**
     * 解析切面SpEL
     *
     * @param spElString 表达式
     * @param joinPoint  切面点
     * @return 执行界面
     */
    public static Object analysisSpEl(String spElString, ProceedingJoinPoint joinPoint) {
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
        return expression.getValue(context);
    }

    /**
     * 批量解析切面SpEL
     *
     * @param spElStrings 表达式
     * @param joinPoint   切面点
     * @return 执行界面
     */
    public static Map<String, Object> analysisSpEls(List<String> spElStrings, ProceedingJoinPoint joinPoint) {
        if (null == spElStrings) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>(spElStrings.size());
        spElStrings.forEach(expression -> resultMap.put(expression, analysisSpEl(expression, joinPoint)));
        return resultMap;
    }
}
