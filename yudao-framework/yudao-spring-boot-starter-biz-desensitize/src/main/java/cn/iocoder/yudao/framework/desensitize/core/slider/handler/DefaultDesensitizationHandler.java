package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Slider;

/**
 * 滑动脱敏处理器
 */
public class DefaultDesensitizationHandler extends AbstractDesensitizationHandler<Slider> {

    @Override
    Object[] getArgs(Slider anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }
}
