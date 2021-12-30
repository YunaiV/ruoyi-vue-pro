package cn.iocoder.yudao.userserver.modules.member.convert.user;

import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserInfoRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserProfileConvert {

    UserProfileConvert INSTANCE = Mappers.getMapper(UserProfileConvert.class);

    MbrUserInfoRespVO convert(MbrUserDO bean);

}
