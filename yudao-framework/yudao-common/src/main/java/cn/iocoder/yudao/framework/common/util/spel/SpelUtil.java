package cn.iocoder.yudao.framework.common.util.spel;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * SpelUtil
 *
 * @author Chopper
 * @version v1.0
 * 2021-01-11 10:45
 */
public class SpelUtil {


    /**
     * spel表达式解析器
     */
    private static SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    /**
     * 参数名发现器
     */
    private static DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 转换 jspl参数
     *
     * @param joinPoint
     * @param spel
     * @return
     */
    public static String compileParams(JoinPoint joinPoint, String spel) { //Spel表达式解析日志信息
        //获得方法参数名数组
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String[] parameterNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
        if (parameterNames != null && parameterNames.length > 0) {
            EvaluationContext context = new StandardEvaluationContext();

            //获取方法参数值
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                //替换spel里的变量值为实际值， 比如 #user -->  user对象
                context.setVariable(parameterNames[i], args[i]);
            }
            return spelExpressionParser.parseExpression(spel).getValue(context).toString();
        }
        return "";
    }

    /**
     * 转换 jspl参数
     *
     * @param joinPoint
     * @param spel
     * @return
     */
    public static String compileParams(JoinPoint joinPoint, Object rvt, String spel) { //Spel表达式解析日志信息
        //获得方法参数名数组
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String[] parameterNames = parameterNameDiscoverer.getParameterNames(signature.getMethod());
        if (parameterNames != null && parameterNames.length > 0) {
            EvaluationContext context = new StandardEvaluationContext();

            //获取方法参数值
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                //替换spel里的变量值为实际值， 比如 #user -->  user对象
                context.setVariable(parameterNames[i], args[i]);
            }
            context.setVariable("rvt", rvt);
            return spelExpressionParser.parseExpression(spel).getValue(context).toString();
        }
        return "";
    }
}
