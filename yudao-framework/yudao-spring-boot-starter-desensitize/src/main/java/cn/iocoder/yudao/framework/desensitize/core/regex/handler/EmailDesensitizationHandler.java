package cn.iocoder.yudao.framework.desensitize.core.regex.handler;

import cn.iocoder.yudao.framework.desensitize.core.regex.annotation.Email;

/**
 * {@link Email} 的脱敏处理器
 *
 * @author gaibu
 */
public class EmailDesensitizationHandler extends AbstractRegexDesensitizationHandler<Email> {

    @Override
    String getRegex(Email annotation) {
        return annotation.regex();
    }

    @Override
    String getReplacer(Email annotation) {
        return annotation.replacer();
    }

}
