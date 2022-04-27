package cn.iocoder.yudao.module.system.convert.common;

import cn.hutool.captcha.AbstractCaptcha;
import cn.iocoder.yudao.module.system.controller.admin.common.vo.CaptchaImageRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CaptchaConvert {

    CaptchaConvert INSTANCE = Mappers.getMapper(CaptchaConvert.class);

    default CaptchaImageRespVO convert(String uuid, AbstractCaptcha captcha) {
        return CaptchaImageRespVO.builder().uuid(uuid).img(captcha.getImageBase64()).build();
    }

}
