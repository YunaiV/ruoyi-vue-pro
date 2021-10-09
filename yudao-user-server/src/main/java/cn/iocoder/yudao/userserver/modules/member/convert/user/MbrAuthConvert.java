package cn.iocoder.yudao.userserver.modules.member.convert.user;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.userserver.modules.member.dal.dataobject.user.MbrUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MbrAuthConvert {

    MbrAuthConvert INSTANCE = Mappers.getMapper(MbrAuthConvert.class);

    LoginUser convert0(MbrUserDO bean);

    default LoginUser convert(MbrUserDO bean) {
        // 目的，为了设置 UserTypeEnum.MEMBER.getValue()
        return convert0(bean).setUserType(UserTypeEnum.MEMBER.getValue());
    }

}
