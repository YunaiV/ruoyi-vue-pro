package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Password;

/**
 * {@link Password} 的码脱敏处理器
 *
 * @author gaibu
 */
public class PasswordDesensitization extends AbstractDesensitizationHandler<Password> {
    @Override
    Integer getPrefixKeep(Password annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(Password annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(Password annotation) {
        return annotation.replacer();
    }
}
