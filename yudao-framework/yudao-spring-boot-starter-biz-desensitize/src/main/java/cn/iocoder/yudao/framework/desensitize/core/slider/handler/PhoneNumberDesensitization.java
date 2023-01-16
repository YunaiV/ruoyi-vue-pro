package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.PhoneNumber;

public class PhoneNumberDesensitization extends AbstractDesensitizationHandler<PhoneNumber> {

    @Override
    Object[] getArgs(PhoneNumber anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }
}
