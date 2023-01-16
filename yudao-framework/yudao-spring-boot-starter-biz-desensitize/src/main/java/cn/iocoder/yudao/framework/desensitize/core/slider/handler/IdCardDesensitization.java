package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.IdCard;

public class IdCardDesensitization  extends AbstractDesensitizationHandler<IdCard> {
    @Override
    Object[] getArgs(IdCard anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }
}
