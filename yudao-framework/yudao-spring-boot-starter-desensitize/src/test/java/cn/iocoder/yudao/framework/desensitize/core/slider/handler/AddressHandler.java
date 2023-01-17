package cn.iocoder.yudao.framework.desensitize.core.slider.handler;

import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;
import cn.iocoder.yudao.framework.desensitize.core.slider.annotation.Address;

public class AddressHandler implements DesensitizationHandler<Address> {
    @Override
    public String desensitize(String origin, Address annotation) {
        return origin + annotation.replacer();
    }
}
