package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.PhoneNumber;

/**
 * 手机号脱敏处理器
 *
 * @author gaibu
 */
public class PhoneNumberDesensitization extends AbstractDesensitizationHandler<PhoneNumber> {

    @Override
    Integer getPrefixKeep(PhoneNumber annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(PhoneNumber annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(PhoneNumber annotation) {
        return annotation.replacer();
    }
}
