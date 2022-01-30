package cn.iocoder.yudao.module.member.convert.auth;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthSocialBindReqVO;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthSocialLogin2ReqVO;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthSocialLoginReqVO;
import cn.iocoder.yudao.module.member.controller.app.auth.vo.AppAuthSocialUnbindReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserUnbindReqDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    @Mapping(source = "mobile", target = "username")
    LoginUser convert0(MemberUserDO bean);

    default LoginUser convert(MemberUserDO bean) {
        // 目的，为了设置 UserTypeEnum.MEMBER.getValue()
        return convert0(bean).setUserType(UserTypeEnum.MEMBER.getValue());
    }

    SocialUserBindReqDTO convert(Long userId, Integer value, AppAuthSocialBindReqVO reqVO);
    SocialUserBindReqDTO convert(Long userId, Integer value, AppAuthSocialLogin2ReqVO reqVO);
    SocialUserBindReqDTO convert(Long userId, Integer value, AppAuthSocialLoginReqVO reqVO);
    SocialUserUnbindReqDTO convert(Long userId, Integer value, AppAuthSocialUnbindReqVO reqVO);

}
