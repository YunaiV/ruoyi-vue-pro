package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Slider;

/**
 * 滑动脱敏处理器
 *
 * @author gaibu
 */
public class DefaultDesensitizationHandler extends AbstractDesensitizationHandler<Slider> {
    @Override
    Integer getPrefixKeep(Slider annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(Slider annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(Slider annotation) {
        return annotation.replacer();
    }
}
