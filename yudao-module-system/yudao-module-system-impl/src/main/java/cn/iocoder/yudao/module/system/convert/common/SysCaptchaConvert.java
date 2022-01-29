package cn.iocoder.yudao.module.system.convert.common;

import cn.hutool.captcha.AbstractCaptcha;
import cn.iocoder.yudao.module.system.controller.common.vo.SysCaptchaImageRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysCaptchaConvert {

    SysCaptchaConvert INSTANCE = Mappers.getMapper(SysCaptchaConvert.class);

    default SysCaptchaImageRespVO convert(String uuid, AbstractCaptcha captcha) {
        return SysCaptchaImageRespVO.builder().uuid(uuid).img(captcha.getImageBase64()).build();
    }

}
