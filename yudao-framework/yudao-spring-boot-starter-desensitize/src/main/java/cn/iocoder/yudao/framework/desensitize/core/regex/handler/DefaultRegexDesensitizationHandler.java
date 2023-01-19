package cn.iocoder.yudao.framework.desensitize.core.regex.handler;

import cn.iocoder.yudao.framework.desensitize.core.regex.annotation.Regex;

/**
 * {@link Regex} 的正则脱敏处理器
 *
 * @author gaibu
 */
public class DefaultRegexDesensitizationHandler extends AbstractRegexDesensitizationHandler<Regex> {

    @Override
    String getRegex(Regex annotation) {
        return annotation.regex();
    }

    @Override
    String getReplacer(Regex annotation) {
        return annotation.replacer();
    }
}
