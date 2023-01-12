package cn.iocoder.yudao.framework.desensitize.handler;

import cn.iocoder.yudao.framework.desensitize.annotation.RegexDesensitize;

/**
 * 正则脱敏处理器
 */
public class RegexDesensitizationHandler implements DesensitizationHandler<RegexDesensitize> {

    @Override
    public String desensitize(String origin, Object... arg) {
        String regex = (String) arg[0];
        String replacer = (String) arg[1];

        return origin.replaceAll(regex, replacer);
    }

    @Override
    public Object[] getAnnotationArgs(RegexDesensitize anno) {
        return new Object[]{anno.regex(), anno.replacer()};
    }

}
