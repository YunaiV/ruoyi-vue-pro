package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Password;

public class PasswordDesensitization extends AbstractDesensitizationHandler<Password> {
    @Override
    Object[] getArgs(Password anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }
}
