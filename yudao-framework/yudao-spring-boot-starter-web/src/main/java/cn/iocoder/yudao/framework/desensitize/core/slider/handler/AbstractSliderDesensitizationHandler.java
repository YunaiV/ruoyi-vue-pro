package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.spring.SpringExpressionUtils;
import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;

import java.lang.annotation.Annotation;

/**
 * 滑动脱敏处理器抽象类，已实现通用的方法
 *
 * @author gaibu
 */
public abstract class AbstractSliderDesensitizationHandler<T extends Annotation>
        implements DesensitizationHandler<T> {

    @Override
    public String desensitize(String origin, T annotation) {
        // 1. 判断是否禁用脱敏
        Object disable = SpringExpressionUtils.parseExpression(getDisable(annotation));
        if (Boolean.TRUE.equals(disable)) {
            return origin;
        }

        // 2. 执行脱敏
        int prefixKeep = getPrefixKeep(annotation);
        int suffixKeep = getSuffixKeep(annotation);
        String replacer = getReplacer(annotation);
        int length = origin.length();
        int interval = length - prefixKeep - suffixKeep;

        // 情况一：原始字符串长度小于等于前后缀保留字符串长度，则原始字符串全部替换
        if (interval <= 0) {
            return buildReplacerByLength(replacer, length);
        }

        // 情况二：原始字符串长度大于前后缀保留字符串长度，则替换中间字符串
        return origin.substring(0, prefixKeep) +
                buildReplacerByLength(replacer, interval) +
                origin.substring(prefixKeep + interval);
    }

    /**
     * 根据长度循环构建替换符
     *
     * @param replacer 替换符
     * @param length   长度
     * @return 构建后的替换符
     */
    private String buildReplacerByLength(String replacer, int length) {
        return StrUtil.repeat(replacer, length);
    }

    /**
     * 前缀保留长度
     *
     * @param annotation 注解信息
     * @return 前缀保留长度
     */
    abstract Integer getPrefixKeep(T annotation);

    /**
     * 后缀保留长度
     *
     * @param annotation 注解信息
     * @return 后缀保留长度
     */
    abstract Integer getSuffixKeep(T annotation);

    /**
     * 替换符
     *
     * @param annotation 注解信息
     * @return 替换符
     */
    abstract String getReplacer(T annotation);

}
