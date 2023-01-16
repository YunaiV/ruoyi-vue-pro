package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.ChineseName;

public class ChineseNameDesensitization extends AbstractDesensitizationHandler<ChineseName> {
    @Override
    Object[] getArgs(ChineseName anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }
}
