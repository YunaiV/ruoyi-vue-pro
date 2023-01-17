package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.BankCard;

/**
 * 银行卡脱敏处理器
 *
 * @author gaibu
 */
public class BankCardDesensitization extends AbstractDesensitizationHandler<BankCard> {

    @Override
    Integer getPrefixKeep(BankCard annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(BankCard annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(BankCard annotation) {
        return annotation.replacer();
    }

}