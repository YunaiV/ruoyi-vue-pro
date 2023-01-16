package cn.iocoder.yudao.framework.desensitize.core.regex.handler;

import cn.iocoder.yudao.framework.desensitize.core.regex.annotation.EmailDesensitize;

public class EmailDesensitizationHandler extends AbstractRegexDesensitizationHandler<EmailDesensitize> {

    @Override
    Object[] getArgs(EmailDesensitize anno) {
        return new Object[]{anno.regex(), anno.replacer()};
    }
}
