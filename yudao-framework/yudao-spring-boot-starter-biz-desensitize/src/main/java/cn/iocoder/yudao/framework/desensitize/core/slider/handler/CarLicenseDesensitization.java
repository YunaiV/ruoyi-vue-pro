package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.CarLicense;

public class CarLicenseDesensitization  extends AbstractDesensitizationHandler<CarLicense> {
    @Override
    Object[] getArgs(CarLicense anno) {
        return new Object[]{anno.prefixKeep(), anno.suffixKeep(), anno.replacer()};
    }
}
