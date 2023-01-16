package cn.iocoder.yudao.framework.desensitize.core.regex.handler;

import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;

import java.lang.annotation.Annotation;

public abstract class AbstractRegexDesensitizationHandler<T extends Annotation> implements DesensitizationHandler<T> {

    @Override
    public String desensitize(String origin, T anno) {
        Object[] args = getArgs(anno);
        String regex = (String) args[0];
        String replacer = (String) args[1];

        return origin.replaceAll(regex, replacer);
    }

    /**
     * 获取注解的参数
     *
     * @param anno 注解信息
     * @return 注解的参数
     */
    abstract Object[] getArgs(T anno);
}
