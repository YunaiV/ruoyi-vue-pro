package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.BankCard;

public class BankCardDesensitization extends AbstractDesensitizationHandler<BankCard> {

    @Override
    Object[] getArgs(BankCard anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }
}