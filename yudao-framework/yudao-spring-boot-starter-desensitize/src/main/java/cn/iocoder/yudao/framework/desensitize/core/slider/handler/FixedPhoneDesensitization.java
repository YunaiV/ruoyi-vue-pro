package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.FixedPhone;

/**
 * 固定电话脱敏处理器
 *
 * @author gaibu
 */
public class FixedPhoneDesensitization extends AbstractDesensitizationHandler<FixedPhone> {
    @Override
    Integer getPrefixKeep(FixedPhone annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(FixedPhone annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(FixedPhone annotation) {
        return annotation.replacer();
    }
}
