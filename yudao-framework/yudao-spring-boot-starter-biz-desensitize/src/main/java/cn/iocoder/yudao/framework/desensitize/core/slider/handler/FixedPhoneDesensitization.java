package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.FixedPhone;

public class FixedPhoneDesensitization extends AbstractDesensitizationHandler<FixedPhone> {
    @Override
    Object[] getArgs(FixedPhone anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }
}
