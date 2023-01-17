package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.ChineseName;

/**
 * 中文姓名脱敏处理器
 *
 * @author gaibu
 */
public class ChineseNameDesensitization extends AbstractDesensitizationHandler<ChineseName> {
    @Override
    Integer getPrefixKeep(ChineseName annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(ChineseName annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(ChineseName annotation) {
        return annotation.replacer();
    }
}
