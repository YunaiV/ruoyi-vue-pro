package cn.iocoder.yudao.framework.desensitize.core.regex.handler;

import cn.iocoder.yudao.framework.desensitize.core.regex.annotation.RegexDesensitize;

/**
 * 正则脱敏处理器
 */
public class DefaultRegexDesensitizationHandler extends AbstractRegexDesensitizationHandler<RegexDesensitize> {

    @Override
    Object[] getArgs(RegexDesensitize anno) {
        return new Object[]{anno.regex(), anno.replacer()};
    }
}
