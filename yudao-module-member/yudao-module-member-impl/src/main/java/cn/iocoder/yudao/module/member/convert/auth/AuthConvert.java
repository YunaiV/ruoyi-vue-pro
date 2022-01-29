package cn.iocoder.yudao.module.member.convert.auth;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.member.dal.dataobject.user.UserDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    @Mapping(source = "mobile", target = "username")
    LoginUser convert0(UserDO bean);

    default LoginUser convert(UserDO bean) {
        // 目的，为了设置 UserTypeEnum.MEMBER.getValue()
        return convert0(bean).setUserType(UserTypeEnum.MEMBER.getValue());
    }

}
