package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.CarLicense;

/**
 * 车牌号脱敏处理器
 *
 * @author gaibu
 */
public class CarLicenseDesensitization extends AbstractDesensitizationHandler<CarLicense> {
    @Override
    Integer getPrefixKeep(CarLicense annotation) {
        return annotation.prefixKeep();
    }

    @Override
    Integer getSuffixKeep(CarLicense annotation) {
        return annotation.suffixKeep();
    }

    @Override
    String getReplacer(CarLicense annotation) {
        return annotation.replacer();
    }
}
