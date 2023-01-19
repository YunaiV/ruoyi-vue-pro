package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.FixedPhoneDesensitize;

/**
 * {@link FixedPhoneDesensitize} 的脱敏处理器
 *
 * @author gaibu
 */
public class FixedPhoneDesensitization extends AbstractDesensitizationHandler<FixedPhoneDesensitize> {
    @Override
    Integer getPrefixKeep(FixedPhoneDesensitize annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(FixedPhoneDesensitize annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(FixedPhoneDesensitize annotation) {
        return annotation.replacer();
    }
}
