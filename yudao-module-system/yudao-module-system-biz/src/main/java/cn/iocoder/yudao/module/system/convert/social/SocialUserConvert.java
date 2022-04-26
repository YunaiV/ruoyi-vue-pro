package cn.iocoder.yudao.module.system.convert.social;

import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserUnbindReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.auth.AuthSocialBindReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.auth.AuthSocialUnbindReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SocialUserConvert {

    SocialUserConvert INSTANCE = Mappers.getMapper(SocialUserConvert.class);

    SocialUserBindReqDTO convert(Long userId, Integer userType, AuthSocialBindReqVO reqVO);

    SocialUserUnbindReqDTO convert(Long userId, Integer userType, AuthSocialUnbindReqVO reqVO);

}
