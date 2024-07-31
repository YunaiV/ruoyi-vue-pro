package cn.iocoder.yudao.framework.desensitize.core.regex.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;

import java.lang.annotation.Annotation;

/**
 * 正则表达式脱敏处理器抽象类，已实现通用的方法
 *
 * @author gaibu
 */
public abstract class AbstractRegexDesensitizationHandler<T extends Annotation>
        implements DesensitizationHandler<T> {

    @Override
    public String desensitize(String origin, T annotation) {
        Object expressionResult = SpringExpressionUtils.parseExpression(SpringUtil.getApplicationContext(), getCondition(annotation));
        if (expressionResult instanceof Boolean && (Boolean) expressionResult) {
            return origin;
        }
        String regex = getRegex(annotation);
        String replacer = getReplacer(annotation);
        return origin.replaceAll(regex, replacer);
    }

    /**
     * 获取注解上的 regex 参数
     *
     * @param annotation 注解信息
     * @return 正则表达式
     */
    abstract String getRegex(T annotation);

    /**
     * 获取注解上的 replacer 参数
     *
     * @param annotation 注解信息
     * @return 待替换的字符串
     */
    abstract String getReplacer(T annotation);

    /**
     * el 表达式
     *
     * @param annotation 注解信息
     * @return el 表达式
     */
    abstract String getCondition(T annotation);

}
