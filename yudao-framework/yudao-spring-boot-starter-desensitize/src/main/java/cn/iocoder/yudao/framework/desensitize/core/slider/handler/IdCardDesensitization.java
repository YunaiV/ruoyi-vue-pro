package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.IdCard;

/**
 * 身份证脱敏处理器
 *
 * @author gaibu
 */
public class IdCardDesensitization extends AbstractDesensitizationHandler<IdCard> {
    @Override
    Integer getPrefixKeep(IdCard annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(IdCard annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(IdCard annotation) {
        return annotation.replacer();
    }
}
