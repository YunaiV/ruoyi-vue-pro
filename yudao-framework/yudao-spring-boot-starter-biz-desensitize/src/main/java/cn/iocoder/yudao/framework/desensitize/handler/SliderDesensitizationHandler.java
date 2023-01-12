package cn.iocoder.yudao.framework.desensitize.handler;

import cn.iocoder.yudao.framework.desensitize.annotation.SliderDesensitize;

/**
 * 滑动脱敏处理器
 */
public class SliderDesensitizationHandler implements DesensitizationHandler<SliderDesensitize> {

    @Override
    public String desensitize(String origin, Object... arg) {
        int prefixKeep = (Integer) arg[0];
        int suffixKeep = (Integer) arg[1];
        String replacer = (String) arg[2];

        int length = origin.length();

        // 原始字符串长度小于等于保留长度，则原始字符串全部替换
        if (prefixKeep >= length || suffixKeep >= length) {
            return buildReplacerByLength(replacer, length);
        }

        // 如果原始字符串小于等于前后缀保留字符串长度，则原始字符串全部替换
        if ((prefixKeep + suffixKeep) >= length) {
            return buildReplacerByLength(replacer, length);
        }

        int interval = length - prefixKeep - suffixKeep;
        return origin.substring(0, prefixKeep) +
                buildReplacerByLength(replacer, interval) +
                origin.substring(prefixKeep + interval);
    }

    @Override
    public Object[] getAnnotationArgs(SliderDesensitize anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }

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
