package cn.iocoder.yudao.module.member.convert.auth;

import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysAuthConvert {

    SysAuthConvert INSTANCE = Mappers.getMapper(SysAuthConvert.class);

    @Mapping(source = "mobile", target = "username")
    LoginUser convert0(MbrUserDO bean);

    default LoginUser convert(MbrUserDO bean) {
        // 目的，为了设置 UserTypeEnum.MEMBER.getValue()
        return convert0(bean).setUserType(UserTypeEnum.MEMBER.getValue());
    }

}
