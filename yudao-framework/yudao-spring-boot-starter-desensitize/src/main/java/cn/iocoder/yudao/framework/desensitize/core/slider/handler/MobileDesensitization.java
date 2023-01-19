package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Mobile;

/**
 * {@link Mobile} 的脱敏处理器
 *
 * @author gaibu
 */
public class MobileDesensitization extends AbstractDesensitizationHandler<Mobile> {

    @Override
    Integer getPrefixKeep(Mobile annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(Mobile annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(Mobile annotation) {
        return annotation.replacer();
    }
}
