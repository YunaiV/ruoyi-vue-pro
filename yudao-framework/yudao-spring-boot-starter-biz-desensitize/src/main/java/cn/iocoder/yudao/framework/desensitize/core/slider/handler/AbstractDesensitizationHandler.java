package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;

import java.lang.annotation.Annotation;

public abstract class AbstractDesensitizationHandler<T extends Annotation> implements DesensitizationHandler<T> {

    @Override
    public String desensitize(String origin, T anno) {
        Object[] args = getArgs(anno);
        int prefixKeep = (Integer) args[0];
        int suffixKeep = (Integer) args[1];
        String replacer = (String) args[2];
        int length = origin.length();

        // 情况一：原始字符串长度小于等于保留长度，则原始字符串全部替换
        if (prefixKeep >= length || suffixKeep >= length) {
            return buildReplacerByLength(replacer, length);
        }

        // 情况二：如果原始字符串小于等于前后缀保留字符串长度，则原始字符串全部替换
        if ((prefixKeep + suffixKeep) >= length) {
            return buildReplacerByLength(replacer, length);
        }

        // 情况三：TODO 城
        int interval = length - prefixKeep - suffixKeep;
        return origin.substring(0, prefixKeep) +
                buildReplacerByLength(replacer, interval) +
                origin.substring(prefixKeep + interval);
    }

    // TODO @城：类似，子类直接获取到参数哈
    /**
     * 获取注解的参数
     *
     * @param anno 注解信息
     * @return 注解的参数
     */
    abstract Object[] getArgs(T anno);

    /**
     * 根据长度循环构建替换符
     *
     * @param replacer 替换符
     * @param length   长度
     * @return 构建后的替换符
     */
    private String buildReplacerByLength(String replacer, int length) {
        return String.valueOf(replacer).repeat(Math.max(0, length));
    }

}
